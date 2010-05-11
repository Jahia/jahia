<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="pagetagging.css"/>
<jcr:nodeProperty node="${currentNode}" name="j:bindedComponent" var="linked"/>

<div  class="tagthispage">
	<template:module node="${linked.node}" forcedTemplate="hidden.tags"/>
	<template:module node="${linked.node}" forcedTemplate="hidden.addTag" editable="false" />
</div>
<template:linker path="*" mixinType="jmix:tagged"/>