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

import org.apache.karaf.shell.api.action.*;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.services.content.*;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

/**
 * Execute JCR Query command
 */
@Command(scope = "jcr", name = "query")
@Service
@SuppressWarnings({"java:S106","java:S1166"})
public class QueryCommand extends JCRCommandSupport implements Action {

    public static final int DEFAULT_LIMIT = 50;
    @Argument(description = "Query")
    private String query;

    @Option(name = "-lang", description = "query language")
    @Completion(value=StringsCompleter.class , values = { Query.JCR_SQL2, Query.XPATH })
    private String language = Query.JCR_SQL2;

    @Option(name = "-l", description = "limit")
    private int limit = DEFAULT_LIMIT;

    @Reference
    Session session;

    @Override
    public Object execute() throws Exception {
        final ShellTable table = new ShellTable();
        table.column(new Col("Path"));
        table.column(new Col("UUID"));
        table.column(new Col("Type"));

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, getCurrentWorkspace(session), null, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
                Query q = jcrsession.getWorkspace().getQueryManager().createQuery(query, language);
                q.setLimit(limit);
                NodeIterator nodes = q.execute().getNodes();
                while (nodes.hasNext()) {
                    JCRNodeWrapper next = (JCRNodeWrapper) nodes.nextNode();
                    table.addRow().addContent(next.getPath(), next.getIdentifier(), next.getPrimaryNodeTypeName());
                }
                return null;
            }
        });

        table.print(System.out, true);

        return null;
    }
}
