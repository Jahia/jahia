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
package org.jahia.services.content;

import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 22, 2008
 * Time: 4:03:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRPropertyWrapperImpl extends JCRItemWrapperImpl implements Property {

    private JCRNodeWrapper node;
    private Property property;

    public JCRPropertyWrapperImpl(JCRNodeWrapper objectNode, Property property, JCRSessionWrapper session, JCRStoreProvider provider) {
        super(session, provider);
        this.node = objectNode;
        this.property = property;
        try {
            this.localPath = property.getPath();
        } catch (RepositoryException e) {
            
        }

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

    public Value getValue() throws ValueFormatException, RepositoryException {
        return property.getValue();
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return property.getValues();
    }

    public String getString() throws ValueFormatException, RepositoryException {
        return property.getString();
    }

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

    public long getLength() throws ValueFormatException, RepositoryException {
        return property.getLength();
    }

    public long[] getLengths() throws ValueFormatException, RepositoryException {
        return property.getLengths();
    }

    public PropertyDefinition getDefinition() throws RepositoryException {
        PropertyDefinition def = property.getDefinition();
        String name = def.getDeclaringNodeType().getName();
        if (name.equals("nt:hierarchyNode") && def.getName().equals("jcr:created")) {
            name = "mix:created";
        }
        if (name.equals("nt:resource") && (def.getName().equals("jcr:mimeType") || def.getName().equals("jcr:encoding"))) {
            name = "mix:mimeType";
        }
        if (name.equals("nt:resource") && def.getName().equals("jcr:lastModified")) {
            name = "mix:lastModified";
        }

        ExtendedNodeType ent = NodeTypeRegistry.getInstance().getNodeType(name);
        ExtendedPropertyDefinition epd = ent.getPropertyDefinition(def.getName());
        return epd;
    }

    public int getType() throws RepositoryException {
        return property.getType();
    }

    public String getName() throws RepositoryException {
        return property.getName();
    }

    public Item getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return property.getAncestor(i);
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return property.getParent();
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

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        property.save();
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        property.refresh(b);
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        property.remove();
    }
}
