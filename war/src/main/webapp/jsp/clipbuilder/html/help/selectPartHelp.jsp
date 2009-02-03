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
<!-- Manual selection -->
<tr>
    <td>
        <table width="100%">
            <!-- title-->
            <tr>
                <td class="boxtitlecolor1">Manual</td>
            </tr>
            <!-- Info -->
            <tr>
                <td>
                    <table width="100%">
                        <!-- Description -->
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">Description</td>
                            <td>Allow to select manually part of the final document.</td>
                        </tr>
                        <!-- How to -->
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">How to</td>
                            <td> Select the desired whith the mouse part and then click on "Extract and preview" button.
                                <br/> Warning: this method works only whith Internet Explorer. (Javascript limitation)
                            </td>
                        </tr>
                        <!-- When to use -->
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">When to use</td>
                            <td> Used only if other methods do not allow to select the whished part.
                            </td>
                        </tr>
                        <!-- Warning -->
                        <tr>
                            <td width="25%" class="topmenugreen bold waBG">Warning</td>
                            <td>Used only if other methods not allow to select the desired part.</td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </td>
</tr>
<!-- Chew-->
<tr>
    <td>
    <table width="100%">
        <!-- title-->
        <tr>
            <td class="boxtitlecolor1">Chew</td>
        </tr>
        <!-- Info -->
        <tr>
            <td>
                <table width="100%">
                    <!-- Description -->
                    <tr>
                        <td width="25%" class="topmenugreen bold waBG">Description</td>
                        <td>Allow to select a part of the final document by cutting the page throught a tag and then
                            choosing the disired part.
                        </td>
                    </tr>
                    <!-- How to use -->
                    <tr>
                        <td width="25%" class="topmenugreen bold waBG">How to use</td>
                        <td>
                            <ul>
                                <li> Select the tag through which the document will be cut out</li>
                                <li> Choose the desired part by clicking on "this"</li>
                            </ul>
                        </td>
                    </tr>
                    <!-- When to use -->
                    <tr>
                        <td width="25%" class="topmenugreen bold waBG">When to use</td>
                        <td>Used when the struture of the target document doesn't vary to much.</td>
                    </tr>
                    <!-- Warning -->
                    <tr>
                        <td width="25%" class="topmenugreen bold waBG">Warning</td>
                        <td> The tag through which the document will be cut out has to be choosen carefully.
                            <br/>
                            For example: if the tag "table" is choosen, then the selected part may not contain all
                            parameters of its parent form.
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <!-- Xpath-->
        <tr>
            <td>
                <table width="100%">
                    <!-- title-->
                    <tr>
                        <td class="boxtitlecolor1">Xpath</td>
                    </tr>
                    <!-- Info -->
                    <tr>
                        <td>
                            <table width="100%">
                                <!-- Description -->
                                <tr>
                                    <td width="25%" class="topmenugreen bold waBG">Description</td>
                                    <td>Allow to select a part of the final document thanks to Xpath.(See <a
                                            href="http://www.w3schools.com/xpath/default.asp"> short introduction</a>)
                                    </td>
                                </tr>
                                <!-- How to use -->
                                <tr>
                                    <td width="25%" class="topmenugreen bold waBG">How to use</td>
                                    <td>
                                        <ul>
                                            <li> Enter a valid xml path.</li>
                                            <li> Click on "Extract and preview" button</li>
                                        </ul>
                                    </td>
                                </tr>
                                <!-- When to use -->
                                <tr>
                                    <td width="25%" class="topmenugreen bold waBG">When to use</td>
                                    <td></td>
                                </tr>
                                <!-- Warning -->
                                <tr>
                                    <td width="25%" class="topmenugreen bold waBG">Warning</td>
                                    <td> The Xpath has to be written carefully in order to always extract the desired
                                        part when
                                        the clipper replays urls sequence. <br/>
                                        If the xpath is not valid, the selected part is empty (white page).
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>


</body>
</html>
