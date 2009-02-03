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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.data.containers;

import net.sf.cglib.proxy.InvocationHandler;

import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaContentFieldFacade;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.files.JahiaFileField;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.shared.JahiaPageEngineTempBean;
import org.jahia.engines.validation.JahiaMltHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.version.EntryLoadRequest;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

/**
 * Used as the base class for all container validator beans.
 *
 * @author Benjamin Papez
 */
public class ContainerValidatorBase implements InvocationHandler {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(ContainerValidatorBase.class);

    protected ContainerFacadeInterface cf = null;

    protected ProcessingContext jParams = null;

    /**
     * The constructor obtains the container facade and the ProcessingContext object used
     * in the Jahia engines.
     *
     * @param newCf
     * @param newParams
     */
    public ContainerValidatorBase(ContainerFacadeInterface newCf,
                                  ProcessingContext newParams) {
        super();
        cf = newCf;
        jParams = newParams;
    }

    /**
     * Gets the Jahia field out of the container field facade. The field is
     * returned in the currently processed language. The version is either the
     * staged or the active version. Keywords for uninitialized values are
     * deleted. For a PAGE, the title is returned.
     *
     * @param fieldName
     * @return value
     */
    protected String getJahiaField(String fieldName) {
        String field = null;
        if (fieldName == null) {
            return null;
        }

        try {
            Map<String, Object> engineMap = (Map<String, Object>) jParams
                    .getSessionState().getAttribute("jahia_session_engineMap");

            List<Locale> locales = new ArrayList<Locale>();
            Locale languageCode = (Locale) engineMap
                    .get(JahiaEngine.PROCESSING_LOCALE);
            locales.add(languageCode);

            EntryLoadRequest entryLoadRequest = new EntryLoadRequest(
                    EntryLoadRequest.STAGING_WORKFLOW_STATE
                            | EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0, locales);

            for (Iterator<JahiaContentFieldFacade> fields = cf.getFields(); fields.hasNext() && field == null;) {
                JahiaContentFieldFacade cff = fields.next();

                JahiaField newJf = cff.getField(entryLoadRequest, false);
                if (fieldName.equals(newJf.getDefinition().getName())) {
                    switch (newJf.getType()) {
                        case FieldTypes.PAGE:
                            Map<String, JahiaPageEngineTempBean> pageBeans = (Map<String, JahiaPageEngineTempBean>) jParams
                                    .getSessionState().getAttribute(
                                    "Page_Field.PageBeans");
                            if (pageBeans == null) {
                                pageBeans = new HashMap<String, JahiaPageEngineTempBean>();
                            }

                            JahiaPageEngineTempBean pageBean = pageBeans
                                    .get(newJf.getDefinition().getName());

                            JahiaPage newPage = (JahiaPage) newJf.getObject();

                            field = "";
                            Map<String, String> titles = (pageBean != null ? pageBean
                                    .getTitles()
                                    : (newPage == null ? null : newPage
                                    .getContentPage().getTitles(true)));
                            if (titles != null) {
                                field = titles.get(languageCode.toString());
                            }

                            break;

                        case FieldTypes.BIGTEXT:
                            field = removeDefaultHtmlTags(newJf.getValue());
                            break;

                        case FieldTypes.DATE:
                            if (newJf.getValue() != null
                                    && newJf.getValue().toUpperCase().indexOf("JAHIA_CALENDAR") != -1)
                                field = "";
                            else
                                field = newJf.getValue();
                            break;

                        case FieldTypes.FILE:
                            field = newJf.getObject() != null ? ((JahiaFileField) newJf
                                    .getObject()).getRealName()
                                    : "";
                            break;

                        case FieldTypes.APPLICATION:
                            field = (newJf.getObject() != null ? newJf.getObject().toString() : "");
                            break;

                        default:
                            field = newJf.getValue();
                            break;
                    }
                }
            }
        }
        catch (JahiaException e) {
            logger.error("Error in retrieving Jahia field :", e);
        }
        if (field != null && "".equals(field.trim()))
            field = null;
        return field;
    }

