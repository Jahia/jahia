<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="option" type="org.jahia.services.content.nodetypes.initializers.ChoiceListValue"--%>
<template:addResources type="css" resources="jquery.autocomplete.css"/>
<template:addResources type="css" resources="thickbox.css"/>
<template:addResources type="javascript" resources="jquery.autocomplete.js"/>
<template:addResources type="javascript" resources="jquery.bgiframe.min.js"/>
<template:addResources type="javascript" resources="thickbox-compressed.js"/>

<template:addResources>
<script type="text/javascript">
    $(document).ready(function() {

        $("#${currentNode.name}").autocomplete("${url.initializers}", {
            dataType: "json",
            parse: function parse(data) {
                return $.map(data, function(row) {
                    return {
                        data: row,
                        value: "" + row["value"],
                        result: "" + row["name"]
                    }
                });
            },
            formatItem: function(item) {
                return "" + item["name"][0];
            },
            extraParams: {
                initializers : "${fn:split(currentNode.properties.type.string,';')[0]}",
                path : "${currentNode.path}",
                name : "type"
            }
        });
    });
</script>
</template:addResources>
<p class="field">
    <label for="${currentNode.name}">${currentNode.properties.label.string}</label>
    <input type="text" id="${currentNode.name}" name="${currentNode.name}" value="<c:if test="${not empty sessionScope.formError}">${sessionScope.formDatas[currentNode.name][0]}</c:if>"/>
</p>