package org.jahia.ajax.gwt.client.core;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.widget.LoginBox;

/**
 * Base AsyncCallback class that handles default errors.
 *
 * @param <T> Type of the return value
 */
public abstract class BaseAsyncCallback<T> implements AsyncCallback<T> {

    public void onFailure(Throwable caught) {
        if (caught instanceof SessionExpirationException) {
            onSessionExpired();
        } else {
            onApplicationFailure(caught);
        }
    }

    public void onApplicationFailure(Throwable caught) {
        Log.error("Error", caught);
    }

    public void onSessionExpired() {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable reason) {
            }

            public void onSuccess() {
                new LoginBox().show();
            }
        });        
    }

}
