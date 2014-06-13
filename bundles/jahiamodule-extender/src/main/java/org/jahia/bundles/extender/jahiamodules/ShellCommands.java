/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.bundles.extender.jahiamodules;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleUtils;
import org.osgi.framework.Bundle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Basic Jahia extender shell commands
 * @todo we might want to move this to a separate bundle later.
 */
public class ShellCommands {

    private Activator activator;

    public ShellCommands(Activator activator) {
        this.activator = activator;
    }

    public void modules() {

        Map<ModuleState.State, Set<Bundle>> modulesByState = activator.getModulesByState();
        for (ModuleState.State moduleState : modulesByState.keySet()) {
            System.out.println("");
            System.out.println("Module State: " + moduleState);
            System.out.println("----------------------------------------");
            Set<Bundle> bundlesInState = modulesByState.get(moduleState);
            for (Bundle bundleInState : bundlesInState) {
                ModuleState bundleModuleState = activator.getModuleState(bundleInState);
                JahiaTemplatesPackage modulePackage = BundleUtils.getModule(bundleInState);
                String dependsOn = "";
                if (modulePackage != null) {
                    dependsOn = " depends on " + modulePackage.getDepends();
                }
                String details = "";
                if (bundleModuleState.getDetails() != null) {
                    if (bundleModuleState.getDetails() instanceof Throwable) {
                        Throwable t = (Throwable) bundleModuleState.getDetails();
                        StringWriter stringWriter = new StringWriter();
                        t.printStackTrace(new PrintWriter(stringWriter));
                        details = " details=" + stringWriter.getBuffer();
                    } else {
                        details = " details=" + bundleModuleState.getDetails();
                    }
                }
                System.out.println(bundleInState.getBundleId() + " : " + bundleInState.getSymbolicName() + " v" + bundleInState.getHeaders().get("Implementation-Version") + dependsOn + details);
            }
        }
    }
}
