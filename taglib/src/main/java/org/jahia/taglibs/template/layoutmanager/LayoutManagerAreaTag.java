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
package org.jahia.taglibs.template.layoutmanager;

import java.io.IOException;

import javax.jcr.Node;
import javax.servlet.jsp.JspWriter;

import org.jahia.taglibs.AbstractJahiaTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: ktlili
 * Date: 21 nov. 2007
 * Time: 11:43:22
 */
@SuppressWarnings("serial")
public class LayoutManagerAreaTag extends AbstractJahiaTag {
    private static final Logger logger = LoggerFactory.getLogger(LayoutManagerAreaTag.class);
    private String width;
    private Node node;

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        try {
            //define area
            out.print("<div uuid='"+node.getIdentifier()+"' id='layoutManager'\"");
            if (width != null) {
                out.print(" jahia-layoutmanager-width='" + width + "'");
            }
            out.print("></div>\n");

            // begin box declaration
            out.print("<div id='layout'  style='display:none;'>\n");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return EVAL_BODY_INCLUDE;
    }


    public int doEndTag() {
        final JspWriter out = pageContext.getOut();
        try {
            // end box declaration
            out.print("\n</div>\n");
            this.width = null;
            this.node = null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return SKIP_BODY;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
