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

<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="nested" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<!-- Main -->
<html>
<head>
    <link rel="stylesheet"
          href="<%=response.encodeURL(request.getContextPath()+"/html/startup/clipbuilder/web_css.jsp?colorSet=blue")%>"
          type="text/css"/>
    <link rel="stylesheet"
          href="<%=response.encodeURL(request.getContextPath()+"/html/startup/clipbuilder/style.css")%>"
          type="text/css"/>
    <title>
        <bean:message key="configuration.title"/>
    </title>
</head>
<body>
<table bgcolor="white" align="center" width="100%">
<!-- Main -->
<table width="100%">
<!-- Connection -->
<tr>
    <td>
        <table width="100%">
            <!-- title-->
            <tr>
                <td class="boxtitlecolor1">
                    <bean:message key="connection.title"/>
                </td>
            </tr>
            <!-- Proxy -->
            <tr>
                <td>
                    <table width="100%">
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">
                                <bean:message key="connection.proxy"/>
                            </td>
                            <td>
                                <bean:message key="help.connection.proxy"/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </td>
</tr>
<!-- Browser -->
<tr>
    <td>
        <table width="100%">
            <tr>
                <td class="boxtitlecolor1">
                    <bean:message key="configuration.browser.title"/>
                </td>
            </tr>
            <!-- Type/javascript/ssl -->
            <tr>
                <td>
                    <table width="100%">
                        <tr>
                            <!-- Option -->
                            <td width="25%" class="topmenugreen bold waBG">
                                <bean:message key="configuration.browser.javascript.code"/>
                            </td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <!-- possible values-->
                                        <td width="33%" class="topmenugreen bold waBG">
                                            <bean:message key="configuration.browser.javascript.code.remove"/>
                                        </td>
                                        <td width="33%" class="topmenugreen bold waBG">
                                            <bean:message key="configuration.browser.javascript.code.refactor"/>
                                        </td>
                                        <td class="topmenugreen bold waBG">
                                            <bean:message key="configuration.browser.javascript.code.nothing"/>
                                        </td>
                                    </tr>
                                    <!-- Explication-->
                                    <tr>
                                        <td>
                                            <bean:message key="help.configuration.browser.javascript.code.remove"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.browser.javascript.code.refactor"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.browser.javascript.code.nothing"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <!-- Javascript code -->
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">
                                <bean:message key="configuration.browser.javascript.event"/>
                            </td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td width="33%" class="topmenugreen bold waBG">
                                            <bean:message key="configuration.browser.javascript.event.remove"/>
                                        </td>
                                        <td width="33%" class="topmenugreen bold waBG">
                                            <bean:message key="configuration.browser.javascript.event.refactor"/>
                                        </td>
                                        <td class="topmenugreen bold waBG">
                                            <bean:message key="configuration.browser.javascript.event.nothing"/>
                                        </td>
                                    </tr>
                                    <!-- Explication-->
                                    <tr>
                                        <td class="leftlevel2 waInput">
                                            <bean:message key="help.configuration.browser.javascript.event.remove"/>
                                        </td>
                                        <td class="leftlevel2 waInput">
                                            <bean:message key="help.configuration.browser.javascript.event.refactor"/>
                                        </td>
                                        <td class="leftlevel2 waInput">
                                            <bean:message key="help.configuration.browser.javascript.event.nothing"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <!-- Javascript Event -->
                    </table>
                </td>
            </tr>
        </table>
    </td>
