<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<c:catch var="ex">
    <sql:setDataSource driver="com.mysql.jdbc.Driver" url="jdbc:mysql://127.0.0.1:3306/jahia" user="jahia"
                       password="jahia" var="ds"/>

    <sql:query var="hits" dataSource="${ds}">
        select count(id) as hits,url from pageviewed where day(ts) = day(now())-1 group by url order by hits desc limit 10
    </sql:query>
    <p>Top Ten Pages for the last 24 hours</p>
    <ol>
    <c:forEach items="${hits.rows}" var="hit">
        <jcr:node path="${hit.url}" var="pageNode"/>
        <li><a href="${url.base}${hit.url}"><jcr:nodeProperty node="${pageNode}" name="jcr:title"/></a></li>
    </c:forEach>
    </ol>
</c:catch>
<c:if test="${not empty ex and renderContext.editMode}">
    <p>For making this module work you need to parse metrics logs of jahia</p>
</c:if>
