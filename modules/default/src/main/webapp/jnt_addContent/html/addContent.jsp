<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<template:addResources type="javascript" resources="jquery.fancybox.pack.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<c:set var="target" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:forEach items="${currentNode.properties['j:type']}" var="type" varStatus="status">
    <jcr:nodeType name="${type.string}" var="nodeType"/>
    <a href="#add${currentNode.identifier}-${status.index}"  id="addButton${currentNode.identifier}-${status.index}">
        Add ${jcr:label(nodeType, renderContext.mainResourceLocale)}
    </a>

    <div style="display:none;">
        <div id="add${currentNode.identifier}-${status.index}"
             style="width:800px;">
            <template:module node="${target}" templateType="edit" template="add">
                <template:param name="resourceNodeType" value="${type.string}"/>
            </template:module>
        </div>
    </div>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#addButton${currentNode.identifier}-${status.index}").fancybox({
                'onComplete':function() {
                    $(".newContentCkeditorContribute${target.identifier}${fn:replace(nodeType.name,':','_')}").each(function() { $(this).ckeditor() })
                },

                'onCleanup':function() {
                    $(".newContentCkeditorContribute${target.identifier}${fn:replace(nodeType.name,':','_')}").each(function
                            () {
                        if ($(this).data('ckeditorInstance')) {
                            $(this).data('ckeditorInstance').destroy()
                        }
                    });
                }
            })
        });
    </script>
</c:forEach>
<template:linker property="j:bindedComponent"/>

