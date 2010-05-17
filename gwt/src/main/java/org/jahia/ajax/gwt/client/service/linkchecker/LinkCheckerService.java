package org.jahia.ajax.gwt.client.service.linkchecker;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.core.client.GWT;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaLinkCheckerStatus;

/**
 * User: romain
 * Date: 11 juin 2009
 * Time: 14:35:00
 */
public interface LinkCheckerService extends RemoteService {

    public static class App {
        private static LinkCheckerServiceAsync app = null;

        public static synchronized LinkCheckerServiceAsync getInstance() {
            if (app == null) {
                String relativeServiceEntryPoint = createEntryPointUrl();
                String serviceEntryPoint = URL.getAbsoluteURL(relativeServiceEntryPoint);
                app = (LinkCheckerServiceAsync) GWT.create(LinkCheckerService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);

                JahiaGWTParameters.addUpdater(new JahiaGWTParameters.UrlUpdater() {
                    public void updateEntryPointUrl() {
                        String relativeServiceEntryPoint = createEntryPointUrl();
                        String serviceEntryPoint = URL.getAbsoluteURL(relativeServiceEntryPoint);
                        ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);
                    }
                });
                
            }
            return app;
        }

        private static String createEntryPointUrl() {
            return JahiaGWTParameters.getServiceEntryPoint() + "linkchecker.gwt?lang="+JahiaGWTParameters.getLanguage() + "&site="+JahiaGWTParameters.getSiteUUID() + "&workspace="+JahiaGWTParameters.getWorkspace();
        }
    }

    public Boolean checkLinks();

    public GWTJahiaLinkCheckerStatus lookForCheckedLinks();

    public void stopCheckingLinks();

}
