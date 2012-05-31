<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/> 
<template:addResources type="css" resources="jquery.fancybox.css"/>
<c:set var="target" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:set var="types" value="${jcr:getContributeTypes(target, null, currentNode.properties['j:type'])}"/>

<c:forEach items="${types}" var="nodeType" varStatus="status">
    <h3 class="titleaddnewcontent">
    <img title="" alt="" src="<c:url value='${url.currentModule}/images/add.png'/>"/>
    <a href="#add${currentNode.identifier}-${status.index}"  id="addButton${currentNode.identifier}-${status.index}">
        <fmt:message key="add"/> ${jcr:label(nodeType, renderContext.mainResourceLocale)}
    </a>
    </h3>

    <div style="display:none;">
        <div id="add${currentNode.identifier}-${status.index}"
             style="width:800px;">
            <template:module node="${target}" view="contribute.add">
                <template:param name="resourceNodeType" value="${nodeType.name}"/>
            </template:module>
        </div>
    </div>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#addButton${currentNode.identifier}-${status.index}").fancybox({
                'onComplete':function() {
                    $(".newContentCkeditorContribute${target.identifier}${fn:replace(nodeType.name,':','_')}").each(function() { $(this).ckeditor(); })
                    
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
        $("#add${currentNode.identifier}-${status.index}").css({'max-height':(window.innerHeight-100)+'px'});
    </script>
</c:forEach>
