<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="separator" value="${functions:default(renderContext.moduleParams.separator, ', ')}"/>
<c:if test="${empty requestScope['org.jahia.javascript.includes.jQuery']}">
	<c:set var="org.jahia.javascript.includes.jQuery" value="true" scope="request"/>
    <template:addResources type="javascript" resources="jquery.min.js" nodetype="jmix:tagged"/>
    <script type="text/javascript">        
        function addNewTag(tagForm, uuid, separator) {
        	//jahia-tags-${currentNode.identifier}
        	var newTag = tagForm.elements['j:newTag'];
        	if (newTag.value.length > 0) {
        		jQuery.post(tagForm.action, jQuery(tagForm).serialize(),
                	function (data) {
                		var tagContainer = jQuery('#jahia-tags-' + uuid);
                        <%-- TODO: need to check if the tag already assigned to prevent dublicates --%>
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
    </script>
</c:if>
<c:if test="${renderContext.user.name != 'guest'}">
    <form action="${url.base}${currentNode.path}" method="post">
        <input type="hidden" name="methodToCall" value="put"/>
		<input type="text" name="j:newTag" value=""/>
        <input type="submit" title="<fmt:message key='add'/>" value="<fmt:message key='add'/>" class="button" onclick="addNewTag(this.form, '${currentNode.identifier}', '${separator}'); return false;"/>
    </form>
</c:if>