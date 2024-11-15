/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jcrcommands.executor.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.jahia.bundles.jcrcommands.executor.KarafCommandExecutor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.security.auth.Subject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.concurrent.*;

import static org.apache.karaf.shell.support.ansi.SimpleAnsi.*;

/**
 * Execute a Karaf Command
 */
@Component(service = KarafCommandExecutor.class, immediate = true)
public class KarafCommandExecutorImpl implements KarafCommandExecutor {
    private static final String[] CONSOLE_CONTROL_STRINGS = new String[]{COLOR_CYAN, COLOR_DEFAULT, COLOR_RED, INTENSITY_BOLD, INTENSITY_NORMAL};
    private static final String[] CONSOLE_CONTROL_STRINGS_REPLACEMENT = new String[CONSOLE_CONTROL_STRINGS.length];

    private static final Long SERVICE_TIMEOUT = 5000L;
    public static final int SLEEP_TIME = 100;

    static {
        Arrays.fill(CONSOLE_CONTROL_STRINGS_REPLACEMENT, StringUtils.EMPTY);
    }

    private static String cleanupOutput(String output) {
        return StringUtils.replaceEach(output, CONSOLE_CONTROL_STRINGS, CONSOLE_CONTROL_STRINGS_REPLACEMENT);
    }

    private SessionFactory sessionFactory;

    private ExecutorService executor;

    @Reference
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    /**
     * Activate
     */
    @Activate
    public void activate() {
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * Deactivate
     */
    @Deactivate
    public void deactivate() {
        executor.shutdown();
    }

    private void waitForCommandService(String command) throws InterruptedException {
        // the commands are represented by services. Due to the asynchronous nature of services they may not be
        // immediately available. This code waits the services to be available, in their secured form. It
        // means that the code waits for the command service to appear with the roles defined.

        if (command == null || command.length() == 0) {
            return;
        }

        int spaceIdx = command.indexOf(' ');
        if (spaceIdx > 0) {
            command = command.substring(0, spaceIdx);
        }
        int colonIndx = command.indexOf(':');
        String scope = (colonIndx > 0) ? command.substring(0, colonIndx) : "*";
        String name = (colonIndx > 0) ? command.substring(colonIndx + 1) : command;
        long start = System.currentTimeMillis();
        long cur = start;
        while (cur - start < SERVICE_TIMEOUT) {
            if (sessionFactory.getRegistry().getCommand(scope, name) != null) {
                return;
            }
            Thread.sleep(SLEEP_TIME);
            cur = System.currentTimeMillis();
        }
    }

    public String executeCommand(String command, final Long timeout, final Principal... principals) throws InterruptedException,
            TimeoutException, ExecutionException {

        if (command != null && command.startsWith("dx:")) {
            // workaround to alias "jahia" scope with "dx" since aliases are not available from here
            // see BACKLOG-10563
            command = command.replace("dx:", "jahia:");
        }

        return execute(command, timeout, principals);
    }

    private String execute(final String command, final Long timeout, final Principal... principals) throws InterruptedException, TimeoutException, ExecutionException {
        waitForCommandService(command);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(byteArrayOutputStream, false, StandardCharsets.UTF_8.name());
             PrintStream errPrintStream = new PrintStream(byteArrayOutputStream, false, StandardCharsets.UTF_8.name());
             ByteArrayInputStream in = new ByteArrayInputStream(new byte[1]);
             Session session = sessionFactory.create(in, printStream, errPrintStream)) {
            final Callable<String> commandCallable = () -> {
                Object result = session.execute(command);
                if (result != null) {
                    session.getConsole().println(result.toString());
                }
                printStream.flush();
                return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
            };

            FutureTask<String> commandFuture;
            if (principals.length == 0) {
                commandFuture = new FutureTask<>(commandCallable);
            } else {
                // If principals are defined, run the command callable via Subject.doAs()
                commandFuture = new FutureTask<>(() -> {
                    Subject subject = new Subject();
                    subject.getPrincipals().addAll(Arrays.asList(principals));
                    return Subject.doAs(subject, (PrivilegedExceptionAction<String>) commandCallable::call);
                });
            }

            executor.submit(commandFuture);

            return cleanupOutput(commandFuture.get(timeout, TimeUnit.MILLISECONDS));
        } catch (IOException e) {
            throw new ExecutionException("IO Exception", e);
        }
    }

}
