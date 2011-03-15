<%@page import="org.jahia.registries.ServicesRegistry"%>
<%@page import="org.jahia.services.usermanager.JahiaUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<div>
    <img src="${pageContext.request.contextPath}/icons/jnt_groupsFolder_large.png" alt=" " style="float: left" />
    <h2>
    <fmt:message key="label.group"/>:&nbsp;${currentNode.displayableName}
    </h2>
    <jcr:nodeProperty node="${currentNode}" name="jcr:description" />
    <ul>
    <c:forEach items="${currentNode.nodes}" var="child">
        <c:if test="${jcr:isNodeType(child, 'jnt:members')}">
        <li>
            <h3><fmt:message key="label.members"/></h3>
            <ul>
            <c:forEach items="${child.nodes}" var="subchild">
                <li>
                    <jcr:nodeProperty node="${subchild}" name="j:member" var="memberRef"/>
                    <c:set var="member" value="${memberRef.node}"/>
                    <div>
                        <c:if test="${jcr:isNodeType(member, 'jnt:group')}" var="isGroup">
                            <img src="${pageContext.request.contextPath}/icons/jnt_groupsFolder_large.png" alt=" " style="float: left" />
                        </c:if>
                        <c:if test="${!isGroup}">
                            <img src="${pageContext.request.contextPath}/icons/jnt_user_large.png" alt=" " style="float: left" />
                        </c:if>
                        <jcr:nodeProperty node="${member}" name="jcr:title" var="title"/>
                        <c:set var="params" value=""/>
                        <a href="<c:url value='${url.base}${member.path}.html${params}'/>"><strong>
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