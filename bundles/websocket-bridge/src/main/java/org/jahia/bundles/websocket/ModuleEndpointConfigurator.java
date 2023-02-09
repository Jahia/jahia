/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.websocket;

import org.osgi.framework.ServiceObjects;

import javax.websocket.Endpoint;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This allow the websocket instance for the given endpoint class
 */
public class ModuleEndpointConfigurator extends ServerEndpointConfig.Configurator {

    private final Set<ModuleEndpoint> endpoints = new HashSet<>();
    private final ServiceObjects<Endpoint> endpointServiceRef;
    private ServerEndpointConfig.Configurator customConfigurator;

    public ModuleEndpointConfigurator(ServiceObjects<Endpoint> endpointServiceRef, ServerEndpointConfig.Configurator customConfigurator) {
        this.endpointServiceRef = endpointServiceRef;
        this.customConfigurator = customConfigurator;
    }

    public void close() {
        Iterator<ModuleEndpoint> iterator = endpoints.iterator();

        while (iterator.hasNext()) {
            ModuleEndpoint moduleEndpoint = iterator.next();
            iterator.remove();
            moduleEndpoint.close();
        }
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) {
        ModuleEndpoint moduleEndpoint = new ModuleEndpoint(endpointServiceRef);
        endpoints.add(moduleEndpoint);
        return (T) moduleEndpoint;
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        if (customConfigurator != null) {
            customConfigurator.modifyHandshake(sec, request, response);
        }
    }

    @Override
    public String getNegotiatedSubprotocol(List<String> supported, List<String> requested) {
        if (customConfigurator != null) {
            return customConfigurator.getNegotiatedSubprotocol(supported, requested);
        }
        return super.getNegotiatedSubprotocol(supported, requested);
    }

    @Override
    public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested) {
        if (customConfigurator != null) {
            return customConfigurator.getNegotiatedExtensions(installed, requested);
        }
        return super.getNegotiatedExtensions(installed, requested);
    }

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        if (customConfigurator != null) {
            return customConfigurator.checkOrigin(originHeaderValue);
        }
        return super.checkOrigin(originHeaderValue);
    }
}
