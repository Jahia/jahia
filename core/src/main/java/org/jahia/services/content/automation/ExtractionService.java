/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.automation;

import org.drools.spi.KnowledgeHelper;
import org.drools.WorkingMemory;
import org.jahia.api.Constants;
import org.apache.log4j.Logger;

import javax.jcr.Node;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 9, 2009
 * Time: 6:33:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExtractionService {
    private static Logger logger = Logger.getLogger(Service.class);
    private static ExtractionService instance;

    private Map<String, Extractor> extractors;

    public static synchronized ExtractionService getInstance() {
        if (instance == null) {
            instance = new ExtractionService();
        }
        return instance;
    }

    public ExtractionService() {
    }

    public Map<String, Extractor> getExtractors() {
        return extractors;
    }

    public void setExtractors(Map<String, Extractor> extractors) {
        this.extractors = extractors;
    }

    public void extractProperties(NodeWrapper node, KnowledgeHelper drools) throws Exception{
        if (!node.getNode().hasNode(Constants.JCR_CONTENT)) {
            return;
        }
        Node contentNode = node.getNode().getNode(Constants.JCR_CONTENT);
        Extractor extractor = (Extractor) extractors.get(node.getMimeType());
        if (extractor != null) {
            try {
                Map<String, Object> m = extractor.extract(contentNode.getProperty(Constants.JCR_DATA).getStream());
                if (m != null) {
                    WorkingMemory memory = drools.getWorkingMemory();
                    for (Map.Entry<String, Object> entry : m.entrySet()) {
                        if (entry.getKey() != null) {
                            String s = entry.getKey().replace("-","_");
                            memory.insert(new ExtractedVariable(node,s,entry.getValue()));
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Error when extracting properties from "+node.getName(),e);
            }
        }
    }

}
