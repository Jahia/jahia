package org.jahia.modules.remotepublish;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 22, 2010
 * Time: 2:16:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemotePublishAction implements Action {
    private static Logger logger = Logger.getLogger(RemotePublishAction.class);
    private RemotePublicationService service;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setService(RemotePublicationService service) {
        this.service = service;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRNodeWrapper node = resource.getNode();

        if (!node.isNodeType("jnt:remotePublication")) {
            return new ActionResult(HttpServletResponse.SC_BAD_REQUEST, null, null);
        }

        String remoteUrl = node.getProperty("remoteUrl").getString();
        String remotePath = node.getProperty("remotePath").getString();
        JCRNodeWrapper source = (JCRNodeWrapper) node.getProperty("node").getNode();
        String remoteUser = node.getProperty("remoteUser").getString();
        String remotePassword = node.getProperty("remotePassword").getString();

        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);

        final URL url = new URL(remoteUrl);
        client.getHostConfiguration().setHost(url.getHost(), url.getPort(), url.getProtocol());

        Credentials defaultcreds = new UsernamePasswordCredentials(remoteUser, remotePassword);
        client.getState()
                .setCredentials(new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), defaultcreds);

        PostMethod prepare = new PostMethod(remoteUrl + remotePath + ".preparereplay.do");
        prepare.addRequestHeader(new Header("accept", "application/json"));
        prepare.addParameter("sourceUuid", source.getIdentifier());
        client.executeMethod(prepare);
        if (prepare.getStatusCode() != HttpServletResponse.SC_OK) {
            logger.error("Error received on prepare : "+prepare.getStatusCode());
            return new ActionResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, new JSONObject(new HashMap()));
        }
        JSONObject response = new JSONObject(prepare.getResponseBodyAsString());

        Boolean ready = response.getBoolean("ready");

        Calendar lastDate = null;
        if (response.has("lastDate")) {
            lastDate = (Calendar) response.get("lastDate");
        }

        prepare.releaseConnection();
        if (ready) {
            final File file = new File("/tmp/remote.log");

            // Get source node from live session ?

            service.generateLog(source, lastDate, new FileOutputStream(file));

            PostMethod replay = new PostMethod(remoteUrl + remotePath + ".replay.do");
            replay.addRequestHeader(new Header("accept", "application/json"));
            Part[] parts = {
                    new StringPart("value", "value"),
                    new FilePart("log", file)
            };

            replay.setRequestEntity(new MultipartRequestEntity(parts, replay.getParams()));
            client.executeMethod(replay);

            if (replay.getStatusCode() != HttpServletResponse.SC_OK) {
                logger.error("Error received on replay : "+prepare.getStatusCode());
                return new ActionResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, new JSONObject(new HashMap()));
            }

            replay.releaseConnection();

            return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(new HashMap()));
        } else {
            return new ActionResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, new JSONObject(new HashMap()));
        }
    }

}
