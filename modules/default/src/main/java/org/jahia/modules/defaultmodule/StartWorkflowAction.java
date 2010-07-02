package org.jahia.modules.defaultmodule;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowVariable;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 18, 2010
 * Time: 12:16:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class StartWorkflowAction implements Action {
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
        String process = parameters.get("process").get(0);
        String workflowDefinitionKey = StringUtils.substringAfter(process, ":");
        String providerKey = StringUtils.substringBefore(process, ":");

        Map<String, Object> map = getVariablesMap(parameters);
        workflowService.startProcess(resource.getNode(), workflowDefinitionKey, providerKey, map);
        return ActionResult.OK;
    }

    private HashMap<String, Object> getVariablesMap(Map<String, List<String>> properties) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, List<String>> property : properties.entrySet()) {
            if (!"process".equals(property.getKey())) {
                List<String> propertyValues = property.getValue();
                List<WorkflowVariable> values = new ArrayList<WorkflowVariable>(propertyValues.size());
                for (String value : propertyValues) {
                    values.add(new WorkflowVariable(value, 1));
                }
                map.put(property.getKey(), values);
            }
        }
        return map;
    }
}
