/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
