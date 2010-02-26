<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="separator" value="${functions:default(renderContext.moduleParams.separator, ', ')}"/>
<c:set var="org.jahia.javascript.includes.jQuery" value="true" scope="request"/>
<template:addResources type="javascript" resources="jquery.min.js" nodetype="jmix:tagged"/>

<template:addResources type="css" resources="jquery.autocomplete.css" />
<template:addResources type="css" resources="thickbox.css" />
<template:addResources type="javascript" resources="jquery.ajaxQueue.js" />
<template:addResources type="javascript" resources="jquery.autocomplete.js" />
<template:addResources type="javascript" resources="jquery.bgiframe.min.js" />
<template:addResources type="javascript" resources="thickbox-compressed.js" />

<script type="text/javascript">
    function addNewTag(tagForm, uuid, separator) {
        var newTag = tagForm.elements['j:newTag'];
        if (newTag.value.length > 0) {
            var tagContainer = jQuery('#jahia-tags-' + uuid);
            if (tagContainer.find("span:contains('" + newTag.value + "')").length == 0) {
	            jQuery.post(tagForm.action, jQuery(tagForm).serialize(), function (data) {
	                if (separator.length > 0 && jQuery('#jahia-tags-' + uuid + ' > span').length > 0) {
	                    tagContainer.append(separator);
	                }
	                var tagDisplay = jQuery('<span>' + newTag.value + '</span>');
	                tagDisplay.hide();
	                tagContainer.append(tagDisplay);
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
                query : "/jcr:root${renderContext.site.JCRPath}/tags//element(*, jnt:tag)[jcr:contains(.,'{$q}*')]/@j:nodename",
                language : "xpath",
                escapeColon : "false",
                propertyMatchRegexp : "{$q}.*",
                removeDuplicatePropValues : "false"
            }
        });
    });
    
</script>
<c:if test="${renderContext.user.name != 'guest'}">
    <form action="${url.base}${currentNode.path}" method="post">
        <input type="hidden" name="methodToCall" value="put"/>
        <input type="text" name="j:newTag" class="newTagInput" value=""/>
        <input type="submit" title="<fmt:message key='add'/>" value="<fmt:message key='add'/>" class="button"
               onclick="addNewTag(this.form, '${currentNode.identifier}', '${separator}'); return false;"/>
    </form>
</c:if>