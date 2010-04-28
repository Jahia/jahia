package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.google.gwt.http.client.*;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Apr 28, 2010
 * Time: 2:26:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExecuteActionItem extends BaseActionItem {
  public static final int STATUS_CODE_OK = 200;
    private String action;
    public void onComponentSelection() {
        String baseURL =  "http://localhost:8080" + JahiaGWTParameters.getContextPath() + "/cms/render";
        String localURL = baseURL + "/default/en"  + linker.getSelectedNode().getPath();
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, localURL + "." + action + ".do");
        try {
          Request response = builder.sendRequest(null, new RequestCallback() {
            public void onError(Request request, Throwable exception) {
              // Code omitted for clarity
            }

            public void onResponseReceived(Request request, Response response) {
              // Code omitted for clarity
            }
          });
        } catch (RequestException e) {
          // Code omitted for clarity
        }

    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();

        setEnabled(lh.isTableSelection() || lh.isMainSelection());
    }

    public void setAction(String action) {
        this.action = action;
    }
}

