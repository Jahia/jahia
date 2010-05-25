package org.jahia.ajax.gwt.client.core;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Window;
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
        new LoginBox().show();
    }

}
