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

<%@ page language="java" session="false" %>
<%@ page import="org.jahia.bin.Jahia" %>
<%@ page import="org.jahia.utils.keygenerator.*" %>
<%@ page import="org.jahia.data.*" %>
<%@ page import="org.jahia.params.*" %>
<%@ page import="org.jahia.services.*" %>
<%@ page import="org.jahia.services.sites.*" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%

    Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    String theURL = jParams.settings().getJahiaEnginesHttpPath();
    Iterator sitesList = (Iterator) engineMap.get("sitesList");
    String jahiaDisplayMessage = Jahia.COPYRIGHT;

    // reset the session
    String idName = "jsessionid";
    String idValue = request.getRequestedSessionId();
    String sessionIdAddOn = "?" + idName + "=" + idValue;


%>


<html>
<head>
    <title><fmt:message key="org.jahia.engines.sites.Sites_Engine.virtualSitesTitle.label"/></title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}<fmt:message key="org.jahia.stylesheet.css"/>"
          type="text/css">
    <script type="text/javascript" src="<%=theURL%>../javascript/jahia.js"></script>
    <script type="text/javascript">
        // openSiteWindow
        // open a site in a window with a random name
        var myWin = null;

        function OpenSiteWindow(url, width, height) {

            var params = "width=" + width + ",height=" + height + ",resizable=1,scrollbars=1,status=1,dependent=0";
            var name = "jahiasite_" + Math.round(Math.random() * 10000);
            //TODO: Remove?

            delCookie('<%=idName%>');
            myWin = window.open(url, name, params);
            delCookie('<%=idName%>');

        } // end OpenSiteWindow


        function delCookie(name) {
            var hier = new Date();
            document.cookie = name + "=" + '';
            expires = ''
                    + hier.setTime(hier.getTime() - 1000 * 60 * 60 * 24 * 1000);
        }

    </script>
</head>

<body class="install" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<center>
<br>
&nbsp;<br>
&nbsp;<br>
<table cellpadding="1" cellspacing="0" bgcolor="#ffffff" width="530" border="0">
<tr>
<td>
<table cellpadding="0" cellspacing="0" width="530" border="0">
<tr>
<td bgcolor="#ffffff">
    <table width="530" height="63" border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td width="126" height="63"><img
                    src="${pageContext.request.contextPath}<fmt:message key="org.jahia.header.image"/>"
                    width="126" height="63" alt=""></td>
            <td width="404" height="63"
                background="${pageContext.request.contextPath}<fmt:message key="org.jahia.headerBg.image"/>">
                &nbsp;</td>
        </tr>
        <tr>
            <td align="right" class="text" colspan="2"><b>::&nbsp;&nbsp;<fmt:message key="org.jahia.engines.sites.Sites_Engine.virtualSites.label"/>&nbsp;&nbsp;::</b>&nbsp;&nbsp;&nbsp;&nbsp;
            </td>
        </tr>
    </table>
    <br>
<table cellpadding="2" cellspacing="0" border="0" width="100%">
    <tr>
        <td colspan="2" width="530">&nbsp;</td>
    </tr>
    <tr>
        <td width="100">&nbsp;&nbsp;&nbsp;</td>
        <td width="100%">
            <table border="0" cellpadding="0" width="90%">
                <tr>
                    <td nowrap><font class="text"><b><fmt:message key="org.jahia.engines.sites.Sites_Engine.virtualSitesUrl.label"/></b>
                        <br>
                        <br>
                        <br>
                        <br>
                    </font></td>
                </tr>
                <tr>
                    <td width="100%">
                        <table border="0" cellpadding="0" cellspacing="5" width="100%">
                            <tr>
                                <td valign="top" align="left" class="text" width="50%"><b>
                                    <fmt:message key="org.jahia.engines.name.label"/></b></td>
                                <td valign="top" align="right" class="text" width="25%"><b>
                                    <fmt:message key="org.jahia.engines.sites.Sites_Engine.url.label"/></b>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2"></td>
                            </tr>
                            <tr>
                                <td colspan="2" width="100%" height="2"
                                    background="${pageContext.request.contextPath}<fmt:message key="org.jahia.hr.image"/>">
                                    <img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image"/>"
                                         width="1" height="1"></td>
                            </tr>
                            <tr>
                                <td colspan="2"><br>
                                </td>
                            </tr>
                            <%
                                if (sitesList != null) {
                                    JahiaSite site = null;
                                    while (sitesList.hasNext()) {
                                        site = (JahiaSite) sitesList.next();
                            %>
                            <tr>
                                <td class="text" valign="top"><%=site.getTitle()%><br>
                                    <br>
                                </td>
                                <td class="text" valign="top" align="right"><a
                                        href="<%=jParams.composeSiteUrl(site)%>"
                                        target="_self"><%=site.getServerName()%></a>
                                </td>
                            </tr>
                            <%
                                }
                            } else { %>
                            <tr>
                                <td colspan="2" class="text"><fmt:message key="org.jahia.engines.sites.Sites_Engine.noSitesFound.label"/></td>
                            </tr>
                            <% } %>
                            <tr>
                                <td colspan="2"><br>
                                </td>
                            </tr>
                            <tr>
                                <td align="right" colspan="2"></td>
                            </tr>
                            <tr>
                                <td colspan="2" align="right"><br>
                                    <br>
                                    <a href="<%=jParams.composeSiteUrl()%>"
                                       onMouseOut="MM_swapImgRestore()"
                                       onMouseOver="MM_swapImage('logout','','${pageContext.request.contextPath}<fmt:message key="org.jahia.exitAdministrationOn.button"/>',1)"><img
                                            name="logout"
                                            src="${pageContext.request.contextPath}<fmt:message key="org.jahia.exitAdministrationOff.button"/>"
                                            width="150" height="17" border="0"></a>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="2" align="right"> &nbsp;<br>
            &nbsp;<br>
            <font class="text2"><%=jahiaDisplayMessage%>&nbsp;&nbsp;&nbsp;</font>
        </td>
    </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
</table>
</table>
</table>
</center>
</body>
</html>