/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.query.filtercreator;

import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModel;
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
