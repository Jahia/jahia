package org.jahia.ajax.gwt.commons.server;

/**
 * simple GWT handler for atmosphere
 */
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Post;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.*;
import org.atmosphere.gwt20.managed.AtmosphereMessageInterceptor;
import org.atmosphere.gwt20.server.GwtRpcInterceptor;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.IdleResourceInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.jahia.api.Constants;
import org.jahia.services.usermanager.JahiaUser;

import java.util.logging.Logger;

@ManagedService(path = "/atmosphere/rpc",
        interceptors = {
                /**
                 * Handle lifecycle for us
                 */
                AtmosphereResourceLifecycleInterceptor.class,
                /**
                 * Send to the client the size of the message to prevent serialization error.
                 */
                TrackMessageSizeInterceptor.class,
                /**
                 * Serialize/Deserialize GWT message for us
                 */
                GwtRpcInterceptor.class,
                /**
                 * Make sure our {@link AtmosphereResourceEventListener#onSuspend} is only called once for transport
                 * that reconnect on every requests.
                 */
                SuspendTrackerInterceptor.class,
                /**
                 * Deserialize the GWT message
                 */
                AtmosphereMessageInterceptor.class,
                /**
                 * Echo the messages we are receiving from the client either as w WebSocket message or an HTTP Post.
                 */
                BroadcastOnPostAtmosphereInterceptor.class,
                /**
                 * Discard idle AtmosphereResource in case the network didn't advise us the client disconnected
                 */
                IdleResourceInterceptor.class
        })
public class ManagedGWTResource {

    static final Logger logger = Logger.getLogger("ManagedGWTResource");

    public static final String GWT_BROADCASTER_ID = "org.jahia.broadcaster";

    @Ready
    public void onReady(final AtmosphereResource r) {
        r.suspend();
        BroadcasterFactory.getDefault().lookup(GWT_BROADCASTER_ID, true).addAtmosphereResource(r);
        JahiaUser user = (JahiaUser) r.getRequest().getSession(true).getAttribute(Constants.SESSION_USER);
        if (user != null) {
            BroadcasterFactory.getDefault().lookup(GWT_BROADCASTER_ID + user.getName(),true).addAtmosphereResource(r);
        }

        logger.info("Received RPC GET");
    }

    @Disconnect
    public void disconnected(AtmosphereResourceEvent event){
        // isCancelled == true. means the client didn't send the close event, so an unexpected network glitch or browser
        // crash occurred.
        if (event.isCancelled()) {
            logger.info("User:" + event.getResource().uuid() + " unexpectedly disconnected");
        } else if (event.isClosedByClient()) {
            logger.info("User:" + event.getResource().uuid() + " closed the connection");
        }
    }

    @Post
    public void post(AtmosphereResource r) {
        // Don't need to do anything, the interceptor took care of it for us.
        logger.info("POST received with transport + " + r.transport());
    }
}
