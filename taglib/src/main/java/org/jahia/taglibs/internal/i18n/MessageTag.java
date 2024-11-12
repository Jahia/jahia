/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.internal.i18n;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.jahia.engines.EngineMessage;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.utility.Utils;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for Jahia Message ResourceBundle within Jahia
 * <p/>
 * Returns the requested resource.
 *
 * @author Khue Nguyen
 * @jsp:tag name="message" body-content="empty"
 * description="display a specific value in a resource bundle and formats it using
 * <a href='http://java.sun.com/j2se/1.4.2/docs/api/java/text/MessageFormat.html' target='tagFrame'>java.text.MessageFormat</a>.
 * <p/>
 * <p><attriInfo>Lowest level support for resource bundle messages. This is inspired by the Struts bean message tag
 * except that it checks a session variable (org.jahia.services.multilang.currentlocale) to
 * determine if a Locale has been chosen.
 * <p/>
 * <p>The value lookup is done in the 'JahiaInternalResources.properties' resource bundle.
 * <p/>
 * <p>Similar in functionality to <a href='resourceBundle.html' target='tagFrame'>content:resourceBundle</a>.
 * <p/>
 * <p/>
 * <p><b>Example 1 :</b>
 * <p/>
 * &lt;logic:present name=\"engineMessages\"&gt; <br>
 * &lt;div id=\"errors\"&gt; <br>
 * &nbsp;&nbsp;&lt;content:resourceBundle resourceBundle=\"jahiatemplates.Corporate_portal_templates\" resourceName=\"mySettingsErrors\"/&gt; : &lt;br/&gt; <br>
 * &nbsp;&nbsp;&lt;ul&gt; <br>
 * &nbsp;&nbsp;&lt;logic:iterate name=\"engineMessages\" property=\"messages\" id=\"curMessage\"&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;li&gt;&lt;content:message name=\"curMessage\"/&gt;&lt;/li&gt; <br>
 * &nbsp;&nbsp;&lt;/logic:iterate&gt; <br>
 * &nbsp;&nbsp;&lt;/ul&gt;  <br>
 * &lt;/div&gt;<br>
 * &lt;/logic:present&gt; <br>
 * <p/>
 * <p>The above example takes all the EngineMessage objects in EngineMessages and displays them using the content:message tag. The
 * EngineMessages is instanciated when Jahia detects an error.
 * <p/>
 * <p><b>Example 2 :</b>
 * <p/>
 * &lt;content:message key=\"org.jahia.bin.JahiaConfigurationWizard.root.adminUserName.label\" /&gt;
 * <p/>
 * </attriInfo>"
 */
@SuppressWarnings("serial")
public class MessageTag extends TagSupport {

    private static final Logger logger = LoggerFactory.getLogger(MessageTag.class);

    private String key = null;
    private String name = null;
    private boolean display = true;
    private String property = null;

    /**
     * @param key the key of the resource to fetch in the resource bundle
     * @jsp:attribute name="key" required="false" rtexprvalue="true"
     * description="the key of the resource to fetch in the resource bundle.
     * <p/>
     * <p><attriInfo>If it is defined, all other attributes are ignored.
     * </attriInfo>"
     */
    public void setKey(String key) {
        if (key == null) {
            key = "";
        }
        this.key = key;
    }

    /**
     * @return name of the pageContext attribute which holds the bean to fetch and display
     * @jsp:attribute name="name" required="false" rtexprvalue="true"
     * description="name of the pageContext attribute which holds the bean to fetch and display.
     * <p/>
     * <p><attriInfo>
     * </attriInfo>"
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public int doStartTag() {
        Locale currentLocale = AbstractJahiaTag.getUILocale(Utils.getRenderContext(pageContext),
                pageContext.getSession(), (HttpServletRequest) pageContext.getRequest());

        String resValue = null;
        try {
            if (key != null) {
                resValue = Messages.getInternal(key, currentLocale);
            } else if (name != null) {
                final EngineMessage message = (EngineMessage) pageContext.findAttribute(name);
                if (message != null) {
                    resValue = message.isResource() ? Messages.getInternalWithArguments(message.getKey(), currentLocale, message.getValues()) : message.getKey();
                } else {
                    logger.error("Couldn't find any EngineMessage bean with name " + name + "!");
                }
            }

        } catch (MissingResourceException mre) {
            logger.warn("Couldn't find resource : ", mre);
        }

        if (resValue == null) {
            resValue = "";
        }

        try {
            final JspWriter out = pageContext.getOut();
            out.print(resValue);
        } catch (IOException ioe) {
            logger.error("IO error while displaying message : ", ioe);
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        key = null;
        name = null;
        property = null;
        display = true;
        return EVAL_PAGE;
    }

}
