package org.jahia.services.content;

import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.*;

public class UGCListener extends DefaultEventListener {
    private static Logger logger = LoggerFactory.getLogger(LastModifiedListener.class);


    public int getEventTypes() {
        return Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED;
    }

    @Override
    public void onEvent(final EventIterator events) {
        final JCRSessionWrapper eventSession = ((JCREventIterator) events).getSession();

        final Locale sessionLocale = eventSession.getLocale();
        final JahiaUser user = eventSession.getUser();

        final Map<String, Set<String>> t = new HashMap<>();
        try {
            while (events.hasNext()) {
                Event event = events.nextEvent();
                JCRPropertyWrapper property = (JCRPropertyWrapper) eventSession.getItem(event.getPath());
                JCRNodeWrapper node = property.getParent();
                if (node.hasProperty("j:originWS") && node.getProperty("j:originWS").getString().equals("default")) {
                    if (!t.containsKey(node.getIdentifier())) {
                        t.put(node.getIdentifier(), new HashSet<String>());
                    }
                    if (property.getName().equals(Constants.JCR_MIXINTYPES)) {
                        if (node instanceof JCRNodeDecorator) {
                            node = ((JCRNodeDecorator) node).getDecoratedNode();
                        }
                        if (node instanceof JCRNodeWrapperImpl) {
                            List<ExtendedNodeType> newMixins = new ArrayList<>(Arrays.asList(node.getMixinNodeTypes()));
                            newMixins.removeAll(Arrays.asList(((JCRNodeWrapperImpl) node).getOriginalMixinNodeTypes()));
                            for (ExtendedNodeType newMixin : newMixins) {
                                t.get(node.getIdentifier()).add(property.getName() + "=" + newMixin.getName());
                            }
                        }
                    } else {
                        t.get(node.getIdentifier()).add(property.getName());
                    }
                }
            }

            if (!t.isEmpty()) {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, workspace, null, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                        for (Map.Entry<String, Set<String>> entry : t.entrySet()) {
                            JCRNodeWrapper n = s.getNodeByIdentifier(entry.getKey());
                            if (!n.isNodeType("jmix:liveProperties")) {
                                n.addMixin("jmix:liveProperties");
                            }
                            JCRPropertyWrapper p = n.hasProperty("j:liveProperties") ? n.getProperty("j:liveProperties") : n.setProperty("j:liveProperties", new Value[0]);
                            for (JCRValueWrapper valueWrapper : p.getValues()) {
                                entry.getValue().remove(valueWrapper.getString());
                            }
                            for (String v : entry.getValue()) {
                                p.addValue(v);
                            }
                            n.getRealNode().getSession().save();
                        }

                        return null;
                    }
                });

            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
}
