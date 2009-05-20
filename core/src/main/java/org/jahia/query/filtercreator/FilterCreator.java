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
package org.jahia.query.filtercreator;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Comparison;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.jahia.data.containers.ContainerFilterInterface;
import org.jahia.data.containers.ContainerSorterInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.containers.ContainerQueryContext;


/**
 * This interface define contract for Concrete Filter Creator
 *
 * User: hollis
 * Date: 15 nov. 2007
 * Time: 16:49:35
 */
public interface FilterCreator {

    /**
     * Jahia content definition name property
     *
     * ComparisonImpl constraint can use this value as propertyName to request a concrete Filter.
     *
     */
    public static final String CONTENT_DEFINITION_NAME = "CONTENT_DEFINITION_NAME";

    /**
     * Page Path constraint name property
     * Should not be used directly.
     */
    static final String PAGE_PATH = "PAGE_PATH";

    /**
     * Returns the rule name.
     * @return
     */
    public String getName();

    public void setName(String name);

    /**
     * Returns a concrete Filter for the given Terminal ConstraintImpl
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public ContainerFilterInterface getContainerFilter(Constraint c,
                                                       QueryObjectModel queryModel,
                                                       ContainerQueryContext queryContext,
                                                       ProcessingContext context) throws JahiaException;

    /**
     * Returns a concrete Range Query Filter.
     *
     * @param c1
     * @param c2
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public ContainerFilterInterface getRangeQueryFilter(   Comparison c1,
                                                            Comparison c2,
                                                            QueryObjectModel queryModel,
                                                            ContainerQueryContext queryContext,
                                                            ProcessingContext context) throws JahiaException;

    /**
     * Returns a concrete Sorter
     *
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public ContainerSorterInterface getContainerSorter(QueryObjectModel queryModel,
                                                       ContainerQueryContext queryContext,
                                                       ProcessingContext context) throws JahiaException;

}
