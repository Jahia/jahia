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
