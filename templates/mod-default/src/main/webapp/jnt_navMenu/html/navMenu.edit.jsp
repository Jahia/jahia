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
<ul>
<c:forEach items="${items}" var="menuItem">
    <template:module node="${menuItem}" editable="true" templateWrapper="wrapper.navMenuItem" template="edit">
        <template:param name="subNodesTemplate" value="hidden.navMenuItem"/>
    </template:module>
</c:forEach>
<c:if test="${renderContext.editMode}">
    <li class="nav-menu-area"><fmt:message key="label.add.new.content"/><template:module path="*"/></li>
</c:if>
</ul>
</c:if>
</fieldset>