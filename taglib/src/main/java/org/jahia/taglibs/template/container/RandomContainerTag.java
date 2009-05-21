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
package org.jahia.taglibs.template.container;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.jahia.taglibs.AbstractJahiaTag;

/**
 * Class RandomContainerTag : Outside from ContainerListTag identifies a specific number of random displayed containers.
 * <p/>
 *
 * @author Werner Assek
 * @jsp:tag name="randomcontainer" body-content="tagdependent"
 */

@SuppressWarnings("serial")
public class RandomContainerTag extends AbstractJahiaTag {


    private static transient final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(RandomContainerTag.class);


    private int displayedContainer = 1;

    public int getDisplayedContainer() {
        return displayedContainer;
    }


    public void setDisplayedContainer(int displayedContainer) {
        this.displayedContainer = displayedContainer;
    }


    public int doStartTag() throws JspException {
        //pushTag();
        return EVAL_BODY_BUFFERED;
    }


    // Body is evaluated one time, so just writes it on standard output
    public int doAfterBody() {
        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
        } catch (IOException ioe) {
            logger.error("Error:", ioe);
        }
        return SKIP_BODY;
    }


    public int doEndTag() throws JspException {

        //JAVAScript to display the random container
        try {
            Integer containerCount = (Integer) pageContext.getRequest().getAttribute("randomCounter");

            if (containerCount != null) {
                String contName = (String) pageContext.getRequest().getAttribute("randomName");
                pageContext.getRequest().removeAttribute("randomCount");
                pageContext.getRequest().removeAttribute("randomName");
                final StringBuffer output = new StringBuffer();

                output.append("<script language=\"JavaScript\"><!--\n");
                output.append("function getRandom").append(contName).append("(Minimum, Maximum, count) { ");
                output.append("if( count > (Maximum - Minimum + 1)) { count = (Maximum - Minimum + 1); } ");
                output.append("var i = 0; var randomArray = new Array(count); ");
                output.append("while(i < count) { ");
                output.append("var randomNum = Math.floor(Minimum+(Maximum-Minimum+1)*(Math.random())); ");
                output.append("var doubled = false; ");
                output.append("for (var j = 0; !doubled && i > 0 && j < count; j++) { ");
                output.append("if(randomArray[j] == randomNum) { doubled = true; } } ");
                output.append("if(i == 0 || !doubled) { ");
                output.append("randomArray[i] = randomNum;  i ++; } } ");
                output.append("return randomArray; }\n");
                output.append("var arrayCount = getRandom").append(contName).append("(1,").append(containerCount).append(",")
                        .append(displayedContainer).append("); \n");
                output.append("var co = ").append(displayedContainer).append("; \n");
                output.append("if( co > ").append(containerCount).append(") { co = ").append(containerCount).append("; } ");
                output.append("for(var x = 0; x < co; x++) { \n");
                output.append("document.getElementById('randomC").append(contName)
                        .append("' + arrayCount[x]).style.display=\"block\";\n }\n ");
                output.append("//--> </script>");

                pageContext.getOut().print(output.toString());

            }

        } catch (final Exception e) {
            logger.error(e, e);
        }
        displayedContainer = 1;
        return EVAL_PAGE;
    }


}
