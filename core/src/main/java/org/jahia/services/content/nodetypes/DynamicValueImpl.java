/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
import org.slf4j.Logger;
import org.jahia.services.content.nodetypes.initializers.ValueInitializer;

/**
 * 
 * User: toto
 * Date: Apr 3, 2008
 * Time: 12:26:22 PM
 * 
 */
public class DynamicValueImpl implements Value {
    
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(DynamicValueImpl.class);
    
    private List<String> params;
    protected ExtendedPropertyDefinition declaringPropertyDefinition;
    private String fn;
    protected int type;
    private boolean isConstraint;

    public DynamicValueImpl(String fn, List<String> params, int type, boolean isConstraint, ExtendedPropertyDefinition declaringPropertyDefinition) {
        this.type = type;
        this.fn = fn;
        this.params = params;
        this.isConstraint = isConstraint;
        this.declaringPropertyDefinition = declaringPropertyDefinition;
    }

    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        return getExpandedValue().getString();
    }

    public InputStream getStream() throws IllegalStateException, RepositoryException {
        return getExpandedValue().getStream();
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

//    public String getString() {
//        if (fn.equals("now")) {
//            return ISO8601.format(new GregorianCalendar());
//        } else if (fn.equals("resourceKey")) {
//            String bundleId = ((ExtendedNodeType) declaringPropertyDefinition.getDeclaringNodeType()).getResourceBundleId();
//            return ResourceBundleMarker.drawMarker(bundleId,declaringPropertyDefinition.getResourceBundleKey()+"."+getParams().get(0),getParams().get(0));
//        } else if (fn.equals("useClass")) {
//            String classname = getParams().get(0);
//            try {
//                ValueInitializer init = (ValueInitializer) Class.forName(classname).newInstance();
//                return init.getValue(Jahia.getThreadParamBean());
//            } catch (InstantiationException e) {
//                logger.error(e.getMessage(), e);
//            } catch (IllegalAccessException e) {
//                logger.error(e.getMessage(), e);
//            } catch (ClassNotFoundException e) {
//                logger.error(e.getMessage(), e);
//            }
//        }
//        return null;
//    }

    public Value[] expand() {
        Value[] v = null;
        String classname;
        if (fn.equals("useClass")) {
            classname = getParams().get(0);
        } else {
            classname = "org.jahia.services.content.nodetypes.initializers."+ StringUtils.capitalize(fn);
        }
        try {
            ValueInitializer init = (ValueInitializer) Class.forName(classname).newInstance();
            v = init.getValues(declaringPropertyDefinition, getParams());
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        List<Value> res = new ArrayList<Value>();
        if (v != null) {
            for (int i = 0; i < v.length; i++) {
                Value value = v[i];
                if (value instanceof DynamicValueImpl) {
                    res.addAll(Arrays.asList(((DynamicValueImpl)value).expand()));
                } else {
                    res.add(value);
                }
            }
        }
        return res.toArray(new Value[res.size()]);
    }

    private Value getExpandedValue() throws ValueFormatException {
        Value[] v = expand();
        if (v.length == 1) {
            return v[0];
        } else {
            throw new ValueFormatException("Dynamic value expanded to none/multiple values : "+v.length );
        }
    }
}
