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
package org.jahia.services.content.impl.jahia;

import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.services.workflow.WorkflowEvent;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 2, 2008
 * Time: 2:00:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRListener extends JahiaEventListener {

    public void aggregatedObjectChanged(JahiaEvent je) {
        List allEvents = (List) je.getObject();


        Set<String> viewed = new HashSet<String>();

        for (int i = 0; i < allEvents.size(); ) {
            WorkflowEvent we = (WorkflowEvent) allEvents.get(i);
            ContentObject object = (ContentObject) we.getObject();
            if (object == null) {
                allEvents.remove(i);
            } else {
                String k = object.getObjectKey() + we.getLanguageCode();
                if (viewed.contains(k)) {
                    allEvents.remove(i);
                } else {
                    i++;
                }
                viewed.add(k);
            }
        }

        ObservationManagerImpl.fireEvents(allEvents);
    }

    @Override
    public void aggregatedContentActivation(JahiaEvent je) {
        List allEvents = (List) je.getObject();

        Set<String> viewed = new HashSet<String>();
        Set events;
        for (int i = 0; i < allEvents.size(); ) {
            ContentActivationEvent event = (ContentActivationEvent) allEvents.get(i);
            ObjectKey objectKey = (ObjectKey) event.getObject();

            if (objectKey == null) {
                allEvents.remove(i);
            } else {

                String k = objectKey.toString();
                if (viewed.contains(k)) {
                    allEvents.remove(i);
                } else {
                    i++;
                }
                viewed.add(k);
            }
        }
        ObservationManagerImpl.fireActivationEvents(allEvents);

    }

    @Override
    public void aggregatedContentWorkflowStatusChanged(JahiaEvent je) {
        List<ContentActivationEvent> allEvents = new LinkedList<ContentActivationEvent>(
                (List) je.getObject());

        Set<String> viewed = new HashSet<String>();
        for (Iterator<ContentActivationEvent> eventIterator = allEvents
                .iterator(); eventIterator.hasNext();) {
            ContentActivationEvent activationEvent = eventIterator.next();
            ObjectKey objectKey = (ObjectKey) activationEvent.getObject();
            Set<String> langs = new TreeSet<String>(activationEvent
                    .getLanguageCodes());
            String viewedKey = objectKey.toString() + langs;
            if (viewed.contains(viewedKey)) {
                eventIterator.remove();
            } else {
                viewed.add(viewedKey);
            }
        }
        ObservationManagerImpl.fireWorkflowStatusEvents(allEvents);
    }
}
