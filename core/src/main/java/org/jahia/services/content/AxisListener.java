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

import org.jahia.api.Constants;

import javax.jcr.*;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 13 d�c. 2007
 * Time: 21:12:42
 * To change this template use File | Settings | File Templates.
 */
public class AxisListener extends DefaultEventListener {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(AxisListener.class);

    public AxisListener(JCRStoreProvider provider) {
        this.provider = provider;
    }

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    public void onEvent(EventIterator eventIterator) {
        try {
            Session s = provider.getSystemSession();

            try {
                QueryResult qr = null;
                while (eventIterator.hasNext()) {
                    Event event = eventIterator.nextEvent();
                    if (isExternal(event)) {
                        continue;
                    }

                    try {
                        if (event.getType() == Event.NODE_ADDED) {
                            Node n = (Node) s.getItem(event.getPath());
                            if (n.isNodeType(Constants.NT_FILE)) {
                                qr = getQueryResults(s, qr);
                                parseAxis(qr, n, event, s);
                            }
                        } else {
                            String path = event.getPath();
                            String propertyName = path.substring(path.lastIndexOf('/')+1);

                            if (!propertiesToIgnore.contains(propertyName)) {
                                Property p = (Property) s.getItem(path);

                                Node parent = p.getParent();
                                if (parent.isNodeType(Constants.NT_FILE)) {
                                    qr = getQueryResults(s, qr);
                                    parseAxis(qr, parent, event, s);
                                }
                            }
                        }
                    } catch (PathNotFoundException e) {
                        // Ignore and continue
                    }
                }
            } finally {
                s.logout();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private QueryResult getQueryResults(Session s, QueryResult qr) throws RepositoryException {
        if(qr == null ) {
            Query q = s.getWorkspace().getQueryManager().createQuery("SELECT * FROM jnt:axisView", Query.SQL);
            qr = q.execute();
        }
        return qr;
    }

    private void parseAxis(QueryResult qr, Node n, Event event, Session s) throws RepositoryException {
        NodeIterator ni = qr.getNodes();
        while (ni.hasNext()) {
            Node rootAxis = ni.nextNode();
            Property nodetype = rootAxis.getProperty("j:nodetype");
            Value[] v = nodetype.getValues();
            boolean match = true;
            for (int i = 0; i < v.length && match; i++) {
                Value value = v[i];
                String nodetypeString = value.getString();
                match = n.isNodeType(nodetypeString);
            }
            if (match) {
                List<Node> path = new ArrayList<Node>();
                List<String> names = new ArrayList<String>();
                NodeIterator props = rootAxis.getNodes("j:property");
                while (props.hasNext()) {
                    Node p = props.nextNode();
                    path.add(p);
                    names.add(p.getProperty("j:name").getString());
                }

                if (event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED || event.getType() == Event.PROPERTY_REMOVED) {
                    String p = event.getPath().substring(event.getPath().lastIndexOf("/")+1);
                    if (!names.contains(p)) {
                        return;
                    }
                }

                Query q = s.getWorkspace().getQueryManager().createQuery("SELECT * FROM jnt:symLink where jcr:path LIKE '"+rootAxis.getPath()+"/%' and j:link='"+n.getUUID()+"'", Query.SQL);
                qr = q.execute();
                NodeIterator ni2 = qr.getNodes();
                while (ni2.hasNext()) {
                    Node old = ni2.nextNode();
                    old.remove();
                }

                parseAxis(rootAxis, n, path,0);
                rootAxis.save();
            }
        }
    }

    private void parseAxis(Node currentAxis, Node n, List<Node> path, int index) throws RepositoryException {
        if (index == path.size()) {
            Node ref = currentAxis.addNode(n.getName(), Constants.JAHIANT_SYMLINK);
            ref.setProperty("j:link",n);
        } else {
            Node currentProperty = path.get(index);

            String propName = currentProperty.getProperty("j:name").getString();
            if (n.hasProperty(propName)) {
                Property property = n.getProperty(propName);
                if (property.getDefinition().isMultiple()) {
                    Value[] vs = property.getValues();
                    for (int i = 0; i < vs.length; i++) {
                        String newAxisName = vs[i].getString();
                        parseAxis(getNewAxis(currentAxis, newAxisName), n, path, index+1);
                    }
                } else {
                    String newAxisName = property.getString();
                    if (property.getType() == PropertyType.DATE) {
                        String format = currentProperty.getProperty("j:format").getString();
                        newAxisName = new SimpleDateFormat(format).format(property.getDate().getTime());
                    }
                    parseAxis(getNewAxis(currentAxis, newAxisName), n, path, index+1);
                }
            }
        }
    }

    private Node getNewAxis(Node currentAxis, String newAxisName) throws RepositoryException {
        Node newAxis;
        try {
            newAxis = currentAxis.getNode(newAxisName);
        } catch (PathNotFoundException e) {
            newAxis = currentAxis.addNode(newAxisName, Constants.JAHIANT_AXISFOLDER);
        }
        return newAxis;
    }

}
