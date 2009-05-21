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
package org.jahia.services.expressions;

import java.util.Map;

import org.apache.commons.jexl.JexlContext;
import org.jahia.data.fields.ExpressionContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 29 janv. 2008
 * Time: 11:53:39
 * To change this template use File | Settings | File Templates.
 */
public class SearchExpressionContext extends ExpressionContext {

    private String timeZone;
    private String dateFormat;
    public SearchExpressionContext(ProcessingContext processingContext) {
        super(processingContext);
    }

    /**
     *
     * @param dateFormat
     * @param timeZone
     * @param processingContext
     */
    public SearchExpressionContext(String dateFormat, String timeZone,
                                   ProcessingContext processingContext) {
        super(processingContext);
        this.dateFormat = dateFormat;
        this.timeZone = timeZone;
    }

    /**
     * used by subclass for custom initialization.
     *
     * @param jc
     * @throws org.jahia.exceptions.JahiaException
     */
    public void init(JexlContext jc) throws JahiaException {
        super.init(jc);
        final Map<String, Object> vars = jc.getVars();
        DateBean dateBean = new DateBean(this.getParamBean(),dateFormat,timeZone);
        vars.put("dateBean", dateBean);
        vars.put("now", dateBean.getNow());
        vars.put("day", dateBean.getDay());
        vars.put("week", dateBean.getWeek());
        vars.put("month", dateBean.getMonth());
        vars.put("year", dateBean.getYear());
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

}
