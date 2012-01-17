/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.*;

/**
 * Execute recurrent calls to the server
 */
public class Poller {

    private transient Timer timer;
    private static Poller instance;

    private Map<String, ArrayList<PollListener>> listeners = new HashMap<String, ArrayList<PollListener>>();

    public static Poller getInstance() {
        if (instance == null) {
            instance = new Poller();
        }
        return instance;
    }

    public Poller() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                timer = new Timer() {
                    public void run() {
                        JahiaContentManagementService.App.getInstance().getPollData(new HashSet<String>(listeners.keySet()), new AsyncCallback<Map<String,Object>>() {
                            public void onSuccess(Map<String,Object> result) {
                                for (Map.Entry<String, ArrayList<PollListener>> entry : listeners.entrySet()) {
                                    for (PollListener listener : entry.getValue()) {
                                        listener.handlePollingResult(entry.getKey(), result.get(entry.getKey()));
                                    }
                                }
                                schedule(5000);
                            }

                            public void onFailure(Throwable caught) {
                                Log.error("Cannot get jobs", caught);
                            }
                        });
                    }
                };
                timer.run();

            }
        });
    }

    public void registerListener(PollListener listener, String key) {
        if (!listeners.containsKey(key)) {
            listeners.put(key, new ArrayList<PollListener>());
        }
        listeners.get(key).add(listener);
    }

    public interface PollListener {
        public void handlePollingResult(String key, Object result);
    }
}
