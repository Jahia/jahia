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

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
    <style type="text/css">
        <!--

        ol.attribute {
        /* border: 1px solid #CFD9E1; */
            display: block;
            padding: 2px;
            clear: both;
        }

        ol.attribute li {
            background: #CFD9E1;
            display: block;
            width: 100%;
        }

        div.map ol.entry {
            background: #CFD9E1;
            display: block;
            padding: 0;
            width: 100%;
            clear: both;
        }

        div.map ol.entry li {
            background: #CFD9E1;
            display: inline;
            float: left;
        }

        div.map ol.entry li.key {
            width: 12%;
        }

        div.map ol.entry li.key-type {
            width: 10%;
        }

        div.map ol.entry li.value-type {
            width: 20%;
        }

        div.map ol.entry li.value {
            width: 55%;
        }

        -->
    </style>
    <title>Session Viewer JSP</title>
</head>
<body>

<utility:sessionViewer/>
</body>
</html>