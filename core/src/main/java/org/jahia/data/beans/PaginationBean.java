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
package org.jahia.data.beans;

/**
 * <p>Title: Jahia Pagination bean </p>
 * <p>Description: This object contains data for a cListPagination tag iteration.
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Jahia Ltd</p>
 * @author Benjamin Papez
 * @version 1.0
 */

public class PaginationBean {
    
    public static final String PAGINATION_ON_TOP = "top";
    public static final String PAGINATION_AT_BOTTOM = "bottom";
    
    private int pageNumber = 0;
    private boolean currentPageFlag = false;

    /**
     * Empty parameter constructor to respect JavaBean pattern.
     */
    public PaginationBean() {
    }

    public PaginationBean(int aPageNumber, boolean aCurrentPageFlag) {
        this.pageNumber = aPageNumber;
        this.currentPageFlag = aCurrentPageFlag;
    }

    public int getPageNumber() {
        return pageNumber;
    }
    public void setPageNumber(int aPageNumber) {
        this.pageNumber = aPageNumber;
    }
    public boolean isCurrentPage() {
        return currentPageFlag;
    }
    public void setCurrentPage(boolean aCurrentPageFlag) {
        this.currentPageFlag = aCurrentPageFlag;
    }
}