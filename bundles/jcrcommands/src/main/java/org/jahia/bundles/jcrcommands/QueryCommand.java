package org.jahia.bundles.jcrcommands;

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

@Command(scope = "jcr", name = "query")
@Service
public class QueryCommand extends JCRCommandSupport implements Action {

    @Argument(description = "Query")
    private String query;

    @Option(name = "-lang", description = "query language")
    @Completion(value=StringsCompleter.class , values = { Query.JCR_SQL2, Query.XPATH })
    private String language = Query.JCR_SQL2;

    @Option(name = "-l", description = "limit")
    private int limit = 50;

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
