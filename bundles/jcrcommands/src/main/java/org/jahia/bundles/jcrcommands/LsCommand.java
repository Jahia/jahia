package org.jahia.bundles.jcrcommands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.services.content.*;

import javax.jcr.RepositoryException;

@Command(scope = "jcr", name = "l")
@Service
public class LsCommand extends JCRCommandSupport implements Action {

    @Argument(description = "Path")
    @Completion(JCRNodeCompleter.class)
    private String path;

    @Reference
    Session session;

    @Override
    public Object execute() throws Exception {
        final ShellTable table = new ShellTable();
        table.column(new Col("Name"));
        table.column(new Col("UUID"));
        table.column(new Col("Type"));

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, getCurrentWorkspace(session), null, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
                JCRNodeWrapper n = getNode(jcrsession, path, session);
                JCRNodeIteratorWrapper nodes = n.getNodes();
                while (nodes.hasNext()) {
                    JCRNodeWrapper next = (JCRNodeWrapper) nodes.nextNode();
                    table.addRow().addContent(next.getName(), next.getIdentifier(), next.getPrimaryNodeTypeName());
                }
                return null;
            }
        });

        table.print(System.out, true);

        return null;
    }
}
