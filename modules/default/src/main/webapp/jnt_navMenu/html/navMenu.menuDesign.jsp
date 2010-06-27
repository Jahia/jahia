<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<style type="text/css">
    .nav-menu-area {
        background-color: #eaeaea;
        border: 1px dashed #333;
        padding: 5px !important;
        margin-bottom: 5px
    }
</style>
<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<fieldset>
    <c:set var="items" value="${currentNode.nodes}"/>
    <c:set var="navMenuLevel" value="${fn:length(jcr:getParentsOfType(currentNode, 'jnt:navMenu')) + 1}"/>
    <c:if test="${navMenuLevel eq 1 and (renderContext.editMode or renderContext.contributionMode)}">
    <p><a href="${url.basePreview}${currentNode.path}.html" target="_blank"><span><fmt:message key="navMenu.label.preview"/></span></a></p>
</c:if>
    <c:if test="${renderContext.editMode || not empty items}">
        <ul>
            <c:forEach items="${items}" var="menuItem">
                <c:if test="${jcr:isNodeType(menuItem, 'jnt:navMenu')}"><li></c:if>
                <c:choose>
                    <c:when test="${jcr:isNodeType(menuItem, 'jnt:navMenu')}">
                        <p>
                            <span><fmt:message key="navMenu.label.submenu"/></span><br/>
                            <span><fmt:message
                                    key="navMenu.label.submenu.name"/>&nbsp;${fn:escapeXml(not empty menuItem.properties['jcr:title'].string ? menuItem.properties['jcr:title'].string : jcr:label(menuItem.primaryNodeType, currentResource.locale))}</span>
                        </p>
                    </c:when>
                    <c:when test="${jcr:isNodeType(menuItem, 'jnt:navMenuQuery')}">
                        <p>
                            <span><fmt:message key="navMenu.label.query"/></span><br/>
                            <span>${menuItem.properties['jcr:statement'].string}</span>
                        </p>
                    </c:when>
                    <c:when test="${jcr:isNodeType(menuItem, 'jnt:navMenuExternalLink')}">
                        <p>
                            <span><fmt:message key="navMenu.label.externalLink"/></span>
                        </p>
                    </c:when>
                    <c:when test="${jcr:isNodeType(menuItem, 'jnt:navMenuNodeLink')}">
                        <p>
                            <span><fmt:message key="navMenu.label.nodeLink"/></span>
                        </p>
                    </c:when>
                    <c:when test="${jcr:isNodeType(menuItem, 'jnt:navMenuText')}">
                        <p>
                            <span><fmt:message key="navMenu.label.text.element"/></span>
                        </p>
                    </c:when>
                    <c:when test="${jcr:isNodeType(menuItem, 'jnt:navMenuMultilevel')}">
                        <p>
                        <span><fmt:message key="navMenu.label.multilevel.element"/></span><br/>
                        <span><fmt:message key="navMenu.label.allowed.types"/>
                            <c:forEach items="${menuItem.properties['j:allowedTypes']}" var="allowedType"
                                       varStatus="allowedStatus">
                                ${allowedType.string}
                                <c:if test="${not allowedStatus.last}">,</c:if>
                            </c:forEach>
                        </span>
                        </p>
                    </c:when>
                    <c:when test="${jcr:isNodeType(menuItem, 'jnt:navMenuChildNodes')}">
                        <p>
                        <span><fmt:message key="navMenu.label.childnodes.element"/></span><br/>
                        <span><fmt:message key="navMenu.label.allowed.types"/>
                            <c:forEach items="${menuItem.properties['j:allowedTypes']}" var="allowedType"
                                       varStatus="allowedStatus">
                                ${allowedType.string}
                                <c:if test="${not allowedStatus.last}">,</c:if>
                            </c:forEach>
                        </span>
                        </p>
                    </c:when>
                </c:choose>
                <template:module node="${menuItem}" editable="true"
                                 template="${jcr:isNodeType(menuItem, 'jnt:navMenu') ? 'menuDesign':'hidden.menuElement'}">
                    <template:param name="subNodesTemplate" value="hidden.menuElement"/>
                    <template:param name="omitFormatting" value="true"/>
                </template:module>
                <c:if test="${jcr:isNodeType(menuItem, 'jnt:navMenu')}"></li></c:if>
            </c:forEach>
            <c:if test="${renderContext.editMode}">
                <li class="nav-menu-area"><fmt:message key="label.add.new.content"/><template:module path="*"/></li>
            </c:if>
        </ul>
    </c:if>
</fieldset>