    /**
     * Gets the Jahia multivalue field out of the container field facade. The
     * field is returned in the currently processed language. The version is
     * either the staged or the active version. The field values are converted to
     * a String array.
     *
     * @param fieldName
     * @return fieldValues
     */
    protected JahiaMltHelper getJahiaMultiLanguageField(String fieldName) {
        JahiaMltHelper field = null;

        try {
            for (Iterator<JahiaContentFieldFacade> fields = cf.getFields(); fields.hasNext() && field == null;) {
                JahiaContentFieldFacade cff = fields.next();

                for (Iterator<JahiaField> fieldsPerLanguage = cff.getFields(); fieldsPerLanguage.hasNext();) {
                    JahiaField newJf = fieldsPerLanguage.next();

                    if (fieldName.equals(newJf.getDefinition().getName())) {
                        if (field == null)
                            field = new JahiaMltHelper(jParams.getSite()
                                    .getLanguageSettings());

                        switch (newJf.getType()) {
                            case FieldTypes.PAGE:
                                Map<String, JahiaPageEngineTempBean> pageBeans = (Map<String, JahiaPageEngineTempBean>) jParams
                                        .getSessionState().getAttribute(
                                                "Page_Field.PageBeans");
                                if (pageBeans == null) {
                                    pageBeans = new HashMap<String, JahiaPageEngineTempBean>();
                                }

                                JahiaPageEngineTempBean pageBean = 
                                        pageBeans.get(newJf.getDefinition().getName());
                                JahiaPage newPage = (JahiaPage) newJf.getObject();

                                Map<String, String> titles = (pageBean != null ? pageBean.getTitles() : (newPage == null ? null : newPage
                                        .getContentPage().getTitles(true)));
                                if (titles != null) {
                                    for (Map.Entry<String, String> entry : titles.entrySet()) {
                                        field.addMltItem(entry.getKey(), entry.getValue());
                                    }
                                }
                                break;

                            case FieldTypes.BIGTEXT:
                                field.addMltItem(newJf.getLanguageCode(),
                                        removeDefaultHtmlTags(newJf.getValue()));
                                break;

                            case FieldTypes.DATE:
                                if (newJf.getValue() != null
                                        && newJf.getValue().toUpperCase().indexOf(
                                        "JAHIA_CALENDAR") != -1)
                                    field.addMltItem(newJf.getLanguageCode(), "");
                                else
                                    field.addMltItem(newJf.getLanguageCode(),
                                            newJf.getValue());
                                break;

                            case FieldTypes.FILE:
                                field.addMltItem(newJf.getLanguageCode(),
                                        newJf.getObject() != null ? ((JahiaFileField) newJf
                                                .getObject()).getRealName() : "");                                
                                break;

                            case FieldTypes.APPLICATION:
                                field.addMltItem(newJf.getLanguageCode(),
                                        (newJf.getObject() != null ?
                                                newJf.getObject().toString() : ""));
                                break;

                            default:
                                field.addMltItem(newJf.getLanguageCode(),
                                        newJf.getValue());
                                break;
                        }
                    }
                }
            }
        }
        catch (JahiaException e) {
            logger.error("Error in retrieving Jahia field :", e);
        }
        return field.isEmpty() ? null : field;
    }

    /**
     * Remove tags <html>, <br>
     *     * @param str
     * @return the string with removed tags
     */
    public String removeDefaultHtmlTags(String str) {
        if (str == null) {
            return null;
        }

        str = removeTags(str, "html");
        str = removeTag(str, "br");

        return str;
    }

    /*
    * Remove a start & end tag in a string
    * For example removeTags(str,"title") will remove <title> and </title>
    * @author POL
    * @version 1.0   POL 23/01/2002
    * @param  str    Input String
    * @param  tag    Tag to remove
    * @return str
    **/
    private String removeTags(String str, String tag) {
        str = removeTag(str, tag);
        str = removeTag(str, "/" + tag);
        str = removeSpacesAndBreaks(str);
        return str;
    }

