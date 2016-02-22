/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.textextraction;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.rules.ExtractionService;
import org.jahia.tools.OutWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for performing text extraction check and fixing.
 * 
 * @author Benjamin Papez
 */
class TextExtractionChecker {

    private static final Logger logger = LoggerFactory
            .getLogger(TextExtractionChecker.class);

    private final boolean fixExtraction;
    
    private final boolean searchByFilter;    

    private boolean forceStop;

    private List<String> nodesToExtract = new ArrayList<String>();

    private final OutWrapper out;

    private final ExtractionCheckStatus status;
    
    private final RepositoryFileFilter filter;    

    TextExtractionChecker(ExtractionCheckStatus status, boolean fixExtraction,
            OutWrapper out) {
        super();
        this.status = status;
        this.fixExtraction = fixExtraction;
        this.out = out;
        searchByFilter = false;
        filter = null;        
    }
    
    TextExtractionChecker(ExtractionCheckStatus status, boolean fixExtraction,
            RepositoryFileFilter filter, OutWrapper out) {
        super();
        this.status = status;
        this.fixExtraction = fixExtraction;
        this.out = out;
        searchByFilter = true;
        this.filter = filter;        
    }

    private void checkTextExtraction(JCRSessionWrapper session)
            throws RepositoryException {
        nodesToExtract = new ArrayList<String>();
        
        QueryManager q = session.getWorkspace().getQueryManager();
        String xpath;
        if (searchByFilter) {
            StringBuilder xpathBuilder = new StringBuilder();
            xpathBuilder.append("/jcr:root/");
            if (StringUtils.isNotBlank(filter.getPath())) {
                xpathBuilder.append(ISO9075.encodePath(filter.getPath().replaceAll("^/|/$", ""))).append(
                        filter.isIncludeDescendants() ? "//" : "/");
            } else {
                xpathBuilder.append("*//");
            }
            xpathBuilder.append("element(*,nt:file)[not(jcr:like(jcr:content/@jcr:mimeType, 'image%')) and not(jcr:like(jcr:content/@jcr:mimeType, '%css')) and not(jcr:like(fn:name(), 'thumbnail%'))");
            if (!filter.getMimeTypes().isEmpty()) {
                xpathBuilder.append(" and (");
                String connector = "";
                for (String mimeType : filter.getMimeTypes()) {
                    xpathBuilder.append(connector).append("jcr:content/@jcr:mimeType = ").append(JCRContentUtils.stringToQueryLiteral(mimeType));
                    connector = " or ";
                }
                xpathBuilder.append(")");
            }
            if (StringUtils.isNotBlank(filter.getFileNamePattern())) {
                String pattern = filter.getFileNamePattern();
                pattern = pattern.replace("\\", "\\\\");
                pattern = pattern.replace("_", "\\_");
                pattern = pattern.replace("%", "\\%");
                pattern = pattern.replace("?", "_");
                pattern = pattern.replace("*", "%");
                xpathBuilder.append(" and jcr:like(fn:name(), ").append(JCRContentUtils.stringToQueryLiteral(pattern)).append(")");
            }
            xpathBuilder.append("]");
            xpath = xpathBuilder.toString();
        } else {
            xpath = "/jcr:root/*//element(*,nt:file)[not(jcr:content/@j:extractedText) and not(jcr:like(jcr:content/@jcr:mimeType, 'image%')) and not(jcr:like(jcr:content/@jcr:mimeType, '%css')) and not(jcr:like(fn:name(), 'thumbnail%'))]";
        }
        QueryResult qr = q.createQuery(xpath, Query.XPATH).execute();
        NodeIterator ni = qr.getNodes();
        JCRNodeWrapper fileNode = null;
        while (ni.hasNext()) {
            try {
                fileNode = ((JCRNodeWrapper) ni.next());
                if (!fileNode.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                    JCRNodeWrapper resource = fileNode
                            .getNode(Constants.JCR_CONTENT);
                    String savedMimeType = "";
                    try {
                        savedMimeType = resource.getProperty(
                                Constants.JCR_MIMETYPE).getString();
                    } catch (PathNotFoundException e) {
                        // ignore
                    }
                    String mimeType = JCRContentUtils.getMimeType(fileNode
                            .getName());
                    if (mimeType != null && !savedMimeType.equals(mimeType)) {
                        logger.warn(
                                "Saved mimetype for {} is '{}', but the suggested one is '{}'",
                                new Object[] { fileNode.getPath(),
                                        savedMimeType, mimeType });
                    }
                    if (ExtractionService.getInstance().canHandle(resource)) {
                        out.echo("{} with mimetype '{}'", fileNode.getPath(),
                                savedMimeType);
                        status.extractable++;
                        nodesToExtract.add(fileNode.getIdentifier());
                    }
                    status.checked++;
                }
                if (forceStop) {
                    return;
                }
            } catch (Exception e) {
                logger.warn("Error when trying to extract: " + fileNode != null ? fileNode.getPath() : "", e);
            }
        }
    }

    private void fixTextExtractions(JCRSessionWrapper session) {
        JCRNodeWrapper fileNode = null;
        for (String nodeToExtract : nodesToExtract) {
            try {
                fileNode = session.getNodeByIdentifier(nodeToExtract);
                if (ExtractionService.getInstance().extractText(
                        fileNode.getProvider(), fileNode.getPath(), null,
                        session.getWorkspace().getName())) {
                    status.fixed++;
                }
            } catch (Exception e) {
                out.echo("Cannot extract text of {} due to {}",
                        fileNode.getPath(), e.getMessage());
            }
            if (forceStop) {
                return;
            }
        }
    }

    void perform(JCRSessionWrapper session) throws RepositoryException {
        checkTextExtraction(session);

        if (forceStop) {
            out.echo("Request received to stop checking nodes.");
        } else if (fixExtraction && nodesToExtract.size() > 0) {
            fixTextExtractions(session);
        }
    }

    void stop() {
        this.forceStop = true;
    }
}