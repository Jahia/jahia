<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

    <jcr:sql var="result"
             sql="select * from [jnt:page] as blog where isdescendantnode(blog, ['${currentNode.parent.path}'])  order by blog.[jcr:lastModified] desc"/>
    <c:set var="renderOptionsOnChild" value="none" scope="request"/>
    <c:set var="currentList" value="${result.nodes}" scope="request"/>
    <c:set var="end" value="${functions:length(result.nodes)}" scope="request"/>
    <c:set var="listTotalSize" value="${end}" scope="request"/>
    <c:set var="subNodesTemplate" value="hidden.blog.short" scope="request"/>