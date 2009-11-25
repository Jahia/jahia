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
package org.jahia.services.content;

import org.jahia.data.beans.CategoryBean;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Wrapper for javax.jcr.property to allow more data format.
 *
 * @author : toto
 */
public class JCRPropertyWrapperImpl extends JCRItemWrapperImpl implements JCRPropertyWrapper {

    private JCRNodeWrapper node;
    private Property property;
    private String name;
    private ExtendedPropertyDefinition def;

    public JCRPropertyWrapperImpl(JCRNodeWrapper objectNode, Property property, JCRSessionWrapper session, JCRStoreProvider provider, ExtendedPropertyDefinition def) {
        super(session, provider);
        this.node = objectNode;
        this.property = property;
        try {
            this.localPath = property.getPath();
            this.name = property.getName();
            this.def = def;
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public JCRPropertyWrapperImpl(JCRNodeWrapper objectNode, Property property, JCRSessionWrapper session, JCRStoreProvider provider, ExtendedPropertyDefinition def, String name) {
        super(session, provider);
        this.node = objectNode;
        this.property = property;
        this.name = name;
        this.localPath = node.getPath()+"/"+name;
        this.def = def;
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        property.setValue(value);
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.setValue(values);
    }

    public void setValue(String s) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.setValue(s);
    }

    public void setValue(String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.setValue(strings);
    }

    /**
     * @deprecated As of JCR 2.0, {@link #setValue(Binary)} should be used instead.
     */
    public void setValue(InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.setValue(inputStream);
    }

    public void setValue(long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.setValue(l);
    }

    public void setValue(double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.setValue(v);
    }

    public void setValue(Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.setValue(calendar);
    }

    public void setValue(boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.setValue(b);
    }

    public void setValue(Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.setValue(node);
    }

    public void setValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Value getValue() throws ValueFormatException, RepositoryException {
        return new JCRValueWrapperImpl(property.getValue(),getDefinition(), getSession());
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        Value[] values = property.getValues();
        Value[] wrappedValues = new Value[values.length];
        for (int i = 0; i < values.length; i++) {
            Value value = values[i];
            wrappedValues[i] = new JCRValueWrapperImpl(value,getDefinition(), getSession());
        }
        return wrappedValues;
    }

    public String getString() throws ValueFormatException, RepositoryException {
        return property.getString();
    }

    /**
     * @deprecated As of JCR 2.0, {@link #getBinary()} should be used instead.
     */
    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return property.getStream();
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        return property.getLong();
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        return property.getDouble();
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return property.getDate();
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return property.getBoolean();
    }

    public Node getNode() throws ValueFormatException, RepositoryException {
        return session.getNodeByUUID(property.getString());
    }

    public Node getReferencedNode() throws ValueFormatException, RepositoryException {
        return session.getNodeByUUID(property.getString());
    }

    public Binary getBinary() throws ValueFormatException, RepositoryException {
        return property.getBinary();
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return property.getDecimal();
    }

    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return session.getProperty(property.getString());
    }

    public long getLength() throws ValueFormatException, RepositoryException {
        return property.getLength();
    }

    public long[] getLengths() throws ValueFormatException, RepositoryException {
        return property.getLengths();
    }

    public ExtendedPropertyDefinition getDefinition() throws RepositoryException {
        return def;
    }

    public int getType() throws RepositoryException {
        return def.getRequiredType();
    }

    /**
     * @deprecated use getNode instead
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    public CategoryBean getCategory() throws ValueFormatException, RepositoryException {
        return ((JCRValueWrapperImpl) getValue()).getCategory();
    }

    public String getName() throws RepositoryException {
        return name;
    }

    public JCRItemWrapper getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return provider.getItemWrapper(property.getAncestor(i),session);
    }

    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return node;
    }

    public int getDepth() throws RepositoryException {
        return property.getDepth();
    }

    public boolean isNode() {
        return property.isNode();
    }

    public boolean isNew() {
        return property.isNew();
    }

    public boolean isModified() {
        return property.isModified();
    }

    public boolean isSame(Item item) throws RepositoryException {
        return property.isSame(item);
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        property.accept(itemVisitor);
    }

    /**
     * @deprecated As of JCR 2.0, {@link Session#save()} should
     * be used instead.
     */
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        property.save();
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        property.refresh(b);
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.remove();
    }

    public boolean isMultiple() throws RepositoryException {
        return def.isMultiple();
    }
}
