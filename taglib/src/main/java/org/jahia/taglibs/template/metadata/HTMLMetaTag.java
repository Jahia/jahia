/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.metadata;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.JahiaTools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;


/**
 * <p>Title: Generates a HTML meta tag to be placed in the head part of the template</p>
 * <p>Description: Generates html meta tags of the form:</br>
 * &lt;meta name="description" lang="en" content="my page"/&gt;</p>
 * <p>Copyright: Copyright (c) 1999-2009</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Xavier Lawrence
 * @version 1.0
 * @jsp:tag name="meta" body-content="empty" description="Generates a HTML meta tag to be placed in the head part of the template"
 * <p/>
 * <p><attriInfo>Generates a HTML meta tag to be placed in the head part of the template.
 * <p/>
 * <p><b>Example :</b>
 * &lt;meta name="description" lang="en" content="my page"/&gt;
 * <p/>
 */
@SuppressWarnings("serial")
public class HTMLMetaTag extends AbstractJahiaTag {
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(HTMLMetaTag.class);

    /**
     * Value to use as it is for the meta content attribute
     */
    private String value;
    /**
     * name attribute of the meta element generated
     */
    private String name;
    /**
     * Name of a Jahia metadata to use in order to fetch the coresponding metadata value and it for the content attribute
     */
    private String metadata;

    private boolean valueOnly;

    public int doStartTag() throws JspException {
        try {
            if (name == null || name.length() < 0) {
                throw new IllegalArgumentException("Attribute 'name' must not be null or empty");
            }
            final boolean valueAttributeSpecified = value != null && value.length() > 0;
            final boolean metaDataAttributeSpecified = metadata != null && metadata.length() > 0;
            if (!valueAttributeSpecified && !metaDataAttributeSpecified) {
                throw new IllegalArgumentException("You must provide a value for attribute 'value' or attribute 'metaData'");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("HTMLMetaTag: name=" + name + ", value=" + value + ", metaData=" + metadata);
            }
            final JspWriter out = pageContext.getOut();
            final StringBuffer buff = new StringBuffer();
            if (!valueOnly) {
                buff.append("<meta ");
                buff.append("name=\"");
                buff.append(name);
                buff.append("\" ");

                if (languageCode != null && languageCode.length() > 0) {
                    buff.append("lang=\"");
                    buff.append(languageCode);
                    buff.append("\" ");
                }

                buff.append("content=\"");
            }

            if (valueAttributeSpecified) {
                buff.append(value);
            } else {
                final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
                final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
                final String metadataValue = jParams.getContentPage().getMetadataValue(metadata, jParams, "").trim();
                if (metadataValue.length() == 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Empty value for metadata " + metadata);
                    }
                    return SKIP_BODY;
                }
                buff.append(JahiaTools.removeTags(metadataValue));
            }

            if (!valueOnly) {
                buff.append("\" ");
                if (xhtmlCompliantHtml) {
                    buff.append("/");
                }
                buff.append(">");
            }
            out.println(buff.toString());

        } catch (final IOException e) {
            logger.error("IOException in doStartTag", e);

        } catch (final JahiaException je) {
            logger.error("JahiaException in doStartTag", je);
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        value = null;
        name = null;
        metadata = null;
        valueOnly = false;
        return EVAL_PAGE;
    }

    public String getValue() {
        return value;
    }

    /**
     * If the generated value should used a specific static value, this is the attribute to use in order to set it
     *
     * @param value Sets the value of the tag
     * @jsp:attribute name="value" required="true" rtexprvalue="true"
     * description="Sets the value of the tag"
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    /**
     * Sets the name of the meta tag
     *
     * @param name Sets the name of the meta tag
     * @jsp:attribute name="name" required="true" rtexprvalue="true"
     * description="Sets the name of the meta tag"
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getMetadata() {
        return metadata;
    }

    /**
     * If the generated value should reflect a Jahia metadata, this is the place where to give the name of the Jahia
     * metadata we are interested in
     *
     * @param metadata Sets the name of the metadata to use to get the value
     * @jsp:attribute name="metadata" required="true" rtexprvalue="true"
     * description="Sets the name of the metadata to use to get the value"
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * Set to true if you only want the value as a String and not the encapsulating HTML meta tag and its attributes
     *
     * @param valueOnly Set to true if you only want the value as a String and not the encapsulating HTML meta tag
     * @jsp:attribute name="valueOnly" required="true" rtexprvalue="true" type="boolean"
     * description="Sets the name of the metadata to use to get the value"
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setValueOnly(boolean valueOnly) {
        this.valueOnly = valueOnly;
    }
}
