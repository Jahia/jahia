/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.nodetypes;

import java.io.InputStream;
import java.util.*;
import java.math.BigDecimal;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.Binary;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.ModuleVersion;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.content.nodetypes.initializers.I15dValueInitializer;
import org.jahia.services.content.nodetypes.initializers.ValueInitializer;

/**
 *
 * User: toto
 * Date: Apr 3, 2008
 * Time: 12:26:22 PM
 *
 */
public class DynamicValueImpl implements Value {

    private static final transient Logger logger = LoggerFactory.getLogger(DynamicValueImpl.class);

    private List<String> params;
    protected ExtendedPropertyDefinition declaringPropertyDefinition;
    private String fn;
    protected int type;

    public DynamicValueImpl(String fn, List<String> params, int type, boolean isConstraint, ExtendedPropertyDefinition declaringPropertyDefinition) {
        this.type = type;
        this.fn = fn;
        this.params = params;
        this.declaringPropertyDefinition = declaringPropertyDefinition;
    }

    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        return getExpandedValue().getString();
    }

    public InputStream getStream() throws IllegalStateException, RepositoryException {
        return getExpandedValue().getBinary().getStream();
    }

    public long getLong() throws IllegalStateException, RepositoryException {
        return getExpandedValue().getLong();
    }

    public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
        return getExpandedValue().getDouble();
    }

    public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
        return getExpandedValue().getDate();
    }

    public boolean getBoolean() throws ValueFormatException, IllegalStateException, RepositoryException {
        return getExpandedValue().getBoolean();
    }

    public Binary getBinary() throws RepositoryException {
        return getExpandedValue().getBinary();
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return getExpandedValue().getDecimal();
    }

    public int getType() {
        return type;
    }

    public String getFn() {
        return fn;
    }

    public List<String> getParams() {
        return params;
    }

    public Value[] expand() {
        return expand(null);
    }

    public Value[] expand(Locale locale) {
        Value[] v = null;
        String classname;
        if (fn.equals("useClass")) {
            classname = getParams().get(0);
        } else {
            classname = "org.jahia.services.content.nodetypes.initializers."+ StringUtils.capitalize(fn);
        }
        try {
            // Resolve class loader from the node type.
            JahiaTemplatesPackage definitionTemplatePackage = getDefinitionTemplatePackage();
            if (definitionTemplatePackage != null) {
                ValueInitializer init = (ValueInitializer) definitionTemplatePackage.getClassLoader().loadClass(classname).newInstance();
                if (init instanceof I15dValueInitializer) {
                    v = ((I15dValueInitializer) init).getValues(declaringPropertyDefinition, getParams(), locale);
                } else {
                    v = init.getValues(declaringPropertyDefinition, getParams());
                }
            } else {
                logger.error("Unable to resolve {} initializer because bundle owning definition cannot be found {} ", fn, declaringPropertyDefinition.getDeclaringNodeType().getSystemId());
            }
        } catch (Exception e) {
            // Show why it failed
            logger.error("Unable to resolve {} initializer because {} {} (set DynamicValueImpl in debug for more details)", fn, e.getClass().getName(), e.getMessage());
            logger.debug(e.getMessage(), e);
        }
        List<Value> res = new ArrayList<Value>();
        if (v != null) {
            for (int i = 0; i < v.length; i++) {
                Value value = v[i];
                if (value instanceof DynamicValueImpl) {
                    res.addAll(Arrays.asList(((DynamicValueImpl)value).expand(locale)));
                } else {
                    res.add(value);
                }
            }
        }
        return res.toArray(new Value[res.size()]);
    }

    private JahiaTemplatesPackage getDefinitionTemplatePackage() {
        JahiaTemplatesPackage definitionTemplatePackage = declaringPropertyDefinition.getDeclaringNodeType().getTemplatePackage();
        if (definitionTemplatePackage == null) {
            Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> allModuleVersions = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry().getAllModuleVersions();
            String systemId = declaringPropertyDefinition.getDeclaringNodeType().getSystemId();
            for (JahiaTemplatesPackage jahiaTemplatesPackage : allModuleVersions.get(systemId).values()) {
                if (jahiaTemplatesPackage.getBundle().getState() >= Bundle.RESOLVED) {
                    return jahiaTemplatesPackage;
                }
            }
        }
        return definitionTemplatePackage;
    }

    private Value getExpandedValue() throws ValueFormatException {
        Value[] v = expand(null);
        if (v.length == 1) {
            return v[0];
        } else {
            throw new ValueFormatException("Dynamic value expanded to none/multiple values : "+v.length );
        }
    }
}
