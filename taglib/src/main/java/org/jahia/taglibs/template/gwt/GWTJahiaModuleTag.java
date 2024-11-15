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
package org.jahia.taglibs.template.gwt;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.internal.gwt.GWTIncluder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.DynamicAttributes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: jahia
 * Date: 2 avr. 2008
 * Time: 14:43:29
 */
@SuppressWarnings("serial")
public class GWTJahiaModuleTag extends AbstractJahiaTag implements DynamicAttributes {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GWTJahiaModuleTag.class);
    private transient Map<String, Object> attributes = new HashMap<String, Object>();

    private String id;
    private String jahiaType;
    private boolean templateUsage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJahiaType() {
        return jahiaType;
    }

    public void setJahiaType(String jahiaType) {
        this.jahiaType = jahiaType;
    }

    public void setTemplateUsage(boolean templateUsage) {
        this.templateUsage = templateUsage;
    }

    public int doStartTag() throws JspException {

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        final JspWriter out = pageContext.getOut();
        // print output
        try {

            // validate
            if (id == null) {
                out.print("<!-- gwt-jahiamodule: 'id' must be not null-->");
                return EVAL_PAGE;
            }

            // print the place holder
            out.print(GWTIncluder.generateJahiaModulePlaceHolder(templateUsage,getCssClassName(),jahiaType,id,attributes));

        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }

        id = null;
        jahiaType = null;
        attributes = new HashMap<String, Object>();
        return EVAL_PAGE;
    }



    public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(localName, value);
    }



}
