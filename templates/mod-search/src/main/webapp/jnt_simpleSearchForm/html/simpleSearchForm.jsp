<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>

<s:form method="get">
   	<div class="form-container">
		<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
		<c:if test="${not empty title.string}">
		<label for="searchTerm">${fn:escapeXml(title.string)}:&nbsp;</label>
		</c:if>
		<fmt:message key='search.startSearching' var="startSearching"/>
       	<s:term id="searchTerm" value="${startSearching}" onfocus="if(this.value==this.defaultValue)this.value='';" onblur="if(this.value=='')this.value=this.defaultValue;" class="text-input"/>
       	<s:site display="false" value="${jahia.site.siteKey}"/>
    	<input type="submit" class="submit" value="<fmt:message key='search.submit'/>" title="<fmt:message key='search.submit'/>"/>
	</div>
</s:form><br class="clear"/>
