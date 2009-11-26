<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<!--start jobsSearchForm -->
<div class="jobsSearchForm">
    <form action="${url.base}${renderContext.mainResource.node.path}.html" method="get">
        <fieldset>
            <legend><fmt:message key='jnt_jobOffers.search.form.label'/></legend>
            <p class="jobsSearchKeyword">
                <label for="jobsSearchKeyword"><fmt:message key='job.keywordSearch'/></label>
                <input type="text" name="jobsSearchKeyword" id="jobsSearchKeyword" class="field jobsSearchKeyword"
                       value="${param.jobsSearchKeyword}" tabindex="4"/>
            </p>

            <div class="divButton">
                <input type="submit" name="submit" id="submit" class="button" value="<fmt:message key="jnt_jobOffers.search.form.label"/>"
                       tabindex="5"/>
            </div>
        </fieldset>
    </form>
</div>
<jcr:jqom var="results">
    <query:selector nodeTypeName="jnt:job" selectorName="jobSelector"/>
    <query:childNode selectorName="jobSelector" path="${currentNode.path}"/>
    <c:if test="${not empty param.jobsSearchKeyword}">
        <query:fullTextSearch selectorName="jobSelector" searchExpression="${param.jobsSearchKeyword}"
                              propertyName="description"/>
    </c:if>
    <query:sortBy propertyName="jcr:created" order="desc" selectorName="jobSelector"/>
</jcr:jqom>
<!--stop jobsSearchForm -->
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<c:set var="listTitle" value="${fn:escapeXml(title.string)}"/>
<c:if test="${empty listTitle}"><c:set var="listTitle"><fmt:message key="jobList"/></c:set></c:if>
<div class="box4">
    <div class="box4-topright"></div>
    <div class="box4-topleft"></div>
    <h3 class="box4-header"><span class="jobsTitle">${listTitle}</span></h3>

    <div class="box4-bottomright"></div>
    <div class="box4-bottomleft"></div>
    <div class="clear"></div>
</div>

<table width="100%" class="tab" summary="">
    <colgroup>
        <col span="1" width="60%" class="col1"/>
        <col span="1" width="20%" class="col2"/>
        <col span="1" width="20%" class="col3"/>
    </colgroup>
    <thead>
    <tr>
        <th id="job" scope="col"><fmt:message key="web_templates_jobContainer.jobtitle"/></th>
        <th id="location" scope="col"><fmt:message key="web_templates.location"/></th>
        <th id="businessUnit" scope="col"><fmt:message key="web_templates_jobContainer.businessUnit"/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${results.nodes}" var="job" varStatus="status">
        <template:module node="${job}" template="list"/>
        <c:if test="${status.last}">
            <template:module node="${job.placeholder}" template="list" editable="true"/>
        </c:if>
    </c:forEach>
    <c:if test="${results.nodes.size == 0 && empty param.jobsSearchKeyword}">
        <c:forEach items="${currentNode.editableChildren}" var="job">
            <template:module node="${job}" template="list" editable="true"/>
        </c:forEach>
    </c:if>
    <c:if test="${results.nodes.size == 0 && not empty param.jobsSearchKeyword}">
        <tr>
            <td><fmt:message key="jnt_jobOffers.no.job.offers.found"/></td>
        </tr>
    </c:if>
    </tbody>
</table>