/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils.maven.plugin.deployers;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 26 d�c. 2007
 * Time: 14:04:51
 * To change this template use File | Settings | File Templates.
 */
public class ServerDeploymentFactory {

    public static final ServerDeploymentFactory instance = new ServerDeploymentFactory();

    private Map<String, ServerDeploymentInterface> implementations = new HashMap<String, ServerDeploymentInterface>();

    static {
        ServerDeploymentInterface tomcatImplementation = new Tomcat5ServerDeploymentImpl();
        instance.addImplementation("tomcat5.5", tomcatImplementation);
        instance.addImplementation("tomcat6", tomcatImplementation);
        ServerDeploymentInterface jbossImplementation = new JBossServerDeploymentImpl();
        instance.addImplementation("jboss4.0.x", jbossImplementation);
        instance.addImplementation("jboss4.2.x", jbossImplementation);
        ServerDeploymentInterface websphereImplementation = new WebsphereServerDeploymentImpl();
        instance.addImplementation("websphere6.1.x", websphereImplementation);
    }

    public static ServerDeploymentFactory getInstance() {
        return instance;
    }

    public ServerDeploymentFactory() {}

    public void addImplementation(String implementationKey, ServerDeploymentInterface implementation) {
        implementations.put(implementationKey, implementation);
    }

    public void removeImplementation(String implementationKey) {
        implementations.remove(implementationKey);
    }

    public ServerDeploymentInterface getImplementation(String implementationKey) {
        return implementations.get(implementationKey);
    }

}
