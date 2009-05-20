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

import org.apache.commons.validator.Field;
import org.apache.commons.validator.Form;
import org.apache.commons.validator.ValidatorResources;
import org.apache.derby.diag.ErrorMessages;
import org.apache.struts.validator.Resources;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;

import java.util.LinkedList;
import java.util.List;


/**
 * <p>Title: EngineValidationHelper</p>
 * <p>Description: This class is used to ease the validation process and groups the
 * validation errors per screen. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class EngineValidationHelper {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(EngineValidationHelper.class);

    private String previousScreen;
    private String nextScreen;
    private final List<ValidationError> validationErrors;

    public EngineValidationHelper() {
        validationErrors = new LinkedList<ValidationError>();
    }

    public String getPreviousScreen() {
        return this.previousScreen;
    }

    public void setPreviousScreen(String screen) {
        this.previousScreen = screen;
    }

    public String getNextScreen() {
        return this.nextScreen;
    }

    public void setNextScreen(String screen) {
        this.nextScreen = screen;
    }

    public void addError(ValidationError ve) {
        if (ve != null) {
            this.validationErrors.add(ve);
        }
    }

    public List<ValidationError> getErrors() {
        return this.validationErrors;
    }

    public boolean hasErrors() {
        return (this.validationErrors.size() > 0);
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append(EngineValidationHelper.class.getName());
        buff.append(": Errors: ").append(validationErrors);
        return buff.toString();
    }

    /**
     * Checks the Validation Rules to see if a given field is mandatory or not.
     */
    public static boolean isFieldMandatory(final JahiaContainer parentContainer,
                                           final JahiaField theField,
                                           final ProcessingContext jParams) throws JahiaException {
        if (parentContainer == null || theField == null) return false;

        final String fieldName = theField.getDefinition().getName();

        final JahiaContainerDefinition def = parentContainer.getDefinition();
        if (def == null) return false;

        String v = def.getNodeType().getValidator();
        String containerBeanName = null;
        String validatorKey = null;
        if ( v != null ) {
            containerBeanName = v.contains(":") ? v.substring(0,v.indexOf(":")) : null;
            validatorKey = v.contains(":") ? v.substring(v.indexOf(":")+1) : v;
        }

        logger.debug("containerBeanName: " + containerBeanName +
                ", validatorKey: " + validatorKey);

        if (containerBeanName != null
                && containerBeanName.length() > 0
                && validatorKey != null
                && validatorKey.length() > 0) {

            final ParamBean paramBean = (ParamBean) jParams;
            final ValidatorResources vr = Resources.getValidatorResources(paramBean.getContext(),
                    paramBean.getRequest());
            if (vr == null) return false;
            final Form form = vr.getForm(jParams.getLocale(), validatorKey);
            logger.debug("Form: " + form);
            if (form == null) return false;
            Field f = form.getField(fieldName);
            if (f == null) {
                String[] aliasNames = theField.getDefinition().getAliasNames();
                for (int i = 0; f == null && i < aliasNames.length; i++) {
                    String aliasName = aliasNames[i];
                    f = form.getField(aliasName);
                }                
            }
            if (f == null) return false;
            return f.getDepends().indexOf("required") > -1;
        }
        return false;
    }

    public EngineMessages getEngineMessages(String property) {
        if (!hasErrors()) {
            return new EngineMessages();
        }

        final EngineMessages result = new EngineMessages();
        for (ValidationError error : getErrors()) {
            result.add(property,
                    error.getRessourceBundleProp() != null ? new EngineMessage(
                            error.getRessourceBundleProp(), error.getValues())
                            : new EngineMessage(error.getMsgError(), false));
        }

        return result;
    }
    
    public ValidationError getFirstError() {
        return getErrors().size() > 0 ? getErrors().get(0) : null;
    }
}