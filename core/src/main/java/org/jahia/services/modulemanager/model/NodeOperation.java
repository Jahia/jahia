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
package org.jahia.services.modulemanager.model;

import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ReferenceBeanConverterImpl;
import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.ReferenceCollectionConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

import java.util.List;

/**
 * A node-level operation, which corresponds to the global operation for a bundle (install, start, stop, uninstall).
 * 
 * @author Sergiy Shyrkov
 */
@Node(jcrType = "jnt:moduleManagementNodeOperation", discriminator = false)
public class NodeOperation extends BaseOperation {

    private static final long serialVersionUID = 1285786369976605215L;

    @Collection(jcrName = "j:dependsOn", collectionConverter = ReferenceCollectionConverterImpl.class)
    private List<String> dependsOn;

    @Bean(jcrName = "j:operation", converter = ReferenceBeanConverterImpl.class)
    private Operation operation;

    /**
     * Initializes an instance of this class.
     */
    public NodeOperation() {
        super();
    }

    /**
     * Initializes an instance of this class.
     *
     * @param name
     *            the node operation name
     * @param state
     *            the current operation state
     * @param operation
     *            the corresponding global operation
     */
    public NodeOperation(String name, String state, Operation operation) {
        super(name);
        setState(state);
        this.operation = operation;
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

}
