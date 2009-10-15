<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<c:set var="searchTerm" value="Emirates"/>

<h2>News containing ${searchTerm} (search only in current language)</h2>

<jcr:sql var="newsList"
         sql="select news.* from [web_templates:newsContainer] as news inner join [jnt:translation] as translation on ischildnode(translation, news) where translation.[jcr:language] = '${currentResource.locale}' and contains(news.*, '${searchTerm}') or contains(translation.*, '${searchTerm}') order by news.[newsDate] desc"
         limit="10"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found with: ${searchTerm}
</c:if>

<c:forEach items="${newsList.nodes}" var="news">
    <template:module node="${news}" editable="false" />
</c:forEach>

<c:set var="searchTerm" value="Chinese"/>

<br/><br/>
<h2>Press releases containing ${searchTerm} (search in all languages not including file search)</h2>

<jcr:sql var="pressList"
         sql="select press.* from [web_templates:pressContainer] as press inner join [jnt:translation] as translation on ischildnode(translation, press) where contains(press.*, '${searchTerm}') or contains(translation.*, '${searchTerm}') order by press.[date] desc"
         limit="10"/>

<c:if test="${pressList.nodes.size == 0}">
    No press releases found with: ${searchTerm}
</c:if>

<c:forEach items="${pressList.nodes}" var="press">
    <template:module node="${press}" editable="false" />
</c:forEach>

<c:set var="searchTerm" value="Europe"/>

<br/><br/>
<h2>Reference search: Press release only searching '' ${searchTerm} in attached files (English only)</h2>
	
<jcr:sql var="pressList"
         sql="select press.* from [web_templates:pressContainer] as press inner join [jnt:translation] as translation on ischildnode(translation, press) inner join [nt:file] as file on translation.pdfVersion_en = file.[jcr:uuid] inner join [nt:resource] as filecontent on ischildnode(filecontent, file) where contains(filecontent.*, '${searchTerm}') order by press.[date] desc"
         limit="10"/>         

<c:if test="${pressList.nodes.size == 0}">
    No press releases found with: ${searchTerm}
</c:if>

<c:forEach items="${pressList.nodes}" var="press">
    <template:module node="${press}" editable="false" />
</c:forEach>