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
