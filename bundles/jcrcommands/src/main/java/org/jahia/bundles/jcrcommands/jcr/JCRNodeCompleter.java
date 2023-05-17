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
package org.jahia.bundles.jcrcommands.jcr;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.jahia.services.content.*;

import javax.jcr.RepositoryException;
import java.util.List;

/**
 * Complete JCR node name
 */
@Service
@SuppressWarnings({"java:S106","java:S1166"})
public class JCRNodeCompleter extends JCRCommandSupport implements Completer {

    @Override
    public int complete(final Session session, final CommandLine commandLine, final List<String> candidates) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, getCurrentWorkspace(session), null, jcrsession -> {
                String arg = commandLine.getCursorArgument();
                if (arg == null) {
                    arg = "";
                } else {
                    arg = arg.substring(0, commandLine.getArgumentPosition());
                }

                JCRNodeWrapper n = jcrsession.getNode(getCurrentPath(session));
                String prefix = "";
                if (arg.indexOf('/') > -1) {
                    prefix = StringUtils.substringBeforeLast(arg, "/") + "/";
                    if (prefix.startsWith("/")) {
                        n = jcrsession.getNode(prefix);
                    } else {
                        n = n.getNode(prefix);
                    }
                    arg = StringUtils.substringAfterLast(arg, "/");
                }
                JCRNodeIteratorWrapper nodes = n.getNodes();
                while (nodes.hasNext()) {
                    JCRNodeWrapper next = (JCRNodeWrapper) nodes.nextNode();
                    if (next.getName().startsWith(arg) && next.getName().indexOf(' ') == -1) {
                        candidates.add(prefix + next.getName()+"/");
                    }
                }
                return null;
            });
        } catch (RepositoryException e) {
            // ignore
        }
        return candidates.isEmpty() ? -1 : commandLine.getBufferPosition() - commandLine.getArgumentPosition();
    }

}
