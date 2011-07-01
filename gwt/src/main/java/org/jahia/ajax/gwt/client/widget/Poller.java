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
