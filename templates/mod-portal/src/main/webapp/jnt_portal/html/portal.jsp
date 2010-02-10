<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="portal.css"/>
<template:addResources type="javascript" resources="jquery-1.2.6.min.js,jquery-ui-personalized-1.6rc2.min.js,inettuts.js"/>
<div id="head">
    <h1>iNettuts</h1>

</div>

<div id="columns">

    <ul id="column1" class="column">
        <li class="widget color-green" id="intro">
            <div class="widget-head">
                <h3>Introduction Widget</h3>
            </div>
            <div class="widget-content">

                <p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam magna sem, fringilla in, commodo a, rutrum ut, massa. Donec id nibh eu dui auctor tempor. Morbi laoreet eleifend dolor. Suspendisse pede odio, accumsan vitae, auctor non, suscipit at, ipsum. Cras varius sapien vel lectus.</p>
            </div>
        </li>
        <li class="widget color-red">
            <div class="widget-head">
                <h3>Widget title</h3>
            </div>
            <div class="widget-content">

                <p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam magna sem, fringilla in, commodo a, rutrum ut, massa. Donec id nibh eu dui auctor tempor. Morbi laoreet eleifend dolor. Suspendisse pede odio, accumsan vitae, auctor non, suscipit at, ipsum. Cras varius sapien vel lectus.</p>
            </div>
        </li>
    </ul>

    <ul id="column2" class="column">
        <li class="widget color-blue">
            <div class="widget-head">
                <h3>Widget title</h3>

            </div>
            <div class="widget-content">
                <p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam magna sem, fringilla in, commodo a, rutrum ut, massa. Donec id nibh eu dui auctor tempor. Morbi laoreet eleifend dolor. Suspendisse pede odio, accumsan vitae, auctor non, suscipit at, ipsum. Cras varius sapien vel lectus.</p>
            </div>
        </li>
        <li class="widget color-yellow">
            <div class="widget-head">
                <h3>Widget title</h3>

            </div>
            <div class="widget-content">
                <p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam magna sem, fringilla in, commodo a, rutrum ut, massa. Donec id nibh eu dui auctor tempor. Morbi laoreet eleifend dolor. Suspendisse pede odio, accumsan vitae, auctor non, suscipit at, ipsum. Cras varius sapien vel lectus.</p>
            </div>
        </li>
    </ul>

    <ul id="column3" class="column">
        <li class="widget color-orange">
            <div class="widget-head">

                <h3>Widget title</h3>
            </div>
            <div class="widget-content">
                <p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam magna sem, fringilla in, commodo a, rutrum ut, massa. Donec id nibh eu dui auctor tempor. Morbi laoreet eleifend dolor. Suspendisse pede odio, accumsan vitae, auctor non, suscipit at, ipsum. Cras varius sapien vel lectus.</p>
            </div>
        </li>
        <li class="widget color-white">
            <div class="widget-head">

                <h3>Widget title</h3>
            </div>
            <div class="widget-content">
                <p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam magna sem, fringilla in, commodo a, rutrum ut, massa. Donec id nibh eu dui auctor tempor. Morbi laoreet eleifend dolor. Suspendisse pede odio, accumsan vitae, auctor non, suscipit at, ipsum. Cras varius sapien vel lectus.</p>
            </div>
        </li>

    </ul>

</div>
<script type="text/javascript">
    iNettuts.addWidgetControls();
    iNettuts.makeSortable();
</script>
