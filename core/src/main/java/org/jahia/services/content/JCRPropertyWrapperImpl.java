/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.ValueHelper;
import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.slf4j.Logger;

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
 * Wrapper for {@link javax.jcr.Property} to allow more data format.
 *
 * @author toto
 */
public class JCRPropertyWrapperImpl extends JCRItemWrapperImpl implements JCRPropertyWrapper {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JCRPropertyWrapperImpl.class);

    private JCRNodeWrapper node;
    private Property property;
    private String name;
    private ExtendedPropertyDefinition def;

    /**
     * Constructor
     *
     * @param objectNode JCRNodeWrapper of the node holding this property
     * @param property wrapped property object
     * @param session wrapped session which loaded this property
     * @param provider JCR store provider for this property
     * @param def definition of this property
     */
    public JCRPropertyWrapperImpl(JCRNodeWrapper objectNode, Property property, JCRSessionWrapper session, JCRStoreProvider provider,
            ExtendedPropertyDefinition def) throws RepositoryException {
        super(session, provider);
        this.node = objectNode;
        this.property = property;
        this.item = property;
        if (property != null) {
            this.localPath = property.getPath();
            this.localPathInProvider = localPath;
            this.name = property.getName();
            setPropertyDefinition(def);
        }
    }

    /**
     * Constructor
     *
     * @param objectNode JCRNodeWrapper of the node holding this property
     * @param property wrapped property object
     * @param session wrapped session which loaded this property
     * @param provider JCR store provider for this property
     * @param def definition of this property
     * @param name name of this property
     */
    public JCRPropertyWrapperImpl(JCRNodeWrapper objectNode, Property property, JCRSessionWrapper session, JCRStoreProvider provider,
            ExtendedPropertyDefinition def, String name) throws RepositoryException {
        super(session, provider);
        this.node = objectNode;
        this.property = property;
        this.item = property;
        this.name = name;
        this.localPath = node.getPath() + "/" + name; // todo : node.getPath() returns the global path, not local path - should use node.getRealNode()
        this.localPathInProvider = localPath;
        setPropertyDefinition(def);
    }

    private void setPropertyDefinition(ExtendedPropertyDefinition definition) throws RepositoryException {
        if (property != null && definition != null && isMultiple() && !definition.isMultiple()) {
            this.def = node.getApplicablePropertyDefinition(name, definition.getRequiredType(), true);
        } else {
            this.def = definition;
        }
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        node.checkLock();
        Value modifiedValue = JCRStoreService.getInstance().getInterceptorChain().beforeSetValue(node, name, def, value);
        property.setValue(modifiedValue);
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        node.checkLock();
        Value[] modifiedValues = JCRStoreService.getInstance().getInterceptorChain().beforeSetValues(node, name, def, values);
        property.setValue(modifiedValues);
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
            Value[] v = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    v[i] = getSession().getValueFactory().createValue(values[i]);
                } else {
                    v[i] = null;
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
            setValue(getSession().getValueFactory().createValue(
                    value instanceof JCRNodeWrapper ? ((JCRNodeWrapper) value).getRealNode() : value,
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

    @Override
    public void addValue(String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    @Override
    public void addValue(InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    @Override
    public void addValue(long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    @Override
    public void addValue(double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    @Override
    public void addValue(Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    @Override
    public void addValue(boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    @Override
    public void addValue(Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(value, true);
    }

    @Override
    public void addValue(Node value, boolean weak)
            throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value, weak));
    }

    @Override
    public void addValue(Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    @Override
    public void addValue(BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValue(getSession().getValueFactory().createValue(value));
    }

    @Override
    public void addValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        addValues(new Value[]{value});
    }

    @Override
    public void addValues(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        List<Value> newValues = new ArrayList<Value>(Arrays.asList(getValues()));
        boolean updated = false;
        for (Value value : values) {
            if (!newValues.contains(value)) {
                newValues.add(value);
                updated = true;
            }
        }
        if (updated) {
            setValue(newValues.toArray(new Value[newValues.size()]));
        }
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
                try {
                    return (absolute) ? session.getNode(path) : getParent().getNode(path);
                } catch (PathNotFoundException e) {
                    throw new ItemNotFoundException(path);
                }

            case PropertyType.STRING:
                try {
                    Value refValue = ValueHelper.convert(value, PropertyType.REFERENCE, session.getValueFactory());
                    return session.getNodeByUUID(refValue.getString());
                } catch (RepositoryException e) {
                    // try if STRING value can be interpreted as PATH value
                    Value pathValue = ValueHelper.convert(value, PropertyType.PATH, session.getValueFactory());
                    absolute = StringUtils.startsWith(pathValue.getString(), "/");
                    try {
                        return (absolute) ? session.getNode(pathValue.getString()) : getParent().getNode(pathValue.getString());
                    } catch (PathNotFoundException e1) {
                        throw new ItemNotFoundException(pathValue.getString());
                    }
                }

            default:
                throw new ValueFormatException("Property value cannot be converted to a PATH, REFERENCE or WEAKREFERENCE");
        }
    }

    public JCRNodeWrapper getContextualizedNode() throws ValueFormatException, RepositoryException {
        JCRNodeWrapper ref = getNode();
        try {
            return session.getNode(getParent().getPath() + JCRSessionWrapper.DEREF_SEPARATOR + ref.getRealNode().getName());
        } catch (PathNotFoundException e) {
            throw new ItemNotFoundException(e.getMessage(), e);
        }
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
        try {
            return (absolute) ? session.getProperty(path) : getParent().getProperty(path);
        } catch (PathNotFoundException e) {
            throw new ItemNotFoundException(path);
        }
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
        return property.getType();
    }

    public String getName() throws RepositoryException {
        return name;
    }

    @Override
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

    @Override
    public boolean isSame(Item item) throws RepositoryException {
        return property.isSame(item) || (item instanceof JCRItemWrapperImpl && item.isSame(property));
    }

    @Override
    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        property.accept(itemVisitor);
    }

    /**
     * @deprecated As of JCR 2.0, {@link Session#save()} should
     *             be used instead.
     */
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException,
            ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        property.save();
    }

    @Override
    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        property.refresh(b);
    }

    @Override
    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        node.checkLock();
        JCRStoreService.getInstance().getInterceptorChain().beforeRemove(node, name, def);
        property.remove();
        JCRNodeWrapper n = node;
        if (n instanceof JCRNodeDecorator) {
            n = ((JCRNodeDecorator) n).getDecoratedNode();
        }
        if (n instanceof JCRNodeWrapperImpl) {
            ((JCRNodeWrapperImpl) n).flushLocalCaches();
        }
    }

    public boolean isMultiple() throws RepositoryException {
        return property.isMultiple();
    }

    public JCRValueWrapper getValue() throws ValueFormatException, RepositoryException {
        Value value = JCRStoreService.getInstance().getInterceptorChain().afterGetValue(this, property.getValue());
        return wrap(value);
    }

    public JCRValueWrapper getRealValue() throws ValueFormatException, RepositoryException {
        return wrap(property.getValue());
    }

    private JCRValueWrapper wrap(Value value) throws RepositoryException {
        return new JCRValueWrapperImpl(value, getDefinition(), getSession());
    }

    public JCRValueWrapper[] getValues() throws ValueFormatException, RepositoryException {
        Value[] values = JCRStoreService.getInstance().getInterceptorChain().afterGetValues(this, property.getValues());

        return wrap(values);
    }

    public JCRValueWrapper[] getRealValues() throws ValueFormatException, RepositoryException {
        return wrap(property.getValues());
    }

    private JCRValueWrapper[] wrap(Value[] values) throws RepositoryException {
        JCRValueWrapper[] wrappedValues = new JCRValueWrapper[values.length];
        for (int i = 0; i < values.length; i++) {
            Value value = values[i];
            wrappedValues[i] = new JCRValueWrapperImpl(value, getDefinition(), getSession());
        }
        return wrappedValues;
    }

    public String getLocale() throws RepositoryException {
        if (def.isInternationalized()) {
            Node parent = property.getParent();
            String parentName = parent.getName();
            if ("jcr:frozenNode".equals(parentName)) {
                // we have a property of a version node -> get the locale as property
                try {
                    return parent.getProperty(Constants.JCR_LANGUAGE).getString();
                } catch (PathNotFoundException e) {
                    return null;
                }
            }
            return StringUtils.substringAfter(parentName, "j:translation_");
        }
        return null;
    }

    @Override
    public boolean removeValue(Value value) throws ValueFormatException, VersionException,
            LockException, ConstraintViolationException, RepositoryException {
        return removeValues(new Value[]{value});
    }

    @Override
    public boolean removeValues(Value[] values) throws ValueFormatException, VersionException,
            LockException, ConstraintViolationException, RepositoryException {
        Value[] valueArray = getValues();
        if (valueArray == null || valueArray.length == 0) {
            return false;
        }

        List<Value> newValues = new ArrayList<Value>(Arrays.asList(valueArray));
        boolean updated = false;
        for (Value value : values) {
            if (newValues.contains(value)) {
                newValues.remove(value);
                updated = true;
            }
        }
        if (updated) {
            setValue(newValues.toArray(new Value[newValues.size()]));
        }

        return updated;
    }

    @Override
    public Property getRealProperty() {
        return property;
    }

}
