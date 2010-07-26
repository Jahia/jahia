<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery-1.3.1.min.js"/>
<template:addResources type="javascript" resources="jquery.juitter.js"/>
<template:addResources type="css" resources="main.css"/>
<jcr:nodeProperty node="${currentNode}" name="lang" var="lang"/>
<script type="text/javascript">
    $(function() {
        $.Juitter.start({
            searchType:"searchWord", // needed, you can use "searchWord", "fromUser", "toUser"
            searchObject:"iPhone,apple,ipod",
            <c:if test="${not empty lang}">lang:"${currentNode.properties.lang.string}",</c:if> // restricts the search by the given language
            live:"live-${currentNode.properties.timeUpdate.long}", // the number after "live-" indicates the time in seconds to wait before request the Twitter API for updates.
            placeHolder:"myContainer", // Set a place holder DIV which will receive the list of tweets example <div id="juitterContainer"></div>
            loadMSG: "Loading messages...", // Loading message, if you want to show an image, fill it with "image/gif" and go to the next variable to set which image you want to use on
            imgName: "loader.gif", // Loading image, to enable it, go to the loadMSG var above and change it to "image/gif"
            total: ${currentNode.properties.numberOfTweets.string}, // number of tweets to be show - max 100
            readMore: "Read it on Twitter", // read more message to be show after the tweet content
            nameUser:"image", // insert "image" to show avatar of "text" to show the name of the user that sent the tweet
            openExternalLinks:"newWindow", // here you can choose how to open link to external websites, "newWindow" or "sameWindow"
            filter:"sex->*BAD word*,porn->*BAD word*,fuck->*BAD word*,shit->*BAD word*"  // insert the words you want to hide from the tweets followed by what you want to show instead example: "sex->censured" or "porn->BLOCKED WORD" you can define as many as you want, if you don't want to replace the word, simply remove it, just add the words you want separated like this "porn,sex,fuck"... Be aware that the tweets will still be showed, only the bad words will be removed
        });
         $("#search").submit(function() {
            $.Juitter.start({
                <c:if test="${not empty lang}">lang:"${currentNode.properties.lang.string}",</c:if>
                searchType:"searchWord",
                placeHolder:"myContainer",
                searchObject:$(".search").val(),
                live:"live-${currentNode.properties.timeUpdate.long}",
                filter:"sex->*BAD word*,porn->*BAD word*,fuck->*BAD word*,shit->*BAD word*"
            });
            return false;
        });

        $(".search").blur(function() {
            if ($(this).val() == "") $(this).val("Type a word and press enter");
        });
        $(".search").click(function() {
            if ($(this).val() == "Type a word and press enter") $(this).val("");
        });
    });
</script>

<div id="myContent">
    <div id="juitter">
        <form action="" method="post" id="search">
            <p>Search Twitter: <input type="text" class="search" value="Type a word and press enter"/></p>
        </form>
        <div id="myContainer">
        </div>
    </div>
</div>