<%@page import="org.jahia.registries.ServicesRegistry"%>
<%@page import="org.jahia.services.usermanager.JahiaUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<div>
    <img src="${pageContext.request.contextPath}/icons/jnt_groupsFolder_large.png" alt=" " style="float: left" />
    <h2>
    <c:if test="${not empty title}">
        ${title.string}&nbsp(${currentNode.name})
    </c:if>
    <c:if test="${empty title}">
        ${child.name}
    </c:if>
    </h2>
    <jcr:nodeProperty node="${currentNode}" name="jcr:description" />
    <ul>
        <c:set var="parent" value="${currentNode.parent}"/>
        <c:if test="${parent.name != ''}">
            <li><a href="${url.base}${parent.path}.html">..</a></li>
        </c:if>
    <c:forEach items="${currentNode.nodes}" var="child">
        <c:if test="${not jcr:isNodeType(child, 'jnt:members')}">
        <li>
            <a href="${url.base}${child.path}.html">${child.name}</a>
        </li>
        </c:if>
    </c:forEach>
    <c:forEach items="${currentNode.nodes}" var="child">
        <c:if test="${jcr:isNodeType(child, 'jnt:members')}">
        <li>
            <h3><fmt:message key="label.members"/></h3>
            <ul>
            <c:forEach items="${child.nodes}" var="subchild">
                <li>
                    <jcr:nodeProperty node="${subchild}" name="j:member" var="memberRef"/>
                    <c:set var="uuid" value="${memberRef.string}"/> 
                    <%
                    pageContext.setAttribute("member", ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession().getNodeByUUID((String) pageContext.getAttribute("uuid")));
                    %>
                    <div>
                        <c:if test="${jcr:isNodeType(member, 'jnt:group')}" var="isGroup">
                            <img src="${pageContext.request.contextPath}/icons/jnt_groupsFolder_large.png" alt=" " style="float: left" />
                        </c:if>
                        <c:if test="${!isGroup}">
                            <img src="${pageContext.request.contextPath}/icons/jnt_user_large.png" alt=" " style="float: left" />
                        </c:if>
                        <jcr:nodeProperty node="${member}" name="jcr:title" var="title"/>
                        <a href="${url.base}${member.path}.html"><strong>
                        <c:if test="${not empty title}">
                            ${title.string}&nbsp(${member.name})
                        </c:if>
                        <c:if test="${empty title}">
                            ${member.name}
                        </c:if>
                        </strong></a><br/>
                        <jcr:nodeProperty node="${member}" name="jcr:description" />
                    </div><br style="clear: both"/>
                </li>
            </c:forEach>
            </ul>
        </li>
        </c:if>
    </c:forEach>
    </ul>
</div>