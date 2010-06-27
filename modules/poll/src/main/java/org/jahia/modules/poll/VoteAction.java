package org.jahia.modules.poll;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.jcr.NodeIterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
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
	    
        JCRNodeWrapper pollNode = renderContext.getMainResource().getNode();

        JCRNodeWrapper answerNode = pollNode.getSession().getNodeByUUID(answerUUID);

        long totalOfVotes = pollNode.getProperty("totalOfVotes").getLong();
        pollNode.setProperty("totalOfVotes", totalOfVotes+1);
               

        long nbOfVotes = answerNode.getProperty("nbOfVotes").getLong();
        answerNode.setProperty("nbOfVotes", nbOfVotes+1);

        pollNode.getSession().save();

        return new ActionResult(HttpServletResponse.SC_OK, null, generateJSONObject(pollNode));
    }

    private JSONObject generateJSONObject(JCRNodeWrapper pollNode) throws Exception {
        NodeIterator answerNodes = pollNode.getNode("answers").getNodes();

        Map<String, Object> props = new HashMap<String, Object>();
        ArrayList<Map<String, Object>> answersContainer = new ArrayList<Map<String, Object>>();

        long totalVote = pollNode.getProperty("totalOfVotes").getLong();

        // Poll name + total votes
        props.put("totalOfVotes", totalVote);
        props.put("question", pollNode.getProperty("question").getString());

        // Each answer name + total vote
        while (answerNodes.hasNext()) {
            Map<String, Object> answerProperties = new HashMap<String, Object>();
            javax.jcr.Node answer = answerNodes.nextNode();
            long answerVotes = answer.getProperty("nbOfVotes").getLong();

            answerProperties.put("label", answer.getProperty("label").getString());
            answerProperties.put("nbOfVotes", answerVotes);
            answerProperties.put("percentage", (totalVote == 0 || answerVotes == 0)?0:(answerVotes/totalVote)*100);

            answersContainer.add(answerProperties);
        }

        props.put("answerNodes", answersContainer.toArray());

        return new JSONObject(props);
    }
}
