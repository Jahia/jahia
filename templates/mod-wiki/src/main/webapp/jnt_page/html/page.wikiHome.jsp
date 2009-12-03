<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addWrapper name="wikiWrapper"/>
<div id="one"><!--start tab One-->

    <div class="intro wiki">
        Welcome to Jahia Wiki !

        <form id="" name="wikiForm"/>
        Create / find a page : <input id="link" name="link"> <input type="button" value="go" onclick="window.location.href='${currentNode.name}/'+form.elements.link.value+'.html'" />
        </form>

    </div>
</div>
<!--stop grid_10-->
