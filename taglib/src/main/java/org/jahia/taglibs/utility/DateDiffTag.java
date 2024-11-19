/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.joda.time.*;

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
