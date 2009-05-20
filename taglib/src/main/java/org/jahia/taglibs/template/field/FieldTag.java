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
package org.jahia.taglibs.template.field;

import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.TextExtractor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.util.EngineOpener;
import org.jahia.ajax.gwt.templates.components.actionmenus.server.helper.ActionMenuLabelProvider;
import org.jahia.ajax.gwt.templates.components.actionmenus.server.helper.ActionMenuURIFormatter;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.CategoryBean;
import org.jahia.data.beans.FieldBean;
import org.jahia.data.beans.FieldValueBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.ResourceBean;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.template.container.ContainerCache;
import org.jahia.taglibs.template.container.ContainerTag;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.jahia.services.categories.Category;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.PageInfoInterface;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.utils.FileUtils;
import org.jahia.utils.JahiaTools;
import org.jahia.content.ContentObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The tag is responsible for displaying content field value or exposing it into
 * a page scope, depending on its type.
 * 
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class FieldTag extends AbstractFieldTag {

    private static final transient Logger logger = Logger.getLogger(FieldTag.class);

    private String name;
    /** 
     * @deprecated use {@link #var} instead
     */
    private String valueBeanID;
    /** 
     * @deprecated use {@link #var} instead
     */
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
    private boolean removeHtmlTags = false;
    private String var;

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

    /**
     * @deprecated use {@link #setVar(String)} instead
     */
    public void setValueBeanID(String valueBeanID) {
        if (logger.isDebugEnabled()) {
            logger.debug("The valueBeanID attribute is deprecated for tag "
                    + StringUtils.substringAfterLast(this.getClass().getName(),
                            ".") + ". Please, use var attribute instead.",
                    new JspException());
        } else {
            logger.info("The valueBeanID attribute is deprecated for tag "
                    + StringUtils.substringAfterLast(this.getClass().getName(),
                            ".") + ". Please, use var attribute instead.");
        }
        this.valueBeanID = valueBeanID == null || valueBeanID.length() == 0 ? null
                : valueBeanID;
    }

    /**
     * @deprecated use {@link #setVar(String)} instead
     */
    public void setBeanID(String beanID) {
        if (logger.isDebugEnabled()) {
            logger.debug("The beanID attribute is deprecated for tag "
                    + StringUtils.substringAfterLast(this.getClass().getName(),
                            ".") + ". Please, use var attribute instead.",
                    new JspException());
        } else {
            logger.info("The beanID attribute is deprecated for tag "
                    + StringUtils.substringAfterLast(this.getClass().getName(),
                            ".") + ". Please, use var attribute instead.");
        }
        this.beanID = beanID == null || beanID.length() == 0 ? null : beanID;
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

    public void setRemoveHtmlTags(boolean removeHtmlTags) {
        this.removeHtmlTags = removeHtmlTags;
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
        if (var != null) {
            pageContext.removeAttribute(var, PageContext.PAGE_SCOPE);
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
                case FieldTypes.APPLICATION:
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
        resetState();
        popTag();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
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
        removeHtmlTags = false;
        var = null;
        super.resetState();
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
        final StringBuilder htmlValue = new StringBuilder();
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

        FieldBean fieldBean = null;
        if (beanID!=null || valueBeanID != null || var != null) {
            fieldBean = new FieldBean(theField, jData.getProcessingContext());
        }
        if (beanID != null) {
            pageContext.setAttribute(beanID, fieldBean);
        }

        if (valueBeanID != null || var != null) {
            
            String fieldValue = null;
            Object rawValue = null;

            switch (theField.getType()) {
                case FieldTypes.FILE:
                    final JahiaFileField jahiaFileField = (JahiaFileField) theField.getObject();
                    if (jahiaFileField != null) {
                        fieldValue = jahiaFileField.getDownloadUrl();
                        rawValue = jahiaFileField;
                    }
                    break;

                case FieldTypes.PAGE:
                    final Object page = theField.getObject();
                    if (page != null) {
                        PageBean pageBean = new PageBean((JahiaPage) page,
                                jData.getProcessingContext());
                        rawValue = pageBean;
                        fieldValue = pageBean.getTitle();
                    }
                    break;

                case FieldTypes.DATE:
                    fieldValue = theField.getValue();
                    rawValue = theField.getObject() != null
                            && StringUtils.isNotEmpty(theField.getObject()
                            .toString()) ? new Date(Long.parseLong(theField
                            .getObject().toString())) : null;
                    break;

                case FieldTypes.BOOLEAN:
                    fieldValue = theField.getValue();
                    rawValue = theField.getObject();
                    break;

                case FieldTypes.INTEGER:
                    fieldValue = theField.getValue();
                    rawValue = theField.getObject() != null ? Integer
                            .valueOf(((Number) theField.getObject()).intValue())
                            : null;
                    break;

                case FieldTypes.FLOAT:
                    fieldValue = theField.getValue();
                    rawValue = theField.getObject() != null ? Float
                            .valueOf(((Number) theField.getObject()).floatValue())
                            : null;
                    break;

                case FieldTypes.CATEGORY:
                    final String[] categoryValues = theField.getValues();
                    fieldValue = theField.getValue();
                    if (categoryValues != null && categoryValues.length > 0) {
                        final List<CategoryBean> result = new ArrayList<CategoryBean>();
                        for (String categoryKey : categoryValues) {
                            final Category curCategory = Category.getCategory(
                                    categoryKey, jData.getProcessingContext()
                                            .getUser());
                            if (curCategory != null) {
                                result.add(new CategoryBean(curCategory, jData
                                        .getProcessingContext()));
                            }
                        }
                        rawValue = result;
                    } else {
                        rawValue = Collections.EMPTY_LIST;
                    }
                    break;

                default:
                    final String value = theField.getValue();
                    fieldValue = value;
                    rawValue = value;
                    if (value != null && value.length() > 0) {
                        final ResourceBundleMarker marker = ResourceBundleMarker.parseMarkerValue(theField.getValue());
                        if (marker != null) {
                            final String key = marker.getResourceKey();
                            final String defaultValue = marker.getDefaultValue();
                            final String localizedValue = marker.getValue(jData.getProcessingContext().getLocale());
                            final ResourceBean bean = new ResourceBean(key, localizedValue, defaultValue);
                            fieldValue = localizedValue;
                            rawValue = bean;

                        } else {
                            final String textValue;
                            if (removeHtmlTags) {
                                textValue = JahiaTools.removeTags(value);
                            } else {
                                textValue = value;
                            }
                            fieldValue = textValue;
                            rawValue = textValue;
                        }
                    }
                    break;
            }

            FieldValueBean<?> valueBean = new FieldValueBean(fieldBean, fieldValue, rawValue);
            if (valueBeanID != null) {
                pageContext.setAttribute(valueBeanID, valueBean);
            }
            if (var != null) {
                pageContext.setAttribute(var, valueBean);
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
        final StringBuilder buff = new StringBuilder();
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
            buff.append(getMessage(defaultValue, defaultValue));
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
        final StringBuilder buff = new StringBuilder();
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
            buff.append(getMessage(defaultValue, defaultValue));
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
        final StringBuilder buff = new StringBuilder();
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
        final boolean createDiv = !removeHtmlTags && (textValueExists || defaultValueSet) && (cssClassName != null && cssClassName.length() > 0);

        if (createDiv) {
            buff.append("<div class=\"");
            buff.append(cssClassName);
            buff.append("\">");
        }

        if (textValueExists) {
            buff.append(renderEditableContentMarkup(theField, truncatedValue, processingContext, removeHtmlTags, inlineEditingActivated));

        } else if (defaultValueSet) {
            final String defValue = getMessage(defaultValue, defaultValue);
            buff.append(renderEditableContentMarkup(theField, defValue, processingContext, removeHtmlTags, inlineEditingActivated));
        }

        if (createDiv) {
            buff.append("</div>");
        }

        if (removeHtmlTags) {
            return JahiaTools.removeTags(buff.toString());
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
        final StringBuilder buff = new StringBuilder();

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
            buff.append(getMessage(defaultValue, defaultValue));
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
    public static String renderEditableContentMarkup(final JahiaField theField,
                                              final String value,
                                              final ProcessingContext processingContext,
                                              final boolean removeHtmlTags,
                                              final boolean inlineEditingActivated) {
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

        if (removeHtmlTags) {  // No inline editing when we do not want any HTML markup in the generated value
            return resultingValue;
        }

        if (processingContext == null) {
            logger.warn("ProcessingContext is null, ignoring contentEditable feature. ");
            return resultingValue;
        }
        try {
            ContentObject picked = theField.getContentField().getPickedObject() ;
            if (picked != null) {
                return resultingValue;
            }
        } catch (JahiaException e) {
            logger.error(e.toString(), e);
        }
        if (inlineEditingActivated && (theField.getType() == FieldTypes.BIGTEXT ||
                theField.getType() == FieldTypes.SMALLTEXT ||
                theField.getType() == FieldTypes.SMALLTEXT_SHARED_LANG) && !resultingValue.startsWith("<jahia-")) {
                if (ParamBean.EDIT.equals(processingContext.getOperationMode()) &&
                        UserPreferencesHelper.isEnableInlineEditing(processingContext.getUser())
                        && theField.checkWriteAccess(processingContext.getUser())) {
                    resultingValue = "<div class=\"editableContent\" onclick=\"onClickEditableContent(this, '" +
                            theField.getctnid() + "', '" + theField.getID() +
                            "')\" onblur=\"onBlurEditableContent(this, '" + theField.getctnid() + "', '" + theField.getID() + "')\">" +
                            resultingValue + "</div>";
                    /* TODO: couldn't make this version work, maybe we can try again ?
                    resultingValue = "<div id=\""+ uniqueValue++ +"\" containerID=\""+theField.getctnid()+
                            "\" fieldID=\""+theField.getID()+"\" " +
                            JahiaType.JAHIA_TYPE + "=\"" + JahiaType.INLINE_EDITING + "\">" + resultingValue + "</div>";
                    */
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
        if (maxChar >= 0 || maxWord >= 0) {

            if (maxChar >= 0) {
                endvalue = (new TextExtractor(new Source(endvalue))).toString();
                if (endvalue.length() > maxChar) {
                    endvalue = endvalue.substring(0, maxChar) + continueString;
                }

            } else {
                //maxWord
                //remove HTML Tags
                String dispText = (new TextExtractor(new Source(endvalue))).toString();
                StringTokenizer tokenizer = new StringTokenizer(dispText);
                StringBuilder enddisp = new StringBuilder();
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

    public void setVar(String var) {
        this.var = var == null || var.length() == 0 ? null : var;
    }
}
