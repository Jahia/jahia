/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bundles.blueprint.extender.config;

import org.eclipse.gemini.blueprint.context.OsgiBundleApplicationContextExecutor;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleUtils;
import org.springframework.beans.BeansException;

/**
 * OsgiBundleXmlApplicationContext that does not start until jahia module is registered.
 */
public class JahiaOsgiBundleXmlApplicationContext extends OsgiBundleXmlApplicationContext {

    private OsgiBundleApplicationContextExecutor executor = new JahiaOsgiApplicationContextExecutor();

    public JahiaOsgiBundleXmlApplicationContext(String[] configLocations) {
        super(configLocations);
    }

    @Override
    protected void doClose() {
        executor.close();
    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        executor.refresh();
    }

    private class JahiaOsgiApplicationContextExecutor implements OsgiBundleApplicationContextExecutor {
        @Override
        public void refresh() throws BeansException, IllegalStateException {
            if (BundleUtils.isJahiaModuleBundle(getBundle())) {
                final ModuleState state = BundleUtils.getModule(getBundle()).getState();
                if (state != null && state.getState() != null && state.getState() == ModuleState.State.STARTED) {
                    // Module is already started by activator, start context now
                    BundleUtils.setContextToStartForModule(getBundle(), null);
                    JahiaOsgiBundleXmlApplicationContext.this.normalRefresh();
                } else {
                    // Delegate start to activator
                    BundleUtils.setContextToStartForModule(getBundle(), JahiaOsgiBundleXmlApplicationContext.this);
                }
            } else {
                // Standard bundle, start context now
                JahiaOsgiBundleXmlApplicationContext.this.normalRefresh();
            }
        }

        @Override
        public void close() {
            if (BundleUtils.isJahiaModuleBundle(getBundle())) {
                final ModuleState state = BundleUtils.getModule(getBundle()).getState();
                if (state != null && state.getState() != null && state.getState() == ModuleState.State.STOPPING) {
                    // Module is currently stopping,
                    JahiaOsgiBundleXmlApplicationContext.this.normalClose();
                } else {
                    // Reset contextToStart if module has never been registered in jahia
                    BundleUtils.setContextToStartForModule(getBundle(), null);
                }
            } else {
                JahiaOsgiBundleXmlApplicationContext.this.normalClose();
            }
        }
    }
}
