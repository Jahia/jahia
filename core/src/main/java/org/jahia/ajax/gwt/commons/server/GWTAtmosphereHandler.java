package org.jahia.ajax.gwt.commons.server;

import org.atmosphere.gwt.server.AtmosphereGwtHandler;
import org.atmosphere.gwt.server.GwtAtmosphereResource;
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
        resource.getBroadcaster().setID("GWT_COMET");
        HttpSession session = resource.getAtmosphereResource().getRequest().getSession(false);
        if (session != null) {
            logger.info("Got session with id: " + session.getId());
            logger.info("Time attribute: " + session.getAttribute("time"));
        } else {
            logger.warn("No session");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Url: " + resource.getAtmosphereResource().getRequest().getRequestURL()
                    + "?" + resource.getAtmosphereResource().getRequest().getQueryString());
        }
        String agent = resource.getRequest().getHeader("user-agent");
        logger.info(agent);
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
