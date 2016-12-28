package org.jahia.bundles.jcrcommands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;

@Command(scope = "jcr", name = "workspace")
@Service
public class WorkspaceCommand extends JCRCommandSupport implements Action {

    @Argument(description = "Workspace")
    @Completion(value=StringsCompleter.class , values = { "default", "live" })
    private String ws;

    @Reference
    Session session;

    @Override
    public Object execute() throws Exception {
        if (ws != null) {
            if (ws.equals("default") || ws.equals("live")) {
                setCurrentWorkspace(session, ws);
            } else {
                System.err.println("Please select default or live");
            }
        }
        System.out.println("Current workspace : " + getCurrentWorkspace(session));

        return null;
    }

}
