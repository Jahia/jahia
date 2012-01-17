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

package org.jahia.services.content.impl.vfs;

import javax.jcr.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * 
 * User: loom
 * Date: Aug 12, 2010
 * Time: 3:04:33 PM
 * 
 */
public class VFSValueImpl implements Value {

    Object value;
    int type;

    public VFSValueImpl(String value) {
        this(value, PropertyType.STRING);
    }

    public VFSValueImpl(Binary value) {
        this(value, PropertyType.BINARY);
    }

    public VFSValueImpl(long value) {
        this(new Long(value), PropertyType.LONG);
    }

    public VFSValueImpl(double value) {
        this(new Double(value), PropertyType.DOUBLE);
    }

    public VFSValueImpl(BigDecimal value) {
        this(value, PropertyType.DECIMAL);
    }

    public VFSValueImpl(Calendar value) {
        this(value, PropertyType.DATE);
    }

    public VFSValueImpl(boolean value) {
        this(new Boolean(value), PropertyType.BOOLEAN);
    }

    public VFSValueImpl(Node value, boolean weakReference) throws RepositoryException {
        if (weakReference) {
            this.value = value.getIdentifier();
            this.type = PropertyType.WEAKREFERENCE;
        } else {
            this.value = value.getIdentifier();
            this.type = PropertyType.REFERENCE;
        }
    }

    public VFSValueImpl(Object value, int type) {
        this.value = value;
        this.type = type;
    }

    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        return value.toString();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public InputStream getStream() throws RepositoryException {
        return (InputStream) value;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Binary getBinary() throws RepositoryException {
        return (Binary) value;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        return ((Long) value).longValue();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        return ((Double) value).doubleValue();
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return (BigDecimal) value;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return (Calendar) value;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return ((Boolean) value).booleanValue();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getType() {
        return type;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
