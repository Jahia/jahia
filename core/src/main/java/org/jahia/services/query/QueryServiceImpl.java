/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.query;


import java.util.Arrays;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.qom.*;

import org.apache.jackrabbit.spi.commons.query.qom.*;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.query.QueryModifierAndOptimizerVisitor.TraversingMode;
import org.slf4j.Logger;


/**
 * The default implementation of Jahia's QueryService.
 *
 * Jahia's query service is based on the JCR QueryObjectModelFactory and thus supports all kinds of complex queries specified in JSR-283
 * (Content Repository for Java� Technology API 2.0)
 *
 * Queries can be created with the API by using the QueryObjectModel. Jahia will also provide a query builder user interface. It is also
 * possible to use SQL-2 and the deprecated XPATH language.
 *
 * As Jahia can plug-in multiple repositories via the universal content hub (UCH), the queries can be converted to other languages, like the
 * EntropySoft connector query language.
 *
 * The query service provides methods to modify and optimize the queries to support and make use of Jahia's internal data model
 * implementation.
 *
 * @author Benjamin Papez
 */
public class QueryServiceImpl extends QueryService {
    static transient Logger logger = org.slf4j.LoggerFactory.getLogger(QueryService.class);

    /**
     * The initialization mode for the first QOM traversing iteration
     */
    static int INITIALIZE_MODE = 1;

    /**
     * The check for modification mode for the second QOM traversing iteration
     */
    static int CHECK_FOR_MODIFICATION_MODE = 2;

    /**
     * The optional modification mode for the third QOM traversing iteration only called if query modification is necessary.
     */
    static int MODIFY_MODE = 3;

    private ValueFactory valueFactory = ValueFactoryImpl.getInstance();

    private QueryServiceImpl() {
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final QueryServiceImpl INSTANCE = new QueryServiceImpl();
    }

    /**
     * Return the unique service instance. If the instance does not exist, a new instance is created.
     *
     * @return The unique service instance.
     */
    public static QueryService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Initializes the service.
     */
    public void start() {
        // do nothing
    }

    public void stop() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.query.QueryService#modifyAndOptimizeQuery(javax.jcr.query.qom.QueryObjectModel,
     * javax.jcr.query.qom.QueryObjectModelFactory, org.jahia.services.content.JCRSessionWrapper)
     */
    public QueryObjectModel modifyAndOptimizeQuery(QueryObjectModel qom,
                                                   QueryObjectModelFactory qomFactory, JCRSessionWrapper session)
            throws RepositoryException {
        ModificationInfo info = getModificationInfo(qom.getSource(), qom.getConstraint(),
                qom.getOrderings(), qom.getColumns(), qomFactory, session);
        return info.getNewQueryObjectModel() != null ? info.getNewQueryObjectModel() : qom;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.query.QueryService#modifyAndOptimizeQuery(javax.jcr.query.qom.Source, javax.jcr.query.qom.Constraint,
     * javax.jcr.query.qom.Ordering[], javax.jcr.query.qom.Column[], javax.jcr.query.qom.QueryObjectModelFactory,
     * org.jahia.services.content.JCRSessionWrapper)
     */
    public QueryObjectModel modifyAndOptimizeQuery(Source source, Constraint constraint,
                                                   Ordering[] orderings, Column[] columns, QueryObjectModelFactory qomFactory,
                                                   JCRSessionWrapper session) throws RepositoryException {
        ModificationInfo info = getModificationInfo(source, constraint, orderings, columns,
                qomFactory, session);
        return info.getNewQueryObjectModel() != null ? info.getNewQueryObjectModel() : qomFactory
                .createQuery(source, constraint, orderings, columns);
    }

    /**
     * We use a QOMTreeVisitor implementation to traverse through the query object model three times.
     *
     * The ModificationInfo.mode changes before each iteration from INITIALIZE_MODE to CHECK_FOR_MODIFICATION_MODE and at last MODIFY_MODE,
     * which is only called if modification is necessary.
     */
    protected ModificationInfo getModificationInfo(Source source, Constraint constraint,
                                                   Ordering[] orderings, Column[] columns, QueryObjectModelFactory qomFactory,
                                                   JCRSessionWrapper session) throws RepositoryException {
        ModificationInfo info = new ModificationInfo(qomFactory);

        QOMTreeVisitor visitor = new QueryModifierAndOptimizerVisitor(getValueFactory(), info, source, session);

        try {
            info.setMode(TraversingMode.INITIALIZE_MODE);
            ((SourceImpl) source).accept(visitor, null);

            if (constraint != null) {
                ((ConstraintImpl) constraint).accept(visitor, null);
            }
            if (orderings != null) {
                for (Ordering ordering : orderings) {
                    ((OrderingImpl) ordering).accept(visitor, null);
                }
            }
            if (columns != null) {
                for (Column column : columns) {
                    ((ColumnImpl) column).accept(visitor, null);
                }
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        try {
            info.setMode(TraversingMode.CHECK_FOR_MODIFICATION_MODE);
            ((SourceImpl) source).accept(visitor, null);

            if (constraint != null) {
                ((ConstraintImpl) constraint).accept(visitor, null);
            }
            if (orderings != null) {
                for (Ordering ordering : orderings) {
                    ((OrderingImpl) ordering).accept(visitor, null);
                }
            }
            if (columns != null) {
                for (Column column : columns) {
                    ((ColumnImpl) column).accept(visitor, null);
                }
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
        if (info.isModificationNecessary()) {
            makeModifications(source, constraint, orderings, columns, info, visitor, qomFactory);
        }

        return info;
    }

    protected void makeModifications(Source source, Constraint constraint, Ordering[] orderings,
                                     Column[] columns, ModificationInfo info, QOMTreeVisitor visitor,
                                     QueryObjectModelFactory qomFactory) throws RepositoryException {
        info.setMode(TraversingMode.MODIFY_MODE);

        try {
            int i = 0;
            Ordering[] newOrderings = null;
            if (orderings != null) {
                newOrderings = new Ordering[orderings.length];
                for (Ordering ordering : orderings) {
                    Ordering newOrdering = ordering;

                    newOrdering = (Ordering) ((OrderingImpl) ordering).accept(visitor, null);
                    newOrderings[i++] = newOrdering;
                }
            }
            Column[] newColumns = new Column[columns.length];
            i = 0;
            for (Column column : columns) {
                Column newColumn = column;

                newColumn = (Column) ((ColumnImpl) column).accept(visitor, null);
                newColumns[i++] = newColumn;
            }

            Constraint newConstraint = null;
            if (constraint != null) {
                newConstraint = (Constraint) ((ConstraintImpl) constraint).accept(visitor, null);
            }

            for (Constraint constraintToAdd : info.getNewConstraints()) {
                if (newConstraint == null) {
                    newConstraint = constraintToAdd;
                } else {
                    newConstraint = info.getQueryObjectModelFactory().and(
                            newConstraint, constraintToAdd);
                }
            }

            Source newSource = (Source) ((SourceImpl) info.getModifiedSource(source)).accept(
                    visitor, null);

            if (newSource == null) {
                logger.error("New source is null! Old source: "
                        + Arrays.toString(((SourceImpl)source).getSelectors()) + " - Modified source: "
                        + Arrays.toString(((SourceImpl)info.getModifiedSource(source)).getSelectors())
                        + "- Modification mode: " + info.getMode());
            }
            
            info.setNewQueryObjectModel(info.getQueryObjectModelFactory().createQuery(newSource,
                    newConstraint, newOrderings, newColumns));
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.query.QueryService#getValueFactory()
     */
    public ValueFactory getValueFactory() {
        return this.valueFactory;
    }
}
