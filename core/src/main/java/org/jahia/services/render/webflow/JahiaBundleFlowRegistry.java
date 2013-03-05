package org.jahia.services.render.webflow;

import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionConstructionException;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;

import java.util.*;

/**
 * Flow registry which aggregates all flows from all bundles
 */
public class JahiaBundleFlowRegistry extends FlowDefinitionRegistryImpl {

    private Map<String,FlowDefinitionRegistry> l = new HashMap<String, FlowDefinitionRegistry>();

    @Override
    public boolean containsFlowDefinition(String id) {
        for (Map.Entry<String, FlowDefinitionRegistry> entry : l.entrySet()) {
            if (id.startsWith(entry.getKey() + "/")) {
                String flowId = id.substring(entry.getKey().length() + 1);
                if (entry.getValue().containsFlowDefinition(flowId)) {
                    return true;
                }
            }
        }
        return super.containsFlowDefinition(id);
    }

    @Override
    public FlowDefinition getFlowDefinition(String id) throws NoSuchFlowDefinitionException, FlowDefinitionConstructionException {
        for (Map.Entry<String, FlowDefinitionRegistry> entry : l.entrySet()) {
            if (id.startsWith(entry.getKey() + "/")) {
                String flowId = id.substring(entry.getKey().length() + 1);
                if (entry.getValue().containsFlowDefinition(flowId)) {
                    return entry.getValue().getFlowDefinition(flowId);
                }
            }
        }
        return super.getFlowDefinition(id);
    }

    @Override
    public int getFlowDefinitionCount() {
        int c = 0;
        for (FlowDefinitionRegistry flowDefinitionRegistry : l.values()) {
            c += flowDefinitionRegistry.getFlowDefinitionCount();
        }
        return c + super.getFlowDefinitionCount();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String[] getFlowDefinitionIds() {
        List<String> s = new ArrayList<String>();
        for (Map.Entry<String, FlowDefinitionRegistry> entry : l.entrySet()) {
            for (String id : entry.getValue().getFlowDefinitionIds()) {
                s.add(entry.getKey() + "/" + id);
            }
        }
        return s.toArray(new String[s.size()]);
    }

    public void addFlowRegistry(String moduleName, FlowDefinitionRegistry r) {
        l.put(moduleName, r);
    }

    public void removeFlowRegistry(String moduleName) {
        l.remove(moduleName);
    }
}
