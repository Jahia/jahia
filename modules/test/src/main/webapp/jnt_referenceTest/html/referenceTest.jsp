<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>


<%@page import="javax.jcr.Node"%><c:set var="searchTerm" value="Emirates"/>

<h2>Containerlist descendant news sorted by title (with XPATH using j:fullpath)</h2>
<% try {
    long startTime = System.currentTimeMillis(); %>

<jcr:xpath var="newsList"
         xpath="//element(*, jnt:news)[jcr:like(@j:fullpath, '/sites/ACME/home/page8/news/%') and j:translation/@jcr:language = '${currentResource.locale}'] order by j:translation/@jcr:title_${currentResource.locale}"
         limit="10"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found
</c:if>

<c:forEach items="${newsList.nodes}" var="news">
<%--    <c:out value="<%=((Node)pageContext.findAttribute("news")).getProperty("j:fullpath")%>"/> --%>
    <template:module node="${news}" editable="false" />
</c:forEach>
<% long endTime = System.currentTimeMillis(); %>
<utility:logger value="<%="Sorted containerlist descendant news list with XPATH using like j:fullpath: " + (endTime-startTime) %>" level="info"/>
<%} catch (Exception ex) {
    log("Error in query", ex);
    %>
    Error in query!!
    <%
  }%>


<h2>Containerlist descendant news sorted by title (with XPATH)</h2>
<% try {
    long startTime = System.currentTimeMillis(); %>

<jcr:xpath var="newsList"
         xpath="/jcr:root/sites/ACME/home/page8/news//element(*, jnt:news)[j:translation/@jcr:language = '${currentResource.locale}'] order by j:translation/@jcr:title_${currentResource.locale}"
         limit="10"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found
</c:if>

<c:forEach items="${newsList.nodes}" var="news">
<%--    <c:out value="<%=((Node)pageContext.findAttribute("news")).getProperty("j:fullpath")%>"/> --%>
    <template:module node="${news}" editable="false" />
</c:forEach>
<% long endTime = System.currentTimeMillis(); %>
<utility:logger value="<%="Sorted containerlist descendant news list with XPATH: " + (endTime-startTime) %>" level="info"/>
<%} catch (Exception ex) {
    log("Error in query", ex);
    %>
    Error in query!!
    <%
  }%>

<h2>Containerlist child news sorted by title (with XPATH)</h2>
<% try {
    long startTime = System.currentTimeMillis(); %>

<jcr:xpath var="newsList"
         xpath="/jcr:root/sites/ACME/home/page8/news/*/element(j:translation, jnt:translation)[@jcr:language = '${currentResource.locale}'] order by @jcr:title_${currentResource.locale}"
         limit="10"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found
</c:if>

<c:forEach items="${newsList.nodes}" var="news">
    <template:module node="${news.parent}" editable="false" />
</c:forEach>
<% long endTime = System.currentTimeMillis(); %>
<utility:logger value="<%="Sorted containerlist child news list with XPATH: " + (endTime-startTime) %>" level="info"/>
<%} catch (Exception ex) {
    log("Error in query", ex);
     %>Error in query!!<%
  }%>

<h2>Containerlist child news sorted by title using predicate (with XPATH)</h2>
<% try {
    long startTime = System.currentTimeMillis(); %>

<jcr:xpath var="newsList"
         xpath="/jcr:root/sites/ACME/home/page8/news/element(*, jnt:news)[j:translation/@jcr:language = '${currentResource.locale}'] order by j:translation/@jcr:title_${currentResource.locale}"
         limit="10"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found
</c:if>

<c:forEach items="${newsList.nodes}" var="news">
    <template:module node="${news}" editable="false" />
</c:forEach>
<% long endTime = System.currentTimeMillis(); %>
<utility:logger value="<%="Sorted containerlist child news list using predicate with XPATH: " + (endTime-startTime) %>" level="info"/>
<%} catch (Exception ex) {
    log("Error in query", ex);
     %>Error in query!!<%
  }%>


