package org.jahia.modules.defaultmodule;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.workflow.WorkflowService;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 18, 2010
 * Time: 12:16:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExecuteTaskAction implements Action {
    private String name;
    private WorkflowService workflowService;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String action = parameters.get("action").get(0);
        String actionId = StringUtils.substringAfter(action, ":");
        String providerKey = StringUtils.substringBefore(action, ":");
        String outcome = parameters.get("outcome").get(0);

        workflowService.assignTask(actionId, providerKey, renderContext.getUser());
        workflowService.completeTask(actionId, providerKey, outcome, new HashMap<String, Object>());

        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
    }
}
