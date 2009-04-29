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
