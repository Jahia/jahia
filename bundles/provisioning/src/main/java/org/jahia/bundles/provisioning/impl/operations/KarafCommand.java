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
