/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.api.action.*;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.JCRValueWrapper;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.List;

/**
 * Set property command
 */
@Command(scope = "jcr", name = "prop-set")
@Service
@SuppressWarnings({"java:S106","java:S1166"})
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

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, getCurrentWorkspace(session), null, jcrsession -> {
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
        });
        table.print(System.out, true);

        return null;
    }
}
