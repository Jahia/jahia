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
 package org.jahia.services.fields;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.jahia.content.*;
import org.jahia.data.ConnectionTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.engines.shared.Category_Field;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;
import org.jahia.utils.JahiaTools;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: Serge Huber
 * Date: 23 ao�t 2005
 * Time: 09:36:54
 * Copyright (C) Jahia Inc.
 */
public class ContentCategoryField extends ContentField {

    private static final long serialVersionUID = -503491586581014649L;
    private static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger (ContentCategoryField.class);

    protected ContentCategoryField (Integer ID,
                                               Integer jahiaID,
                                               Integer pageID,
                                               Integer ctnid,
                                               Integer fieldDefID,
                                               Integer fieldType,
                                               Integer connectType,
                                               Integer aclID,
                                               List<ContentObjectEntryState> activeAndStagingEntryStates,
                                               Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super (ID.intValue (), jahiaID.intValue (), pageID.intValue (), ctnid.intValue (), fieldDefID.intValue (),
                fieldType.intValue (), connectType.intValue (), aclID.intValue (), activeAndStagingEntryStates,
                activeAndStagedDBValues);
    }

    public static synchronized ContentCategoryField createCategoryField (int siteID,
                                                                                int pageID,
                                                                                int containerID,
                                                                                int fieldDefID,
                                                                                int parentAclID,
                                                                                int aclID,
                                                                                String categories,
                                                                                ProcessingContext jParams)
            throws JahiaException {
        ContentCategoryField result =
                (ContentCategoryField) ContentField.createField (siteID, pageID,
                        containerID, fieldDefID,
                        ContentFieldTypes.CATEGORY,
                        ConnectionTypes.LOCAL,
                        parentAclID, aclID);
        // EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(), jParams.getLocale().toString());
        EntrySaveRequest saveRequest = new EntrySaveRequest (jParams.getUser (),
                ContentField.SHARED_LANGUAGE, true);
        result.setCategories (categories, saveRequest);
        return result;
    }

    /**
     * Gets the String representation of this field. In case of an Application,
     * it will be the output of the application, in case of a bigtext it will
     * be the full content of the bigtext, etc. This is called by the public
     * method getValue of ContentField, which does the entry resolving
     * This method should call getDBValue to get the DBValue
     * Note that until setField() is called, getValue returns always the
     * same value, even if the content was set by a setter such as setText!!
     */
    public String getValue (ProcessingContext jParams, ContentObjectEntryState entryState)
            throws JahiaException {
        if (entryState == null) {
            return "";
        }
        final JahiaFieldDefinition theDef = JahiaFieldDefinitionsRegistry.
                getInstance().getDefinition(this.getFieldDefID());

        String result;
        result = ServicesRegistry.getInstance().getJahiaTextFileService().
                loadBigTextValue(this.getSiteID(),
                                 this.getPageID(),
                                 this.getID(),
                                 theDef.getDefaultValue(),
                                 entryState.getVersionID(),
                                 entryState.getWorkflowState(),
                                 entryState.getLanguageCode());

        if (result == null || result.equals("<empty>") || result.equals("<text>")) {
            result = "";
        }

        return result;
    }

    /**
     * Sets the String representation of this field.
     * This method should call preSet and postSet.
     */
    public void setCategories(final String value, EntrySaveRequest saveRequest) throws JahiaException {
        if (!ContentField.SHARED_LANGUAGE.equals(saveRequest.getLanguageCode())) {
            logger.debug("Found non shared language in setting, enforcing shared language...");
            saveRequest.setLanguageCode(ContentField.SHARED_LANGUAGE);
        }
        if ("".equals(value)) {
            final ContentObjectEntryState verInfo = preSet("<empty>", saveRequest);
            if (logger.isDebugEnabled()) {
                logger.debug("Saving big text field..." + verInfo.toString());
            }
            ServicesRegistry.getInstance().getJahiaTextFileService().deleteFile(this.getSiteID(),
                                                                                this.getPageID(),
                                                                                this.getID(),
                                                                                verInfo.getVersionID(),
                                                                                verInfo.getWorkflowState(),
                                                                                verInfo.getLanguageCode());

        } else {
            final ContentObjectEntryState verInfo = preSet("<text>", saveRequest);
            if (logger.isDebugEnabled()) {
                logger.debug("Saving big text field..." + verInfo.toString());
            }

            ServicesRegistry.getInstance().getJahiaTextFileService().saveContents(
                    this.getSiteID(),
                    this.getPageID(),
                    this.getID(),
                    value.replaceAll("<text>",""),
                    verInfo.getVersionID(),
                    verInfo.getWorkflowState(),
                    verInfo.getLanguageCode());
        }
        postSet(saveRequest);
    }

