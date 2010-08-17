<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<query:definition var="listQuery"
             statement="select * from [jnt:page] as blog where isdescendantnode(blog, ['${renderContext.mainResource.node.path}'])  order by blog.[jcr:lastModified] desc"/>
<c:set target="${moduleMap}" property="editable" value="false" />
<c:set target="${moduleMap}" property="listQuery" value="${listQuery}" />


