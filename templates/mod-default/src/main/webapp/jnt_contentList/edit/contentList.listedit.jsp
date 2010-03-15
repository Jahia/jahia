<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<div>
    listedit jnt:contentList ${currentNode.name}
<%-- include list display --%>
<template:module node="${currentNode}" templateType="html" template="${currentResource.resolvedTemplate}"/>

<%-- include add nodes forms --%>
<template:module node="${currentNode}" templateType="edit" template="add">
    <template:param name="resourceNodeType" value="jnt:news"/>
</template:module>
</div>