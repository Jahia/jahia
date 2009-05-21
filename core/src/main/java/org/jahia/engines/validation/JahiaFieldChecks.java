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
package org.jahia.engines.validation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.Arg;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.validator.FieldChecks;
import org.apache.struts.validator.Resources;
import org.jahia.bin.Jahia;
import org.jahia.engines.shared.JahiaPageEngineTempBean;
import org.jahia.engines.shared.Page_Field;
import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.SiteLanguageSettings;

/**
*
* <p>Title: JahiaFieldChecks</p>
* <p>Description: This class implements the Jahia specific user input validation methods.</p>
* <p>Copyright: Copyright (c) 2004</p>
* <p>Company: Jahia Ltd</p>
* @author not attributable
* @version 1.0
*/
public class JahiaFieldChecks extends FieldChecks {
    private static final org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaFieldChecks.class);

    /**
     * 
     */
    public JahiaFieldChecks() {
        super();
    }

    /**
     * Checks if texts for all languages are provided.
     * 
     * @param bean
     * @param va
     * @param field
     * @param errors
     * @param request
     * @return true if text in all languages is given, false if not
     */
    public static boolean validateMandatoryMLText(
        Object bean,
        ValidatorAction va,
        Field field,
        ActionMessages errors,
        HttpServletRequest request) {
            
        Arg mltProperty = field.getArg("mltProperty", 0);
        Object value = getPropertyValue(bean, 
            mltProperty != null && "mltProperty".equals(mltProperty.getName())? 
                mltProperty.getKey()  : field.getProperty());

        if (!(value instanceof JahiaMltHelper)) {
            if (value != null) {
                logger.error(
                    "property value must be an JahiaMltHelper "
                        + field.getProperty());
            }
            return false;
        } else {
            JahiaMltHelper mltHelper = (JahiaMltHelper)value;
            if (!isAllMandatoryLanguagesSet(mltHelper)) {
                errors.add(
                    field.getKey(),
                    Resources.getActionMessage(request, va, field));
                return false;
            }
            return true;
        }
    }
    
    /**
     * Checks if texts for all languages are provided, if a text in at least
     * one language was already set.
     * 
     * @param bean
     * @param va
     * @param field
     * @param errors
     * @param request
     * @return true if text in all languages is given, false if not
     */
    public static boolean validateMandatoryMLTextIfSet(
        Object bean,
        ValidatorAction va,
        Field field,
        ActionMessages errors,
        HttpServletRequest request) {
            
        Arg mltProperty = field.getArg("mltProperty", 0);
        Object value = getPropertyValue(bean, 
            mltProperty != null && "mltProperty".equals(mltProperty.getName())? 
                mltProperty.getKey() : field.getProperty());

        if (!(value instanceof JahiaMltHelper)) {
            if (value != null) {
                logger.error(
                    "property value must be an JahiaMltHelper "
                        + field.getProperty());
            }
            return false;
        } else {
            JahiaMltHelper mltHelper = (JahiaMltHelper)value;
            String[] text = mltHelper.getText();
            boolean allEmpty = true;
            if (text != null) {
                for (int i = 0; i < text.length && allEmpty; i++) {
                    if (text[i] != null && text[i].length() > 0) {
                        allEmpty = false;
                    }
                }
            }
            if (!allEmpty && !isAllMandatoryLanguagesSet(mltHelper)) {
                errors.add(
                    field.getKey(),
                    Resources.getActionMessage(request, va, field));
                return false;
            }
            return true;
        }
    }    

    /**
     * Checks if title of a page field is set, only when the selected option
     * is not "No link" or "Reset link"
     * 
     * @param bean
     * @param va
     * @param field
     * @param errors
     * @param request
     * @return true if text in all languages is given, false if not
     */
    public static boolean validateMandatoryTitleIfLinkValid(Object bean,
            ValidatorAction va, Field field, ActionMessages errors,
            HttpServletRequest request) {
        return validateMandatoryTitleIfLinkValid(bean, field.getProperty(),
                Jahia.getThreadParamBean());
    }    
    

    /**
     * Checks if texts for all languages do not exceed a specified maximum length.
     * 
     * @param bean
     * @param va
     * @param field
     * @param errors
     * @param request
     * @return true if text in all languages are less than maxlength, false if not
     */
    public static boolean validateMaxLengthMLText(
        Object bean,
        ValidatorAction va,
        Field field,
        ActionMessages errors,
        HttpServletRequest request) {
            
        Arg mltProperty = field.getArg("mltProperty", 0);
        Object value = getPropertyValue(bean, 
            mltProperty != null && "mltProperty".equals(mltProperty.getName())? 
                mltProperty.getKey()  : field.getProperty());            

        if (!(value instanceof JahiaMltHelper)) {
            if (value != null) {
                logger.error(
                    "property value must be an JahiaMltHelper "
                        + field.getProperty());
            }
            return false;
        } else {
            JahiaMltHelper mltHelper = (JahiaMltHelper)value;

            int maxLength = Integer.parseInt(field.getVarValue("maxlength"));
            String[] texts = mltHelper.getText();
            for (int i = 0; i < texts.length; i++) {
                if (texts[i] != null && texts[i].length() > maxLength) {
                    errors.add(
                        field.getKey(),
                        Resources.getActionMessage(request, va, field));
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Checks if texts for all languages isn't less than a specified minimum length.
     * 
     * @param bean
     * @param va
     * @param field
     * @param errors
     * @param request
     * @return true if text in all languages are not less than minlength, false if they are
     */
    public static boolean validateMinLengthMLText(
        Object bean,
        ValidatorAction va,
        Field field,
        ActionMessages errors,
        HttpServletRequest request) {
            
        Arg mltProperty = field.getArg("mltProperty", 0);
        Object value = getPropertyValue(bean, 
            mltProperty != null && "mltProperty".equals(mltProperty.getName())? 
                mltProperty.getKey()  : field.getProperty());

        if (!(value instanceof JahiaMltHelper)) {
            if (value != null) {
                logger.error(
                    "property value must be an JahiaMltHelper "
                        + field.getProperty());
            }
            return false;
        } else {
            JahiaMltHelper mltHelper = (JahiaMltHelper)value;

            int minLength = Integer.parseInt(field.getVarValue("minlength"));
            String[] texts = mltHelper.getText();
            for (int i = 0; i < texts.length; i++) {
                if (texts[i] != null && texts[i].length() < minLength) {
                    errors.add(
                        field.getKey(),
                        Resources.getActionMessage(request, va, field));
                    return false;
                }
            }
            return true;
        }
    }    
    
    /**
     * Check if all mandatory languages are set within the passed JahiaMltHelper Object
     * 
     * @param mltHelper
     * @return true if text in all languages is given, false if not
     */
    public static boolean isAllMandatoryLanguagesSet(JahiaMltHelper mltHelper) {
        String[] texts = mltHelper.getText();
        String[] lang = mltHelper.getLanguage();

        for (Iterator languageIterator = mltHelper.getLanguageSettings()
                .listIterator(); languageIterator.hasNext();) {
            SiteLanguageSettings siteLanguageSettings = (SiteLanguageSettings) languageIterator
                    .next();
            if (siteLanguageSettings.isMandatory()) {
                boolean found = false;
                for (int i = 0; texts != null && i < texts.length && !found; i++) {
                    if (lang[i].equals("shared")
                            || lang[i].equals(siteLanguageSettings.getCode())) {
                        found = true;
                        if (texts[i] == null || texts[i].length() == 0) {
                            return false;
                        }
                    }
                }
                if (!found)
                    return false;
            }
        }
        return true;
    }

    /**
     * Uses PropertyUtils to get property from bean.
     * If PropertyUtils.getProperty() throws an exception, the exception is logged and null is returned.
     * @param bean
     * @param property
     * @return the bean property or null
     */
    private static Object getPropertyValue(Object bean, String property) {
        Object value = null;
        try {
            value = PropertyUtils.getProperty(bean, property);
        } catch (Exception ex) {
            logger.error("unable to get property value " + property, ex);
        }
        return value;
    }

    /**
     * Checks if title of a page field is set, only when the selected option
     * is not "No link" or "Reset link"
     * 
     * @param bean
     * @param fieldName
     * @param ctx
     * @return true if text in all languages is given, false if not
     */
    public static boolean validateMandatoryTitleIfLinkValid(Object bean,
            String fieldName, ProcessingContext ctx) {
        boolean fieldValid = false;
        String value = (String) getPropertyValue(bean, fieldName);
        if (value != null) {
            fieldValid = true;
        } else {
            Map pageBeans = null;
            if (ctx != null) {
                pageBeans = (Map) ctx.getSessionState().getAttribute(
                        "Page_Field.PageBeans");
            }
            if (pageBeans == null) {
                pageBeans = new HashMap();
            }

            JahiaPageEngineTempBean pageBean = (JahiaPageEngineTempBean) pageBeans
                    .get(fieldName);

            if (pageBean == null
                    || Page_Field.RESET_LINK.equals(pageBean.getOperation())) {
                fieldValid = true;
            }
        }
        return fieldValid;
    }

}
