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
package org.jahia.taglibs.utility;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.taglibs.internal.date.AbstractDateTag;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Years;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.Date;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 12 févr. 2010
 */
public class DateDiffTag extends AbstractDateTag {
    private static final long serialVersionUID = -1376268533803433468L;
    private Date startDate;
    private Date endDate;
    private String format;
    private int scope = PageContext.PAGE_SCOPE;

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }
    
	public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            if("years".equals(format)) {
            	int years = Years.yearsBetween(new DateTime(startDate), new DateTime(endDate)).getYears();
                if(getVar() != null) {
                	pageContext.setAttribute(getVar(), new Integer(years), scope);
                } else {
                	pageContext.getOut().print(years);
                }
            }
            else if("months".equals(format)) {
            	int months = Months.monthsBetween(new DateTime(startDate), new DateTime(endDate)).getMonths();
                if(getVar() != null) {
               	    pageContext.setAttribute(getVar(), new Integer(months), scope);
                } else {
            	    pageContext.getOut().print(months);
                }
            }
            else if("days".equals(format)) {
            	int days = Days.daysBetween(new DateTime(startDate), new DateTime(endDate)).getDays();
                if(getVar() != null) {
               	  	pageContext.setAttribute(getVar(), new Integer(days), scope);
                } else {
            	    pageContext.getOut().print(days); 
                }
            }
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
}
