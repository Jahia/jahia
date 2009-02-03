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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