<h2>News sorted by title (with SQL isdescendantnode join)</h2>
<% try {
    long startTime = System.currentTimeMillis(); %>

<jcr:sql var="newsList"
         sql="SELECT * FROM [jnt:translation] AS translation inner join [jnt:news] as news on ischildnode(translation, news) WHERE ISDESCENDANTNODE(news, [/sites/ACME/home/page8]) and translation.[jcr:language] = '${currentResource.locale}' ORDER BY translation.[jcr:title_${currentResource.locale}]"
         limit="10"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found
</c:if>

<c:forEach items="${newsList.nodes}" var="news">
    <template:module node="${news}" editable="false" />
</c:forEach>
<% long endTime = System.currentTimeMillis(); %>
<utility:logger value="<%="Sorted news list with SQL-2 isdescendantnode join: " + (endTime-startTime) %>" level="info"/>
<%} catch (Exception ex) {
    log("Error in query", ex);
     %>Error in query!!<%
  }%>


<h2>News sorted by title (with SQL ischildnode join)</h2>
<% try {
    long startTime = System.currentTimeMillis(); %>

<jcr:sql var="newsList"
         sql="SELECT * FROM [jnt:translation] AS translation inner join [jnt:news] as news on ischildnode(translation, news) WHERE ISCHILDNODE(news, [/sites/ACME/home/page8/news]) and translation.[jcr:language] = '${currentResource.locale}' ORDER BY translation.[jcr:title_${currentResource.locale}]"
         limit="10"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found
</c:if>

<c:forEach items="${newsList.nodes}" var="news">
    <template:module node="${news}" editable="false" />
</c:forEach>
<% long endTime = System.currentTimeMillis(); %>
<utility:logger value="<%="Sorted news list with SQL-2 ischildnode join: " + (endTime-startTime) %>" level="info"/>
<%} catch (Exception ex) {
    log("Error in query", ex);
     %>Error in query!!<%
  }%>

<h2>News sorted by title (with SQL PATH constraint)</h2>
<% try {
    long startTime = System.currentTimeMillis(); %>

<jcr:sql var="newsList"
         sql="SELECT * FROM [jnt:translation] AS translation WHERE translation.[j:fullpath] like '/sites/ACME/home/page8/news/%' and translation.[jcr:language] = '${currentResource.locale}' ORDER BY translation.[jcr:title_${currentResource.locale}]"
         limit="10"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found
</c:if>

<c:forEach items="${newsList.nodes}" var="news">
    <template:module node="${news.parent}" editable="false" />
</c:forEach>
<% long endTime = System.currentTimeMillis(); %>
<utility:logger value="<%="Sorted news list with SQL-2 path constraint: " + (endTime-startTime) %>" level="info"/>
<%} catch (Exception ex) {
    log("Error in query", ex);
     %>Error in query!!<%
  }%>

<h2>XPATH News containing ${searchTerm} (search only in current language)</h2>

<jcr:xpath var="newsList"
         xpath="/jcr:root/sites/ACME/home//element(*, jnt:news)[jcr:deref(@j:tags, 'test')]"
         limit="10"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found with: ${searchTerm}
</c:if>

<c:forEach items="${newsList.nodes}" var="news">
    <template:module node="${news}" editable="false" />
</c:forEach>
 
<h2>News containing ${searchTerm} (search only in current language)</h2>

<jcr:sql var="newsList"
         sql="select news.* from [jnt:news] as news inner join [jnt:translation] as translation on ischildnode(translation, news) where translation.[jcr:language] = '${currentResource.locale}' and contains(news.*, '${searchTerm}') or contains(translation.*, '${searchTerm}') order by news.[newsDate] desc"
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
         sql="select press.* from [jnt:press] as press inner join [jnt:translation] as translation on ischildnode(translation, press) where contains(press.*, '${searchTerm}') or contains(translation.*, '${searchTerm}') order by press.[date] desc"
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
         sql="select press.* from [jnt:press] as press inner join [jnt:translation] as translation on ischildnode(translation, press) inner join [nt:file] as file on translation.pdfVersion_en = file.[jcr:uuid] inner join [nt:resource] as filecontent on ischildnode(filecontent, file) where contains(filecontent.*, '${searchTerm}') order by press.[date] desc"
         limit="10"/>         

<c:if test="${pressList.nodes.size == 0}">
    No press releases found with: ${searchTerm}
</c:if>

<c:forEach items="${pressList.nodes}" var="press">
    <template:module node="${press}" editable="false" />
</c:forEach>