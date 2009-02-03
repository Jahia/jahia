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

package org.jahia.taglibs.template.field;

import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.TextExtractor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.commons.client.ui.EngineOpener;
import org.jahia.ajax.gwt.templates.components.actionmenus.server.helper.ActionMenuLabelProvider;
import org.jahia.ajax.gwt.templates.components.actionmenus.server.helper.ActionMenuURIFormatter;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.CategoryBean;
import org.jahia.data.beans.FieldBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.ResourceBean;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaDateFieldUtil;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.resourcebundle.ResourceBundleMarker;
import org.jahia.services.categories.Category;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.PageInfoInterface;
import org.jahia.taglibs.template.container.ContainerCache;
import org.jahia.taglibs.template.container.ContainerTag;
import org.jahia.utils.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class FieldTag extends AbstractFieldTag {

    private static final transient Logger logger = Logger.getLogger(FieldTag.class);

    private String name;
    private String valueBeanID;
    private String beanID;
    private String containerName;
    private boolean display = true;
    private boolean diffActive = true;
    private boolean inlineEditingActivated = true;
    private String defaultValue;
    private int maxChar = -1;
    private int maxWord = -1;
    private String continueString = "...";
    private String namePostFix;
    private boolean displayUpdateFieldUrl = false;

    private static long uniqueValue = 0; // used by GWT jahiaType tag.

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public void setContinueString(String continueString) {
        this.continueString = continueString;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public void setMaxChar(int maxChar) {
        this.maxChar = maxChar;
    }

    public void setMaxWord(int maxWord) {
        this.maxWord = maxWord;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValueBeanID(String valueBeanID) {
        this.valueBeanID = valueBeanID;
    }

    public void setBeanID(String beanID) {
        this.beanID = beanID;
    }

    public void setInlineEditingActivated(boolean inlineEditingActivated) {
        this.inlineEditingActivated = inlineEditingActivated;
    }

    public void setDiffActive(boolean diffActive) {
        this.diffActive = diffActive;
    }

    public void setNamePostFix(String namePostFix) {
        this.namePostFix = namePostFix;
    }

    // todo workaround, remove attr
    public void setDisplayUpdateFieldUrl(boolean displayUpdateFieldUrl) {
        this.displayUpdateFieldUrl = false ;
    }

    public int doStartTag() throws JspException {
        // we must take care to reset page attribute first !!!!!
        pushTag();
        if (valueBeanID != null) {
            pageContext.removeAttribute(valueBeanID, PageContext.PAGE_SCOPE);
        }
        if (beanID != null) {
            pageContext.removeAttribute(beanID, PageContext.PAGE_SCOPE);
        }

        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        try {
            final JahiaField theField = loadField(this.name, containerName);
            setValueIDAttribute(theField, jData);

            if (display) {
                final JspWriter out = pageContext.getOut();
                out.print(readValue(jData, theField));
                if (displayUpdateFieldUrl) {
                    final String updateFieldUrl = ActionMenuURIFormatter.drawFieldUpdateUrl(theField.getContentField(),
                            jData.getProcessingContext());
                    final StringBuilder updateFieldLink = new StringBuilder("<div class=\"directAction\"><a onClick=\"window.open('").
                            append(updateFieldUrl).append("', '").append(EngineOpener.ENGINE_FRAME_NAME).append("', '").
                            append(EngineOpener.ENGINE_WINDOW_PARAMS).append("');\"><span class=\"updateField\">").
                            append(ActionMenuLabelProvider.getLocalizedActionLabel(
                                    getResourceBundle(), jData.getProcessingContext(), "update", namePostFix, ActionMenuLabelProvider.FIELD)).append("</span></a></div>");
                    out.print(updateFieldLink);
                }
            }

            // in the case of the application field, we must expire the container cache entry immediately, even if this
            // tag doesn't immediately display the value (which is usually the case, as we want to display the window
            // and mode beans first.
            if (theField == null) {
                return SKIP_BODY;
            }
            switch (theField.getType()) {
                case FieldTypes.APPLICATION :
                    final String appId = theField.getValue();
                    final boolean appIdSet = appId != null && appId.length() > 0;
                    if (appIdSet) {
                        ContainerCache cacheTag = (ContainerCache) findAncestorWithClass(this, ContainerCache.class);
                        if (cacheTag != null) {
                            final ContainerTag fragmentTag = (ContainerTag) cacheTag;
                            fragmentTag.setExpiration("0");
                            ContainerCache ancestor = (ContainerCache) findAncestorWithClass(fragmentTag, ContainerCache.class);
                            if (ancestor != null) {
                                ((ContainerTag) ancestor).setExpiration("1");
                            }
                        }
                    }   
                    break;
            }

        } catch (IOException ioe) {
            logger.error("Error while displaying tag '" + this.name +
                    "' : " + ioe.toString(), ioe);
        } catch (JahiaException je) {
            logger.error("Error while displaying tag '" + this.name +
                    "' : " + je.toString(), je);
        } catch (NullPointerException npe) {
            logger.error("Error while displaying tag '" + this.name +
                    "' : " + npe.toString(), npe);
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        maxChar = -1;
        maxWord = -1;
        continueString = "...";
        valueBeanID = null;
        beanID = null;
        display = true;
        inlineEditingActivated = true;
        diffActive = true;
        containerName = null;
        defaultValue = null;
        name = null;
        namePostFix = null;
        displayUpdateFieldUrl = false;
        popTag();
        return EVAL_PAGE;
    }

    /**
     * Let's set the value into the page context if the valueId parameter
     * was set.
     *
     * @param jData    The Jahia data of the request
     * @param theField The field instance
     * @return A String containing the HTML value of the field
     */
    protected String readValue(JahiaData jData, JahiaField theField) {
        final StringBuffer htmlValue = new StringBuffer();
        if (theField == null && (defaultValue == null || defaultValue.length() == 0)) {
            return htmlValue.toString();
        } else if (theField == null) {
            return defaultValue;
        }
        final ProcessingContext processingContext = jData.getProcessingContext();
        final int fieldType = theField.getType();

        switch (fieldType) {
            case FieldTypes.FILE:
                htmlValue.append(readFileFieldValue(theField));
                break;

            case FieldTypes.PAGE:
                htmlValue.append(readPageFieldValue(theField, processingContext));
                break;

            case FieldTypes.APPLICATION:
                htmlValue.append(readApplcationFiedValue(theField, processingContext));
                break;

            default:
                htmlValue.append(readTextFieldValue(theField, processingContext));
                break;
        }

        return htmlValue.toString();
    }

    /**
     * Let's set the value into the page context if the valueId parameter
     * was set.
     *
     * @param theField The field
     * @param jData    The current Jahia Data
     * @throws JahiaException if something goes wrong with a date field
     */
    protected void setValueIDAttribute(final JahiaField theField, final JahiaData jData) throws JahiaException {
        if (theField == null) return;

        if (beanID != null && beanID.length() > 0) {
            pageContext.setAttribute(beanID, new FieldBean(theField, jData.getProcessingContext()));
        }

        if (valueBeanID != null && valueBeanID.length() > 0) {
            switch (theField.getType()) {
                case FieldTypes.FILE:
                    final Object jahiaFileField = theField.getObject();
                    if (jahiaFileField != null) {
                        pageContext.setAttribute(valueBeanID, jahiaFileField);
                    }
                    break;

                case FieldTypes.PAGE:
                    final Object page = theField.getObject();
                    if (page != null) {
                        pageContext.setAttribute(valueBeanID, new PageBean((JahiaPage) page,
                                jData.getProcessingContext()));
                    }
                    break;

                case FieldTypes.DATE:
                    final SimpleDateFormat fmt = JahiaDateFieldUtil.getDateFormatForParsing(theField.getDefinition().
                            getDefaultValue(), jData.getProcessingContext().getLocale());
                    if (theField.getValue() != null
                            && theField.getValue().length() > 0) {
                        try {
                            final Date date = fmt.parse(theField.getValue());
                            pageContext.setAttribute(valueBeanID, date);

                        } catch (final ParseException pe) {
                            logger.error("Error parsing date", pe);
                        }
                    }
                    break;

                case FieldTypes.BOOLEAN:
                    final String booleanValue = theField.getValue();
                    if (booleanValue != null) {
                        pageContext.setAttribute(valueBeanID, Boolean.parseBoolean(booleanValue));
                    }
                    break;

                case FieldTypes.INTEGER:
                    final String integerValue = theField.getValue();
                    if (integerValue != null && integerValue.trim().length() > 0) {
                        pageContext.setAttribute(valueBeanID, Integer.parseInt(integerValue));
                    }
                    break;

                case FieldTypes.FLOAT:
                    final String floatValue = theField.getValue();
                    if (floatValue != null && floatValue.trim().length() > 0) {
                        pageContext.setAttribute(valueBeanID, Float.parseFloat(floatValue));
                    }
                    break;

                case FieldTypes.CATEGORY:
                    final String[] categoryValues = theField.getValues();
                    if (categoryValues != null && categoryValues.length > 0) {
                        final List<CategoryBean> result = new ArrayList<CategoryBean>();
                        for (String categoryKey : categoryValues) {
                            final Category curCategory = Category.getCategory(categoryKey,
                                    jData.getProcessingContext().getUser());
                            if (curCategory != null) {
                                result.add(new CategoryBean(curCategory, jData.getProcessingContext()));
                            }
                        }
                        pageContext.setAttribute(valueBeanID, result);
                    }
                    break;

                default:
                    final String value = theField.getValue();
                    if (value != null) {
                        final ResourceBundleMarker marker = ResourceBundleMarker.parseMarkerValue(theField.getValue());
                        if (marker != null) {
                            final String key = marker.getResourceKey();
                            final String defaultValue = marker.getDefaultValue();
                            final String localizedValue = marker.getValue(jData.getProcessingContext().getLocale());
                            final ResourceBean bean = new ResourceBean(key, localizedValue, defaultValue);
                            pageContext.setAttribute(valueBeanID, bean);

                        } else {
                            final ResourceBean bean = new ResourceBean(null, value, value);
                            pageContext.setAttribute(valueBeanID, bean);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Read the field value of a file Field
     *
     * @param theField the field instance
     * @return the field value
     */
    protected String readFileFieldValue(final JahiaField theField) {
        final StringBuffer buff = new StringBuffer();
        if (theField == null) return buff.toString();
        final JahiaFileField theFile = (JahiaFileField) theField.getObject();
        if (theFile != null && theFile.isDownloadable()) {
            buff.append("<a class=\"");
            if (cssClassName != null && cssClassName.length() > 0) {
                buff.append(cssClassName);
            } else {
                buff.append(FileUtils.getFileIcon(theFile.getFileFieldTitle()));
            }
            buff.append("\"");

            buff.append(" title=\"");
            buff.append(theFile.getFileFieldTitle());
            buff.append("\"");

            buff.append(" href=\"");
            buff.append(theFile.getDownloadUrl());
            buff.append("\">");

            final String title;
            if (maxChar > 0) {
                final String tmp = theFile.getFileFieldTitle();
                if (tmp != null && tmp.length() > maxChar) {
                    title = tmp.substring(0, maxChar - 3) + "...";
                } else {
                    title = theFile.getFileFieldTitle();
                }

            } else {
                title = theFile.getFileFieldTitle();
            }
            buff.append(title);
            buff.append("</a>");

        } else if (defaultValue != null && defaultValue.length() > 0) {
            buff.append(resolveTitle(defaultValue, getBundleKey(), defaultValue, getProcessingContext().getLocale()));
        }
        return buff.toString();
    }

    /**
     * Read the field value of a page Field
     *
     * @param theField          the field instance
     * @param processingContext The current processing context instance
     * @return the field value
     */
    protected String readPageFieldValue(final JahiaField theField,
                                        final ProcessingContext processingContext) {
        final StringBuffer buff = new StringBuffer();
        if (theField == null) return buff.toString();
        final JahiaPage thePage = (JahiaPage) theField.getObject();

        if (thePage != null) {
            if (thePage.getPageType() == PageInfoInterface.TYPE_URL) {
                buff.append("<span class=\"externallink\">");
            } else if (thePage.getPageType() == PageInfoInterface.TYPE_LINK) {
                buff.append("<span class=\"link\">");
            } else {
                buff.append("<span class=\"page\">");
            }

            try {
                buff.append("<a href=\"");
                buff.append(thePage.getURL(processingContext));
                buff.append("\"");
            } catch (Exception e) {
                logger.error("Error in readPageFieldValue", e);
            }

            if (cssClassName != null && cssClassName.length() > 0) {
                buff.append(" class=\"");
                buff.append(cssClassName);
                buff.append("\"");
            }

            if (thePage.getPageType() == PageInfoInterface.TYPE_URL) {
                buff.append(" target=\"_blank\"");
            }
            buff.append(">");

            final String link;
            final String tmp;

            if (diffActive) {
                tmp = thePage.getHighLightDiffTitle(processingContext);
            } else {
                tmp = thePage.getTitle();
            }

            if (maxChar > 0 && tmp != null && tmp.length() > maxChar) {
                link = tmp.substring(0, maxChar - 3) + "...";
            } else {
                link = tmp;
            }

            buff.append(link);
            buff.append("</a>\n</span>");

        } else if (defaultValue != null && defaultValue.length() > 0) {
            buff.append(resolveTitle(defaultValue, getBundleKey(), defaultValue, processingContext.getLocale()));
        }
        return buff.toString();
    }

    /**
     * Read the field value of a text Field
     *
     * @param theField          the field instance
     * @param processingContext The current processing context instance
     * @return the field value
     */
    final String readTextFieldValue(final JahiaField theField, final ProcessingContext processingContext) {
        final StringBuffer buff = new StringBuffer();
        final boolean defaultValueSet = defaultValue != null && defaultValue.length() > 0;

        final String textValue;
        if (diffActive) {
            textValue = theField.getHighLightDiffValue(processingContext);
        } else {
            textValue = theField.getValue();
        }

        final String textValueNoRBMarker = getValueFromResourceBundleMarker(textValue);
        final String truncatedValue = getTruncatedValue(textValueNoRBMarker);
        final boolean textValueExists = truncatedValue != null && truncatedValue.length() > 0;
        final boolean createDiv = (textValueExists || defaultValueSet) && (cssClassName != null && cssClassName.length() > 0);

        if (createDiv) {
            buff.append("<div class=\"");
            buff.append(cssClassName);
            buff.append("\">");
        }

        if (textValueExists) {
            buff.append(renderEditableContentMarkup(theField, truncatedValue, processingContext));

        } else if (defaultValueSet) {
            final String defValue = resolveTitle(defaultValue, getBundleKey(), defaultValue, getProcessingContext().getLocale());
            buff.append(renderEditableContentMarkup(theField, defValue, processingContext));
        }

        if (createDiv) {
            buff.append("</div>");
        }

        return buff.toString();
    }

    /**
     * Read the field value of an Application Field
     *
     * @param theField          the field instance
     * @param processingContext The current processing context instance
     * @return the field value
     */
    final String readApplcationFiedValue(final JahiaField theField, final ProcessingContext processingContext) {
        final StringBuffer buff = new StringBuffer();

        final boolean defaultValueSet = defaultValue != null && defaultValue.length() > 0;
        final String appId;
        if (diffActive) {
            appId = theField.getHighLightDiffValue(processingContext);
        } else {
            appId = theField.getValue();
        }
        final boolean appIdSet = appId != null && appId.length() > 0;
        final boolean createDiv = (defaultValueSet || appIdSet) && (cssClassName != null && cssClassName.length() > 0);

        if (createDiv) {
            buff.append("<div class=\"");
            buff.append(cssClassName);
            buff.append("\">");
        }

        if (appIdSet) {
            ContainerCache cacheTag = (ContainerCache) findAncestorWithClass(this, ContainerCache.class);
            if (cacheTag != null) {
                final ContainerTag fragmentTag = (ContainerTag) cacheTag;
                fragmentTag.setExpiration("0");
                ContainerCache ancestor = (ContainerCache) findAncestorWithClass(fragmentTag, ContainerCache.class);
                if (ancestor != null) {
                    ((ContainerTag) ancestor).setExpiration("1");
                }
            }

        } else if (defaultValueSet) {
            buff.append(resolveTitle(defaultValue, getBundleKey(), defaultValue, getProcessingContext().getLocale()));
        }
        if (createDiv) {
            buff.append("</div>");
        }
        return buff.toString();
    }

    /**
     * Let's set the value into the page context if the valueId parameter
     * was set.
     *
     * @param theField          the field instance
     * @param value             String value to use rather than the field's raw value
     * @param processingContext The ProcessingContext instance
     * @return the HTML with the field value
     */
    protected String renderEditableContentMarkup(final JahiaField theField,
                                                 final String value,
                                                 final ProcessingContext processingContext) {
        String resultingValue;
        if (theField == null) {
            logger.warn("Field is null, ignoring contentEditable feature rendering.");
            return value;
        }
        if (value == null) {
            resultingValue = theField.getValue();
            if (resultingValue == null) {
                return null;
            }
        } else {
            resultingValue = value;
        }

        if (processingContext == null) {
            logger.warn("ProcessingContext is null, ignoring contentEditable feature. ");
            return resultingValue;
        }
        if (!resultingValue.startsWith("<jahia-")) {
            if (theField.getType() == FieldTypes.BIGTEXT ||
                theField.getType() == FieldTypes.SMALLTEXT ||
                theField.getType() == FieldTypes.SMALLTEXT_SHARED_LANG) {
                if (processingContext.settings().isInlineEditingActivated() &&
                    ParamBean.EDIT.equals(processingContext.getOperationMode()) &&
                    inlineEditingActivated &&
                    theField.checkWriteAccess(processingContext.getUser())) {
                    resultingValue = "<div class=\"editableContent\" onclick=\"onClickEditableContent(this, '" +
                            theField.getctnid() + "', '" + theField.getID() +
                            "')\" onblur=\"onBlurEditableContent(this, '" + theField.getctnid() + "', '" + theField.getID() + "')\">" +
                            resultingValue + "</div>";
                    /* couldn't make this version work, maybe we can try again ?
                    resultingValue = "<div id=\""+ uniqueValue++ +"\" containerID=\""+theField.getctnid()+
                            "\" fieldID=\""+theField.getID()+"\" " +
                            JahiaType.JAHIA_TYPE + "=\"" + JahiaType.INLINE_EDITING + "\">" + resultingValue + "</div>";
                    */
                }
            }
        }
        return resultingValue;
    }

    /**
     * Let's set the value into the page context if the valueId parameter
     * was set.
     *
     * @param initialValue Initial value of the field
     * @return the truncated value
     */
    protected String getTruncatedValue(final String initialValue) {
        if (initialValue == null) return defaultValue;
        String endvalue = initialValue;
        if (endvalue != null) {
            String tmp = StringUtils.replace(endvalue, "\r", "");
            tmp = StringUtils.replace(tmp, "\n", "");
            tmp = tmp.trim();
            if (tmp.startsWith("<p>")) {
                String result = tmp.substring(3);
                if (result.endsWith("</p>")) {
                    result = result.substring(0, result.length() - 4);
                }
                endvalue = result;
            }
        }
        if (maxChar >= 0 || maxWord >= 0) {

            if (maxChar >= 0) {
                endvalue = (new TextExtractor(new Source(endvalue))).toString();
                if (endvalue.length() > maxChar) {
                    endvalue = endvalue.substring(0,maxChar)+continueString;
                }
            } else {
                //maxWord
                //remove HTML Tags
                String dispText = (new TextExtractor(new Source(endvalue))).toString();
                StringTokenizer tokenizer = new StringTokenizer(dispText);
                StringBuffer enddisp = new StringBuffer();
                int i = 0;
                while (tokenizer.hasMoreElements() && i < maxWord) {
                    enddisp.append((String) tokenizer.nextElement()).append(" ");
                    i++;
                }
                if (i == maxWord)
                    enddisp.append(continueString);
                endvalue = enddisp.toString();
            }
        }

        return endvalue;
    }
}
