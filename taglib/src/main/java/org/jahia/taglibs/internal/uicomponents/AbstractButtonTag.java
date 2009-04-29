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
 package org.jahia.taglibs.internal.uicomponents;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.taglibs.utility.Utils;

/**
 * Class AbstractButtonTag : defines common code for different buttons
 *
 * @author  Jerome Tamiotti
 */
@SuppressWarnings("serial")
public abstract class AbstractButtonTag extends TagSupport {

    /** logging */
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(AbstractButtonTag.class);

    public int doStartTag() {

        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        // only used in rollover tags
        checkCounter(request);

        // makes a test on rights
        if (!testRights(jData)) {
            return SKIP_BODY;
        }
        String link = buildButton(jData, request);
        if (link != null) {
            try {
                JspWriter out = pageContext.getOut();
                out.print( link );
            } catch (IOException ioe) {
                logger.error(ioe.toString(), ioe);
            }
        }
        return SKIP_BODY;
    }


    public void checkCounter(HttpServletRequest request) {
        // does not perform anything, defined only for rollover tags
        return;
    }

    public String buildButton (JahiaData jData, HttpServletRequest request) {

        String url = null;
        try {
            url = getLauncher(jData);
        } catch (JahiaException jex) {
            logger.error( "Can not get launcher from subclass !", jex);
            return null;
        }

		if ( url == null || url.trim().equals("") )
			return url;

        StringBuffer text = new StringBuffer( "<a " );
        String style = getStyle();
        if ( !style.equals("") ) {
            text.append( "class=\"" );
            text.append( style );
            text.append( "\" " );
        }
        text.append( "href=\"" );
        text.append( url );
        text.append( "\">" );
        text.append( Utils.insertContextPath( jData.gui().drawHttpJspContext(request), getTitle() ) );
        text.append( "</a>" );
        return text.toString();
    }


    // The following methods will be implemented in the real tags
    public abstract boolean testRights(JahiaData jData);

    public abstract String getLauncher(JahiaData jData) throws JahiaException;

    public abstract String getTitle();

    public abstract String getStyle();

}
