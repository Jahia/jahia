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
        function addNewTag(tagForm, uuid, separator) {
            var regExp = /\s/g;
            var newTag = tagForm.elements['j:newTag'];
            if (newTag.value.length > 0) {
                var tagContainer = jQuery('#jahia-tags-' + uuid);
                if(jQuery(".notaggeditem"+uuid).length>0  && $(".notaggeditem" + uuid).is(":visible")){
                    jQuery(".notaggeditem"+uuid).hide();
                    separator = '';
                }
                if (tagContainer.find("span:contains('" + newTag.value + "')").length == 0) {
                    jQuery.post(tagForm.action, jQuery(tagForm).serialize(), function (data) {
                        var tagToAdds = newTag.value.split(',');
                        for (i =0; i< tagToAdds.length; i++) {
                        	var theTag = tagToAdds[i] != null ? jQuery.trim(tagToAdds[i]) : null;
                            if(theTag != null && theTag.length > 0){
                                var tagDiv = $('<div></div>').attr('id', 'tag-' + theTag.replace(regExp, "-")).attr('style', 'display:inline');
                                var tagDisplay = jQuery('<span class="taggeditem">' + theTag + '</span>');
                                var tagLinkDelete = $('<a></a>').attr('onclick', 'deleteTag(\'' + theTag + '\')').attr('class', 'delete').attr('href', '#');
                                tagContainer.append(tagDiv);
                                if (separator.length > 0) {
                                    tagDiv.append(separator);
                                }
                                tagDiv.append(tagDisplay);
                                tagDiv.append(tagLinkDelete);
                            }
                        }
                        newTag.value = '';
                    },"json");
                }
            }
        }

        $(document).ready(function() {

            function getText(node) {
                return node["nodename"];
            }

            function format(result) {
                return getText(result["node"]);
            }

            $(".newTagInput").autocomplete("<c:url value='${url.find}'/>", {
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
                    query : "select * from [jnt:tag] as tags where isdescendantnode(tags,'${functions:sqlencode(renderContext.site.path)}/tags') and localname(tags) like '%{$q}%'",
                    escapeColon : "false",
                    propertyMatchRegexp : "{$q}.*",
                    removeDuplicatePropValues : "false"
                }
            });
        });

    </script>
    <c:if test="${renderContext.user.name != 'guest'}">
        <form action="<c:url value='${url.base}${boundComponent.path}'/>" method="post">
            <label><fmt:message key="label.add.tags"/></label>
            <input type="hidden" name="jcrMethodToCall" value="put"/>
            <input type="hidden" name="jcr:mixinTypes" value="jmix:tagged"/>
            <input type="text" name="j:newTag" class="newTagInput" value=""/>
            <input type="submit" title="<fmt:message key='add'/>" value="<fmt:message key='add'/>" class="button"
                   onclick="addNewTag(this.form, '${boundComponent.identifier}', '${separator}'); return false;"/>
        </form>
    </c:if>
</c:if>
