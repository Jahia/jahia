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

package org.jahia.taglibs.internal.i18n;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.jahia.data.JahiaData;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.JahiaConsole;
import org.jahia.utils.LanguageCodeConverters;


/**
 * Class I18nTag : support for I18n within Jahia
 * Do not use this tag in the same time with the JSP directive :
 * <%@ page contentType= "text/html; charset = ... %>
 *
 * (now @deprecated behavior: The charset attribute is only used when
 * JData.params().settings().getCharSet().equals("") is true)
 *
 * @author Khue Nguyen
 *
 *jsp:tag name="i18n" body-content="empty"
 * description="sets the encoding charset and the content type of the page.
 *
 * <p><attriInfo>If the users's locale is defined, this tag also specifies the language of the document using
 * the HTML \"Content-Language\" attribute of the &lt;meta&gt; tag. It also sets the
 *  the \"Vary: Accept-Language\" request-header to ensure that all HTTP/1.1 Proxy
 * servers will serve the proper content in different languages to your users.
 * <p>Do not use this tag in conjunction with the JSP directive : <br>
 * &lt;%@ page contentType= \"text/html; charset = ... %&gt;
 * <p>Read <a href='http://java.sun.com/j2ee/1.4/docs/tutorial-update2/doc/WebI18N5.html'>background info</a> for more details on
 * character sets and encoding.
 *
 * <p><b>Example :</b>
 * <p>&lt;content:i18n/&gt;
 * </attriInfo>"
 */
public class I18nTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(I18nTag.class);

    protected String contentType = "text/html"; // default value
    protected String charSet	 = "ISO-8859-1";

    private String pageContentType = "";

    /**
        * jsp:attribute name="pageContentType" required="false" rtexprvalue="true" type="Boolean"
        * description="set the pageEncoding attribute of the page or not.
        *
        * <p><attriInfo>The pageEncoding attribute defines the character encoding for the JSP page i.e.
        * &lt;%@ page contentType= \"text/html; charset = ... %&gt; .
        * <p>Default is \"false\".
        * </attriInfo>"
     */
    public void setPageContentType (String pageContentType) {
      this.pageContentType = pageContentType;
    }

    public String getPageContentType() {
      return this.pageContentType;
    }

    /**
        * jsp:attribute name="contentType" required="false" rtexprvalue="true"
        * description="sets the contentType attribute of the response.
        *
        * <p><attriInfo>The contentType attribute sets the MIME type and the character set for the response.
        * <p>Default is \"text/html\".
        * </attriInfo>"
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
         * jsp:attribute name="charSet" required="false" rtexprvalue="true"
         * description="sets the character set encoding attribute of the response.
         *
         * <p><attriInfo>This attribute is ignored if UTF-8 is already selected and therefore used instead.
         * <p>Default is \"ISO-8859-1\".
         * </attriInfo>"
     */
    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    public int doStartTag() {

        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        // Caution : UTF-8 is not realy a charset !!
        String jahiaCharSet = "";
        if (jData.getProcessingContext().settings().isUtf8Encoding()) {
            jahiaCharSet = "UTF-8";
        }
        //System.out.println(CLASS_NAME+".doStartTag: Jahia Char Set is " + jahiaCharSet);

        StringBuffer buff = new StringBuffer(contentType);
        buff.append(";");
        if (!jahiaCharSet.equals("")) {
            buff.append("charset=");
            buff.append(jahiaCharSet);
        } else if (!charSet.equals("")) {
            buff.append("charset=");
            buff.append(charSet);
        }

        pageContext.getResponse().setContentType(buff.toString());
        logger.debug("Content type : " + buff.toString());

        if (pageContentType.trim().equals("true")) {
          StringBuffer pageContentTypeBuf = new StringBuffer(
              "<%@ page contentType=\"text/html,charset=");
          if (!jahiaCharSet.equals("")) {
            pageContentTypeBuf.append(jahiaCharSet);
          } else if (!charSet.equals("")) {
            pageContentTypeBuf.append(charSet);
          }
          pageContentTypeBuf.append("\" %>");
          try {
            JspWriter out = pageContext.getOut();
            out.print(pageContentTypeBuf.toString());
          }
          catch (IOException ioe) {
            JahiaConsole.println("I18nTag: doStartTag ", ioe.toString());
          }
        }

        Locale locale = jData.getProcessingContext().getLocale();
        if ( locale != null ){
            ((HttpServletResponse) pageContext.getResponse()).setHeader("Content-Language", LanguageCodeConverters.localeToLanguageTag(locale));
            ((HttpServletResponse) pageContext.getResponse()).setHeader("Vary","Accept-Language");
        }
        return SKIP_BODY;
    }


    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        contentType = "text/html"; // default value
        charSet	 = "ISO-8859-1";
        pageContentType = "";
        return EVAL_PAGE;
    }
}
