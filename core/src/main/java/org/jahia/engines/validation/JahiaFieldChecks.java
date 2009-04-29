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
        boolean fieldValid = false;
        String value = (String) getPropertyValue(bean, field.getProperty());
        if (value != null) {
            fieldValid = true;
        } else {
            ProcessingContext jParams = Jahia.getThreadParamBean();
            Map pageBeans = null;
            if (jParams != null) {
                pageBeans = (Map) jParams.getSessionState().getAttribute(
                    "Page_Field.PageBeans");
            }
            if (pageBeans == null) {
                pageBeans = new HashMap();
            }

            JahiaPageEngineTempBean pageBean = (JahiaPageEngineTempBean) pageBeans
                .get(field.getProperty());

            if (pageBean == null || Page_Field.RESET_LINK.equals(pageBean.getOperation())) {
                fieldValid = true;
            }
        }
        return fieldValid;
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

}
