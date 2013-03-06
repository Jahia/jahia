package org.jahia.services.render.webflow;

import org.springframework.context.ApplicationContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionConstructionException;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.Flow;

import java.util.*;

/**
 * Flow registry which aggregates all flows from all bundles
 */
public class BundleFlowRegistry extends FlowDefinitionRegistryImpl {

    private Set<FlowDefinitionRegistry> l = new HashSet<FlowDefinitionRegistry>();

    @Override
    public boolean containsFlowDefinition(String id) {
        for (FlowDefinitionRegistry entry : l) {
            if (entry.containsFlowDefinition(id)) {
                return true;
            }
        }
        return super.containsFlowDefinition(id);
    }

    @Override
    public FlowDefinition getFlowDefinition(String id) throws NoSuchFlowDefinitionException, FlowDefinitionConstructionException {
        for (final FlowDefinitionRegistry entry : l) {
            if (entry.containsFlowDefinition(id)) {
                return entry.getFlowDefinition(id);
            }
        }
        return super.getFlowDefinition(id);
    }

    @Override
    public int getFlowDefinitionCount() {
        int c = 0;
        for (FlowDefinitionRegistry flowDefinitionRegistry : l) {
            c += flowDefinitionRegistry.getFlowDefinitionCount();
        }
        return c + super.getFlowDefinitionCount();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String[] getFlowDefinitionIds() {
        List<String> s = new ArrayList<String>();
        for (FlowDefinitionRegistry entry : l) {
            for (String id : entry.getFlowDefinitionIds()) {
                s.add(id);
            }
        }
        return s.toArray(new String[s.size()]);
    }

    public void addFlowRegistry(FlowDefinitionRegistry r) {
        if (r.getFlowDefinitionCount() > 0) {
        l.add(r);
        }
    }

    public void removeFlowRegistry(FlowDefinitionRegistry r) {
        l.remove(r);
    }
}
