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
package org.jahia.bundles.jcrcommands.jcr;

import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.jahia.services.content.*;

import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Complete JCR property name
 */
@Service
@SuppressWarnings({"java:S106","java:S1166"})
public class JCRPropCompleter extends JCRCommandSupport implements Completer {

    @Override
    public int complete(final Session session, CommandLine commandLine, List<String> candidates) {
        final Set<String> strings = new HashSet<>();

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(jcrsession -> {
                JCRNodeWrapper n = jcrsession.getNode(getCurrentPath(session));
                PropertyIterator props = n.getProperties();
                while (props.hasNext()) {
                    JCRPropertyWrapper next = (JCRPropertyWrapper) props.nextProperty();
                    strings.add(next.getName());
                }
                return null;
            });
        } catch (RepositoryException e) {
            System.err.print(e.getMessage());
        }
        return new StringsCompleter(strings).complete(session, commandLine, candidates);
    }

}
