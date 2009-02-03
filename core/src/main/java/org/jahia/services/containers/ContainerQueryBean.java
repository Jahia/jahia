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

package org.jahia.services.containers;

import org.jahia.data.containers.ContainerSorterInterface;
import org.jahia.data.containers.ContainerFilters;
import org.jahia.services.search.ContainerSearcher;
import org.jahia.services.containers.ContainerQueryContext;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 oct. 2007
 * Time: 14:37:31
 * To change this template use File | Settings | File Templates.
 */
public class ContainerQueryBean {

    private ContainerQueryContext queryContext;
    private ContainerFilters filters;
    private ContainerSearcher searcher;
    private ContainerSorterInterface sorter;

    public ContainerQueryBean(){
    }

    /**
     *
     * @param queryContext
     */
    public ContainerQueryBean(ContainerQueryContext queryContext) {
        this.queryContext = queryContext;
        if (this.queryContext!=null){
            this.queryContext.getContainerDefinitionsIncludingType(true);
        }
    }

    public ContainerQueryBean(ContainerFilters filters, ContainerSearcher searcher,
                              ContainerSorterInterface sorter, ContainerQueryContext queryContext) {
        this(queryContext);
        this.filters = filters;
        this.searcher = searcher;
        this.sorter = sorter;
    }

    public ContainerQueryContext getQueryContext() {
        return queryContext;
    }

    public void setQueryContext(ContainerQueryContext queryContext) {
        this.queryContext = queryContext;
        if (this.queryContext!=null){
            this.queryContext.getContainerDefinitionsIncludingType(true);
        }
    }

    public ContainerFilters getFilter() {
        return filters;
    }

    public void setFilter(ContainerFilters filters) {
        this.filters = filters;
    }

    public ContainerSearcher getSearcher() {
        return searcher;
    }

    public void setSearcher(ContainerSearcher searcher) {
        this.searcher = searcher;
    }

    public ContainerSorterInterface getSorter() {
        return sorter;
    }

    public void setSorter(ContainerSorterInterface sorter) {
        this.sorter = sorter;
    }
}
