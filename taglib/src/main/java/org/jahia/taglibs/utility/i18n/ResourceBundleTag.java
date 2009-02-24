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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.utility.i18n;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jahia.data.beans.I18nBean;
import org.jahia.taglibs.AbstractJahiaTag;

/**
 * Support for ResourceBundle within Jahia
 * If locale is not set, the locale used is the one returned by paramBean.getLocale()
 *oi
 * @author Khue Nguyen
 *
 * @jsp:tag name="resourceBundle" body-content="empty"
 * description="display a specific value in a resource bundle.
 *
 * <p><attriInfo>This tag can be used to dynamically request a given resource from the engine resource bundle.
 * <p>If locale is not set, the locale used is the one returned by paramBean.getLocale().
 *
 * <p>Similar in functionality to <a href='message.html' target='tagFrame'>content:message</a>.
 *
 * <p><b>Background :</b>
 * Resource bundles contain key/value pairs for specific locales and are used for internalization.
 * Go <a href=http://java.sun.com/j2se/1.4.2/docs/api/java/util/ResourceBundle.html>here</a> for more details.
 * <p>Note that Jahia finds the .properties file associated to a given bundle identifier by doing a lookup in resourcebundles_config.xml
 * located in \WEB-IN\etc\config. The resourcebundles_config.xml file is generated automatically during template deployment. For example,
 * given the the following resourcebundles_config.xml:
 * <p><br>&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; ?&gt;
 * <br>&nbsp;&lt;registry&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;resource-bundle&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;key&gt;myjahiasite_CORPORATE_PORTAL_TEMPLATES&lt;/key&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;file&gt;jahiatemplates.Corporate_portal_templates&lt;/file&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/resource-bundle&gt;
 * <br>&lt;/registry&gt;</p>
 * <p>Then given a bundle identifier of myjahiasite_CORPORATE_PORTAL_TEMPLATES, the key/value lookup will be in Corporate_portal_templates.properties
 * (located in directory \WEB-INF\classes\jahiatemplates) for the default language; or for example in  Corporate_portal_templates_de.propreties
 * if the current user's locale is German.
 *
 * <p><b>Example :</b>
 * <p> &lt;content:resourceBundle resourceBundle=\"jahiatemplates.acme_templates\"
 * resourceName=\"search\"/&gt;
 *
 *
 *
 * </attriInfo>"
 *
 */
public class ResourceBundleTag extends AbstractJahiaTag {

    private static transient final Logger logger = Logger.getLogger(ResourceBundleTag.class);

    private String resourceName = "";
    private String defaultValue = "";
    private String localeLanguage = "";
    private String localeCountry = "";
    private String localeVariant = "";
    private Locale locale = null;
    private String namePostFix = null;
    private String name = null;
    private Object arg0;
    private Object arg1;
    private Object arg2;
    private Object arg3;
    private Object arg4;
    private boolean escape;

    /**
     * @jsp:attribute name="resourceName" required="false" rtexprvalue="true"
     * description="the key of the resource to fetch in the resource bundle.
     *
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setResourceName (String resourceName) {
        if (resourceName == null) {
            resourceName = "";
        }
        this.resourceName = resourceName;
    }

    /**
     * @jsp:attribute name="defaultValue" required="false" rtexprvalue="true"
     * description="the value to use if the resource couldn't be accessed.
     *
     * <p><attriInfo>such as if the resourceBundle doesn't exist, couldn't be read or the resource key entry
     * couldn't be found.
     * </attriInfo>"
     */
    public void setDefaultValue (String value) {
        this.defaultValue = value;
    }

    /**
     * @jsp:attribute name="localeLanguage" required="false" rtexprvalue="true"
     * description="the ISO Language Code to use for the resource bundle.
     *
     * <p><attriInfo><a href ="http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt">
     * <code>iso639 details</code></a>
     * </attriInfo>"
     */
    public void setLocaleLanguage (String localeLanguage) {
        if (localeLanguage != null) {
            this.localeLanguage = localeLanguage.trim();
        }
    }

