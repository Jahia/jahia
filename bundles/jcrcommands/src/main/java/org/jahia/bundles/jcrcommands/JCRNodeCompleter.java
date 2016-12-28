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
