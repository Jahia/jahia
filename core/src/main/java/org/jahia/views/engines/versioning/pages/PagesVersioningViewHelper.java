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

package org.jahia.views.engines.versioning.pages;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.views.engines.versioning.ContentVersioningViewHelper;

/**
 * <p>Title: Helper for Pages Versioning Engine </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class PagesVersioningViewHelper extends ContentVersioningViewHelper {


    /**
     * Exact restore
     */
    private boolean exactRestore = true;

    private boolean applyPageMoveWhenRestore = false;

    /**
     * the page level *
     */
    private int pageLevel = 1;

    // Site map selected pages
    private Set selectedPages = new HashSet();

    // Selected pages to restore
    private List pagesToRestore = new ArrayList();

    /**
     * @param page
     */
    public PagesVersioningViewHelper(final ContentPage page, CalendarHandler restoreDateCalendar)
    throws JahiaException {
        super(page,restoreDateCalendar);
   }

    public ContentPage getPage() {
        return (ContentPage)this.getContentObject();
    }

    public boolean exactRestore() {
        return this.exactRestore;
    }

    public void setExactRestore(final boolean value) {
        this.exactRestore = value;
    }

    public boolean applyPageMoveWhenRestore() {
        return this.applyPageMoveWhenRestore;
    }

    public void setApplyPageMoveWhenRestore(final boolean value) {
        this.applyPageMoveWhenRestore = value;
    }

    public Integer getPageLevel() {
        return new Integer(this.pageLevel);
    }

    public String getPageLevelAsStr() {
        return String.valueOf(this.pageLevel);
    }

    public void setPageLevel(final int value) {
        this.pageLevel = value;
    }

    /**
     * Sitemap selected pages
     *
     */
    public Set getSelectedPages() {
        return this.selectedPages;
    }

    /**
     * Pages to restore, the array contains alternatively the language to restore
     * followed by the page to restore
     *
     */
    public List getPagesToRestore() {
        return this.pagesToRestore;
    }

    /**
     * Set the pages to restore
     *
     */
    public void setPagesToRestore(final List pages) {
        this.pagesToRestore = pages;
    }

    public void loadSiteMapViewHelper(final JahiaUser user, final HttpServletRequest request)
            throws JahiaSessionExpirationException {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (this.getFromRevisionDateCalendar().getDateLong().longValue() >
                this.getToRevisionDateCalendar().getDateLong().longValue()){
            cal.setTimeInMillis(this.getToRevisionDateCalendar().getDateLong().longValue());
            cal.set(Calendar.WEEK_OF_MONTH,-1);
            this.getFromRevisionDateCalendar().setDateLong(new Long(cal.getTimeInMillis()));
        }
    }

}

