<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<c:set var="boundComponent" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty boundComponent && not jcr:isLockedAndCannotBeEdited(boundComponent)}">
    <c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ', ')}"/>
    <template:addResources type="javascript" resources="jquery.min.js"/>
    <template:addResources type="css" resources="jquery.autocomplete.css"/>
    <template:addResources type="css" resources="thickbox.css"/>
    <template:addResources type="javascript" resources="jquery.autocomplete.js"/>
    <template:addResources type="javascript" resources="jquery.bgiframe.min.js"/>
    <template:addResources type="javascript" resources="thickbox-compressed.js"/>
    <script type="text/javascript">
        $(document).ready(function() {
            $(".newTagInput${currentNode.identifier}").autocomplete("<c:url value='${url.base}${boundComponent.path}.matchingTags.do'/>", {
                multiple:true,
                multipleSeparator:",",
                dataType: "json",
                cacheLength: 1,
                parse: function parse(data) {
                    var parsed = [];
                    if(data.tags && data.tags.length > 0){
                        for (var i=0; i < data.tags.length; i++) {
                            parsed[parsed.length] = {
                                data: [data.tags[i].name],
                                value: data.tags[i].name,
                                result: data.tags[i].name
                            }
                        }
                    }
                    return parsed;
                }
            });
        });

        function addTag_${fn:replace(boundComponent.identifier, "-", "_")} (inputSelector) {
            var separator = ',';
            var $this = $(inputSelector);
            if ($this.val().length > 0) {
                var tagContainer = jQuery('#jahia-tags-${boundComponent.identifier}');
                if (tagContainer.length > 0 && tagContainer.find("span:contains('" + $this.val() + "')").length == 0) {
                    var options = {
                        url: "<c:url value="${url.base}${boundComponent.path}"/>.addTag.do",
                        type: "POST",
                        dataType: "json",
                        data: {tag: $this.val().split(',')},
                        traditional: true
                    };
                    $.ajax(options)
                            .done(function (result) {
                                if (result.addedTags && result.addedTags.length > 0) {
                                    for (var i = 0; i < result.addedTags.length; i++) {
                                        var $noTaggedItem = $(".notaggeditem${boundComponent.identifier}");
                                        if ($noTaggedItem.length > 0 && $noTaggedItem.is(":visible")) {
                                            $noTaggedItem.hide();
                                            separator = '';
                                        } else {
                                            separator = ',';
                                        }

                                        var tagDiv = $('<div></div>').attr('style', 'display:inline');
                                        var tagDisplay = $('<span class="taggeditem"></span>').text(result.addedTags[i].name);
                                        var tagLinkDelete = $('<a></a>').attr('onclick', 'deleteTag_${fn:replace(boundComponent.identifier, "-", "_")}(this); return false;').attr('class', 'delete').attr('href', '#');
                                        tagLinkDelete.attr('data-tag', result.addedTags[i].escapedName);
                                        tagContainer.append(tagDiv);
                                        if (separator.length > 0) {
                                            tagDiv.append(separator);
                                        }
                                        tagDiv.append(tagDisplay);
                                        tagDiv.append(tagLinkDelete);
                                        $this.val("");
                                    }
                                }
                            });
                }
            }
            return false;
        }

    </script>
    <c:if test="${renderContext.user.name != 'guest'}">
        <form action="/" method="post">
            <label><fmt:message key="label.add.tags"/></label>
            <input type="text" name="tag" class="newTagInput${currentNode.identifier}" value=""/>
            <input type="submit" title="<fmt:message key='add'/>" value="<fmt:message key='add'/>" class="button"
                    onclick="addTag_${fn:replace(boundComponent.identifier, "-", "_")} ('.newTagInput${currentNode.identifier}'); return false;"/>
        </form>
    </c:if>
</c:if>
