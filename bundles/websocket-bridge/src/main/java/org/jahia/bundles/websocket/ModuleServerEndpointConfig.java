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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class ModuleServerEndpointConfig implements ServerEndpointConfig {

    private static final Logger logger = LoggerFactory.getLogger(ModuleServerEndpointConfig.class);
    // An empty endpoint that is used in case the module endpoint service is unregistered
    private final UnregisteredEndpointConfigurator unregisteredEndpointConfigurator = new UnregisteredEndpointConfigurator();
    // The module endpoint
    private ModuleEndpointConfigurator moduleEndpointConfigurator = null;
    // The config
    private ServerEndpointConfig serverEndpointConfig;

    public ModuleServerEndpointConfig(String path, List<Class<? extends Decoder>> decoders, List<Class<? extends Encoder>> encoders, List<String> subprotocols) {
        buildConfig(path, decoders, encoders, subprotocols);
    }

    private void buildConfig(String path, List<Class<? extends Decoder>> decoders, List<Class<? extends Encoder>> encoders, List<String> subprotocols) {
        Builder builder = Builder.create(Endpoint.class, path);
        builder.decoders(decoders);
        builder.encoders(encoders);
        builder.subprotocols(subprotocols);
        serverEndpointConfig = builder.build();
    }

    /**
     * This allow to update the current server endpoint with new configs
     *
     * @param decoders     the decoders
     * @param encoders     the encoders
     * @param subprotocols the subprotocols
     */
    public void update(List<Class<? extends Decoder>> decoders, List<Class<? extends Encoder>> encoders, List<String> subprotocols) {
        buildConfig(serverEndpointConfig.getPath(), decoders, encoders, subprotocols);
    }

    public void removeConfigurator() {
        moduleEndpointConfigurator.close();
        moduleEndpointConfigurator = null;
    }

    @Override
    public Configurator getConfigurator() {
        if (moduleEndpointConfigurator == null) {
            return unregisteredEndpointConfigurator;
        }

        return moduleEndpointConfigurator;
    }

    public void setConfigurator(ModuleEndpointConfigurator moduleEndpointConfigurator) {
        this.moduleEndpointConfigurator = moduleEndpointConfigurator;
    }

    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return serverEndpointConfig.getDecoders();
    }

    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return serverEndpointConfig.getEncoders();
    }

    @Override
    public Class<?> getEndpointClass() {
        return serverEndpointConfig.getEndpointClass();
    }

    @Override
    public List<Extension> getExtensions() {
        return serverEndpointConfig.getExtensions();
    }

    @Override
    public String getPath() {
        return serverEndpointConfig.getPath();
    }

    @Override
    public List<String> getSubprotocols() {
        return serverEndpointConfig.getSubprotocols();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return serverEndpointConfig.getUserProperties();
    }

    /**
     * This is a closed End point implem, unfortunately it's not possible to unregister an endPoint on the ServerContainer,
     * so we replace it with this endpoint.
     */
    public static final class UnregisteredEndpointConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) {
            return (T) new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    try {
                        session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Service is unregistered"));
                    } catch (IOException e) {
                        logger.error("Unable to close the session", e);
                    }
                }
            };
        }
    }
}