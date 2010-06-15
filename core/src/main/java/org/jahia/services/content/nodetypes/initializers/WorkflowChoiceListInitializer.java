package org.jahia.services.content.nodetypes.initializers;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Initializer that returns the list of all workflow definitions
 */
public class WorkflowChoiceListInitializer implements ChoiceListInitializer {
    private WorkflowService workflowService;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        List<ChoiceListValue> choiceListValues = new ArrayList<ChoiceListValue>();
        try {
            List<WorkflowDefinition> defs = workflowService.getWorkflows();

            for (WorkflowDefinition def : defs) {
                choiceListValues.add(new ChoiceListValue(def.getName(), new HashMap<String, Object>(),
                        new ValueImpl(def.getProvider() + ":" + def.getKey(), PropertyType.STRING, false)));
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return choiceListValues;
    }
}
