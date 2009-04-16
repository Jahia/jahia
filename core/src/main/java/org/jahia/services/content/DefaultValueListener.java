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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.DynamicValueImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 30, 2008
 * Time: 11:56:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultValueListener extends DefaultEventListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DefaultValueListener.class);

    public DefaultValueListener() {
    }


    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    public void onEvent(EventIterator eventIterator) {
        try {
            List<Event> events = new ArrayList<Event>();
            String username = null;
            while (eventIterator.hasNext()) {
                Event event = eventIterator.nextEvent();
                username = event.getUserID();
                events.add(event);
            }

            Session s = provider.getSystemSession(username,workspace);

            Iterator<Event> it = events.iterator();

            try {
                while (it.hasNext()) {
                    Event event = it.next();
                    if (isExternal(event)) {
                        continue;
                    }
                    try {
                        Node n = null;
                        if (event.getType() == Event.NODE_ADDED) {
                            n = (Node) s.getItem(event.getPath());
                        }
                        if (event.getPath().endsWith(Constants.JCR_MIXINTYPES)) {
                            n = (Node) s.getItem(event.getPath().substring(0,event.getPath().lastIndexOf('/')));                            
                        }
                        if (n != null) {
                            List<NodeType> l = new ArrayList<NodeType>();
                            NodeType nt = n.getPrimaryNodeType();
                            l.add(nt);
                            NodeType mixin[] = n.getMixinNodeTypes();
                            l.addAll(Arrays.asList(mixin));
                            for (Iterator<NodeType> iterator = l.iterator(); iterator.hasNext();) {
                                NodeType nodeType = iterator.next();
                                ExtendedNodeType ent = NodeTypeRegistry.getInstance().getNodeType(nodeType.getName());
                                if (ent != null ) {
                                    ExtendedPropertyDefinition[] pds = ent.getPropertyDefinitions();
                                    for (int i = 0; i < pds.length; i++) {
                                        ExtendedPropertyDefinition pd = pds[i];
                                        Value[] v = pd.getDefaultValues();
                                        for (int j = 0; j < v.length; j++) {
                                            Value value = v[j];
                                            if (value instanceof DynamicValueImpl) {
                                                if (!n.hasProperty(pd.getName())) {
                                                    n.setProperty(pd.getName(), value.getString());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error when executing event",e);
                    }
                }
                if (s.hasPendingChanges()) {
                    s.save();
                }
            } finally {
                s.logout();
            }
        } catch (NoSuchNodeTypeException e) {
            // silent ignore
        } catch (Exception e) {
            logger.error("Error when executing event",e);
        }

    }
}
