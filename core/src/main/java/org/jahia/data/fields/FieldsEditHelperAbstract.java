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

package org.jahia.data.fields;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.data.containers.ContainerEditView;
import org.jahia.data.containers.ContainerEditViewFieldGroup;
import org.jahia.data.containers.ContainerFacadeInterface;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.JahiaEngineTools;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.engines.validation.ValidationError;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author not attributable
 * @version 1.0
 */
public abstract class FieldsEditHelperAbstract implements FieldsEditHelper {

    private static final long serialVersionUID = 1791180400570331125L;

    private static final Logger logger = Logger.getLogger(FieldsEditHelperAbstract.class);

    public static final String FIELDS_EDIT_HELPER_CONTEXTID =
            "org.jahia.data.fields.FieldsEditHelper.ContextID";

    private int selectedFieldId; // selected fieldId
    private int lastFieldId;
    private ContainerEditView ctnEditView; // the current container edit view
    private Map<Integer, String> fieldForms = new HashMap<Integer, String>(); // set of fieldForm
    private Map<Integer, List<String>> updatedFields = new HashMap<Integer, List<String>>(); // updated fields
    private boolean stayOnSameField;

    public JahiaField getSelectedField() {
        return this.getField(this.getSelectedFieldId());
    }

    public int getSelectedFieldId() {
        return this.selectedFieldId;
    }

    public void setSelectedFieldId(int fieldId) {
        this.selectedFieldId = fieldId;
    }

    public int getLastFieldId() {
        return this.lastFieldId;
    }

    public void setLastFieldId(int fieldId) {
        this.lastFieldId = fieldId;
    }

    public Map<Integer, List<String>> getUpdatedFields() {
        if (this.updatedFields == null) {
            this.updatedFields = new HashMap<Integer, List<String>>();
        }
        return this.updatedFields;
    }

    public void addUpdatedField(int fieldId, String languageCode) {
        if (languageCode == null) {
            return;
        }
        List<String> langs = this.getUpdatedFields().get(fieldId);
        if (langs == null) {
            langs = new ArrayList<String>();
            this.getUpdatedFields().put(fieldId, langs);
        }
        if (!langs.contains(languageCode)) {
            langs.add(languageCode);
        }
    }

    public boolean containsUpdatedField(int fieldId, String languageCode) {
        if (languageCode == null) {
            return false;
        }
        List<String> langs = this.getUpdatedFields().get(new Integer(fieldId));
        if (langs != null) {
            return (languageCode.equals(ContentField.SHARED_LANGUAGE)
                    || langs.contains(languageCode));
        }
        return false;
    }

    public Collection<String> getEditedLanguages() {
        Collection<String> s = new HashSet<String>();
        for (List<String> locales : updatedFields.values()) {
            s.addAll(locales);
        }
        return s;
    }

    public Map<Integer, String> getFieldForms() {
        if (this.fieldForms == null) {
            this.fieldForms = new HashMap<Integer, String>();
        }
        return this.fieldForms;
    }

    public void setFieldForms(Map<Integer, String> fieldForms) {
        this.fieldForms = fieldForms;
    }

    public void addFieldForm(int fieldId, String fieldForm) {
        if (fieldForm == null) {
            return;
        }
        this.getFieldForms().put(new Integer(fieldId), fieldForm);
    }

    public String getFieldForm(int fieldId) {
        return this.getFieldForms().get(new Integer(fieldId));
    }

    public void setContainerEditView(ContainerEditView ctnEditView) {
        this.ctnEditView = ctnEditView;
    }

    public ContainerEditView getContainerEditView() {
        return this.ctnEditView;
    }

    public void setStayOnSameField(boolean stayOnSameField) {
        this.stayOnSameField = stayOnSameField;
    }

    public boolean getStayOnSameField() {
        return this.stayOnSameField;
    }

    public void processRequest(ProcessingContext jParams, int lastFieldId) {
        String fieldIdStr = jParams.getParameter("lastfid");
        int fieldId = 0;
        try {
            fieldId = Integer.parseInt(fieldIdStr);
            this.setLastFieldId(fieldId);
        } catch (NumberFormatException nfe) {
        }

        fieldIdStr = jParams.getParameter("editfid");
        try {
            fieldId = Integer.parseInt(fieldIdStr);
            this.setSelectedFieldId(fieldId);
        } catch (NumberFormatException nfe) {
        }
        if (lastFieldId != 0) {
            this.setSelectedFieldId(lastFieldId);
        }
    }