</tr>
<!-- Web Client -->
<tr>
    <td>
        <table width="100%">
            <tr>
                <td class="boxtitlecolor1">
                    <bean:message key="client.title"/>
                </td>
            </tr>
            <!-- Type/javascript/ssl -->
            <tr>
                <td>
                    <table width="100%">
                        <!-- Type -->
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">
                                <bean:message key="client.type"/>
                            </td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td class="topmenugreen bold waBG" width="50%">
                                            <bean:message key="client.type.httpClient"/>
                                        </td>
                                        <td class="topmenugreen bold waBG" width="50%">
                                            <bean:message key="client.type.htmlunit"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <bean:message key="help.configuration.httpclient"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.htmlunit"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <!-- javascript -->
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">
                                <bean:message key="client.javascript.enable"/>
                            </td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td class="topmenugreen bold waBG" width="50%">
                                            <bean:message key="help.configuration.checked"/>
                                        </td>
                                        <td class="topmenugreen bold waBG" width="50%">
                                            <bean:message key="help.configuration.unchecked"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <bean:message key="help.configuration.emulateJavascript.true"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.emulateJavascript.false"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <!-- SSL -->
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">
                                <bean:message key="client.ssl.enable"/>
                            </td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td class="topmenugreen bold waBG" width="50%">
                                            <bean:message key="help.configuration.checked"/>
                                        </td>
                                        <td class="topmenugreen bold waBG" width="50%">
                                            <bean:message key="help.configuration.unchecked"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <bean:message key="help.configuration.certificat.true"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.certificat.false"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </td>
</tr>
<!-- Html Document -->
<tr>
    <td>
        <table width="100%">
            <!-- Title  -->
            <tr>
                <td class="boxtitlecolor1">
                    <bean:message key="htmlDocument.title"/>
                </td>
            </tr>
            <tr>
                <td>
                    <table width="100%">
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">
                                <bean:message key="htmlDocument.type"/>
                            </td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td width="50%" class="topmenugreen bold waBG">
                                            <bean:message key="htmlDocument.type.htmlparser"/>
                                        </td>
                                        <td class="topmenugreen bold waBG">
                                            <bean:message key="htmlDocument.type.dom"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <bean:message key="help.configuration.htmlparser"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.dom"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">
                                <bean:message key="htmlDocument.css.enable"/>
                            </td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td class="topmenugreen bold waBG" width="50%">
                                            <bean:message key="help.configuration.checked"/>
                                        </td>
                                        <td class="topmenugreen bold waBG">
                                            <bean:message key="help.configuration.unchecked"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <bean:message key="help.configuration.css.true"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.css.false"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </td>
</tr>
<!-- Portlet -->
<tr>
    <td>
        <table width="100%">
            <!-- Title  -->
            <tr>
                <td class="boxtitlecolor1">
                    <bean:message key="portlet.config.title"/>
                </td>
            </tr>
            <tr>
                <td>
                    <table width="100%">
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">
                                <bean:message key="portlet.config.ssl"/>
                            </td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td width="50%" class="topmenugreen bold waBG">
                                            <bean:message key="help.configuration.checked"/>
                                        </td>
                                        <td width="50%" class="topmenugreen bold waBG">
                                            <bean:message key="help.configuration.unchecked"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <bean:message key="help.configuration.certificat.afterClipping.true"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.certificat.afterClipping.false"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">
                                <bean:message key="portlet.config.clipbuilder"/>
                            </td>
                            <td>
                                <table border="1" rules="cols">
                                    <tr>
                                        <td class="topmenugreen bold waBG">
                                            <bean:message key="portlet.config.clipbuilder.iframe.actif"/>
                                        </td>
                                        <td class="topmenugreen bold waBG">
                                            <bean:message key="portlet.config.clipbuilder.iframe.passif"/>
                                        </td>
                                        <td class="topmenugreen bold waBG">
                                            <bean:message key="portlet.config.clipbuilder.popup.actif"/>
                                        </td>
                                        <td class="topmenugreen bold waBG">
                                            <bean:message key="portlet.config.clipbuilder.popup.passif"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <bean:message key="help.configuration.actifIframe"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.passifIframe"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.actifPopup"/>
                                        </td>
                                        <td>
                                            <bean:message key="help.configuration.passifPopup"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </td>
</tr>
</table>
</table>
</body>
</html>
