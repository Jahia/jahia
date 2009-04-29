/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.definition;

import java.io.Serializable;

/**
 * Constants for GWT bean GWTJahiaNodeProperty.
 */
public class GWTJahiaNodePropertyType implements Serializable {
    
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

}
