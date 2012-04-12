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

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.value.AbstractValueFactory;
import org.jahia.api.Constants;

/**
 * Jahia's extension of the value factory, which takes care that references are always created as 
 * weakrerences, unless one is using the method createValue(Node, false)   
 * 
 * @author Benjamin Papez
 *
 */
public class JCRValueFactoryImpl extends AbstractValueFactory {

    private static final ValueFactory valueFactory = new JCRValueFactoryImpl();

    /**
     * Constructs a <code>ValueFactory</code> object.
     */
    protected JCRValueFactoryImpl() {
    }

    /**
     *
     */
    public static ValueFactory getInstance() {
        return valueFactory;
    }

    /**
     * @see AbstractValueFactory#checkPathFormat(String)
     */
    protected void checkPathFormat(String pathValue) throws ValueFormatException {
        // ignore
    }

    /**
     * @see AbstractValueFactory#checkNameFormat(String)
     */
    protected void checkNameFormat(String nameValue) throws ValueFormatException {
        // ignore
    }

    @Override
    public Value createValue(Node value) throws RepositoryException {
        // always generate weakreferences on default
        return super.createValue(value, value.isNodeType(Constants.NT_VERSION) ? false : true);
    }

    @Override
    public Value createValue(String value, int type)
            throws ValueFormatException {
        return super.createValue(value, type != PropertyType.REFERENCE ? type
                : PropertyType.WEAKREFERENCE);
    }
}
