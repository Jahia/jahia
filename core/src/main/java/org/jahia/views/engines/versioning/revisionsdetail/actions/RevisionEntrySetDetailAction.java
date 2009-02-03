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

 package org.jahia.views.engines.versioning.revisionsdetail.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.RevisionEntry;
import org.jahia.services.version.RevisionEntrySet;
import org.jahia.utils.GUITreeTools;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.textdiff.HunkTextDiffVisitor;
import org.jahia.views.engines.JahiaEngineButtonsHelper;
import org.jahia.views.engines.JahiaEngineCommonData;
import org.jahia.views.engines.JahiaEngineViewHelper;

/**
 *
 * <p>Title: RevisionEntrySet Dispatch Action</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Jahia</p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class RevisionEntrySetDetailAction extends org.apache.struts.actions.DispatchAction {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(RevisionEntrySetDetailAction.class);

    private static final String ENGINE_TITLE = "Revisions Detail";

    private static final String ENGINE_VIEW_HELPER =
            RevisionEntrySetDetailAction.class + " ENGINE_VIEW_HELPER";

    public static final String ENGINE_VIEW_HELPER_SESSION_ATTRIBUTE
            = RevisionEntrySetDetailAction.class + "_"
            + JahiaEngineViewHelper.ENGINE_VIEW_HELPER;

    public static final String REVISIONS_TREE =
            RevisionEntrySetDetailAction.class + "_RevisionTree";

    public static final String REVISIONENTRYSET =
            RevisionEntrySetDetailAction.class + "_RevisionEntrySet";

    public static final String REVISIONENTRY =
            RevisionEntrySetDetailAction.class + "_RevisionEntry";

    public static final String ACTION_URL_REQUEST_ATTRIBUTE = "ActionURL";

    /**
     * Init Engine Data
     *
     * @param request
     * @return
     */
    private void init(ActionMapping mapping, HttpServletRequest request)
    throws JahiaException {

        try {

            // test whether the data already exists in session or should be created
            String engineView = request.getParameter ("engineview");
            String reloaded = request.getParameter("reloaded");

            JTree tree = null;
            RevisionEntrySet revisionEntrySet = null;
            List flatRevisionsList = null;

            // engine view Helper
            JahiaEngineViewHelper engineViewHelper = null;
            if ( engineView != null && !"yes".equals(reloaded) ){
                // try to retrieve engine data from session
                try {
                    engineViewHelper =
                        (JahiaEngineViewHelper)request.getSession()
                        .getAttribute(ENGINE_VIEW_HELPER);
                    tree = (JTree)request.getSession()
                        .getAttribute(REVISIONS_TREE);
                    revisionEntrySet = (RevisionEntrySet)request.getSession()
                                     .getAttribute(REVISIONENTRYSET);
                }catch(Throwable t){
                }
            }
            if ( tree == null ){
                // try to get them from request
                tree = (JTree)request.getAttribute(REVISIONS_TREE);
                revisionEntrySet = (RevisionEntrySet)request
                                 .getAttribute(REVISIONENTRYSET);
                // store in session
                request.getSession().setAttribute(REVISIONS_TREE,tree);
                request.getSession().setAttribute(REVISIONENTRYSET,revisionEntrySet);
            }

            GUITreeTools.updateGUITree(tree,request);

            if ( tree != null ){
                DefaultMutableTreeNode rootNode =
                        (DefaultMutableTreeNode)tree.getModel().getRoot();
                if ( rootNode != null ){
                    flatRevisionsList = GUITreeTools.getFlatTree(tree,rootNode);
                }
            }

            // For JSP Output
            request.setAttribute("flatRevisionsList",flatRevisionsList);

            // engine common data
            JahiaEngineCommonData engineCommonData =
                    new JahiaEngineCommonData(request);
            engineCommonData.setEngineTitle(ENGINE_TITLE);

            // Prepare engine buttons helper
            JahiaEngineButtonsHelper jahiaEngineButtonsHelper =
                    new JahiaEngineButtonsHelper();
            jahiaEngineButtonsHelper.addOkButton();
            jahiaEngineButtonsHelper.addCancelButton();

            request.setAttribute(JahiaEngineButtonsHelper.JAHIA_ENGINE_BUTTONS_HELPER,
                                 jahiaEngineButtonsHelper);

            if ( engineViewHelper == null ){
                // Prepage a new engine view helper
                engineViewHelper =
                        new JahiaEngineViewHelper(engineCommonData);

                // store engine data in session
                request.getSession().setAttribute(ENGINE_VIEW_HELPER,
                                     engineViewHelper);
            }
            request.setAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA,
                                 engineCommonData);

            request.setAttribute(ENGINE_VIEW_HELPER,
                                 engineViewHelper);

            // JSP attributes
            // store in session
            request.setAttribute(REVISIONS_TREE,tree);
            request.setAttribute(REVISIONENTRYSET,revisionEntrySet);

            // Prepare the Action URL for this Action Dispatcher
            String actionURL = composeActionURL(engineCommonData.getParamBean(),
                    mapping.getPath(),null,null);
            request.setAttribute(ACTION_URL_REQUEST_ATTRIBUTE,actionURL);
            engineCommonData.setEngineURL(actionURL);

        } catch ( Exception e ){
            logger.debug("Error occurred: " + e.getMessage(), e);
            throw new JahiaException("Exception occured initializing engine's objects",
                                     "Exception occured initializing engine's objects",
                                     JahiaException.ENGINE_ERROR,
                                     JahiaException.ENGINE_ERROR, e);
        }
    }

    /**
     * Forward to revision detail.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward revisionsDetail(ActionMapping mapping,
                                     ActionForm form,
                                     HttpServletRequest request,
                                     HttpServletResponse response)
    throws IOException, ServletException {

        ActionForward forward = mapping.findForward("revisionsDetail");
        ActionMessages errors = new ActionMessages();
        JahiaEngineCommonData engineCommonData = null;

        try {
            init(mapping,request);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            ProcessingContext jParams = engineCommonData.getParamBean();

            // Retrieve selected revisionEntry
            RevisionEntry revisionEntry = null;
            String revisionEntryKey = request.getParameter("revisionEntry");
            revisionEntry = RevisionEntry.getRevisionEntry(revisionEntryKey);
            if ( revisionEntry != null ){
                // retrieve the complete instance
                RevisionEntrySet revisionEntrySet = (RevisionEntrySet)
                        request.getAttribute(REVISIONENTRYSET);
                if ( revisionEntrySet != null ){
                    List revisions = new ArrayList(revisionEntrySet.getRevisions());
                    int revIndex = revisions.indexOf(revisionEntry);
                    if ( revIndex != -1 ){
                        revisionEntry = (RevisionEntry)revisions.get(revIndex);
                    }
                }
            }

            String oldValue = "";
            String newValue = "";
            boolean isStagingValue = false;

            if ( revisionEntry != null ){
                ContentObject contentObject = (ContentObject)
                        JahiaObject.getInstance(revisionEntry.getObjectKey());

                Set stagingLangs = contentObject.getStagingLanguages(false,true);
                isStagingValue = stagingLangs.contains(revisionEntry.getLanguageCode());

                if ( contentObject instanceof ContentField ){
                    ContentField contentField = (ContentField)contentObject;
                    EntryLoadRequest loadRequest =
                            new EntryLoadRequest(revisionEntry.getEntryState());
                    loadRequest.setWithDeleted(true);
                    loadRequest.setWithMarkedForDeletion(true);

                    EntryLoadRequest jParamsLoadRequest =
                            jParams.getEntryLoadRequest();

                    // Old Value
                    JahiaField jahiaField =
                            ServicesRegistry.getInstance().getJahiaFieldService()
                            .contentFieldToJahiaField(contentField,loadRequest);
                    if ( jahiaField != null ){
                        // load field value
                        jParams.setSubstituteEntryLoadRequest(loadRequest);
                        jahiaField.load(LoadFlags.ALL,jParams);
                        jahiaField.setHasChanged(false);
                        oldValue = jahiaField.getValue();
                    }

                    // New Value ( Active or Staging )
                    loadRequest =
                            new EntryLoadRequest(ContentObjectEntryState.WORKFLOW_STATE_START_STAGING,
                            0, new ArrayList());
                    loadRequest.setWithDeleted(true);
                    loadRequest.setWithMarkedForDeletion(true);

                    loadRequest.getLocales().add(LanguageCodeConverters.languageCodeToLocale(revisionEntry.getLanguageCode()));
                    jahiaField =
                            ServicesRegistry.getInstance().getJahiaFieldService()
                            .contentFieldToJahiaField(contentField,loadRequest);
                    if ( jahiaField != null ){
                        // load field value
                        jParams.setSubstituteEntryLoadRequest(loadRequest);
                        jahiaField.load(LoadFlags.ALL,jParams);
                        jahiaField.setHasChanged(false);
                        newValue = jahiaField.getValue();
                    }

                    // restore loadRequest
                    jParams.setSubstituteEntryLoadRequest(jParamsLoadRequest);
                }
            }

            // Highlight text diff
            HunkTextDiffVisitor hunkTextDiffV =
                    new HunkTextDiffVisitor(oldValue,newValue);
            hunkTextDiffV.highLightDiff();

            request.setAttribute("oldValue",hunkTextDiffV.getHighlightedOldText());
            request.setAttribute("newValue",hunkTextDiffV.getHighlightedNewText());
            request.setAttribute("mergedValue",hunkTextDiffV.getMergedDiffText());
            request.setAttribute("isStagingValue",Boolean.valueOf(isStagingValue));
            request.setAttribute(REVISIONENTRY,revisionEntry);

        } catch ( Exception t ){
            logger.debug("Error occurred",t);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                       new ActionMessage("Exception processing show restore version test result"));
        }
        request.setAttribute("engineView","revisionsDetail");
        return continueForward(mapping,request,errors,forward);
    }
    /**
     * Forward to errors if any or to continueForward
     *
     * @param mapping
     * @param request
     * @param errors
     * @param continueForward
     * @return
     */
    private ActionForward continueForward(
            ActionMapping mapping,
            HttpServletRequest request,
            ActionMessages errors,
            ActionForward continueForward){

        if(errors != null && !errors.isEmpty()){
            saveErrors(request,errors);
            return mapping.findForward("EnginesErrors");
        }
        return continueForward;
    }


    /**
     * Generate a valid url for this Struts Action
     *
     * @param jParams
     * @param strutsAction
     * @param properties
     * @param params
     * @return
     * @throws JahiaException
     */
    public static String composeActionURL(ProcessingContext jParams,
                                          String strutsAction,
                                          Properties properties,
                                          String params)
    throws JahiaException {
        // Prepare this struts action Mapping url
        if ( properties == null ){
            properties = new Properties();
        }
        String url = jParams.composeStrutsUrl(strutsAction,properties,params);
        return url;
    }
}
