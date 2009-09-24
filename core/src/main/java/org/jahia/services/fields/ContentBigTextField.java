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

import org.jahia.data.ConnectionTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;
import org.jahia.utils.LanguageCodeConverters;

import java.util.*;


public class ContentBigTextField extends ContentField {

    private static final long serialVersionUID = 844954881445411548L;

    private static final org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(ContentBigTextField.class);

    protected ContentBigTextField(final Integer ID,
                                  final Integer jahiaID,
                                  final Integer pageID,
                                  final Integer ctnid,
                                  final Integer fieldDefID,
                                  final Integer fieldType,
                                  final Integer connectType,
                                  final Integer aclID,
                                  List<ContentObjectEntryState> activeAndStagingEntryStates,
                                  Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super(ID, jahiaID, pageID, ctnid, fieldDefID, fieldType, connectType, aclID,
                activeAndStagingEntryStates, activeAndStagedDBValues);
    }

    //--------------------------------------------------------------------------
    public static synchronized ContentBigTextField createBigText(final int siteID,
                                                                 final int pageID,
                                                                 final int containerID,
                                                                 final int fieldDefID,
                                                                 final int parentAclID,
                                                                 final int aclID,
                                                                 final String text,
                                                                 final ProcessingContext jParams)
            throws JahiaException {

        final ContentBigTextField result =
                (ContentBigTextField) ContentField.createField(siteID, pageID,
                        containerID, fieldDefID,
                        ContentFieldTypes.BIGTEXT,
                        ConnectionTypes.LOCAL,
                        parentAclID, aclID);
        final EntrySaveRequest saveRequest =
                new EntrySaveRequest(jParams.getUser(),
                        jParams.getLocale().toString(), true);

        result.setText(text, saveRequest, null);
        return result;
    }

    //--------------------------------------------------------------------------

    /**
     * Gets the String representation of this field. In case of an Application,
     * it will be the output of the application, in case of a bigtext it will
     * be the full content of the bigtext, etc. This is called by the public
     * method getValue of ContentField, which does the entryState resolving
     * This method should call getDBValue to get the DBValue
     */
    public String getValue(final ProcessingContext jParams,
                           final ContentObjectEntryState entryState)
            throws JahiaException {

        if (logger.isDebugEnabled()) {
            logger.debug("Loading big text field... (ID = " + getID() + ")");
        }

        final JahiaFieldDefinition theDef = JahiaFieldDefinitionsRegistry.
                getInstance().getDefinition(this.getFieldDefID());

        String result = null;
        if (entryState != null) {
            result = ServicesRegistry.getInstance().getJahiaTextFileService().
                    loadBigTextValue(this.getSiteID(),
                            this.getPageID(),
                            this.getID(),
                            theDef.getDefaultValue(),
                            entryState.getVersionID(),
                            entryState.getWorkflowState(),
                            entryState.getLanguageCode());
        }

        if (result == null || result.equals("<empty>")) {
            result = "";
        }
        /*
        else
        {
            result = FormDataManager.formDecode(result);
        }
         */
        return result;
    }

    //--------------------------------------------------------------------------

    /**
     * Sets the String representation of this field.
     * This method should call preSet.
     */
    public void setText(final String value, final EntrySaveRequest saveRequest, final String defaultValue)
            throws JahiaException {
        if ("".equals(value)) {
            final ContentObjectEntryState verInfo = preSet("<empty>", saveRequest);
            if (logger.isDebugEnabled()) {
                logger.debug("Saving big text field..." + verInfo.toString());
            }

            if (defaultValue != null && defaultValue.length() > 0) {
                ServicesRegistry.getInstance().getJahiaTextFileService().saveContents(
                        this.getSiteID(),
                        this.getPageID(),
                        this.getID(),
                        value,
                        verInfo.getVersionID(),
                        verInfo.getWorkflowState(),
                        verInfo.getLanguageCode());
            } else {
                ServicesRegistry.getInstance().getJahiaTextFileService().deleteFile(this.getSiteID(),
                        this.getPageID(),
                        this.getID(),
                        verInfo.getVersionID(),
                        verInfo.getWorkflowState(),
                        verInfo.getLanguageCode());
            }
        } else {
            final ContentObjectEntryState verInfo = preSet("<text>", saveRequest);
            if (logger.isDebugEnabled()) {
                logger.debug("Saving big text field..." + verInfo.toString());
            }

            ServicesRegistry.getInstance().getJahiaTextFileService().saveContents(
                    this.getSiteID(),
                    this.getPageID(),
                    this.getID(),
                    value,
                    verInfo.getVersionID(),
                    verInfo.getWorkflowState(),
                    verInfo.getLanguageCode());
        }
        postSet(saveRequest);
    }

