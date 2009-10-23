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

import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class ContentFileField extends ContentField {
    private static final long serialVersionUID = 701288568949223178L;

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentFileField.class);

    protected ContentFileField(Integer ID,
                               Integer jahiaID,
                               Integer pageID,
                               Integer ctnid,
                               Integer fieldDefID,
                               Integer fieldType,
                               Integer connectType,
                               Integer aclID,
                               List<ContentObjectEntryState> activeAndStagingEntryStates,
                               Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super(ID.intValue(), jahiaID.intValue(), pageID.intValue(),
                ctnid.intValue(), fieldDefID.intValue(), fieldType.intValue(),
                connectType.intValue(), aclID.intValue(), activeAndStagingEntryStates,
                activeAndStagedDBValues);
    }

    //--------------------------------------------------------------------------
    /**
     * Gets the String representation of this field. In case of an Application,
     * it will be the output of the application, in case of a bigtext it will
     * be the full content of the bigtext, etc. This is called by the public
     * method getValue of ContentField, which does the entryState resolving
     * This method should call getDBValue to get the DBValue
     */
    public String getValue(ProcessingContext jParams,
                           ContentObjectEntryState entryState)
            throws JahiaException {
        return getDBValue(entryState);
    }

    //--------------------------------------------------------------------------
    /**
     * This method should call preSet.
     */
    public void setFile(JahiaFileField fField, EntrySaveRequest saveRequest)
            throws JahiaException {
        if (fField == null) {
            return;
        }
        preSet(fField.getStorageName(), saveRequest);
        postSet(saveRequest);
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
    public ActivationTestResults changeEntryState(ContentObjectEntryState fromEntryState,
                                                  ContentObjectEntryState toEntryState,
                                                  ProcessingContext jParams,
                                                  StateModificationContext stateModifContext)
            throws JahiaException {
        JahiaFieldXRefManager fieldLinkManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());

        JCRNodeWrapper file = JCRStoreService.getInstance().getFileNode(
                this.getValue(fromEntryState), jParams.getUser());

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

        if (toEntryState.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
            fieldLinkManager.activateField(getID(), getSiteID(), toEntryState.getLanguageCode());
        }
        if (fromEntryState.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
            fieldLinkManager.deleteReferencesForField(getID(), toEntryState.getLanguageCode(), EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        }


        return new ActivationTestResults();
    }

    //--------------------------------------------------------------------------
    protected ActivationTestResults isContentValidForActivation(
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        /** @todo to be implemented */
        return new ActivationTestResults();
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
    public void copyEntry(EntryStateable fromEntryState,
                          EntryStateable toEntryState)
            throws JahiaException {

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
    public void deleteEntry(EntryStateable deleteEntryState)
            throws JahiaException {
        /** @todo to be implemented */
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
        /** @todo FIXME : to be implemented. */
    }

    private JahiaSite getSite(ProcessingContext ctx) throws JahiaException {
        return getSiteID() == ctx.getSiteID() ? ctx.getSite()
                : ServicesRegistry.getInstance().getJahiaSitesService()
                .getSite(getSiteID());
    }

}
