<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="http://struts.apache.org/tags-nested" prefix="nested"%>
<%@taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>

<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@page language="java" import="java.util.*"%>
<html:html>
<head>
  <link rel="stylesheet" href="<%=response.encodeURL(request.getContextPath()+"/html/startup/clipbuilder/web_css.jsp?colorSet=blue")%>" type="text/css"/>
  <link rel="stylesheet" href="<%=response.encodeURL(request.getContextPath()+"/html/startup/clipbuilder/style.css")%>" type="text/css"/>
</head>
<body>
  <!-- Error-->
  <html:form action="webBrowser" method="post">
    <logic:messagesPresent>
      <br/>
      <!-- Error -->
      <table align="center" bgcolor="#800000" cellpadding="0" cellspacing="1" width="100%">
        <tbody>
          <tr>
            <td>
              <table align="center" bgcolor="#ffffff" border="0" width="100%">
                <tbody>
                  <tr>
                    <td align="center" bgcolor="#dcdcdc">
                      <font color="#cc0000">
                        <bean:message key="error.error"/>
                      </font>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <UL>
                        <html:messages id="error">
                          <LI> <bean:write name="error"/> </LI>
                        </html:messages>
                      </UL>
                    </td>
                  </tr>
                </tbody>
              </table>
            </td>
          </tr>
        </tbody>
      </table>
      <br/>
      <!-- Possible solutions -->
      <table align="center" bgcolor="#800000" cellpadding="0" cellspacing="1" width="100%">
        <tbody>
          <tr>
            <td>
              <table align="center" bgcolor="#ffffff" border="0" width="100%">
                <tbody>
                  <tr>
                    <td align="center" bgcolor="#dcdcdc">
                      <font color="#cc0000">
                        <bean:message key="error.solution"/>
                      </font>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <UL>
                        <LI> <bean:message key="error.solution.browser.javscript"/> </LI>
                        <LI> <bean:message key="error.solution.client.javascript"/> </LI>
                        <LI> <bean:message key="error.solution.url"/> </LI>
                        <LI> <bean:message key="error.solution.client"/> </LI>
                        <LI> <bean:message key="error.solution.page.frame"/> </LI>
                        <LI> <bean:message key="error.solution.htmldocument"/> </LI>
                        <LI> <bean:message key="error.solution.reportBug"/> </LI>
                      </UL>
                    </td>
                  </tr>
                </tbody>
              </table>
            </td>
          </tr>
        </tbody>
      </table>
    </logic:messagesPresent>
  </html:form>
</body>
</html:html>
