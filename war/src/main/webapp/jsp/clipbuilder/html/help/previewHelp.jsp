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
                            <td width="25%" class="topmenugreen bold waBG">Type</td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td class="topmenugreen bold waBG" width="50%">HttpClient</td>
                                        <td class="topmenugreen bold waBG" width="50%">HtmlUnit</td>
                                    </tr>
                                    <tr>
                                        <td>Url oriented client</td>
                                        <td>User oriented client</td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <!-- javascript -->
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">Enable Javascript</td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td class="topmenugreen bold waBG" width="50%">Checked</td>
                                        <td class="topmenugreen bold waBG" width="50%">Unchecked</td>
                                    </tr>
                                    <tr>
                                        <td>Emulate javascript in webClient side</td>
                                        <td>Doesn't emulate javscript</td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <!-- SSL -->
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">Enable SSL</td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td class="topmenugreen bold waBG" width="50%">Checked</td>
                                        <td class="topmenugreen bold waBG" width="50%">Unchecked</td>
                                    </tr>
                                    <tr>
                                        <td>Accept automatically all certificates during browse process. Allow Https
                                            connections
                                        </td>
                                        <td>Reject automatically all certificates</td>
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
                            <td width="25%" class="topmenugreen bold waBG">Type</td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td width="50%" class="topmenugreen bold waBG">HTMLParserDocument</td>
                                        <td class="topmenugreen bold waBG">DOMDocument</td>
                                    </tr>
                                    <tr>
                                        <td>Document based on a sax approch</td>
                                        <td>Document based on dom approcche</td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">Enable Css</td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td class="topmenugreen bold waBG" width="50%">Checked</td>
                                        <td class="topmenugreen bold waBG">Unchecked</td>
                                    </tr>
                                    <tr>
                                        <td>All linked Css from are removed the original document</td>
                                        <td>Remove linked Css from the original document. It will be the Css of the page
                                            container that will be applied
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
                            <td width="25%" class="topmenugreen bold waBG">Enable SSL</td>
                            <td>
                                <table rules="cols" border="1" width="100%">
                                    <tr>
                                        <td width="50%" class="topmenugreen bold waBG">Checked</td>
                                        <td width="50%" class="topmenugreen bold waBG">Unchecked</td>
                                    </tr>
                                    <tr>
                                        <td>Accept automatically all certificates AFTER clipping</td>
                                        <td>Refuse automatically all certificates AFTER clipping</td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">Continual Clipping</td>
                            <td>
                                <table border="1" rules="cols">
                                    <tr>
                                        <td class="topmenugreen bold waBG">false</td>
                                        <td class="topmenugreen bold waBG">active iframe</td>
                                        <td class="topmenugreen bold waBG">passif iframe</td>
                                        <td class="topmenugreen bold waBG">active popup</td>
                                        <td class="topmenugreen bold waBG">passif popup</td>
                                    </tr>
                                    <tr>
                                        <td>Clipping content is static and doesn't allow user to interact whith it</td>
                                        <td>Clipping content is not static. The user can click on any linf or button.
                                            The result will be shown in an iframe tha is embedded in the portlet. In
                                            addtion to that, the html is processed by the WebClient.
                                        </td>
                                        <td>Clipping content is not static. The user can click on any linf or button.
                                            The result will be shown in an iframe tha is embedded in the portlet. In
                                            addtion to that, the html is NOT processed by the WebClient.
                                        </td>
                                        <td>Clipping content is not static. The user can click on any linf or button.
                                            The result will be shown in another window.In addtion to that, the html is
                                            processed by the WebClient
                                        </td>
                                        <td>Clipping content is not static. The user can click on any linf or button.
                                            The result will be shown in another window.In addtion to that, the html is
                                            NOT processed by the WebClient
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
