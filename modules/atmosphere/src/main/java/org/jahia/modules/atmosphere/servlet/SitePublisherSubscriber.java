package org.jahia.modules.atmosphere.servlet;

import org.atmosphere.annotation.Broadcast;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.jersey.Broadcastable;
import org.atmosphere.jersey.SuspendResponse;
import org.jahia.services.atmosphere.EventsLogger;

import javax.ws.rs.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 22/11/11
 * Time: 9:33 AM
 * To change this template use File | Settings | File Templates.
 */
@Path("/pubsub/sites/{site}")
public class SitePublisherSubscriber {

    private @PathParam("site")
    Broadcaster topic;

    @GET
    public SuspendResponse<String> subscribe() {
        return new SuspendResponse.SuspendResponseBuilder<String>()
                .broadcaster(topic)
                .outputComments(true)
                .entity("")
                .addListener(new EventsLogger())
                .resumeOnBroadcast(true)
                .build();
    }

    @POST
    @Broadcast
    public Broadcastable publish(@FormParam("message") String message) {
        return new Broadcastable(message, "", topic);
    }
}
