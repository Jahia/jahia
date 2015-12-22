/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
