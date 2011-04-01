<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent and jcr:isNodeType(bindedComponent, 'jmix:list')}">
    <div class="alphabeticalMenu">
        <div class="alphabeticalNavigation">
            <c:forTokens items="a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z" delims="," var="letter">
                <c:url value='${url.base}${renderContext.mainResource.node.path}.html' var="urlLetter">
                    <c:param name="urlLetter" value="${letter}"/>
                </c:url>
                <c:set var="selected" value=""/>
                <c:if test="${param.urlLetter == letter}"><c:set var="selected" value="current"/></c:if>
                <a href="${urlLetter}" class="alphabeticalLetter ${selected}">${letter}</a>
            </c:forTokens>
        </div>
    </div>
    <query:definition var="listQuery"
                  statement="select * from [jnt:content] as content  where
              (content.['${currentNode.properties.field.string}'] like '${fn:toLowerCase(param.urlLetter)}%' or
              content.['${currentNode.properties.field.string}'] like '${fn:toUpperCase(param.urlLetter)}%') and
               isdescendantnode(content, ['${bindedComponent.path}'])
               order by content.['${currentNode.properties.field}']"/>
    <c:set target="${moduleMap}" property="listQuery" value="${listQuery}"/>
</c:if>
