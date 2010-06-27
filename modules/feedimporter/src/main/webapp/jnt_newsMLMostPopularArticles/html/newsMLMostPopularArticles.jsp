<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<c:catch var="ex">
    <sql:setDataSource driver="com.mysql.jdbc.Driver" url="jdbc:mysql://127.0.0.1:3306/jahia-6.5" user="jahia"
                       password="jahia" var="ds"/>

    <sql:query var="hits" dataSource="${ds}">
        SELECT url, count(*) as url_count FROM objviewed o where nodetype='jnt:newsMLItem' group by url order by url_count desc limit 10
    </sql:query>
    <p>Top Ten News Articles</p>
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
