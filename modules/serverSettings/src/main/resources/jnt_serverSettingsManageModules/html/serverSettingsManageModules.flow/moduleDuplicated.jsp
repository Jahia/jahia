<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<template:addResources type="javascript" resources="jquery.min.js"/>
<c:url var="urlToStudio" value="/cms/studio/default/${currentResource.locale}/modules/${newModuleId}.html"/>
<template:addResources type="inlinejavascript">
    <script type="text/javascript">
        $(document).ready(function() {
            if (typeof(Storage) !== "undefined") {
                $(window.parent).unload(function() {
                    var path = window.parent.sessionStorage.getItem("adminmode_path");
                    var index = path.indexOf("?");
                    if (index > -1) {
                        window.parent.sessionStorage.setItem("adminmode_path", path.substring(0, index));
                    }
                });
            }
            window.parent.location.assign("${urlToStudio}");
        });
    </script>
</template:addResources>
