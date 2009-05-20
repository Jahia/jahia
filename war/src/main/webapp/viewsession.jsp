<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

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