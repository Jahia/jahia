package org.jahia.services.content;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.value.AbstractValueFactory;

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
        return super.createValue(value, true);
    }

    @Override
    public Value createValue(String value, int type)
            throws ValueFormatException {
        return super.createValue(value, type != PropertyType.REFERENCE ? type
                : PropertyType.WEAKREFERENCE);
    }
}
