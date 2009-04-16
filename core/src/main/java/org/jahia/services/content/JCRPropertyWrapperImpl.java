/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
