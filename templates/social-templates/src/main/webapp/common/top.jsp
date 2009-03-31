<%@ include file="declarations.jspf" %>
<div id="pageTop"><!--start top-->
    <div id="logotop">
        <div class="name">
            <a href='<template:composePageURL pageID="${requestScope.currentSite.homepageID}"/>'>
                <img class="logotop" src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/logo-top.png'/>" alt="logo" />
                <h1><span>${currentPage.title}</span></h1>
            </a>
        </div>
        <div class="desc">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce auctor dapibus nibh. Proin viverra arcu eget lorem.Maecenas ligula ligula, tristique in, venenatis posuere.
        </div>
        <blockquote>&nbsp;</blockquote>
    </div>
</div><!--stop top-->