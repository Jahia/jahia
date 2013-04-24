<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<fmt:message key="label.delete" var="i18nDelete"/><c:set var="i18nDelete" value="${functions:escapeJavaScript(i18nDelete)}"/>
<fmt:message key="label.htmlFiltering.invalidTag" var="i18nInvalidTag"/><c:set var="i18nInvalidTag" value="${functions:escapeJavaScript(i18nInvalidTag)}"/>
<fmt:message key="label.changeSaved" var="i18nSaved"/><c:set var="i18nSaved" value="${functions:escapeJavaScript(i18nSaved)}"/>
<c:url value="/css/images/andromeda/icons/delete.png" var="iconDelete"/>

<template:addResources type="javascript" resources="jquery.min.js,jquery.form.min.js"/>
<template:addResources>
<script type="text/javascript">
$(document).ready(function() {
    $('#addHtmlTag').click(function() {
    	htmlFilteringAddHtmlTag();
    	return false;
    });
    $("#newHtmlTag").keypress(function(evt) {
     	if (evt.which == 13) {
     		htmlFilteringAddHtmlTag();
         	return false;
       	}
   	});
    $("input.btnDeleteHtmlTag").click(function() {
    	$(this).parent().parent().remove(); return false;
   	});
});
function htmlFilteringAddHtmlTag() {
	var newTag = $('#newHtmlTag');
	var val = newTag.val();
	if (val.length == 0) {
		return;
	}
	var match = val.match(/[A-Za-z]+[1-9]*/);
	if (match == null || match[0] != val) {
		alert('${i18nInvalidTag}');
		return;
	}
	if ($('#btnDeleteHtmlTag' + val).length == 0) {
    	$('#tblHtmlTags').find('tbody:last').append('<tr><td><strong class="htmlTagToFilter">' + val + '</strong></td><td>'
    			+ '<input type="image" title="${i18nDelete}" alt="${i18nDelete}" src="${iconDelete}" width="16" height="16" '
    			+ ' onclick="$(this).parent().parent().remove(); return false;"/>'
    			+ '</td></tr>');
	}
	newTag.val('');
}
function updateSiteHtmlFiltering(btn) {
	btn.attr('disabled', 'disabled');
	var tags='';
	$('strong.htmlTagToFilter').each(function() {
			if (tags.length > 0) {
				tags+=',';
			}
			tags+=$(this).text();
		}
	);
	var data={
			'j:doTagFiltering':$('#activateTagFiltering').is(':checked'),
			'j:filteredTags':tags
	};
    $('#updateSiteForm').ajaxSubmit({
        data: data,
        dataType: "json",
        success: function(response) {
            if (response.warn != undefined) {
                alert(response.warn);
            } else {
            	alert('${i18nSaved}');
            }
            btn.removeAttr('disabled');
        },
        error: function() {
            btn.removeAttr('disabled');
        }
    });
}
</script>
</template:addResources>
<c:set var="site" value="${renderContext.mainResource.node.resolveSite}"/>
<c:set var="propFilteringActivated" value="${site.properties['j:doTagFiltering']}"/>
<c:set var="propFilteredTags" value="${site.properties['j:filteredTags']}"/>
<c:set var="filteredTags" value="${not empty propFilteredTags ? propFilteredTags.string : ''}"/>

<h2>${fn:escapeXml(currentNode.displayableName)} - ${fn:escapeXml(site.displayableName)}</h2>
<p><fmt:message key="label.htmlFiltering.description"/>:</p>

<form id="updateSiteForm" action="<c:url value='${url.base}${renderContext.mainResource.node.resolveSite.path}'/>" method="post">
    <input type="hidden" name="jcrMethodToCall" value="put"/>
    <input type="hidden" name="jcr:mixinTypes" value="jmix:htmlSettings"/>

    <label class="checkbox" for="activateTagFiltering">
        <input type="checkbox" name="activateTagFiltering" id="activateTagFiltering"${not empty propFilteringActivated && propFilteringActivated.boolean ? ' checked="checked"' : ''}/>
        <fmt:message key="label.active"/>
    </label>

                <fmt:message key="label.add" var="i18nAdd"/>
                <input type="text" name="newHtmlTag" id="newHtmlTag" value="" size="10"/>
                <input type="image" id="addHtmlTag" title="${i18nAdd}" alt="${i18nAdd}" src="<c:url value='/css/images/andromeda/icons/add2.png'/>" width="16" height="16" />

        <table id="tblHtmlTags" class="table table-bordered table-striped table-hover" >
        <tbody>
            <c:forTokens var="tag" items="${filteredTags}" delims=", ">
                <tr id="rowHtmlTag${tag}">
                    <fmt:message key="label.delete" var="i18nDelete"/><c:set var="i18nDelete" value="${fn:escapeXml(i18nDelete)}"/>
                    <td><input type="image" title="${i18nDelete}" alt="${i18nDelete}" src="${iconDelete}" width="16" height="16" onclick="$(this).parent().parent().remove(); return false;" /></td>
                    <td width="100%"><strong class="htmlTagToFilter">${tag}</strong></td>

                </tr>
            </c:forTokens>
        </tbody>
    </table>
    <button class="btn btn-primary" type="button" name="save" onclick="updateSiteHtmlFiltering($(this)); return false;"><i class="icon-ok-sign icon-white"></i> <fmt:message key='label.save'/></button>
</form>
