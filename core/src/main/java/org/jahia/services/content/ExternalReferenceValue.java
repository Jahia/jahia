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

package org.jahia.services.content;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Special case of a REFERENCE or WEAKREFERENCE value that can store a String identifier that's not necessarily a
 * UUID.
 *
 * @author loom
 *         Date: Sep 1, 2010
 *         Time: 3:26:42 PM
 */
public class ExternalReferenceValue implements Value {

    private final int type;
    private final String identifier;

    public ExternalReferenceValue(String identifier, int type) {
        this.identifier = identifier;
        this.type = type;
    }

    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        return identifier;
    }

    public InputStream getStream() throws RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to stream !");
    }

    public Binary getBinary() throws RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to binary !");
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to long !");
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to double !");
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to decimal !");
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to date !");
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        throw new ValueFormatException("Cannot convert external reference type to boolean !");
    }

    public int getType() {
        return type;
    }
}