    /**
     * Process last field, return false on error
     *
     * @param engineName                  String
     * @param jahiaContentContainerFacade ContainerFacadeInterface
     * @param elh                         EngineLanguageHelper
     * @param jParams                     ProcessingContext
     * @param engineMap                   HashMap
     * @param mode                        int
     * @return boolean
     * @throws JahiaException
     */
    public boolean processLastFields(String engineName,
                                     ContainerFacadeInterface jahiaContentContainerFacade,
                                     EngineLanguageHelper elh,
                                     ProcessingContext jParams,
                                     Map<String, Object> engineMap,
                                     int mode)
            throws JahiaException {

        engineMap.put("fieldsEditCallingEngineName", engineName); // @todo : make as Constant
        engineMap.put(JahiaEngine.PROCESSING_LOCALE,
                elh.getPreviousLocale());

        JahiaContainer theContainer = jahiaContentContainerFacade.getContainer(
                elh.getPreviousEntryLoadRequest(), true);

        engineMap.put(engineName + "." + "theContainer", theContainer);

        JahiaField theField = theContainer.getField(this.getLastFieldId());

        if (theField == null) {
            if (theContainer.getctndefid() > 0) {
                logger.error("Field not found in container " + theContainer.getID() + " definition name = " + theContainer.getDefinition().getName() + ", we are searching field " + getLastFieldId() + " in fields " + ToStringBuilder.reflectionToString(theContainer.getFields()));
            }
            this.setStayOnSameField(true);
            return false;
        }

        if (theField.getDefinition() == null) {
            logger.error("Field definition for field " + theField.getID() + " is null !");
            this.setStayOnSameField(true);
            return false;
        }

        ContainerEditViewFieldGroup fieldGroup = getContainerEditView()
                .getFieldGroupByFieldName(theField.getDefinition().getName());

        boolean errorOccured = false;

        // get the lists of fields in the edit view
        int size = fieldGroup.getFieldNames().size();
        String fieldName = "";
        for (int i = 0; i < size; i++) {
            fieldName = (String) fieldGroup.getFieldNames().get(i);

            // dispatches to the appropriate sub engine
            theField = theContainer.getField(fieldName);
            if (theField != null) {

                int fieldType = theField.getType();

                logger.debug("Update container field " +
                        theField.getDefinition().getName() + "...");
                engineMap.put(engineName + "." + "theField", theField);
                engineMap.put(engineName + "." + "isSelectedField", Boolean.FALSE);

                boolean doUpdate = false;
                Map<Integer, Integer> ctnListFieldAcls = this.getCtnListFieldAcls();
                int fieldId = theField.getID();
                if (theContainer.getListID() != 0 && ctnListFieldAcls != null && fieldId > 0) {
                    JahiaBaseACL acl = JahiaEngineTools.getCtnListFieldACL(ctnListFieldAcls, fieldId);
                    if (acl != null) {
                        doUpdate = acl.getPermission(jParams.getUser(),
                                JahiaBaseACL.WRITE_RIGHTS, JahiaEngineTools.isCtnListFieldACLDefined(ctnListFieldAcls, fieldId));
                    }
                } else {
                    doUpdate = true;
                }
                EntryLoadRequest savedEntryLoadRequest =
                        jParams.getSubstituteEntryLoadRequest();
                jParams.setSubstituteEntryLoadRequest(elh.getPreviousEntryLoadRequest());
                engineMap.put("fieldsEditCallingEngineName", engineName); // todo : make as Constant
                if (doUpdate &&
                        !EngineToolBox.getInstance().processFieldTypes(theField, theContainer, engineName, jParams, mode, engineMap)) {

                    if (!errorOccured) {
                        errorOccured = true;
                    }

                    /*
                    // if there was an error, come back to last screen
                    engineMap.put ("screen", lastScreen);
                    engineMap.put ("jspSource", TEMPLATE_JSP);
                    // error on field, then stay on same field
                    flagStayOnSameField = true;
                    jParams.resetSubstituteEntryLoadRequest ();
                    break;
                    */
                } else {
                    this.addUpdatedField(fieldId, theField.getLanguageCode());
                    if (fieldType != theField.getType()) {
                        // field type has changed
                        jahiaContentContainerFacade.changeType(fieldId,
                                theField.getType(), jParams);
                        theField = jahiaContentContainerFacade.
                                getContentFieldFacade(fieldId).
                                getField(elh.getPreviousEntryLoadRequest(), true);
                        theContainer.setField(theField);
                        engineMap.put(engineName + "." + "theField", theField);
                    }

                    if (doUpdate && "true".equals(jParams.getParameter("apply_change_to_all_lang_" + fieldId))) {
                        applyChangeToAllLang(engineName, theField,
                                jahiaContentContainerFacade,
                                engineMap, jParams);
                    }
                }
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
                logger.debug("processLastScreen > the field value is now " +
                        theField.getValue());
            }
        }
        if (errorOccured) {
            this.setStayOnSameField(true);
            return false;
        }

        return true;
    }

