package org.jahia.modules.remotepublish;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.tools.files.FileUpload;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 22, 2010
 * Time: 1:45:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReplayAction implements Action {
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

        final ParamBean paramBean = (ParamBean) Jahia.getThreadParamBean();
        final FileUpload fileUpload = paramBean.getFileUpload();
        if (fileUpload != null && fileUpload.getFileItems() != null && fileUpload.getFileItems().containsKey("log")) {
            DiskFileItem file = fileUpload.getFileItems().get("log");
            try {
                final JCRNodeWrapper target = resource.getNode();
                final InputStream in = file.getInputStream();

                logger.info("Received log from "+req.getRemoteHost());

                service.replayLog(target, in);
            } catch (Exception e) {
                logger.error("Error when replaying log",e);
                return new ActionResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, null);
            }
            fileUpload.markFilesAsConsumed();
        }

        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(new HashMap()));
    }

}
