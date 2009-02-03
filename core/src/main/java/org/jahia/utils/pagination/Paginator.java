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

//
//  Paginator
//
//

package org.jahia.utils.pagination;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.utils.JahiaConsole;

/**
 * Paginates a list for partial display (i.e. showing only a window of the 
 * list elements at a time).
 *
 * @author MJ
 */
public class Paginator
{

    /**
     * The context key used to add the total numbner of items in the list.
     */
    public static final String TOTAL_ITEMS = "totalItems";

    /**
     * The context key used to retrieve the current page number.
     */
    public static final String PAGE_KEY = "paginated_page";

    /**
     * The context key used to add the previous page number.
     */
    public static final String PREV_PAGE = "prevPage";

    /**
     * The context key used to add the current page number.
     */
    public static final String CUR_PAGE = "currentPage";

    /**
     * The context key used to add the next page number.
     */
    public static final String NEXT_PAGE = "nextPage";

    /**
     * The context key used to add the list of page numbers.
     */
    public static final String PAGES = "pages";

    /**
     * Handles paging of the list in cases where the list represents all
     * items avaliable.
     */
    public static List paginate (List                 items,
                                 int                  nbrItemsPerPage,
                                 HttpServletRequest   request,
                                 HttpServletResponse  response,
                                 HttpSession          session )
    {
        return paginate(items, items.size(), nbrItemsPerPage, request, response, session);
    }

    /**
     * Handles paging of the list in cases where there may be more items
     * total than are in the list (eg. if some items were filtered out
     * due to a query).
     */
    public static List paginate ( List                 items,
                                  int                  totalNbrItems,
                                  int                  nbrItemsPerPage,
                                  HttpServletRequest   request,
                                  HttpServletResponse  response,
                                  HttpSession          session )
    {
        session.setAttribute(TOTAL_ITEMS, new Integer(totalNbrItems));
        JahiaConsole.println("Paginator",  "totalNbrItems=" + totalNbrItems);

        if (totalNbrItems > 0) {
            int nbrPages = getNbrPages(nbrItemsPerPage, totalNbrItems);
            JahiaConsole.println("Paginator",  "nbrPages=" + nbrPages);

            if (nbrPages > 1) {
                int curPageNbr = getCurrentPageNbr(request, response, session, nbrPages);
                session.setAttribute(CUR_PAGE, new Integer(curPageNbr));

                // Create the page links.
                Integer[] pages = new Integer[nbrPages];
                for (int i = 0; i < nbrPages; i++) {
                    pages[i] = new Integer(i + 1);
                }
                session.setAttribute(PAGES, pages);
                session.setAttribute(PREV_PAGE, 
                            (curPageNbr > 1 ? 
                             new Integer(curPageNbr - 1) : null));
                session.setAttribute(NEXT_PAGE, 
                            (curPageNbr < nbrPages ? 
                             new Integer(curPageNbr + 1) : null));

                // Get the subset of items to display.
                items = getPage(items, curPageNbr, nbrItemsPerPage);
            }
        }
        else
        {
            items = null;
        }

        return items;
    }

    /**
     * Returns a valid page number for the page to display.
     *
     * @param session  The session to pull the page number from.
     * @param nbrPages The highest possible page number.
     */
    private static final int getCurrentPageNbr ( HttpServletRequest   request,
                                                 HttpServletResponse  response,
                                                 HttpSession          session,
                                                 int                  nbrPages )
    {
        String page = request.getParameter(PAGE_KEY);
        int pgNbr = (page != null ? Integer.parseInt(page) : 1);
        if (pgNbr < 1) {
            pgNbr = 1;
        } else if (pgNbr > nbrPages) {
            pgNbr = nbrPages;
        }
        return pgNbr;
    }

    /**
     * Calculates the number of pages based on the number of items.
     *
     * @param nbrItemsPerPage The number of items to display per page.
     * @param nbrItems        The total number of items.
     * @return                The number of pages to display items on.
     */
    protected static final int getNbrPages (int nbrItemsPerPage, int nbrItems)
    {
        // The cast is to assure the division returns a float.
        return (int)Math.ceil((float)nbrItems / nbrItemsPerPage);
    }

    /**
     * Gets the sub list of items to display for the current page.
     *
     * @param items           The complete list of items.
     * @param pgNbr           The current page number.
     * @param nbrItemsPerPage The number of items to display per page.
     */
    protected static final List getPage (List items, int pgNbr, 
                                       int nbrItemsPerPage)
    {
        return items.subList
            ((pgNbr - 1) * nbrItemsPerPage, 
             Math.min(pgNbr * nbrItemsPerPage, items.size()));
    }

    /**
     * Quick and dirty function to take a type of thing being
     * searched for and turning it into an SQL regular expression.
     */
    protected static String makeSQLRegex(String type, String searchFor)
    {
        if (type == null) type = "Contains";
        if (type.equals("Contains") || type.equals("StartsWith"))
            searchFor = searchFor + "%";
        if (type.equals("Contains") || type.equals("EndsWith"))
            searchFor = "%" + searchFor;   
        return searchFor;
    }
}
