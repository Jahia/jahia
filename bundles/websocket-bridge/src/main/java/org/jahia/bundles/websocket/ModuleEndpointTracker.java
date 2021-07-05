/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.websocket;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component(immediate = true, service = {ModuleEndpointTracker.class})
public class ModuleEndpointTracker {

    private static final Logger logger = LoggerFactory.getLogger(ModuleEndpointTracker.class);

    // In memory list of registered endpoints
    private final ConcurrentMap<String, ModuleServerEndpointConfig> moduleServerEndpointConfigs = new ConcurrentHashMap<>();

    // The service tracker to listener on endpoints services provided by other modules
    private ServiceTracker<Endpoint, ModuleServerEndpointConfig> serverEndpointConfigWrapperServiceTracker;

    @Activate
    protected void activate(final BundleContext bundleContext) {
        ServletContext servletContext = JahiaContextLoaderListener.getServletContext();
        ServerContainer serverContainer = (ServerContainer) servletContext.getAttribute(ServerContainer.class.getName());

        if (serverContainer == null) {
            logger.warn("No WebSocket server container available");
            return;
        }

        serverEndpointConfigWrapperServiceTracker = new ServiceTracker<>(bundleContext, Endpoint.class,
                new ServiceTrackerCustomizer<Endpoint, ModuleServerEndpointConfig>() {

                    @Override
                    public ModuleServerEndpointConfig addingService(ServiceReference<Endpoint> serviceReference) {

                        final ServiceObjects<Endpoint> endpointServiceRef = bundleContext.getServiceObjects(serviceReference);
                        ServerEndpoint serverEndpoint = endpointServiceRef.getService().getClass().getAnnotation(ServerEndpoint.class);

                        // no config on the EndPoint available.
                        if (serverEndpoint == null) {
                            return null;
                        }

                        // read @ServerEndPoint infos
                        String path = "/modules" + serverEndpoint.value();
                        List<Class<? extends Decoder>> decoders = Arrays.asList(serverEndpoint.decoders());
                        List<Class<? extends Encoder>> encoders = Arrays.asList(serverEndpoint.encoders());
                        List<String> subprotocol = Arrays.asList(serverEndpoint.subprotocols());

                        ModuleServerEndpointConfig moduleServerEndpointConfig = moduleServerEndpointConfigs.get(path);

                        // it's a new endpoint
                        if (moduleServerEndpointConfig == null) {
                            moduleServerEndpointConfig = new ModuleServerEndpointConfig(path, decoders, encoders, subprotocol);
                            moduleServerEndpointConfig.setConfigurator(new ModuleEndpointConfigurator(endpointServiceRef));

                            try {
                                serverContainer.addEndpoint(moduleServerEndpointConfig);
                            } catch (DeploymentException e) {
                                logger.error("Unable to register module WebSocket endpoint for path {}", path, e);
                                return null;
                            }

                            moduleServerEndpointConfigs.put(path, moduleServerEndpointConfig);
                        } else {
                            // it's an endpoint already known
                            ServerEndpointConfig.Configurator configurator = moduleServerEndpointConfig.getConfigurator();
                            if (configurator.getClass().equals(ModuleServerEndpointConfig.UnregisteredEndpointConfigurator.class)) {
                                moduleServerEndpointConfig.update(decoders, encoders, subprotocol);
                            }
                            moduleServerEndpointConfig.setConfigurator(new ModuleEndpointConfigurator(endpointServiceRef));
                        }

                        return moduleServerEndpointConfig;
                    }

                    @Override
                    public void modifiedService(ServiceReference<Endpoint> serviceReference, ModuleServerEndpointConfig serverEndpointConfigWrapper) {
                        removedService(serviceReference, serverEndpointConfigWrapper);
                        addingService(serviceReference);
                    }

                    @Override
                    public void removedService(ServiceReference<Endpoint> serviceReference, ModuleServerEndpointConfig serverEndpointConfigWrapper) {
                        serverEndpointConfigWrapper.removeConfigurator();
                    }
                });

        serverEndpointConfigWrapperServiceTracker.open();
    }

    @Deactivate
    protected void deactivate() {
        if (serverEndpointConfigWrapperServiceTracker != null) {
            serverEndpointConfigWrapperServiceTracker.close();
        }
    }
}