package org.jahia.bundles.jcrcommands;

import org.apache.commons.lang.StringUtils;
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

import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

@Command(scope = "jcr", name = "prop-get")
@Service
public class PropGetCommand extends JCRCommandSupport implements Action {

    @Argument(description = "Name")
    @Completion(JCRPropCompleter.class)
    private String name;

    @Reference
    Session session;

    @Override
    public Object execute() throws Exception {
        final ShellTable table = new ShellTable();
        table.column(new Col("Name"));
        table.column(new Col("Type"));
        table.column(new Col("Value"));

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, getCurrentWorkspace(session), null, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
                JCRNodeWrapper n = jcrsession.getNode(getCurrentPath(session));
                PropertyIterator properties = name == null ? n.getProperties() : n.getProperties(name);
                while (properties.hasNext()) {
                    JCRPropertyWrapper next = (JCRPropertyWrapper) properties.nextProperty();
                    String value;
                    if (next.isMultiple()) {
                        List<String> l = new ArrayList<>();
                        for (JCRValueWrapper wrapper : next.getValues()) {
                            l.add(wrapper.getString());
                        }
                        value = StringUtils.join(l, ", ");
                    } else {
                        value = next.getValue().getString();
                    }
                    table.addRow().addContent(next.getName(), PropertyType.nameFromValue(next.getType()), value);
                }
                return null;
            }
        });

        table.print(System.out, true);

        return null;
    }
}
