<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jcr:nodeProperty node="${currentNode}" name="j:nodeTypeFilter" var="nodeTypeFilter"/>
<jcr:nodeProperty node="${currentNode}" name="j:recommendationLimit" var="recommendationLimit"/>
<c:catch var="ex">
    <sql:setDataSource driver="com.mysql.jdbc.Driver" url="jdbc:mysql://127.0.0.1:3306/jahia" user="jahia"
                       password="jahia" var="ds"/>

    <sql:query var="refs" dataSource="${ds}">
        SELECT * FROM objxref WHERE leftpath='${currentNode.path}' and rightnodetype='${nodeTypeFilter.string}' order by counter desc limit ${recommendationLimit.long};
    </sql:query>
    <p><fmt:message key="recommendationsIntro"/></p>
    <ol>
    <c:forEach items="${refs.rows}" var="ref">
        <jcr:node path="${ref.rightpath}" var="curNode"/>
        <li><a href="${url.base}${ref.rightpath}"><jcr:nodeProperty node="${curNode}" name="jcr:title"/> (${ref.counter})</a></li>
    </c:forEach>
    </ol>
</c:catch>
<c:if test="${not empty ex and renderContext.editMode}">
    <p>For this module to work you need to parse metrics logs of jahia</p>
</c:if>
