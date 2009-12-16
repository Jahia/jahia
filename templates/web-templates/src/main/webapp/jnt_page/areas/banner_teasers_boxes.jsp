<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:area path="banner" areaType="jnt:contentList" />
<template:area path="promo" areaType="web_templates:promoAdvancedContainerList" template="twoColumns"/>
<template:area path="promoLarge" areaType="web_templates:promoAdvancedContainerList" template="twoColumns">
    <template:param name="subNodesTemplate" value="large"/>
</template:area>
<template:area path="columnA_box" areaType="jnt:contentList"/>
