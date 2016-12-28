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

@Command(scope = "jcr", name = "cd")
@Service
public class CdCommand extends JCRCommandSupport implements Action {

    @Argument(description = "Path")
    @Completion(JCRNodeCompleter.class)
    private String path;

    @Reference
    Session session;

    @Override
    public Object execute() throws Exception {

        if (path == null) {
            System.out.println(getCurrentPath(session));
            return null;
        }

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, getCurrentWorkspace(session), null, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
                String newPath = getNode(jcrsession ,path, session).getPath();
                setCurrentPath(session,newPath);
                System.out.println(newPath);
                return null;
            }
        });

        return null;
    }

}
