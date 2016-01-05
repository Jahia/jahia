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
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

/**
 * Represents a node-level bundle, which has a reference to one of the available bundles in the global storage and the state of this bundle
 * on the current cluster node.
 * 
 * @author Sergiy Shyrkov
 */
@Node(jcrType = "jnt:moduleManagementNodeBundle", discriminator = false)
public class NodeBundle extends BasePersistentObject {

    private static final long serialVersionUID = 7399867537701726556L;

    @Bean(jcrName = "j:bundle", converter = ReferenceBeanConverterImpl.class)
    private Bundle bundle;

    @Field(jcrName = "j:state")
    private String state;

    /**
     * Initializes an instance of this class.
     */
    public NodeBundle() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name
     */
    public NodeBundle(String name) {
        super(name);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public String getState() {
        return state;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public void setState(String state) {
        this.state = state;
    }

}
