<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <script>
        var configTree = {
            context : "${pageContext.request.contextPath}" == "/" ? "" : "${pageContext.request.contextPath}"
        }
    </script>
    <link rel="stylesheet" href="../modules/assets/css/jquery-ui.smoothness.css" media="screen" type="text/css"/>
    <link rel="stylesheet" href="../modules/assets/css/jquery-ui.smoothness-jahia.css" media="screen" type="text/css"/>

    <link rel="stylesheet" href="../modules/assets/css/jquery.treeview.css" media="screen" type="text/css"/>
    <style>

        img {
            border: none;
        }

        /*  */

        #treepreview {
            position: absolute;
            border: 1px solid #ccc;
            background: #333;
            padding: 5px;
            display: none;
            color: #fff;
            z-index:9999;
        }

        /*  */
    </style>
</head>
<body>
<ul id="imagepicker-treeItemSelectorTree"></ul>
<fmt:message key="label.select.file" var="fileLabel"/>
<script>
    // Helper function to get parameters from the query string.
    function getUrlParam(paramName) {
        var reParam = new RegExp('(?:[\?&]|&amp;)' + paramName + '=([^&]+)', 'i');
        var match = window.location.search.match(reParam);

        return (match && match.length > 1) ? match[1] : '';
    }

    var funcNum = getUrlParam('CKEditorFuncNum');
    var base = getUrlParam('base');
    var files = getUrlParam('files');
    var root = getUrlParam('root');
    var type = getUrlParam('type');
    if (type == '') {
        type = 'file';
    }
    $(document).ready(function() {
        var queryString = "nodeTypes=" +
                          encodeURIComponent(type == 'pages' ? 'jnt:page' : 'nt:folder,nt:file,jnt:virtualsite') +
                          "&selectableNodeTypes=" + encodeURIComponent(type == 'pages' ? 'jnt:page' : 'nt:file');
        queryString = queryString.length > 0 ? "?" + queryString : "";
        $("#imagepicker-treeItemSelectorTree").treeview($.extend({
            urlBase: base,
            urlExtension: ".tree.json" + queryString,
            urlStartWith: base + root + ".treeRootItem.json" + queryString,
            url: base + root + ".treeRootItem.json" + queryString,
            preview:type=='file',
            previewPath:files,
            callback: function (uuid, path, title) {
                window.opener.CKEDITOR.tools.callFunction(funcNum, files + path + (type == 'pages' ? '.html' : ''));
                window.close();
            }
        }, {}));
    });
</script>
</body>
</html>