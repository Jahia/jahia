/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
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
        return c + super.getFlowDefinitionCount();
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

    /**
     * Tests if a FlowDefinitionRegistry is already registered inside the BundleFlowRegistry
     * @param r the flowDefinitionRegistry instance we want to test. Should implement the equals
     *          method for this to work properly.
     * @return true if the FlowDefinitionRegistry instance is already registered
     */
    public boolean containsFlowRegistry(FlowDefinitionRegistry r) {
        return l.contains(r);
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
