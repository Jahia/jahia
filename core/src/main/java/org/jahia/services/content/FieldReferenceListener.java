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
        if (node.isNodeType(JAHIAMIX_HIERARCHYNODE)) {
            if (!node.isCheckedOut()) {
                node.checkout();
            }
            if (node.hasProperty(FULLPATH)) {
                String oldPath = node.getProperty(FULLPATH).getString();
                move(node.getUUID(), oldPath, node.getPath());
            }
            node.setProperty(FULLPATH, node.getPath());
            node.setProperty("j:nodename",node.getName());
            node.save();
        }
        if (node.isNodeType(NT_FOLDER)) {
            for (NodeIterator ni = node.getNodes(); ni.hasNext();) {
                updateFullPath(ni.nextNode());
            }
        }        
    }

    public void setFieldXRefManager(JahiaFieldXRefManager fieldXRefManager) {
        this.fieldXRefManager = fieldXRefManager;
    }
}
