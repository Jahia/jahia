package org.apache.jackrabbit.core.query.lucene;

import javax.jcr.query.InvalidQueryException;

import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.query.PropertyTypeRegistry;
import org.apache.jackrabbit.core.query.lucene.constraint.Constraint;
import org.apache.jackrabbit.spi.commons.query.QueryNodeFactory;

public class JahiaQueryImpl extends QueryImpl {
    
    private Constraint constraint = null;

    public JahiaQueryImpl(SessionImpl session, ItemManager itemMgr, SearchIndex index,
            PropertyTypeRegistry propReg, String statement, String language,
            QueryNodeFactory factory) throws InvalidQueryException {
        super(session, itemMgr, index, propReg, statement, language, factory);
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

}
