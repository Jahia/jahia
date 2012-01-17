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

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.ValueHelper;
import org.jahia.data.beans.CategoryBean;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
        setItem(property);
        if (property != null) {
            try {
                this.localPath = property.getPath();
                this.localPathInProvider = localPath;
                this.name = property.getName();
                this.def = def;
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }

    public JCRPropertyWrapperImpl(JCRNodeWrapper objectNode, Property property, JCRSessionWrapper session, JCRStoreProvider provider, ExtendedPropertyDefinition def, String name) {
        super(session, provider);
        this.node = objectNode;
        this.property = property;
        setItem(property);
        this.name = name;
        this.localPath = node.getPath() + "/" + name; // todo : node.getPath() returns the global path, not local path - should use node.getRealNode()
        this.localPathInProvider = localPath;
        this.def = def;
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        value = JCRStoreService.getInstance().getInterceptorChain().beforeSetValue(node, name, def, value);
        property.setValue(value);
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        values = JCRStoreService.getInstance().getInterceptorChain().beforeSetValues(node, name, def, values);
        property.setValue(values);
    }

    public void setValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (value != null) {
            setValue(getSession().getValueFactory().createValue(value));
        } else {
            remove();
        }
    }

    public void setValue(String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (values != null) {
            Value[] v = null;
            if (values != null) {
                v = new Value[values.length];
                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null) {
                        v[i] = getSession().getValueFactory().createValue(values[i]);
                    } else {
                        v[i] = null;
                    }
                }
            }
            setValue(v);
        } else {
            remove();
        }
    }

    /**
     * @deprecated As of JCR 2.0, {@link #setValue(Binary)} should be used instead.
     */
    public void setValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (value != null) {
            setValue(getSession().getValueFactory().createValue(value));
        } else {
            remove();
        }
    }

    public void setValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        setValue(getSession().getValueFactory().createValue(value));
    }

    public void setValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        setValue(getSession().getValueFactory().createValue(value));
    }

    public void setValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (value != null) {
            setValue(getSession().getValueFactory().createValue(value));
        } else {
            remove();
        }
    }

    public void setValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        setValue(getSession().getValueFactory().createValue(value));
    }

    public void setValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (value != null) {
            if (value instanceof JCRNodeWrapper) {
                value = ((JCRNodeWrapper) value).getRealNode();
            }
            setValue(getSession().getValueFactory().createValue(value,
                    def.getRequiredType() == PropertyType.WEAKREFERENCE));
        } else {
            remove();
        }
    }

    public void setValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (value != null) {
            setValue(getSession().getValueFactory().createValue(value));
        } else {
            remove();
        }
    }

    public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (value != null) {
            setValue(getSession().getValueFactory().createValue(value));
        } else {
            remove();
        }
    }

    public void addValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    public void addValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    public void addValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    public void addValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    public void addValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    public void addValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    public void addValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    public void addValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    public void addValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    public void addValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValues(new Value[]{value});
    }

    public void addValues(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        List<Value> newValues = new ArrayList<Value>(Arrays.asList(getValues()));
        for (Value value : values) {
            if (!newValues.contains(value)) {
                newValues.add(value);
            }
        }
        setValue(newValues.toArray(new Value[newValues.size()]));
    }

    public Value getValue() throws ValueFormatException, RepositoryException {
        Value value = JCRStoreService.getInstance().getInterceptorChain().afterGetValue(this, property.getValue());
        value = new JCRValueWrapperImpl(value, getDefinition(), getSession());
        return value;
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        Value[] values = JCRStoreService.getInstance().getInterceptorChain().afterGetValues(this, property.getValues());

        Value[] wrappedValues = new Value[values.length];
        for (int i = 0; i < values.length; i++) {
            Value value = values[i];
            wrappedValues[i] = new JCRValueWrapperImpl(value, getDefinition(), getSession());
        }
        return wrappedValues;
    }

    public String getString() throws ValueFormatException, RepositoryException {
        return getValue().getString();
    }

    /**
     * @deprecated As of JCR 2.0, {@link #getBinary()} should be used instead.
     */
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

    public JCRNodeWrapper getNode() throws ValueFormatException, RepositoryException {
        Value value = getValue();
        int type = value.getType();
        switch (type) {
            case PropertyType.REFERENCE:
            case PropertyType.WEAKREFERENCE:
                return session.getNodeByUUID(value.getString());

            case PropertyType.PATH:
            case PropertyType.NAME:
                String path = value.getString();
                boolean absolute = StringUtils.startsWith(path, "/");
                return (absolute) ? session.getNode(path) : getParent().getNode(path);

            case PropertyType.STRING:
                try {
                    Value refValue = ValueHelper.convert(value, PropertyType.REFERENCE, session.getValueFactory());
                    return session.getNodeByUUID(refValue.getString());
                } catch (RepositoryException e) {
                    // try if STRING value can be interpreted as PATH value
                    Value pathValue = ValueHelper.convert(value, PropertyType.PATH, session.getValueFactory());
                    absolute = StringUtils.startsWith(pathValue.getString(), "/");
                    return (absolute) ? session.getNode(pathValue.getString()) : getParent().getNode(pathValue.getString());
                }

            default:
                throw new ValueFormatException("Property value cannot be converted to a PATH, REFERENCE or WEAKREFERENCE");
        }
    }

    public JCRNodeWrapper getContextualizedNode() throws ValueFormatException, RepositoryException {
        JCRNodeWrapper ref = getNode();
        return session.getNode(getParent().getPath()+JCRSessionWrapper.DEREF_SEPARATOR+ref.getRealNode().getName());
    }

    public JCRNodeWrapper getReferencedNode() throws ValueFormatException, RepositoryException {
        return getNode();
    }

    public Binary getBinary() throws ValueFormatException, RepositoryException {
        return getValue().getBinary();
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return getValue().getDecimal();
    }

    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        Value value = getValue();
        Value pathValue = ValueHelper.convert(value, PropertyType.PATH, session.getValueFactory());
        String path = pathValue.getString();
        boolean absolute = StringUtils.startsWith(path, "/");
        return (absolute) ? session.getProperty(path) : getParent().getProperty(path);
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
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException
     * @deprecated use getNode instead
     */
    public CategoryBean getCategory() throws ValueFormatException, RepositoryException {
        return ((JCRValueWrapperImpl) getValue()).getCategory();
    }

    public String getName() throws RepositoryException {
        return name;
    }

    public JCRItemWrapper getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return provider.getItemWrapper(property.getAncestor(i), session);
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
        return property.isSame(item) || (item instanceof JCRItemWrapperImpl && item.isSame(property));
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        property.accept(itemVisitor);
    }

    /**
     * @deprecated As of JCR 2.0, {@link Session#save()} should
     *             be used instead.
     */
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        property.save();
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        property.refresh(b);
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        JCRStoreService.getInstance().getInterceptorChain().beforeRemove(node, name, def);
        if (property instanceof ExternalReferencePropertyImpl) {
            ((JCRNodeWrapperImpl) node).removeExternalReferenceProperty(name, def);
        } else {
            property.remove();
        }
        if(node instanceof JCRNodeWrapperImpl)
        ((JCRNodeWrapperImpl) node).flushLocalCaches();
    }

    public boolean isMultiple() throws RepositoryException {
        return property.isMultiple();
    }

    public Value getRealValue() throws ValueFormatException, RepositoryException {
        return new JCRValueWrapperImpl(property.getValue(), getDefinition(), getSession());
    }

    public Value[] getRealValues() throws ValueFormatException, RepositoryException {
        Value[] values = property.getValues();

        Value[] wrappedValues = new Value[values.length];
        for (int i = 0; i < values.length; i++) {
            Value value = values[i];
            wrappedValues[i] = new JCRValueWrapperImpl(value, getDefinition(), getSession());
        }
        return wrappedValues;
    }

    public String getLocale() throws RepositoryException {
        if (def.isInternationalized()) {
            return StringUtils.substringAfter(property.getParent().getName(), "j:translation_");
        }
        return null;
    }
}
