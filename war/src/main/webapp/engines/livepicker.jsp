<%@ page contentType="text/html;charset=UTF-8" language="java" %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
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
    <script type="text/javascript" src="../modules/assets/javascript/jquery.min.js"></script>
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