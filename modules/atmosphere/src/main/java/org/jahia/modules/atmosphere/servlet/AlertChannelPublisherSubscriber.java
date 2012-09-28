package org.jahia.modules.atmosphere.servlet;

import org.atmosphere.annotation.Broadcast;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.jersey.Broadcastable;
import org.atmosphere.jersey.SuspendResponse;
import org.jahia.services.atmosphere.EventsLogger;

import javax.ws.rs.*;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 22/11/11
 * Time: 9:33 AM
 * To change this template use File | Settings | File Templates.
 */
@Path("/pubsub/alert/{channel}")
public class AlertChannelPublisherSubscriber {

    private @PathParam("channel")
    Broadcaster topic;

    @GET
    public SuspendResponse<String> subscribe() {
        return new SuspendResponse.SuspendResponseBuilder<String>()
                .broadcaster(topic)
                .outputComments(true)
                .addListener(new EventsLogger())
                .build();
    }

    @POST
    @Broadcast
    public Broadcastable publish(@FormParam("message") String message) {
        return new Broadcastable(message, "", topic);
    }
}
