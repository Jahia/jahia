package org.jahia.services.content;

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
    private String nodeIdentifier;
    private Session session;

    public ExternalReferencePropertyImpl(String name, Node parentNode, Session session, final String nodeIdentifier, Node referencedNode) throws RepositoryException {
        this.name = name;
        this.path = parentNode.getPath() + "/" + name;
        this.parentNode = parentNode;
        this.session = session;
        this.nodeIdentifier = nodeIdentifier;
        this.value = new ExternalReferenceValue(nodeIdentifier, PropertyType.WEAKREFERENCE);
        this.referencedNode = referencedNode;
    }

    public void setValue(Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
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
        return new Value[0];
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
        return null;
    }

    public int getType() throws RepositoryException {
        return value.getType();
    }

    public boolean isMultiple() throws RepositoryException {
        return false;
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
