/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jcrcommands;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.jahia.services.content.*;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JCRNodeCompleter extends JCRCommandSupport implements Completer {

    @Override
    public int complete(final Session session, final CommandLine commandLine, final List<String> candidates) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, getCurrentWorkspace(session), null, new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
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
                        if (next.getName().startsWith(arg)) {
                            candidates.add(prefix + next.getName()+"/");
                        }
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            // ignore
        }
        return candidates.isEmpty() ? -1 : commandLine.getBufferPosition() - commandLine.getArgumentPosition();
    }

}