    /*
    * Remove a tag from a string
    * Exemple: removeTag(str,"body") will remove <body bgcolor="#ffffff">
    * @author POL
    * @version 1.0   POL 23/01/2002
    * @param  str    Input String
    * @param  tag    Tag to remove
    * @return str
    **/
    private String removeTag(String str, String tag) {
        if (str == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(str.length());
        String strLower = str.toLowerCase();
        tag = tag.toLowerCase();
        int startOfIndex = 0;
        int i = strLower.indexOf("<" + tag);
        while (i != -1) {
            result.append(str.substring(startOfIndex, i));
            i = strLower.indexOf(">", i);
            if (i != -1) {
                startOfIndex = i + 1;
            }
            i = strLower.indexOf("<" + tag, startOfIndex);
        }
        str = result.append(str.substring(startOfIndex, str.length())).toString();
        return str;
    }

    /*
    * Remove a tag from a string
    * Exemple: removeTag(str,"body") will remove <body bgcolor="#ffffff">
    * @author POL
    * @version 1.0   POL 23/01/2002
    * @param  str    Input String
    * @param  tag    Tag to remove
    * @return str
    **/
    private String removeSpacesAndBreaks(String str) {
        if (str == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(str.length());
        int startOfIndex = 0;
        int i = str.indexOf("/r");
        while (i != -1) {
            result.append(str.substring(startOfIndex, i));
            startOfIndex = i + 2;
            i = str.indexOf("/r", startOfIndex);
        }

        str = result.append(str.substring(startOfIndex, str.length())).toString();
        result = new StringBuffer(str.length());
        startOfIndex = 0;
        i = str.indexOf("/r");
        while (i != -1) {
            result.append(str.substring(startOfIndex, i));
            startOfIndex = i + 2;
            i = str.indexOf("/r", startOfIndex);
        }

        str = result.append(str.substring(startOfIndex, str.length())).toString();

        return str.trim();
    }

    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
        logger.debug("Calling method " + method.getName());
        Object field = null;        
        if (method.getName().startsWith("get") && method.getName().length() > 3) {
            String fieldName = method.getName().substring(3).toLowerCase();
            if (fieldName.endsWith("mlt")) {
                fieldName = fieldName.substring(0, fieldName.length() - 3);
            }
            
            String matchingFieldName = getMatchingFieldName(fieldName);

            if (method.getReturnType().getName().equals(String.class.getName())) {
                field = getJahiaField(matchingFieldName);
            } else if (method.getReturnType().getName().equals(
                    JahiaMltHelper.class.getName())) {
                field = getJahiaMultiLanguageField(matchingFieldName);
            } else {
                field = method.invoke(this, objects);
            }
        } else {
            field = method.invoke(this, objects);
        }
        return field;
    }
    
    protected String getMatchingFieldName(String fieldName) throws JahiaException {
            String matchingFieldName = null;
        for (Iterator<JahiaContentFieldFacade> fields = cf.getFields(); fields.hasNext() && matchingFieldName == null;) {
            JahiaContentFieldFacade cff = fields.next();

            for (Iterator<JahiaField> fieldsPerLanguage = cff.getFields(); fieldsPerLanguage.hasNext() && matchingFieldName == null;) {
                JahiaField newJf = fieldsPerLanguage.next();
                JahiaFieldDefinition fieldDef = newJf.getDefinition(); 
                String[] aliasNames = fieldDef.getAliasName();
                    if (aliasNames == null || aliasNames.length == 0) {
                    aliasNames = new String[]{fieldDef.getName()};
                    }
                for (String defName : aliasNames) {
                    if (defName.toLowerCase().equals(fieldName)) {
                        matchingFieldName = fieldDef.getName();
                        }
                    }
                }
            }
            if (matchingFieldName == null) {
                // try to find field by matching name
            for (Iterator<JahiaContentFieldFacade> fields = cf.getFields(); fields.hasNext() && matchingFieldName == null;) {
                JahiaContentFieldFacade cff = fields.next();

                for (Iterator<JahiaField> fieldsPerLanguage = cff.getFields(); fieldsPerLanguage.hasNext() && matchingFieldName == null;) {
                    JahiaField newJf = fieldsPerLanguage.next();
                    JahiaFieldDefinition fieldDef = newJf.getDefinition();
                    String defName = fieldDef.getName().toLowerCase();
                        if (defName.equals(fieldName)) {
                        matchingFieldName = fieldDef.getName();
                        }
                    }
                }
            }
        
        return matchingFieldName;
    }
}

