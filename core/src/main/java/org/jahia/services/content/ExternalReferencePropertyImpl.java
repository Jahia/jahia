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

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * A property implementation that point to external repository nodes.
 *
 * @author loom
 *         Date: Aug 25, 2010
 *         Time: 3:14:08 PM
 */
public class ExternalReferencePropertyImpl implements Property {

    private String name;
    private String path;
    private Node referencedNode;
    private Node parentNode;
    private Value value;
    private Value[] values;
    private String nodeIdentifier;
    private Session session;
    private ExtendedPropertyDefinition epd;

    public ExternalReferencePropertyImpl(String name, ExtendedPropertyDefinition epd, Node parentNode, Session session, final String nodeIdentifier, Node referencedNode) throws RepositoryException {
        this.name = name;
        this.epd = epd;
        this.path = parentNode.getPath() + "/" + name;
        this.parentNode = parentNode;
        this.session = session;
        this.nodeIdentifier = nodeIdentifier;
        this.value = new ExternalReferenceValue(nodeIdentifier, PropertyType.WEAKREFERENCE);
        this.referencedNode = referencedNode;
    }

    public ExternalReferencePropertyImpl(String name, ExtendedPropertyDefinition epd, Node parentNode, Session session, Value[] values) throws RepositoryException {
        this.name = name;
        this.epd = epd;
        this.path = parentNode.getPath() + "/" + name;
        this.parentNode = parentNode;
        this.session = session;
        this.values = values;
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        this.value = value;
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        this.values = values;
    }

    public void setValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void setValue(String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void setValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void setValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void setValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void setValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void setValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void setValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void setValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public Value getValue() throws ValueFormatException, RepositoryException {
        return value;
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return values;
    }

    public String getString() throws ValueFormatException, RepositoryException {
        return value.getString();
    }

    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return null;
    }

    public Binary getBinary() throws ValueFormatException, RepositoryException {
        return null;
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        return 0;
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        return 0;
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return null;
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return null;
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return false;
    }

    public Node getNode() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return referencedNode;
    }

    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return this;
    }

    public long getLength() throws ValueFormatException, RepositoryException {
        return 0;
    }

    public long[] getLengths() throws ValueFormatException, RepositoryException {
        return new long[0];
    }

    public PropertyDefinition getDefinition() throws RepositoryException {
        return epd;
    }

    public int getType() throws RepositoryException {
        return value.getType();
    }

    public boolean isMultiple() throws RepositoryException {
        return epd.isMultiple();
    }

    public String getPath() throws RepositoryException {
        return path;
    }

    public String getName() throws RepositoryException {
        return name;
    }

    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return null;
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return parentNode;
    }

    public int getDepth() throws RepositoryException {
        return 0;
    }

    public Session getSession() throws RepositoryException {
        return session;
    }

    public boolean isNode() {
        return false;
    }

    public boolean isNew() {
        return false;
    }

    public boolean isModified() {
        return false;
    }

    public boolean isSame(Item otherItem) throws RepositoryException {
        return false;
    }

    public void accept(ItemVisitor visitor) throws RepositoryException {
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
    }

    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
    }
}
