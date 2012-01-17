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

package org.jahia.ajax.gwt.client.data.definition;

import java.io.Serializable;

/**
 * Constants for GWT bean GWTJahiaNodeProperty.
 */
public class GWTJahiaNodePropertyType implements Serializable {
    
    /** The serialVersionUID. */
    private static final long serialVersionUID = 668716928345969623L;

    /**
     * The <code>STRING</code> property type is used to store strings. It has
     * the same characteristics as the Java <code>String</code> class.
     */
    public static final int STRING = 1;

    /**
     * <code>BINARY</code> properties are used to store binary data.
     */
    public static final int BINARY = 2;

    /**
     * The <code>LONG</code> property type is used to store integers. It has
     * the same characteristics as the Java primitive type <code>long</code>.
     */
    public static final int LONG = 3;

    /**
     * The <code>DOUBLE</code> property type is used to store floating point
     * numbers. It has the same characteristics as the Java primitive type
     * <code>double</code>.
     */
    public static final int DOUBLE = 4;

    /**
     * The <code>DATE</code> property type is used to store time and date
     * information. See <i>4.2.6.1 Date</i> in the specification.
     */
    public static final int DATE = 5;

    /**
     * The <code>BOOLEAN</code> property type is used to store boolean values.
     * It has the same characteristics as the Java primitive type
     * <code>boolean</code>.
     */
    public static final int BOOLEAN = 6;

    /**
     * A <code>NAME</code> is a pairing of a namespace and a local name. When
     * read, the namespace is mapped to the current prefix. See
     * <i>4.2.6.2 Name</i> in the specification.
     */
    public static final int NAME = 7;

    /**
     * A <code>PATH</code> property is an ordered list of path elements. A path
     * element is a <code>NAME</code> with an optional index. When read, the
     * <code>NAME</code>s within the path are mapped to their current prefix.
     * A path may be absolute or relative. See <i>4.2.6.3 Path</i> in the
     * specification.
     */
    public static final int PATH = 8;

    /**
     * A <code>REFERENCE</code> property stores the identifier of a
     * referenceable node (one having type <code>mix:referenceable</code>),
     * which must exist within the same workspace or session as the
     * <code>REFERENCE</code> property. A <code>REFERENCE</code> property
     * enforces this referential integrity by preventing (in level 2
     * implementations) the removal of its target node. See
     * <i>4.2.6.4 Reference</i> in the specification.
     */
    public static final int REFERENCE = 9;

    /**
     * A <code>WEAKREFERENCE</code> property stores the identifier of a
     * referenceable node (one having type <code>mix:referenceable</code>).
     * A <code>WEAKREFERENCE</code> property does not enforce referential
     * integrity. See <i>4.2.6.5 Weak Reference</i> in the specification.
     *
     * @since JCR 2.0
     */
    public static final int WEAKREFERENCE = 10;

    /**
     * A <code>URI</code> property is identical to <code>STRING</code> property
     * except that it only accepts values that conform to the syntax of a
     * URI-reference as defined in RFC 3986. See also <i>4.2.6.6 URI</i> in the
     * specification.
     *
     * @since JCR 2.0
     */
    public static final int URI = 11;

    /**
     * The <code>DECIMAL</code> property type is used to store precise decimal
     * numbers. It has the same characteristics as the Java class
     * <code>java.math.BigDecimal</code>.
     *
     * @since JCR 2.0
     */
    public static final int DECIMAL = 12;

    /**
     * This constant can be used within a property definition (see
     * <i>4.7.5 Property Definitions</i>) to specify that the property in
     * question may be of any type. However, it cannot be the actual type of
     * any property instance. For example it will never be returned by
     * Property.getType and (in level 2 implementations) it cannot be
     * assigned as the type when creating a new property.
     */
    public static final int UNDEFINED = 0;

    public static final int ASYNC_UPLOAD = 20;

    public static final int PAGE_LINK = 21;

}
