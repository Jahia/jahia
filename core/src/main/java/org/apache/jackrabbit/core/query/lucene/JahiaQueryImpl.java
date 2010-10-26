package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.query.PropertyTypeRegistry;
import org.apache.jackrabbit.core.query.lucene.constraint.Constraint;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.spi.commons.query.QueryNodeFactory;

import javax.jcr.query.InvalidQueryException;

public class JahiaQueryImpl extends QueryImpl {

    private Constraint constraint = null;

    public JahiaQueryImpl(SessionContext sessionContext, SearchIndex index,
                          PropertyTypeRegistry propReg, String statement, String language,
                          QueryNodeFactory factory) throws InvalidQueryException {
        super(sessionContext, index, propReg, statement, language, factory);
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

}
