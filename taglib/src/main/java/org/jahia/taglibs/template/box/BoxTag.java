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
package org.jahia.taglibs.template.box;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.settings.SettingsBean;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.container.ContainerTag;
import org.jahia.taglibs.template.field.FieldTag;
import org.jahia.utils.StringResponseWrapper;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Tag used to dispatch the request to the JSP file responsible of displaying
 * the actual box. Also handles any exception occurred during box rendering
 * depending on the configured policy.
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class BoxTag extends AbstractJahiaTag {

    private static enum OnErrorType {
        COMPACT, DEFAULT, FULL, HIDE, PROPAGATE
    }

    public static final String BOX_PROPERTY_ON_ERROR = "boxes.onError";

    public static final String BOX_TITLE_FIELD_NAME = "boxTitle";

    public static final String BOX_TYPE_FIELD_NAME = "jcr_primaryType";

    private static final String DEFAULT_ERROR_MESSAGE_KEY = "boxes.onError.message";

    private static final OnErrorType DEFAULT_ON_ERROR = OnErrorType.FULL;

    private static final transient Logger logger = Logger.getLogger(BoxTag.class);

    private String boxTitle;
    private boolean displayTitle = true;
    private String errorMessage;
    private String errorMessageKey;
    private String id;
    private OnErrorType onError;
    private String surroundingDivCssClass;
    private boolean inlineEditingActivated;

    public int doEndTag() throws JspException {

        try {
            String content = getBoxContent();
            if (content != null) {
                pageContext.getOut().append(content);
            }
        } catch (IOException e) {
            throw new JspException(e);
        } finally {
            reset();
        }

        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {
        resolveOnError();
        return super.doStartTag();
    }

    private String getBoxContent() throws JspException {

        String content = null;
        String view = null;

        try {
            final ContainerTag parentContainerTag = (ContainerTag) findAncestorWithClass(
                    this, ContainerTag.class);
            final JahiaContainer parentContainer = parentContainerTag.getContainer();
//            pageContext.setAttribute(PARENT_TAG_REQUEST_ATTRIBUTE,
//                    parentContainerTag, PageContext.REQUEST_SCOPE);
            String boxType = extractKey(parentContainer.getFieldValue(BOX_TYPE_FIELD_NAME, ""));
            boxType = boxType.replace(":", "_");
            view = resolveIncludeFullPath("common/box/display/"
                    + boxType.substring(boxType.lastIndexOf('_') + 1)
                    + "Display.jsp");
            boxTitle = parentContainer.getFieldValue(BOX_TITLE_FIELD_NAME, "");
            final JahiaField boxTitleField = parentContainer.getField(BOX_TITLE_FIELD_NAME);

            RequestDispatcher dispatcher = pageContext.getRequest()
                    .getRequestDispatcher(view);
            if (null == dispatcher) {
                throw new JahiaException(
                        "Unable to get a request dispatcher for the box view JSP '" + view + "'",
                        "Unable to get a request dispatcher for the box view JSP '" + view + "'",
                        JahiaException.TEMPLATE_SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }
            StringResponseWrapper responseWrapper = new StringResponseWrapper(
                    (HttpServletResponse) pageContext.getResponse());

            dispatcher.include(pageContext.getRequest(), responseWrapper);

            StringBuilder out = new StringBuilder(512);

            if (surroundingDivCssClass != null
                    && surroundingDivCssClass.length() != 0) {
                out.append("<div class=\"").append(surroundingDivCssClass).append("\">");
            }
            if (displayTitle) {
                out.append("<span class=\"boxTitle\">").
                        append(FieldTag.renderEditableContentMarkup(boxTitleField, boxTitle, getProcessingContext(), false, inlineEditingActivated)).
                        append("</span>");
            }

            out.append(responseWrapper.getString());

            if (surroundingDivCssClass != null && surroundingDivCssClass.length() != 0) {
                out.append("</div>");
            }

            content = out.toString();
        } catch (final Exception e) {
            content = handleException(view, e);
        }

        return content;
    }

    public boolean getDisplayTitle() {
        return displayTitle;
    }

    private Object getExceptionDetails(Throwable ex) {

        StringWriter out = new StringWriter();
        out.append(ex.getMessage()).append("\n<!--\n");

        ex.printStackTrace(new PrintWriter(out));

        out.append("\n-->\n");

        return out.toString();
    }

    public String getId() {
        return id;
    }

    private String handleException(String view, Exception ex)
            throws JspException {

        String content = null;

        ContainerTag parentContainerTag = (ContainerTag) findAncestorWithClass(this, ContainerTag.class);

        // disable cache for the parent container tag 
        if (parentContainerTag != null) {
            parentContainerTag.disableCache();
            // TODO seems this is not enough: check how to disable cache correctly
        }

        Throwable cause = null;
        if (ex instanceof ServletException) {
            cause = ((ServletException) ex).getRootCause();
        } else if (ex instanceof JspException) {
            cause = ((JspException) ex).getRootCause();
        }
        cause = cause != null ? cause : ex;

        if (OnErrorType.HIDE.equals(onError)) {
            logger.warn("Error including box JSP view file '" + view
                    + "'. Cause: " + cause.getMessage(), cause);
        } else if (OnErrorType.PROPAGATE.equals(onError)) {
            // do propagate exception to the higher level
            if (ex instanceof JspException) {
                throw (JspException) ex;
            } else {
                throw new JspException("Error including box JSP view file '"
                        + view + "'. Cause: " + cause.getMessage(), cause);
            }
        } else {
            logger.error("Error including box JSP view file '" + view
                    + "'. Cause: " + cause.getMessage(), cause);
            StringBuilder out = new StringBuilder(256);
            if (surroundingDivCssClass != null
                    && surroundingDivCssClass.length() != 0) {
                out.append("<div class=\"").append(surroundingDivCssClass).append("\">");
            }
            if (displayTitle && boxTitle != null) {
                out.append("<span class=\"boxTitle\">").append(
                        StringEscapeUtils.escapeXml(boxTitle))
                        .append("</span>");
            }

            out.append("<div class=\"box-error-message\">").append(
                    OnErrorType.COMPACT.equals(onError) ? resolveErrorMessage()
                            : getExceptionDetails(cause)).append("</div>");

            if (surroundingDivCssClass != null
                    && surroundingDivCssClass.length() != 0) {
                out.append("</div>");
            }

            content = out.toString();
        }

        return content;
    }

    @Override
    public void release() {
        super.release();
        reset();
    }

    private void reset() {
        boxTitle = null;
        displayTitle = true;
        errorMessage = null;
        errorMessageKey = null;
        id = null;
        onError = null;
        inlineEditingActivated = true;
        surroundingDivCssClass = null;
    }

    private Object resolveErrorMessage() {
        String msg = null;
        if (errorMessageKey != null) {
            msg = getMessage(errorMessageKey);
        } else if (errorMessage != null) {
            msg = errorMessage;
        } else {
            msg = getMessage(DEFAULT_ERROR_MESSAGE_KEY);
        }
        return msg;
    }

    private void resolveOnError() {

        if (null == onError || OnErrorType.DEFAULT.equals(onError)) {
            // check template set property first
            String value = getJahiaBean().getSite().getTemplatePackage()
                    .getProperty(BOX_PROPERTY_ON_ERROR);
            if (value == null) {
                // get the default property value from jahia.properties
                value = SettingsBean.getInstance().lookupString(
                        "templates.boxes.onError");
            }
            onError = value != null ? OnErrorType.valueOf(value.toUpperCase())
                    : DEFAULT_ON_ERROR;
        }
    }

    public void setDisplayTitle(boolean displayTitle) {
        this.displayTitle = displayTitle;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setErrorMessageKey(String errorMessageKey) {
        this.errorMessageKey = errorMessageKey;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOnError(String onErrorString) {
        onError = OnErrorType.valueOf(onErrorString.toUpperCase());
    }

    public void setSurroundingDivCssClass(String surroundingDivCssClass) {
        this.surroundingDivCssClass = surroundingDivCssClass;
    }

    public void setInlineEditingActivated(boolean inlineEditingActivated) {
        this.inlineEditingActivated = inlineEditingActivated;
    }
}
