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
                            Buttons
                        </td>
                    </tr>
                    <!-- button -->
                    <tr>
                        <td>
                            <table width="100%">
                                <!-- Clear -->
                                <tr>
                                    <td width="25%" class="topmenugreen bold waBG">
                                        Clear
                                    </td>
                                    <td>
                                        Remove all saved Urls from the clipper.
                                    </td>
                                </tr>
                                <!-- Remove -->
                                <tr>
                                    <td width="25%" class="topmenugreen bold waBG">
                                        Remove
                                    </td>
                                    <td>
                                        Remove the last saved Url from the clipper.
                                    </td>
                                </tr>
                                <!-- Start -->
                                <tr>
                                    <td width="25%" class="topmenugreen bold waBG">
                                        Start
                                    </td>
                                    <td>
                                        Start recording urls.
                                    </td>
                                </tr>
                                <!-- Stop -->
                                <tr>
                                    <td width="25%" class="topmenugreen bold waBG">
                                        Stop
                                    </td>
                                    <td>
                                        Stop recording urls.
                                    </td>
                                </tr>
                                <!-- Next -->
                                <tr>
                                    <td width="25%" class="topmenugreen bold waBG">
                                        Next
                                    </td>
                                    <td>
                                        Stop recording and go to the next step.
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
