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
<c:set var="bindedComponent" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
    <c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ', ')}"/>
    <template:addResources type="javascript" resources="jquery.js"/>
    <template:addResources type="css" resources="jquery.autocomplete.css"/>
    <template:addResources type="css" resources="thickbox.css"/>
    <template:addResources type="javascript" resources="jquery.autocomplete.js"/>
    <template:addResources type="javascript" resources="jquery.bgiframe.min.js"/>
    <template:addResources type="javascript" resources="thickbox-compressed.js"/>
    <script type="text/javascript">
        function addNewTag(tagForm, uuid, separator) {
            var newTag = tagForm.elements['j:newTag'];
            if (newTag.value.length > 0) {
                var tagContainer = jQuery('#jahia-tags-' + uuid);
                if(jQuery(".notaggeditem"+uuid).length>0){
                    jQuery(".notaggeditem"+uuid).hide();
                    separator = '';
                }
                if (tagContainer.find("span:contains('" + newTag.value + "')").length == 0) {
                    jQuery.post(tagForm.action, jQuery(tagForm).serialize(), function (data) {
                        if (separator.length > 0 && jQuery('#jahia-tags-' + uuid + ' > span').length > 0) {
                            tagContainer.append(separator);
                        }
                        var tagDisplay = jQuery('<span class="taggeditem">' + newTag.value + '</span>');
                        tagDisplay.hide();
                        if(jQuery(".notaggeditem"+uuid).length>0){
                            jQuery(".notaggeditem"+uuid).replaceWith(tagDisplay);
                        } else {
                            tagContainer.append(tagDisplay);
                        }
                        tagDisplay.fadeIn('fast');
                        newTag.value = '';
                    });
                }
            }
        }

        $(document).ready(function() {

            function getText(node) {
                return node["j:nodename"];
            }

            function format(result) {
                return getText(result["node"]);
            }

            $(".newTagInput").autocomplete("${url.find}", {
                dataType: "json",
                cacheLength: 1,
                parse: function parse(data) {
                    return $.map(data, function(row) {
                        return {
                            data: row,
                            value: getText(row["node"]),
                            result: getText(row["node"])
                        }
                    });
                },
                formatItem: function(item) {
                    return format(item);
                },
                extraParams: {
                    query : "/jcr:root${renderContext.site.path}/tags//element(*, jnt:tag)[jcr:contains(.,'{$q}*')]/@j:nodename",
                    language : "xpath",
                    escapeColon : "false",
                    propertyMatchRegexp : "{$q}.*",
                    removeDuplicatePropValues : "false"
                }
            });
        });

    </script>
    <c:if test="${renderContext.user.name != 'guest'}">
        <form action="${url.base}${bindedComponent.path}" method="post">
            <label><fmt:message key="label.add.tags"/></label>
            <input type="hidden" name="methodToCall" value="put"/>
            <input type="hidden" name="jcr:mixinTypes" value="jmix:tagged"/>
            <input type="text" name="j:newTag" class="newTagInput" value=""/>
            <input type="submit" title="<fmt:message key='add'/>" value="<fmt:message key='add'/>" class="button"
                   onclick="addNewTag(this.form, '${bindedComponent.identifier}', '${separator}'); return false;"/>
        </form>
    </c:if>
</c:if>
