/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.query;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * Tag used to create a Query Object Model Selector.
 * The selector is assigned to the parent QueryObjectModel taken from the parent QueryDefinitionTag
 * <code>parentQueryDefinitionTag.setSource(selector)</code>
 *
 * User: hollis
 * Date: 8 nov. 2007
 * Time: 13:08:23
 */
public class SelectorTag extends QOMBuildingTag {

    private static final long serialVersionUID = -4715677329006113172L;

    private String nodeTypeName;

    public int doEndTag() throws JspException {
        try {
            if (getSelectorName() == null) {
                setSelectorName(nodeTypeName);
            }

            getQOMBuilder().setSource(getQOMFactory().selector(nodeTypeName, getSelectorName()));
        } catch (RepositoryException e) {
            throw new JspTagException(e);
        } finally {
            resetState();
        }

        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        nodeTypeName = null;
        super.resetState();
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }
}
