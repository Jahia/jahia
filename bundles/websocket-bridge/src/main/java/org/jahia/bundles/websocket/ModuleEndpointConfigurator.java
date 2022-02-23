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