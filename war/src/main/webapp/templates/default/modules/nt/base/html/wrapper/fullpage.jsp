<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>

<template:template>
    <template:templateHead>
        <title>${currentNode.name}</title>
    </template:templateHead>
    <template:templateBody>

        <template:module node="${currentNode}" template="${currentResource.template}" />

    </template:templateBody>
</template:template>
