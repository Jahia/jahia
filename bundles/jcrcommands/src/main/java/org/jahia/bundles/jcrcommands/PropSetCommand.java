/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.bundles.jcrcommands;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.api.action.*;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.services.content.*;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.List;

@Command(scope = "jcr", name = "prop-set")
@Service
public class PropSetCommand extends JCRCommandSupport implements Action {

    @Argument(description = "Name")
    @Completion(JCRPropCompleter.class)
    private String name;

    @Argument(description = "Value", index = 1, multiValued = true)
    private List<String> valuesAsString;

    @Option(name = "-multiple", required = false, multiValued = false)
    private boolean multiple;

    @Option(name = "-op", required = false, multiValued = false)
    @Completion(value=StringsCompleter.class , values = { "add", "replace", "remove" })
    private String multipleOp = "add";

    @Option(name = "-type", required = false, multiValued = false)
    @Completion(value=StringsCompleter.class , values = {
            PropertyType.TYPENAME_STRING,
            PropertyType.TYPENAME_BINARY,
            PropertyType.TYPENAME_LONG ,
            PropertyType.TYPENAME_DOUBLE,
            PropertyType.TYPENAME_DECIMAL,
            PropertyType.TYPENAME_DATE ,
            PropertyType.TYPENAME_BOOLEAN,
            PropertyType.TYPENAME_NAME ,
            PropertyType.TYPENAME_PATH,
            PropertyType.TYPENAME_REFERENCE,
            PropertyType.TYPENAME_WEAKREFERENCE ,
            PropertyType.TYPENAME_URI,
            PropertyType.TYPENAME_UNDEFINED})
    private String type = PropertyType.TYPENAME_UNDEFINED;

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
            public Object doInJCR(final JCRSessionWrapper jcrsession) throws RepositoryException {
                JCRNodeWrapper n = jcrsession.getNode(getCurrentPath(session));
                final int typeAsInt = PropertyType.valueFromName(type);

                List<Value> values = new ArrayList<>(valuesAsString.size());
                for (String input : valuesAsString) {
                    if (typeAsInt != PropertyType.UNDEFINED) {
                        values.add(jcrsession.getValueFactory().createValue(input, typeAsInt));
                    } else {
                        values.add(jcrsession.getValueFactory().createValue(input));
                    }
                }
                String output = null;
                JCRPropertyWrapper property = null;
                if (n.hasProperty(name)) {
                    property = n.getProperty(name);
                }

                if (multiple) {
                    if (multipleOp.equals("remove")) {
                        if (property != null) {
                            for (JCRValueWrapper v : n.getProperty(name).getValues()) {
                                if (valuesAsString.contains(v.getString())) {
                                    n.getProperty(name).removeValue(v);
                                }
                            }
                        }
                    } else if (multipleOp.equals("replace") || property == null) {
                        property = n.setProperty(name, values.toArray(new Value[values.size()]));
                    } else if (multipleOp.equals("add")) {
                        property = n.getProperty(name);
                        for (Value s : values) {
                            property.addValue(s);
                        }
                    }
                    if (property != null) {
                        List<String> l = new ArrayList<>();
                        for (JCRValueWrapper wrapper : property.getValues()) {
                            l.add(wrapper.getString());
                        }
                        output = StringUtils.join(l, ", ");
                    }
                } else {
                    if (values.size() == 1) {
                        property = n.setProperty(name, values.iterator().next());
                        output = property.getValue().getString();
                    }
                }

                if (property != null && output != null) {
                    table.addRow().addContent(name, PropertyType.nameFromValue(property.getType()), output);
                }

                jcrsession.save();
                return null;
            }
        });
        table.print(System.out, true);

        return null;
    }
}
