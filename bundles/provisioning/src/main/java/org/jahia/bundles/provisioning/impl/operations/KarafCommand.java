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
package org.jahia.bundles.provisioning.impl.operations;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.jahia.bundles.jcrcommands.executor.KarafCommandExecutor;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Karaf command
 */
@Component(service = Operation.class, property = "type=karafCommand")
public class KarafCommand implements Operation {
    public static final String KARAF_COMMAND = "karafCommand";
    public static final String KARAF_TIMEOUT = "timeout";
    public static final long DEFAULT_TIMEOUT = 1000L;
    private static final Logger logger = LoggerFactory.getLogger(KarafCommand.class);
    private KarafCommandExecutor commandExecutor;

    @Reference
    public void setCommandExecutor(KarafCommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.get(KARAF_COMMAND) instanceof String;
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        try {
            long timeout = entry.containsKey(KARAF_TIMEOUT) ? Long.parseLong((String) entry.get(KARAF_TIMEOUT)) : DEFAULT_TIMEOUT;
            String s = commandExecutor.executeCommand((String) entry.get(KARAF_COMMAND), timeout, new RolePrincipal("manager"), new RolePrincipal("admin"));
            logger.info("Karaf command result : {}", s);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            logger.error("Cannot execute command {}", entry.get(KARAF_COMMAND), e);
        }
    }
}
