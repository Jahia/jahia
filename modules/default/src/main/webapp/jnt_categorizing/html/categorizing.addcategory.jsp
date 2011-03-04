<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
    <c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ' ,')}"/>
    <template:addResources type="javascript" resources="jquery.js"/>
    <template:addResources type="css" resources="jquery.autocomplete.css"/>
    <template:addResources type="css" resources="thickbox.css"/>
    <template:addResources type="javascript" resources="jquery.autocomplete.js"/>
    <template:addResources type="javascript" resources="jquery.bgiframe.min.js"/>
    <template:addResources type="javascript" resources="thickbox-compressed.js"/>
    <script type="text/javascript">
        var uuids = new Array();
        <jcr:nodeProperty node="${bindedComponent}" name="j:defaultCategory" var="assignedCategories"/>
        <c:forEach items="${assignedCategories}" var="category" varStatus="status">
        <c:if test="${not empty category.node}">
        uuids.push("${category.node.identifier}");
        </c:if>
        </c:forEach>

        function addCategory(uuid, separator) {
            var isAlreadyExist = new Boolean();
            isAlreadyExist = false;
            for (i = 0; i < uuids.length; i++) {
                if (uuids[i] == $("#categorytoadd").val()) {
                    isAlreadyExist = true;
                }
            }
            if ($("#categorytoadd").val() != "" && !isAlreadyExist) {
                uuids.push($("#categorytoadd").val());
                $.post("${url.base}${bindedComponent.path}", {"j:defaultCategory":uuids,methodToCall:"put","jcr:mixinTypes":"jmix:categorized"}, function(result) {
                    var catContainer = jQuery('#jahia-categories-' + uuid);
                    if (jQuery(".nocategorizeditem" + uuid).length > 0) {
                        jQuery(".nocategorizeditem" + uuid).hide();
                        separator = '';
                    }

                    if (separator.length > 0 && jQuery('#jahia-categories-' + uuid + ' > span').length > 0) {
                        catContainer.append(separator);
                    }
                    var catDisplay = jQuery('<span class="categorizeditem">' + $("#category").val() + '</span>');
                    catDisplay.hide();
                    if (jQuery(".nocategorizeditem" + uuid).length > 0) {
                        jQuery(".nocategorizeditem" + uuid).replaceWith(catDisplay);
                    } else {
                        catContainer.append(catDisplay);
                    }
                    catDisplay.fadeIn('fast');
                    $("#category").val();
                }, "json");
            } else {
                return false;
            }
        }

    </script>
    <c:if test="${renderContext.user.name != 'guest'}">
        <label><fmt:message key="label.add.categories"/></label>
        <input type="hidden" id="categorytoadd"/>
        <input type="text" id="category" disabled="true"/>
        <fmt:message key="label.select.category" var="categoryLabel"/>
        <ui:treeItemSelector fieldId="categorytoadd"  valueType="identifier"
                             nodeTypes="jnt:category" selectableNodeTypes="jnt:category" displayIncludeChildren="false"
                             root="${jcr:getSystemSitePath()}/categories" label="${categoryLabel}" displayFieldId="category"/>
        <input type="submit" title="<fmt:message key='add'/>" value="<fmt:message key='add'/>" class="button"
               onclick="addCategory('${bindedComponent.identifier}', '${separator}')">
    </c:if>
</c:if>


