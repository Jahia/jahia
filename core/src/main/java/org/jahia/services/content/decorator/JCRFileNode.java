/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.content.decorator;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

/**
 *
 * User: toto
 * Date: Dec 3, 2008
 * Time: 5:55:11 PM
 *
 */
public class JCRFileNode extends JCRNodeDecorator {
    protected static final Logger logger = org.slf4j.LoggerFactory.getLogger(JCRFileNode.class);

    public JCRFileNode(JCRNodeWrapper node) throws RepositoryException {
        super(node);
    }

    @Override
    public String getDisplayableName() {
        try {
            if(isNodeType(Constants.NT_FOLDER)) {
                return super.getDisplayableName();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        String title = null;
        String name = null;
        try {
            title = getProperty(Constants.JCR_TITLE).getValue().getString();
        } catch (RepositoryException e) {
            logger.debug("could not retrieve jcr:title of " + this.getPath());
        }
        name = getUnescapedName();
        return title != null ? name+" ("+title+")" : name;
    }
}