    //--------------------------------------------------------------------------

    /**
     * get the Value that will be added to the search engine for this field.
     * for a bigtext it will be the content of the bigtext, for an application
     * the string will be empty!
     * Do not return null, return an empty string instead.
     */
    public String getValueForSearch(final ProcessingContext jParams,
                                    final ContentObjectEntryState entryState) {
        return ""; /* todo fixme */
    }

    //--------------------------------------------------------------------------

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
     * @return null if the entry state change wasn't an activation, otherwise it
     *         returns an object that contains the status of the activation (whether
     *         successfull, partial or failed, as well as messages describing the
     *         warnings during the activation process)
     */
    public ActivationTestResults changeEntryState(final ContentObjectEntryState fromEntryState,
                                                  final ContentObjectEntryState toEntryState,
                                                  final ProcessingContext jParams,
                                                  final StateModificationContext stateModifContext)
            throws JahiaException {

        if (logger.isDebugEnabled()) {
            logger.debug("From " + fromEntryState.toString()
                    + " To " + toEntryState.toString());
        }

        final ActivationTestResults activationResults = new ActivationTestResults();
        try {
            ServicesRegistry.getInstance().getJahiaTextFileService().
                    renameFile(
                            this.getSiteID(),
                            this.getPageID(),
                            this.getID(),
                            fromEntryState.getVersionID(),
                            fromEntryState.getWorkflowState(),
                            fromEntryState.getLanguageCode(),
                            this.getSiteID(),
                            this.getPageID(),
                            this.getID(),
                            toEntryState.getVersionID(),
                            toEntryState.getWorkflowState(),
                            toEntryState.getLanguageCode()
                    );
            activationResults.setStatus(ActivationTestResults.COMPLETED_OPERATION_STATUS);
        } catch (Exception t) {
            logger.error("Unable to change Entry State !", t);
            activationResults.setStatus(ActivationTestResults.FAILED_OPERATION_STATUS);
        }

        JahiaFieldXRefManager fieldLinkManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
        for (String s : fieldLinkManager.getFieldReferences(getID(), fromEntryState.getLanguageCode(), fromEntryState.getWorkflowState())) {
            if (s.startsWith("file:")) {
                s = s.substring("file:".length());

                JCRNodeWrapper file = JCRStoreService.getInstance().getFileNode(s, jParams.getUser());
                if (file.isValid()) {
                    int usageCount = fieldLinkManager.countUsages("file:" + file.getPath(), EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
                    boolean wasLocked = fromEntryState.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE || fromEntryState.getWorkflowState() == EntryLoadRequest.WAITING_WORKFLOW_STATE;
                    boolean wasUnlocked = fromEntryState.getWorkflowState() != EntryLoadRequest.ACTIVE_WORKFLOW_STATE && fromEntryState.getWorkflowState() != EntryLoadRequest.WAITING_WORKFLOW_STATE;
                    boolean shouldBeLocked = toEntryState.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE || toEntryState.getWorkflowState() == EntryLoadRequest.WAITING_WORKFLOW_STATE;
                    boolean shouldBeUnlocked = toEntryState.getWorkflowState() != EntryLoadRequest.ACTIVE_WORKFLOW_STATE && toEntryState.getWorkflowState() != EntryLoadRequest.WAITING_WORKFLOW_STATE;
                    if (wasLocked && shouldBeUnlocked) {
                        if (usageCount <= 1) {
                            file.forceUnlock();
                        }
                    } else if (wasUnlocked && shouldBeLocked && getSite(jParams).isFileLockOnPublicationEnabled()) {
                        if (usageCount == 0) {
                            file.forceUnlock();
                            file.lockAsSystemAndStoreToken();
                        }
                    }
                }
            }
        }
        if (toEntryState.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
            fieldLinkManager.activateField(getID(), getSiteID(), toEntryState.getLanguageCode());
        }
        if (fromEntryState.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
            fieldLinkManager.deleteReferencesForField(getID(), toEntryState.getLanguageCode(), EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        }

        return activationResults;
    }

    //--------------------------------------------------------------------------

    /**
     * If URL Integrity and WAI compliance are activated (or simply 1 of them),
     * warning messages generated by the URL integrity checker and the WAI
     * validator cannot be ignored and the field cannot be validated.
     */
    protected ActivationTestResults isContentValidForActivation(
            final Set<String> languageCodes,
            final ProcessingContext jParams,
            final StateModificationContext stateModifContext)
            throws JahiaException {
        final ActivationTestResults results = new ActivationTestResults();
        return results;
    }

    //--------------------------------------------------------------------------

    /**
     * This method is called when a entry should be copied into a new entry
     * it is called when an    old version -> active version   move occurs
     * This method should not write/change the DBValue, the service handles that.
     *
     * @param fromEntryState the entry state that is currently was in the database
     * @param toEntryState   the entry state that will be written to the database
     */
    public void copyEntry(final EntryStateable fromEntryState,
                          final EntryStateable toEntryState) throws JahiaException {

        int fromVersionID = fromEntryState.getVersionID();

        if (fromEntryState.getWorkflowState() ==
                ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
            // lookup for the last archive done before the deleted entryState
            // and restore it.
            final ContentObjectEntryState entryState =
                    new ContentObjectEntryState(fromEntryState);
            final ContentObjectEntryState archiveEntryState =
                    getClosestVersionedEntryState(entryState, true);
            if (archiveEntryState != null) {
                fromVersionID = archiveEntryState.getVersionID();
            }
        }

        ServicesRegistry.getInstance().getJahiaTextFileService().copyFile(
                this.getSiteID(),
                this.getPageID(),
                this.getID(),
                fromVersionID,
                fromEntryState.getWorkflowState(),
                fromEntryState.getLanguageCode(),
                this.getSiteID(),
                this.getPageID(),
                this.getID(),
                toEntryState.getVersionID(),
                toEntryState.getWorkflowState(),
                toEntryState.getLanguageCode()
        );

        super.copyEntry(fromEntryState, toEntryState);
    }

    //--------------------------------------------------------------------------

    /**
     * This method is called when an entry should be deleted for real.
     * It is called when a field is deleted, and versioning is disabled, or
     * when staging values are undone.
     * For a bigtext for instance, this method should delete the text file
     * corresponding to this field entry
     *
     * @param deleteEntryState the entry state to delete
     */
    public void deleteEntry(final EntryStateable deleteEntryState)
            throws JahiaException {
        ServicesRegistry.getInstance().getJahiaTextFileService().deleteFile(
                this.getSiteID(),
                this.getPageID(),
                this.getID(),
                deleteEntryState.getVersionID(),
                deleteEntryState.getWorkflowState(),
                deleteEntryState.getLanguageCode());

        super.deleteEntry(deleteEntryState);
    }

    //--------------------------------------------------------------------------

    /**
     * Is this kind of field shared (i.e. not one version for each language,
     * but one version for every language)
     */
    public boolean isShared() {
        return false;
    }

    protected void purgeContent()
            throws JahiaException {
        // not necessary since already done in site admin interface, but we
        // should implement this if we want to be able to do atomic purging.
    }

    /**
     * Called when marking a language for deletion on a field. This is done
     * first to allow field implementation to provide a custom behavior when
     * marking fields for deletion. It isn't abstract because most fields will
     * not need to redefine this method.
     *
     * @param user
     * @param languageCode
     * @param stateModifContext used to detect loops in deletion marking.
     * @throws JahiaException in the case there was an error processing the
     *                        marking of the content.
     */
    protected void markContentLanguageForDeletion(final JahiaUser user,
                                                  final String languageCode,
                                                  final StateModificationContext stateModifContext)
            throws JahiaException {
        final List<Locale> locales = new ArrayList<Locale>();
        locales.add(LanguageCodeConverters.languageCodeToLocale(languageCode));
        final EntryLoadRequest loadRequest = new EntryLoadRequest(
                EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0, locales);
        final ContentObjectEntryState fromEntryState = getEntryState(loadRequest);
        final ContentObjectEntryState toEntryState = new ContentObjectEntryState(
                ContentObjectEntryState.WORKFLOW_STATE_START_STAGING, -1, languageCode);
        if (fromEntryState != null) {
            ServicesRegistry.getInstance().getJahiaTextFileService().copyFile(
                    this.getSiteID(),
                    this.getPageID(),
                    this.getID(),
                    fromEntryState.getVersionID(),
                    fromEntryState.getWorkflowState(),
                    fromEntryState.getLanguageCode(),
                    this.getSiteID(),
                    this.getPageID(),
                    this.getID(),
                    toEntryState.getVersionID(),
                    toEntryState.getWorkflowState(),
                    toEntryState.getLanguageCode()
            );
        }
    }

    private JahiaSite getSite(ProcessingContext ctx) throws JahiaException {
        return getSiteID() == ctx.getSiteID() ? ctx.getSite()
                : ServicesRegistry.getInstance().getJahiaSitesService()
                .getSite(getSiteID());
    }

}