    /**
     * Process current fields, return false on error
     *
     * @param engineName                  String
     * @param jahiaContentContainerFacade ContainerFacadeInterface
     * @param elh                         EngineLanguageHelper
     * @param jParams                     ProcessingContext
     * @param engineMap                   HashMap
     * @param mode                        int
     * @return boolean
     * @throws JahiaException
     */
    public boolean processCurrentFields(String engineName,
                                        ContainerFacadeInterface jahiaContentContainerFacade,
                                        EngineLanguageHelper elh,
                                        ProcessingContext jParams, Map<String, Object> engineMap, int mode)
            throws JahiaException {

        engineMap.put("fieldsEditCallingEngineName", engineName); // @todo : make as Constant

        JahiaContainer theContainer = jahiaContentContainerFacade.getContainer(
                elh.getCurrentEntryLoadRequest(), true);

        engineMap.put(engineName + "." + "theContainer", theContainer);

        engineMap.put(JahiaEngine.PROCESSING_LOCALE,
                elh.getCurrentLocale());

        ContainerEditView editView = this.getContainerEditView();

        ContainerEditViewFieldGroup fieldGroup = null;

        // get first visible field
        JahiaField theField = null;
        if (this.getSelectedFieldId() != 0) {
            try {
                theField = theContainer.getField(this.getSelectedFieldId());
            } catch (JahiaException je) {
                // field not found
            }
        }
        if (theField != null) {
            fieldGroup = editView.getFieldGroupByFieldName(theField.
                    getDefinition().getName());
        }
        if (fieldGroup == null) {
            if (editView.getFields().size() > 0) {
                String fieldName = (String) editView.getFields().get(0);
                theField = theContainer.getFieldByName(fieldName);
            }
            if (theField != null) {
                fieldGroup = editView.getFieldGroupByFieldName(theField.
                        getDefinition().getName());
                this.setSelectedFieldId(theField.getID());
                this.setLastFieldId(theField.getID());
            }
        }

        //engineMap.put (engineName+"."+"field_id", new Integer (fieldID));

        List<Integer> fieldIDs = new ArrayList<Integer>();

        if (theField != null) {
            // at least one visible field

            // get the lists of fields in the edit view
            int size = fieldGroup.getFieldNames().size();
            String fieldName = "";
            EntryLoadRequest processingEntryLoadRequest = null;

            for (int i = 0; i < size; i++) {
                fieldName = (String) fieldGroup.getFieldNames().get(i);

                // dispatches to the appropriate sub engine
                theField = theContainer.getField(fieldName);

                if (theField != null) {
                    engineMap.put(engineName + "." + "fieldForm", null); // reset field form
                    logger.debug("The field value is now " + theField.getValue());

                    engineMap.put(engineName + "." + "theField", theField);
                    engineMap.put(engineName + "." + "isSelectedField",
                            Boolean.valueOf(theField.getID() == this.getSelectedFieldId()));
                    engineMap.put(engineName + "." + "fieldID", new Integer(theField.getID()));

                    processingEntryLoadRequest = new EntryLoadRequest(theField.
                            getWorkflowState(), theField.getVersionID(),
                            new ArrayList<Locale>());
                    processingEntryLoadRequest.getLocales()
                            .add(LanguageCodeConverters
                                    .languageCodeToLocale(theField.getLanguageCode()));
                    if (ContentObject.SHARED_LANGUAGE.equals(theField.getLanguageCode())) {
                        // allow engine locale too as the field is shared lang
                        processingEntryLoadRequest.getLocales()
                                .add(elh.getCurrentLocale());
                    }
                    EntryLoadRequest savedEntryLoadRequest =
                            jParams.getSubstituteEntryLoadRequest();
                    jParams.setSubstituteEntryLoadRequest(
                            processingEntryLoadRequest);
                    if (EngineToolBox.getInstance().processFieldTypes(theField, theContainer, engineName, jParams, mode, engineMap)) {

                        if ("true".equals(jParams.getParameter(
                                "apply_change_to_all_lang_" + theField.getID()))) {
                            applyChangeToAllLang(engineName, theField,
                                    jahiaContentContainerFacade,
                                    engineMap, jParams);
                        }
                    }
                    jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);

                    if (engineMap.get(engineName + "." + "fieldForm") != null) {
                        fieldIDs.add(new Integer(theField.getID()));
                        Map<Integer, Object> fieldForms = (Map<Integer, Object>) engineMap.get(
                                engineName + "." + "fieldForms");
                        Object fieldForm = engineMap.get(engineName + "." + "fieldForm");
                        fieldForms.put(new Integer(theField.getID()),
                                fieldForm);
                    }
                }
            }
        }
        engineMap.put(engineName + "." + "fieldID", new Integer(this.getSelectedFieldId()));
        engineMap.put(engineName + "." + "theField", theField);
        engineMap.put(engineName + "." + "fieldIDs", fieldIDs);
        return true;
    }

    /**
     * Check Fields validation
     *
     * @param engineName                  String
     * @param jahiaContentContainerFacade ContainerFacadeInterface
     * @param elh                         EngineLanguageHelper
     * @param jParams                     ProcessingContext
     * @param engineMap                   HashMap
     * @return EngineValidationHelper
     * @throws JahiaException
     */
    public EngineValidationHelper validate(String engineName,
                                           ContainerFacadeInterface jahiaContentContainerFacade,
                                           EngineLanguageHelper elh,
                                           ProcessingContext jParams,
                                           Map<String, Object> engineMap)
            throws JahiaException {
        final EngineValidationHelper evh = new EngineValidationHelper();
        final Iterator<JahiaContentFieldFacade> fieldEnum = jahiaContentContainerFacade.getFields();
        while (fieldEnum.hasNext()) {
            final JahiaContentFieldFacade cff = (JahiaContentFieldFacade) fieldEnum.next();
            final Iterator<JahiaField> fEnum = cff.getFields();
            while (fEnum.hasNext()) {
                final JahiaField field = (JahiaField) fEnum.next();
                if (!field.getDefinition().getItemDefinition().isProtected()) {
                    if (this.getContainerEditView().getFieldGroupByFieldName(field.getDefinition().getName()) != null) {
                        if (getEditedLanguages().contains(field.getLanguageCode())) {
                            final ValidationError ve = field.validate();
                            if (ve != null) {
                                evh.addError(ve);
                                ve.setLanguageCode(field.getLanguageCode());
                            }
                        }
                    }
                }
                String nodeTypeName = field.getDefinition().getItemDefinition().getDeclaringNodeType().getName(); 
                String propDef = field.getDefinition().getItemDefinition().getName();
                if (nodeTypeName != null && nodeTypeName.length() > 0 && propDef != null && propDef.length() > 0) {
                    ExtendedNodeType nt = null;
                    try {
                        nt = NodeTypeRegistry.getInstance().getNodeType(nodeTypeName);
                    ExtendedPropertyDefinition pd = nt.getPropertyDefinition(propDef);
                    if (pd != null) {
                        Value[] vcs = pd.getValueConstraintsAsValue();
                        if (vcs.length > 0) {
                            String v = field.getValue();
                            if (field.getType() == FieldTypes.DATE) {
                                v = (String) field.getObject();
                            }
                            List<String> values;
                            if (pd.isMultiple()) {
                                values = Arrays.asList(v.split("\\$\\$\\$"));
                            } else {
                                values = Collections.singletonList(v);
                            }
                            for (String value : values) {
                                boolean nok = true;
                                for (int i = 0; i < vcs.length; i++) {
                                    Value vc = vcs[i];
                                    try {
                                        if (vc instanceof DynamicValueImpl) {
                                            Value[] vcs2 = ((DynamicValueImpl) vc).expand();
                                            for (int j = 0; j < vcs2.length; j++) {
                                                if (((ValueImpl) vcs2[j]).checkConstraint(value)) {
                                                    nok = false;
                                                    break;
                                                }
                                            }
                                        } else {
                                            if (((ValueImpl) vc).checkConstraint(value)) {
                                                nok = false;
                                                break;
                                            }
                                        }
                                    } catch (RepositoryException e) {
                                        logger.error("Cannot evaluate constraint", e);
                                    }
                                }
                                if (nok) {
                                    String[] constraints = null;
                                    try {
                                        constraints = new String[vcs.length];
                                        for (int i = 0; i < vcs.length; i++) {
                                            constraints[i] = vcs[i].getString();
                                        }
                                    } catch (RepositoryException e) {
                                        logger.warn(e.getMessage(), e);
                                        constraints = null;
                                    }
                                    ValidationError error = new ValidationError(field,
                                            constraints != null ? "Value does not match constraint (any of the constraints): "+ StringUtils.join(constraints,", "): "Value does not match constraint",
                                            pd.getResourceBundleKey() + ".invalidConstraint", constraints != null ? constraints : ArrayUtils.EMPTY_STRING_ARRAY);
                                    error.setLanguageCode(field.getLanguageCode());
                                    evh.addError(error);
                                }
                            }
                        }
                    }
                } catch (NoSuchNodeTypeException e) {
                    logger.error("No such nodetype",e);
                }
                }

            }
        }

        return evh;
    }

    /**
     * @param theField
     * @param jahiaContentContainerFacade
     * @throws JahiaException todo : put in a Tool class
     */
    public void applyChangeToAllLang(String engineName,
                                     JahiaField theField,
                                     ContainerFacadeInterface jahiaContentContainerFacade,
                                     Map<String, Object> engineMap,
                                     ProcessingContext jParams)
            throws JahiaException {
        final JahiaContentFieldFacade contentFieldFacade = jahiaContentContainerFacade.getContentFieldFacade(theField.getID());
        final Iterator<JahiaField> fieldEnum = contentFieldFacade.getFields();
        while (fieldEnum.hasNext()) {
            final JahiaField field = (JahiaField) fieldEnum.next();
            theField.copyValueInAnotherLanguage(field, jParams);
            // remember change
            this.addUpdatedField(field.getID(), field.getLanguageCode());
        }
    }

    public boolean areValuesTheSameInAllActiveLanguages(final JahiaField theField,
                                                        final ContainerFacadeInterface jahiaContentContainerFacade)
            throws JahiaException {
        final JahiaContentFieldFacade contentFieldFacade = jahiaContentContainerFacade.getContentFieldFacade(theField.getID());
        if (contentFieldFacade == null) return false;
        final Iterator<JahiaField> fieldEnum = contentFieldFacade.getFields();
        String oldValue = null;
        String theFieldValue = theField.getValue();
        if (theField.getType() == FieldTypes.BIGTEXT) {
            theFieldValue = StringUtils.replace(theFieldValue, "\r", "");
            theFieldValue = StringUtils.replace(theFieldValue, "\n", "");
            theFieldValue = theFieldValue.trim();
        }
        if (theFieldValue == null || theFieldValue.length() == 0 || theFieldValue.startsWith("<jahia")) return false;
        while (fieldEnum.hasNext()) {
            final JahiaField field = fieldEnum.next();
            String value = field.getValue();
            if (field.getType() == FieldTypes.BIGTEXT) {
                value = StringUtils.replace(value, "\r", "");
                value = StringUtils.replace(value, "\n", "");
                value = StringUtils.replace(value, "<BODY>", "");
                value = StringUtils.replace(value, "</BODY>", "");
                value = StringUtils.replace(value, "<body>", "");
                value = StringUtils.replace(value, "</body>", "");
                value = value.trim();
            }
            if (oldValue != null && !oldValue.equals(value)) {
                return false;
            }
            oldValue = value;
        }
        return true;
    }

    public abstract JahiaField getField(int fieldId);

    public abstract JahiaField getField(String fieldName);


}
