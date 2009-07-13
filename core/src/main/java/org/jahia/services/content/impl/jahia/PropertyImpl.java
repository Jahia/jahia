/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.jahia;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

/**
 * Represents a single JCR property.
 * @author Serge Huber
 * Date: 17 d�c. 2007
 * Time: 16:54:47
 */
public class PropertyImpl extends ItemImpl implements Property {

    protected Node node;
    protected Value[] values;
    protected ExtendedPropertyDefinition def;
    protected String name;
    protected boolean i18n;
    protected Locale locale;

    public PropertyImpl(SessionImpl session, Node node, ExtendedPropertyDefinition def, Locale locale, Value value) {
        this(session, node, def, locale, new Value[] {value});
    }

    public PropertyImpl(SessionImpl session, Node node, String name, ExtendedPropertyDefinition def, Locale locale, Value value) {
        this(session, node, name, def, locale, new Value[] {value});
    }

    public PropertyImpl(SessionImpl session, Node node, ExtendedPropertyDefinition def, Locale locale, Value[] values) {
        this(session, node, null, def, locale, values);
    }

    public PropertyImpl(SessionImpl session, Node node, String name, ExtendedPropertyDefinition def, Locale locale, Value[] values) {
        super(session);
        this.node = node;
        this.values = values;
        this.name = name;
        this.def = def;
        this.i18n = locale != null;
        this.locale = locale;
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, RepositoryException {
        setValue(new Value[] { value });
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(String s) throws ValueFormatException, VersionException, LockException, RepositoryException {
        setValue(new ValueImpl(s, PropertyType.STRING));
    }

    public void setValue(String[] strings) throws ValueFormatException,
            VersionException, LockException, RepositoryException {
        Value[] values = new Value[strings.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = new ValueImpl(strings[i], PropertyType.STRING);
        }
        setValue(values);
    }

    public void setValue(InputStream inputStream) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setValue(long l) throws ValueFormatException, VersionException, LockException, RepositoryException {
        setValue(new ValueImpl(l));
    }

    public void setValue(double v) throws ValueFormatException, VersionException, LockException, RepositoryException {
        setValue(new ValueImpl(v));
    }

    public void setValue(Calendar calendar) throws ValueFormatException, VersionException, LockException, RepositoryException {
        setValue(new ValueImpl(calendar));
    }

    public void setValue(boolean b) throws ValueFormatException, VersionException, LockException, RepositoryException {
        setValue(new ValueImpl(b));
    }

    public void setValue(Node node) throws ValueFormatException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Value getValue() throws ValueFormatException, RepositoryException {
        if (values.length != 1)
            throw new ValueFormatException();
        return values[0];
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return values;
    }

    public String getString() throws ValueFormatException, RepositoryException {
        return getValue().getString();
    }

    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return getValue().getStream();
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        return getValue().getLong();
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        return getValue().getDouble();
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return getValue().getDate();
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return getValue().getBoolean();
    }

    public Node getNode() throws ValueFormatException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public long getLength() throws ValueFormatException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public long[] getLengths() throws ValueFormatException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean isI18n() {
        return i18n;
    }

    public void setI18n(boolean i18n) {
        this.i18n = i18n;
    }

    public Locale getLocale() {
        return locale;
    }

    public PropertyDefinition getDefinition() throws RepositoryException {
        return def;
    }

    public int getType() throws RepositoryException {
        if (def != null) {
            return def.getRequiredType();
        }
        return 0;
    }

    public String getName() throws RepositoryException {
        if (name != null) {
            if (locale != null) {
                return name + "_"+locale;
            }
            return name;
        }
        if (def != null) {
            if (locale != null) {
                return def.getName() + "_"+locale;
            }
            return def.getName();
        }
        return null;
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return node;
    }

}
