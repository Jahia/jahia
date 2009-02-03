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
//  PaginatableList
//
//

package org.jahia.utils.pagination;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//------------------------------------------------import com.workingdogs.village.*;

//------------------------------------------------import org.joist.database.*;

/**
 * An abstract class describing a list which can be paginated.
 *
 * @author MJ
 */
abstract public class PaginatableList extends ArrayList
{   
    protected int itemsPerPage;

    /**
     * Returns a sublist of this list, the contents determined by
     * the page requested.
     */
    public List getPage(Integer page) throws SQLException
    {
        int start = firstItem(page);
        int finish = lastItem(page);
        return subList(start,finish);
    }

    /**
     * Prints out an HTML navigation bar for the given page; 
     */
    public String navigationBar(String linkText, Integer page)
    throws SQLException
    {
        boolean prependDots = false;
        int pageNo = page.intValue();
        int pages = numberOfPages();
        int startPage = pageNo - 5;
        if (startPage <= 0) startPage = 1;
        if (startPage > 1) prependDots = true;
        int endPage = pageNo + 4;
        if (endPage < 10) endPage = 10;
        if (endPage > pages) endPage = pages;
        
        String extra = appendQueryString();
        if (!extra.equals("")) {
            if (linkText.lastIndexOf("?") == -1) {
                linkText += "?";
            } else { 
                linkText += "&"; 
            }
            linkText += extra;
        }

        StringBuffer bar = new StringBuffer();
        if (startPage != endPage) {
            if (pageNo != 1) {
                bar.append(makeLink(linkText, pageNo-1, "Prev"));
            } else {
                bar.append("Prev");
            }
            bar.append(" | ");

            if (prependDots) bar.append(" ... | ");

            for (int i = startPage; i <= endPage; i++) {
                String linkedText = i + " " + makeGuide(new Integer(i));

                if (i == pageNo) {
                    bar.append(linkedText);
                } else {
                    bar.append(makeLink(linkText, i, linkedText));
                }
                if (i < endPage) {
                    bar.append (" | ");
                }
            }

            bar.append(" | ");

            if (prependDots) bar.append(" ... | ");

            if (pageNo != endPage) {
                bar.append(makeLink(linkText, pageNo+1, "Next"));
            } else {
                bar.append("Next");
            }
        }
        return bar.toString();
    }

    /**
     * Makes a link to the given page.
     */    
    protected String makeLink(String linkText, int pageNo, String linkedText)
    {
        StringBuffer link = new StringBuffer();
        link.append("<a href=\"").append(linkText);
        if (linkText.lastIndexOf("?") == -1) {
            link.append("?");
        } else { 
            link.append("&"); 
        }
        link.append("page=").append(pageNo).append("\">").append(linkedText);
        link.append("</a>");
        return link.toString();
    }

    /**
     * Makes a guide to the given page for use in the navbar. A
     * list of Users, for instance, might print "(A-N)" for a page
     * in which the first entry had a name beginning with A and the
     * last entry had a name beginning with N.
     */
    public String makeGuide(Integer page) throws SQLException
    {
        return "";
    }

    /**
     * Generate whatever text is neccessary to mantain this list's
     * state in a link to another page. A list which had had a filter
     * applied to it would need to return fields which would reinstate
     * the filter.
     */
    public String appendQueryString()
    {
        return "";
    }

    /**
     * Returns the number of items in the list.
     */
    public int numberOfItems() throws SQLException
    {
        return size();
    }

    /**
     * Returns the number of pages the list will occupy.
     */
    public int numberOfPages() throws SQLException
    {
        int entries = numberOfItems();
        int pages = entries / itemsPerPage;
        int entriesOnExtraPage = entries % itemsPerPage;
        if (entriesOnExtraPage != 0) pages++;
        return pages;
    }

    /**
     * Returns the maximum number of items that should be on any
     * given page. If the list was created with 0 items per page,
     * display the whole list.
     */
    public int itemsOnPage() throws SQLException
    {
        int itemsOnPage = itemsPerPage;
        if (itemsOnPage == 0) {
            itemsOnPage = numberOfItems();
        }
        return itemsOnPage;
    }

    /**
     * Returns the index of the first item on the given page.
     */
    public int firstItem(Integer pageNo) throws SQLException
    {
        int itemsOnThisPage = itemsOnPage();
        
        int start = ((pageNo.intValue()-1) * itemsOnThisPage);
        if (start >= numberOfItems()) {
            start = 0;
        }
        return start;
    }

    /**
     * Returns the index of the last item on the given page.
     */
    public int lastItem(Integer pageNo) throws SQLException
    {
        int finish = firstItem(pageNo) + itemsOnPage();
        if (finish >= numberOfItems()) {
            finish = numberOfItems();
        }
        return finish;
    }

}
