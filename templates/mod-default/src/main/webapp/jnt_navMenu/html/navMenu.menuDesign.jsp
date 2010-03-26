<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<style type="text/css">
.nav-menu-area {
    background-color:#eaeaea;
    border:1px dashed #333;
    padding:5px !important;
    margin-bottom:5px
}
</style>
<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<fieldset>
<legend>${fn:escapeXml(not empty title.string ? title.string : jcr:label(currentNode.primaryNodeType))}</legend>
<c:set var="items" value="${currentNode.nodes}"/>
<c:if test="${renderContext.editMode || not empty items}">
<ul class="navmenu level_${fn:length(jcr:getParentsOfType(currentNode, 'jnt:navMenu')) + 1}">
<c:forEach items="${items}" var="menuItem">
    <template:module node="${menuItem}" editable="true" templateWrapper="${jcr:isNodeType(menuItem, 'jmix:list,jnt:navMenuMultilevel') ? '' : 'wrapper.listItem'}" template="menuDesign">
        <template:param name="subNodesTemplate" value="link"/>
        <template:param name="subNodesWrapper" value="wrapper.listItem"/>
        <template:param name="omitFormatting" value="true"/>
    </template:module>
</c:forEach>
<c:if test="${renderContext.editMode}">
    <li class="nav-menu-area"><fmt:message key="label.add.new.content"/><template:module path="*"/></li>
</c:if>
</ul>
</c:if>
</fieldset>