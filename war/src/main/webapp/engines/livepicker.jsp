<%@ page contentType="text/html;charset=UTF-8" language="java" %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/functions" prefix="functions" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="ui" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<html>
<head>
    <title><fmt:message
            key="org.jahia.admin.sitepermissions.permission.engines.importexport.ManageContentPicker.label"/></title>
    <script> var configTree={ context : "${pageContext.request.contextPath}" }; </script>
    <link rel="stylesheet" href="../modules/assets/css/jquery-ui.smoothness.css" media="screen" type="text/css"/>
    <link rel="stylesheet" href="../modules/assets/css/jquery-ui.smoothness-jahia.css" media="screen" type="text/css"/>
    <link rel="stylesheet" href="../modules/assets/css/jquery.treeview.css" media="screen" type="text/css"/>
    <script type="text/javascript" src="../modules/assets/javascript/jquery.js"></script>
    <script type="text/javascript" src="../modules/assets/javascript/jquery.jeditable.js"></script>
    <script type="text/javascript" src="../modules/assets/javascript/jquery.ajaxfileupload.js"></script>
    <script type="text/javascript" src="../modules/assets/javascript/jquery.jeditable.ajaxupload.js"></script>
    <script type="text/javascript" src="../modules/assets/javascript/jquery.treeview.min.js"></script>
    <script type="text/javascript" src="../modules/assets/javascript/jquery.treeview.async.jahia.js"></script>
    <script type="text/javascript" src="../modules/assets/javascript/jquery.fancybox.js"></script>
    <script type="text/javascript" src="../modules/assets/javascript/jquery.defer.js"></script>
    <script type="text/javascript" src="../modules/assets/javascript/treeselector.js"></script>
    <script type="text/javascript" src="../modules/assets/javascript/jquery.jeditable.treeItemSelector.js"></script>
    <script type="text/javascript" src="livepicker.js"></script>
    <style>
        body {
            background-color: white;
        }
        img {
            border: none;
        }
        #treepreview {
            position: absolute;
            border: 1px solid #ccc;
            background: #333;
            padding: 5px;
            display: none;
            color: #fff;
            z-index: 9999;
        }
    </style>
</head>
<body>
<div class="bodywrapper">
    <ul id="imagepicker-treeItemSelectorTree"></ul>
</div>
</body>
</html>