package org.jahia.modules.poll;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: fabrice
 * Date: Apr 15, 2010
 * Time: 10:30:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class VoteAction implements Action {
     private transient static Logger logger = Logger.getLogger(VoteAction.class);
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {

        String answerUUID = req.getParameter("answerUUID");
	    logger.info("answerUUID: " + answerUUID);

        JCRNodeWrapper pollNode = renderContext.getMainResource().getNode();
	    logger.info("pollNode: " + pollNode);
        JCRNodeWrapper answerNode = pollNode.getSession().getNodeByUUID(answerUUID);

        long totalOfVotes = pollNode.getProperty("totalOfVotes").getLong();
        pollNode.setProperty("totalOfVotes", totalOfVotes+1);
               

        long nbOfVotes = answerNode.getProperty("nbOfVotes").getLong();
        answerNode.setProperty("nbOfVotes", nbOfVotes+1);

        pollNode.getSession().save();

        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
    }
}
