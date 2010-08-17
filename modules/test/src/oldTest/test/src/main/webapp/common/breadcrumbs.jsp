<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="history">
    <ul class="breadcrumbs">
        <c:forEach var="historyBean" items="${sessionScope['org.jahia.toolbar.history']}">
            <li><a href="${historyBean.url}">${historyBean.pageTitle}</a></li>
        </c:forEach>
    </ul>
</div>