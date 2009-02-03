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

package org.jahia.services.content;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.engines.filemanager.URLUtil;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentBigTextField;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 21, 2008
 * Time: 2:36:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class FieldReferenceListener extends DefaultEventListener {
    private static Logger logger = Logger.getLogger (FieldReferenceListener.class);

    private JahiaFieldXRefManager fieldXRefManager = null;

    public FieldReferenceListener() {
    }

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

                String path = provider.decodeInternalName(event.getPath());

                if (event.getType() == Event.NODE_ADDED) {
                    if (session == null) {
                        session = provider.getSystemSession();
                    }
                    JCRNodeWrapper node = provider.getNodeWrapper(provider.decodeInternalName(path), null, session);
                    if (node.isNodeType(Constants.NT_HIERARCHYNODE)) {
                        String oldPath = node.getPropertyAsString("j:fullpath");
                        if (oldPath != null) {
                            move(node.getUUID(), oldPath, path);
                        }
                        node.setProperty("j:fullpath", path);
                        node.save();
                    }
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
        try {
            if (fieldXRefManager == null) {
                fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
            }

            Collection<JahiaFieldXRef> c = fieldXRefManager.getReferencesForTargetWithWildcard(JahiaFieldXRefManager.FILE+provider.getKey()+":"+uuid);

            for (JahiaFieldXRef jahiaFieldXRef : c) {
                int fieldId = jahiaFieldXRef.getComp_id().getFieldId();
                int workflow = jahiaFieldXRef.getComp_id().getWorkflow();
                String language = jahiaFieldXRef.getComp_id().getLanguage();
                int version = 0;
                if (workflow == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    version = ContentField.getField(fieldId).getActiveVersionID();
                }

                ContentField field = ContentField.getField(fieldId);

                if (field instanceof ContentBigTextField) {
                    String bigText = ServicesRegistry.getInstance().getJahiaTextFileService().loadBigTextValue(jahiaFieldXRef.getSiteId(), field.getPageID(),
                            field.getID(), "", version, workflow, language);
                    String prefix = "###";
                    bigText = bigText.replace(prefix+ URLUtil.URLEncode(sourceUri, "UTF-8"), prefix+ URLUtil.URLEncode(destinationUri, "UTF-8"));
                    ServicesRegistry.getInstance().getJahiaTextFileService().saveContents(jahiaFieldXRef.getSiteId(), field.getPageID(),
                            field.getID(), bigText, version, workflow, language);
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
