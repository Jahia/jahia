/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

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
