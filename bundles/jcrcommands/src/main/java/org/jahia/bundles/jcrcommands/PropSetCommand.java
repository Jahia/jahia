package org.jahia.bundles.jcrcommands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.jahia.services.content.*;

import javax.jcr.RepositoryException;

@Command(scope = "jcr", name = "prop-set")
@Service
public class PropSetCommand extends JCRCommandSupport implements Action {

    @Argument(description = "Name")
    @Completion(JCRPropCompleter.class)
    private String name;

    @Argument(description = "Value", index = 1)
    private String value;

    @Reference
    Session session;

    @Override
    public Object execute() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, getCurrentWorkspace(session), null, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
                JCRNodeWrapper n = jcrsession.getNode(getCurrentPath(session));
                n.setProperty(name, value);
                jcrsession.save();
                return null;
            }
        });

        return null;
    }
}
