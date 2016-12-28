package org.jahia.bundles.jcrcommands;

import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.jahia.services.content.*;

import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JCRPropCompleter extends JCRCommandSupport implements Completer {

    @Override
    public int complete(final Session session, CommandLine commandLine, List<String> candidates) {
        final Set<String> strings = new HashSet<String>();

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
                    JCRNodeWrapper n = jcrsession.getNode(getCurrentPath(session));
                    PropertyIterator props = n.getProperties();
                    while (props.hasNext()) {
                        JCRPropertyWrapper next = (JCRPropertyWrapper) props.nextProperty();
                        strings.add(next.getName());
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            System.err.print(e.getMessage());
        }
        return new StringsCompleter(strings).complete(session, commandLine, candidates);
    }

}
