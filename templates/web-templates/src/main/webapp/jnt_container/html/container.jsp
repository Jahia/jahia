<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType" scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType" scope="application"/>
<h2>Container : ${currentNode.name}</h2>
<ul>
    <c:forEach items="${currentNode.properties}" var="property">
        <c:if test="${property.definition.jahiaContentItem}">
            <li>${property.name}:&nbsp;
                <c:if test="${!property.definition.multiple}">
                    <c:choose>
                        <c:when test="${property.type == jcrPropertyTypes.REFERENCE || property.type == jcrPropertyTypes.WEAKREFERENCE}">
                            <template:module node="${property.referencedNode}"/>
                        </c:when>
                        <c:when test="${property.definition.selector == selectorType.COLOR}">
                            <span style="background-color:${property.string}">${property.string}</span>
                        </c:when>
                        <c:otherwise>
                            ${property.string}
                        </c:otherwise>
                    </c:choose>
                </c:if>
                <c:if test="${property.definition.multiple}">
                    <ul>
                        <c:forEach items="${property.values}" var="val">
                            <li><c:choose>
                                <c:when test="${property.type == jcrPropertyTypes.REFERENCE || property.type == jcrPropertyTypes.WEAKREFERENCE}">
                                    <template:module node="${val.node}"/>
                                </c:when>
                                <c:otherwise>
                                    ${val.string}
                                </c:otherwise>
                            </c:choose></li>
                        </c:forEach>
                    </ul>
                </c:if>
            </li>
        </c:if>
    </c:forEach>
</ul>

Metadata :
<ul>
    <c:forEach items="${currentNode.properties}" var="property">
        <c:if test="${property.definition.metadataItem}">
            <li>
            <c:if test="${!property.definition.multiple}">
                ${property.name} ${property.string}
            </c:if>
            <c:if test="${property.definition.multiple}">
                <ul>
                    <c:forEach items="${property.values}" var="val">
                        <li>
                            <c:choose>
                            <c:when test="${property.type == jcrPropertyTypes.REFERENCE || property.type == jcrPropertyTypes.WEAKREFERENCE}">
                                ${val.node.name}
                            </c:when>
                            <c:otherwise>
                                ${val.string}
                            </c:otherwise>
                        </c:choose>
                        </li>
                    </c:forEach>
                </ul>
            </c:if>
            </li>
        </c:if>
    </c:forEach>
</ul>

