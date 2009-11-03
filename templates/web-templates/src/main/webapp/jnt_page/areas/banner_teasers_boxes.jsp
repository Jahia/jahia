<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:module path="banner" autoCreateType="jnt:contentList" />
<template:module path="promo" autoCreateType="web_templates:promoAdvancedContainerList" template="twoColumns"/>
<template:module path="promoLarge" autoCreateType="web_templates:promoAdvancedContainerList" template="twoColumns">
    <template:param name="subNodesTemplate" value="large"/>
</template:module>
<template:module path="columnA_box" autoCreateType="jnt:contentList"/>
