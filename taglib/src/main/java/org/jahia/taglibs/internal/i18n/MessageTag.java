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
package org.jahia.taglibs.internal.i18n;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.core.Core_Engine;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;

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
 * <p>The value lookup is done in the 'JahiaMessageResources.properties' resource bundle.
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
 * @see JahiaResourceBundle
 *      see SetAdminResourceBundleTag
 *      see JahiaInternalResources.properties
 */
@SuppressWarnings("serial")
public class MessageTag extends TagSupport {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MessageTag.class);

    private String key = null;
    private String name = null;
    private boolean display = true;
    private String property = null;

    /**
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
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
        Locale currentLocale = null;
        if (jParams != null) {
            final Map<String, ?> engineMap = !Core_Engine.ENGINE_NAME.equals(jParams.getEngineName()) ? (Map<String, ?>) jParams.getSessionState().getAttribute("jahia_session_engineMap") : null;
            if (engineMap != null) {
                final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
                if (elh != null) {
                    currentLocale = elh.getCurrentLocale();
                }
            }
            if (currentLocale == null) {
                currentLocale = jParams.getLocale();
            }
        } else {
            final HttpSession session = pageContext.getSession();
            if (session != null) {
                if (session.getAttribute(ProcessingContext.SESSION_LOCALE) != null) {
                    currentLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
                }
            }
        }

        if (currentLocale == null) {
            currentLocale = request.getLocale();
        }

        String resValue = null;
        try {
            if (key != null) {
                resValue = JahiaResourceBundle.getInternalOrMessageResource(key, currentLocale);
            } else if (name != null) {
                final EngineMessage message = (EngineMessage) pageContext.findAttribute(name);
                if (message != null) {
                    resValue = message.isResource() ? MessageFormat.format(JahiaResourceBundle.getMessageResource(message.getKey(), currentLocale), message.getValues()) : message.getKey();
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
