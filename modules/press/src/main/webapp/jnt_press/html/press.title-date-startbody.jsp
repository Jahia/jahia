<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:defaultCategory" var="pressReleaseContainerCatKeys"/>
<br/>
<h4><jcr:nodeProperty node="${currentNode}" name='date' var="datePress"/><span class="pressRealeseDate"><fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/></span> : <jcr:nodeProperty node="${currentNode}" name='jcr:title'/></h4>


<div>${fn:substring(currentNode.properties.body.string,0,350)}... <br/>
<fmt:message key='readFullStory'/> : <a href="<c:url value='${url.base}${currentNode.path}.detail.html'/>"><jcr:nodeProperty node="${currentNode}" name='jcr:title'/></a></div>