    /**
     * get the Value that will be added to the search engine for this field.
     * for a bigtext it will be the content of the bigtext, for an application
     * the string will be empty!
     * Do not return null, return an empty string instead.
     *
     * @param jParams the jParam containing the loadVersion and locales
     */
    public String getValueForSearch (ProcessingContext jParams,
                                     ContentObjectEntryState entryState) throws JahiaException {
        //return FormDataManager.formDecode(getDBValue(entryState));
        return getDBValue (entryState);
    }

    /**
     * This method is called when there is a workflow state change
     * Such as  staged mode -> active mode (validation), active -> inactive (for versioning)
     * and also staged mode -> other staged mode (workflow)
     * This method should not write/change the DBValue, the service handles that.
     *
     * @param fromEntryState the entry state that is currently was in the database
     * @param toEntryState   the entry state that will be written to the database
     * @param jParams        ProcessingContext object used to get information about the user
     *                       doing the request, the current locale, etc...
     *
     * @return null if the entry state change wasn't an activation, otherwise it
     *         returns an object that contains the status of the activation (whether
     *         successfull, partial or failed, as well as messages describing the
     *         warnings during the activation process)
     */
    public ActivationTestResults changeEntryState (ContentObjectEntryState fromEntryState,
                                                   ContentObjectEntryState toEntryState,
                                                   ProcessingContext jParams,
                                                   StateModificationContext stateModifContext)
            throws JahiaException {
        return new ActivationTestResults ();
    }

    public RestoreVersionTestResults restoreVersion (JahiaUser user,
                                                     String operationMode,
                                                     ContentObjectEntryState entryState,
                                                     boolean removeMoreRecentActive,
                                                     RestoreVersionStateModificationContext stateModificationContext)
    throws JahiaException {
        String stagedValue = null;
        ContentObjectEntryState stagedEntryState = this.getEntryState(EntryLoadRequest.STAGED);
        if (stagedEntryState != null){
            stagedValue = this.getValue(stagedEntryState);
        }

        RestoreVersionTestResults result = super.restoreVersion(user,operationMode,entryState,removeMoreRecentActive,
                stateModificationContext);
        String newStagedValue = null;
        stagedEntryState = this.getEntryState(EntryLoadRequest.STAGED);
        if (stagedEntryState != null){
            newStagedValue = this.getValue(stagedEntryState);
        }

        if (!new EqualsBuilder()
                .append(stagedValue, newStagedValue)
                .isEquals()){

            ObjectKey targetObjectKey = null;
            try {
                targetObjectKey = findCategoryTargetObjectKey(this);
                if (targetObjectKey != null){
                    // so first let's remove the categories that were previously selected.
                    for (Category curCategory : Category.getObjectCategories(targetObjectKey)) {
                        curCategory.removeChildObjectKey(targetObjectKey);
                    }

                    if (newStagedValue != null && !"".equals(newStagedValue.trim())
                            && !Category_Field.NOSELECTION_MARKER.equals(newStagedValue)){

                        // now we can add the current object key to all the selected
                        // categories.
                        List<String> catKeys = JahiaTools.getTokensList(newStagedValue, JahiaField.MULTIPLE_VALUES_SEP);
                        for (String curCategoryKey : catKeys){
                            Category curCategory = Category.getCategoryByUUID(curCategoryKey, user);
                            if (curCategory != null) {
                              curCategory.addChildObjectKey(targetObjectKey);
                            }
                        }
                    }
                }
            } catch ( Exception t ){
                logger.debug("Cannot retrieve the target Content Object for the Category Field " + this.getID()
                        + " for versioning restore operation ", t);
                result.appendWarning("Cannot retrieve the target Content Object for the Category Field " + this.getID()
                        + " for versioning restore operation ");
            }
        }
        return result;
    }

    private ObjectKey findCategoryTargetObjectKey(ContentField theField) throws JahiaException {
        ObjectKey objectKey = null;
        int fieldDefId = theField.getFieldDefID();
        JahiaFieldDefinition fieldDef = JahiaFieldDefinitionsRegistry.getInstance().getDefinition(fieldDefId);
        if (fieldDef == null){
            return null;
        }
        if (fieldDef.getIsMetadata()) {
            JahiaObject ownerObject = ContentObject.getContentObjectFromMetadata(new ContentFieldKey(theField.getID()));
            if (ownerObject != null) {
                objectKey = ownerObject.getObjectKey();
            }
        } else {
            // here we are not in the case of a metadata field, so we have
            // as a parent object either a container or a page.
            if (theField.getContainerID() > 0) {
                objectKey = new ContentContainerKey(theField.getContainerID());
            } else {
                objectKey = new ContentPageKey(theField.getPageID());
            }
        }
        return objectKey;
    }

    protected ActivationTestResults isContentValidForActivation (
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {
        /** @todo to be implemented */
        return new ActivationTestResults ();
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
     */
    public boolean isShared () {
        return true;
    }

}
