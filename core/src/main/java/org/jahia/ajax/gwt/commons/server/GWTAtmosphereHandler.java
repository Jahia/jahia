package org.jahia.ajax.gwt.commons.server;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.DefaultBroadcaster;
import org.atmosphere.gwt.server.AtmosphereGwtHandler;
import org.atmosphere.gwt.server.GwtAtmosphereResource;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class GWTAtmosphereHandler extends AtmosphereGwtHandler {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(GWTAtmosphereHandler.class);

    @Override
    public int doComet(GwtAtmosphereResource resource) throws ServletException, IOException {
        Broadcaster broadcaster = BroadcasterFactory.getDefault().lookup(Broadcaster.class, GWT_BROADCASTER_ID);
        if (broadcaster == null) {
            broadcaster = BroadcasterFactory.getDefault().get(DefaultBroadcaster.class, GWT_BROADCASTER_ID);
        }
        resource.getAtmosphereResource().setBroadcaster(broadcaster);

        JahiaUser user = (JahiaUser) resource.getSession().getAttribute(ProcessingContext.SESSION_USER);

        if (user != null) {
            Broadcaster userBroadcaster = BroadcasterFactory.getDefault().lookup(Broadcaster.class, GWT_BROADCASTER_ID + user.getName());
            if (userBroadcaster == null) {
                userBroadcaster = BroadcasterFactory.getDefault().get(DefaultBroadcaster.class, GWT_BROADCASTER_ID + user.getName());
            }
            userBroadcaster.addAtmosphereResource(resource.getAtmosphereResource());
        }

        return NO_TIMEOUT;

    }

    @Override
    public void cometTerminated(GwtAtmosphereResource cometResponse, boolean serverInitiated) {
        super.cometTerminated(cometResponse, serverInitiated);
        logger.info("Comet disconnected");
    }

    @Override
    public void doPost(HttpServletRequest postRequest, HttpServletResponse postResponse, List<?> messages, GwtAtmosphereResource cometResource) {
        HttpSession session = postRequest.getSession(false);
        if (session != null) {
            logger.info("Post has session with id: " + session.getId());
        } else {
            logger.info("Post has no session");
        }
        super.doPost(postRequest, postResponse, messages, cometResource);
    }

}