    /**
     * @jsp:attribute name="localeCountry" required="false" rtexprvalue="true"
     * description="the ISO Country Code to use for the resource bundle.
     *
     * <p><attriInfo><a href ="http://www.chemie.fu-berlin.de/diverse/doc/ISO_3166.html">
     * <code>iso3166 details</code></a>
     * </attriInfo>"
     */
    public void setLocaleCountry (String localeCountry) {
        if (localeCountry != null) {
            this.localeCountry = localeCountry.trim();
        }
    }

    /**
     * @jsp:attribute name="localeVariant" required="false" rtexprvalue="true"
     * description="The variant argument is a vendor or browser-specific code.
     *
     * <p><attriInfo>For example, use WIN for Windows, MAC for Macintosh, and POSIX for POSIX. See
     * java.util.Locale for more info.
     * </attriInfo>"
     */
    public void setLocaleVariant (String localeVariant) {
        if (localeVariant != null) {
            this.localeVariant = localeVariant.trim();
        }
    }

    /**
     * @jsp:attribute name="name" required="false" rtexprvalue="true"
     * description="the name of the pageContext attribute which contains the
     * resourceName to fetch (i.e. the key of the resource to fetch in the resource bundle).
     *
     * <p><attriInfo>
     * </attriInfo>"
     */
    public String getName () {
        return name;
    }

    /**
     * @jsp:attribute name="namePostFix" required="false" rtexprvalue="true"
     * description="the string to append at the end of the 'name' attribute before it is
     * looked up in the resource bundle.
     *
     * <p><attriInfo>Format of generated resource key : name + namePostFix
     * </attriInfo>"
     */
    public String getNamePostFix () {
        return namePostFix;
    }

    public void setNamePostFix (String namePostFix) {
        this.namePostFix = namePostFix;
    }

    public void setName (String name) {
        this.name = name;
    }

    public int doStartTag () {

        String resValue = null;

        if (localeLanguage != null && localeLanguage.length() != 0) {
            locale = new Locale(localeLanguage, localeCountry, localeVariant);
        }

        String messageKey = resourceName;
        if (name != null) {
            String keyName = (String) pageContext.findAttribute(name);
            if (keyName != null) {
                if (namePostFix != null) {
                    keyName = keyName + namePostFix;
                }
                messageKey = keyName;
            } else {
                logger
                        .error("Unable to find a key name for page context attribute "
                                + name);
            }
        }
        I18nBean i18n = locale != null ? getI18n(getResourceBundle(), locale)
                : getI18n();
        resValue = i18n.get(messageKey, defaultValue);
        if (resValue != null && resValue.contains("{")) {
            try {
                resValue = new MessageFormat(resValue, locale != null ? locale
                        : getProcessingContext().getLocale()).format(new Object[] {
                        arg0, arg1, arg2, arg3, arg4 });
            } catch (IllegalArgumentException e) {
                logger.warn(e.getMessage(), e);
            }
        }

        if (resValue != null) {
            try {
                pageContext.getOut().print(
                        escape ? StringEscapeUtils.escapeXml(resValue)
                                : resValue);
            } catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
            }
        }
        return SKIP_BODY;
    }

    public int doEndTag ()
        throws JspException {
        resetState();
        
        return EVAL_PAGE;
    }

    
    public void setArg0(Object arg0) {
        this.arg0 = arg0;
    }

    public void setArg1(Object arg1) {
        this.arg1 = arg1;
    }

    public void setArg2(Object arg2) {
        this.arg2 = arg2;
    }

    public void setArg3(Object arg3) {
        this.arg3 = arg3;
    }

    public void setArg4(Object arg4) {
        this.arg4 = arg4;
    }

    public void setEscape(boolean escape) {
        this.escape = escape;
    }

    @Override
    protected void resetState() {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        resourceName = "";
        localeLanguage = "";
        localeCountry = "";
        localeVariant = "";
        locale = null;
        name = null;
        namePostFix = null;
        arg0 = arg1 = arg2 = arg3 = arg4 = null;
        escape = false;
        
        super.resetState();
    }

}
