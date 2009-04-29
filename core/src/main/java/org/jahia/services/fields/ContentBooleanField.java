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
package org.jahia.services.fields;

import org.jahia.data.ConnectionTypes;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.services.version.StateModificationContext;
import org.jahia.utils.xml.XMLSerializationOptions;
import org.jahia.utils.xml.XmlWriter;

import java.util.*;

public class ContentBooleanField extends ContentField implements ContentSimpleField {
    private static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger (ContentBooleanField.class);

    private static final long serialVersionUID = -5628568671070258190L;

    //--------------------------------------------------------------------------
    protected ContentBooleanField (Integer ID,
                                   Integer jahiaID,
                                   Integer pageID,
                                   Integer ctnid,
                                   Integer fieldDefID,
                                   Integer fieldType,
                                   Integer connectType,
                                   Integer aclID,
                                   List<ContentObjectEntryState> activeAndStagingEntryStates,
                                   Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super (ID.intValue (), jahiaID.intValue (), pageID.intValue (),
                ctnid.intValue (), fieldDefID.intValue (), fieldType.intValue (),
                connectType.intValue (), aclID.intValue (),
                activeAndStagingEntryStates, activeAndStagedDBValues);
    }

    //--------------------------------------------------------------------------
    public static synchronized ContentBooleanField
            createBoolean (int siteID,
                           int pageID,
                           int containerID,
                           int fieldDefID,
                           int parentAclID, int aclID,
                           boolean value,
                           ProcessingContext jParams)
            throws JahiaException {
        ContentBooleanField result =
                (ContentBooleanField)
                ContentField.createField (siteID, pageID, containerID,
                        fieldDefID, ContentFieldTypes.BOOLEAN,
                        ConnectionTypes.LOCAL, parentAclID,
                        aclID);

        EntrySaveRequest saveRequest =
                new EntrySaveRequest (jParams.getUser (),
                        ContentField.SHARED_LANGUAGE, true);

        result.setValue (value, saveRequest);
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
    public String getValue (ProcessingContext jParams,
                               ContentObjectEntryState entryState)
            throws JahiaException {
        //return FormDataManager.formDecode(getDBValue(entryState));
        return getDBValue (entryState);
    }

    //--------------------------------------------------------------------------
    /**
     * Sets the String representation of this field.
     * This method should call preSet.
     */
    public void setValue (boolean value,
                          EntrySaveRequest saveRequest)
            throws JahiaException {
        preSet ((value) ? "true" : "false", saveRequest);
        postSet(saveRequest);
    }

    //--------------------------------------------------------------------------
    /**
     * get the Value that will be added to the search engine for this field.
     * for a bigtext it will be the content of the bigtext, for an application
     * the string will be empty!
     * Do not return null, return an empty string instead.
     *
     * @param jParams the jParam containing the loadVersion and locales
     */
    public String getValueForSearch (ProcessingContext jParams,
                                     ContentObjectEntryState entryState)
            throws JahiaException {
        //return FormDataManager.formDecode(getDBValue(entryState));
        return getDBValue (entryState);
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

    //--------------------------------------------------------------------------
    protected ActivationTestResults isContentValidForActivation (
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        /** @todo to be implemented */
        return new ActivationTestResults ();
    }

    //--------------------------------------------------------------------------
    /**
     * Is this kind of field shared (i.e. not one version for each language,
     * but one version for every language)
     */
    public boolean isShared () {
        return true;
    }

    /**
     * This is called on all content fields to have them serialized only their
     * specific part. The actual field metadata seriliazing is handled by the
     * ContentField class. This method is called multiple times per field
     * according to the workflow state, languages and versioning entries we
     * want to serialize.
     *
     * @param xmlWriter               the XML writer object in which to write the XML output
     * @param xmlSerializationOptions options used to activate/bypass certain
     *                                output of elements.
     * @param entryState              the ContentFieldEntryState for which to generate the
     *                                XML export.
     * @param processingContext               specifies context of serialization, such as current
     *                                user, current request parameters, entry load request, URL generation
     *                                information such as ServerName, ServerPort, ContextPath, etc... URL
     *                                generation is an important part of XML serialization and this is why
     *                                we pass this parameter down, as well as user rights checking.
     *
     * @throws IOException in case there was an error writing to the Writer
     *                     output object.
     */
    protected void serializeContentToXML (XmlWriter xmlWriter,
                                          XMLSerializationOptions
            xmlSerializationOptions,
                                          ContentObjectEntryState entryState,
                                          ProcessingContext processingContext)
            throws java.io.IOException {
        try {
            //String result = FormDataManager.formDecode(getDBValue(entryState));
            String result = getDBValue (entryState);
            xmlWriter.writeText (result);
        } catch (JahiaException je) {
            logger.debug ("Error while serializing small text field to XML : ", je);
        }
    }


}
