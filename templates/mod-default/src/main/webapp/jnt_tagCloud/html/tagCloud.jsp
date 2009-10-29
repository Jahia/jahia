<%@ page import="org.jahia.utils.FileUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:node var="tagsRoot" path="${renderContext.siteNode.path}/tags"/>
<div class="tags">
<h3><fmt:message key="tags"/></h3>
<ul>
<jcr:sql var="tags" sql="select * from [jnt:tag] as sel where ischildnode(sel,['${tagsRoot.path}']) order by sel.[j:nodename]"/>
<c:forEach items="${tags.nodes}" var="tag">
<c:set var="tagCount" value="${tag.references.size}"/>
<c:if test="${tag.references.size > 9}"> <c:set var="tagCount" value="9"/></c:if>
<li><a class="tag${tagCount}0">${tag.name}</a></li>
</c:forEach>
</div>