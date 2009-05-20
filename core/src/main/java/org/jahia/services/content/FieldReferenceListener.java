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
package org.jahia.services.content;

import static org.jahia.api.Constants.*;

import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.fields.JahiaBigTextField;
import org.jahia.engines.filemanager.URLUtil;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentBigTextField;
import org.jahia.services.fields.ContentField;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Listener implementation used to update field references when a node is moved/renamed.
 * User: toto
 * Date: Jul 21, 2008
 * Time: 2:36:05 PM
 */
public class FieldReferenceListener extends DefaultEventListener {
    private static Logger logger = Logger.getLogger (FieldReferenceListener.class);

    private JahiaFieldXRefManager fieldXRefManager = null;

    public int getEventTypes() {
        return Event.NODE_ADDED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    public void onEvent(EventIterator eventIterator) {
        Session session = null;

        try {
            while (eventIterator.hasNext()) {
                Event event = eventIterator.nextEvent();

                if (isExternal(event)) {
                    continue;
                }

                String path = event.getPath();

                if (event.getType() == Event.NODE_ADDED) {
                    if (session == null) {
                        session = provider.getSystemSession();
                    }
                    updateFullPath((Node) session.getItem(path));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }


    public void move (String uuid, String sourceUri, String destinationUri) {
        if (sourceUri == null || destinationUri == null || sourceUri.equals(destinationUri)) {
            return;
        }
        try {
            JahiaSearchService searchService = ServicesRegistry.getInstance().getJahiaSearchService();
            ProcessingContext processingContext = Jahia.getThreadParamBean();
            JahiaUser user = processingContext != null ? processingContext.getUser() : null;
            
            Collection<JahiaFieldXRef> c = fieldXRefManager.getReferencesForTarget(JahiaFieldXRefManager.FILE+provider.getKey()+":"+uuid);
            
            if (c.size() > 0) {
                sourceUri = provider.decodeInternalName(sourceUri);
                destinationUri = provider.decodeInternalName(destinationUri);
                
                for (JahiaFieldXRef jahiaFieldXRef : c) {
                    int fieldId = jahiaFieldXRef.getComp_id().getFieldId();
                    int workflow = jahiaFieldXRef.getComp_id().getWorkflow();
                    String language = jahiaFieldXRef.getComp_id().getLanguage();
                    int version = 0;
                    if (workflow == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                        version = ContentField.getField(fieldId).getActiveVersionID();
                    }
    
                    ContentField field = ContentField.getField(fieldId);
    
                    if (field != null) {
                        if (field instanceof ContentBigTextField) {
                            String bigText = ServicesRegistry.getInstance().getJahiaTextFileService().loadBigTextValue(jahiaFieldXRef.getSiteId(), field.getPageID(),
                                    field.getID(), "", version, workflow, language);
                            if (bigText != null) {
                                String replacement = JahiaBigTextField.URL_MARKER
                                        + JahiaFieldXRefManager.FILE
                                        + destinationUri;
                                String modifiedBigText = bigText
                                        .replace(
                                                JahiaBigTextField.URL_MARKER
                                                        + JahiaFieldXRefManager.FILE
                                                        + URLUtil.URLEncode(sourceUri,
                                                                "UTF-8"), replacement)
                                        .replace(
                                                JahiaBigTextField.URL_MARKER
                                                        + JahiaFieldXRefManager.FILE
                                                        + sourceUri, replacement);
                                if (!bigText.equals(modifiedBigText)) {
                                    ServicesRegistry.getInstance()
                                            .getJahiaTextFileService().saveContents(
                                                    jahiaFieldXRef.getSiteId(),
                                                    field.getPageID(), field.getID(),
                                                    modifiedBigText, version, workflow, language);
                                }
                            }
                        }
                        RuleEvaluationContext ctx = new RuleEvaluationContext(
                                field.getObjectKey(), field,
                                processingContext, user);
                        searchService.indexContentObject(field,
                                user, ctx);
                    } else {
                        logger.warn("No content field with the ID " + fieldId + " can be found.");
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void updateFullPath(Node node) throws RepositoryException {
        if (node.isNodeType(NT_HIERARCHYNODE)) {
            if (node.hasProperty(FULLPATH)) {
                String oldPath = node.getProperty(FULLPATH).getString();
                move(node.getUUID(), oldPath, node.getPath());
            }
            node.setProperty(FULLPATH, node.getPath());
            node.save();
            if (node.isNodeType(NT_FOLDER)) {
                for (NodeIterator ni = node.getNodes(); ni.hasNext();) {
                    updateFullPath(ni.nextNode());
                }
            }
        }
    }

    public void setFieldXRefManager(JahiaFieldXRefManager fieldXRefManager) {
        this.fieldXRefManager = fieldXRefManager;
    }
}
