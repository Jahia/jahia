<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<%-- Include the VideoJS Library --%>
<template:addResources type="javascript" resources="video.js"/>

<template:addResources>

    <script type="text/javascript">
        // Must come after the video.js library

        // Add VideoJS to all video tags on the page when the DOM is ready
        VideoJS.setupAllWhenReady();

        /* ============= OR ============ */

        // Setup and store a reference to the player(s).
        // Must happen after the DOM is loaded
        // You can use any library's DOM Ready method instead of VideoJS.DOMReady

        /*
        VideoJS.DOMReady(function(){

          // Using the video's ID or element
          var myPlayer = VideoJS.setup("example_video_1");

          // OR using an array of video elements/IDs
          // Note: It returns an array of players
          var myManyPlayers = VideoJS.setup(["example_video_1", "example_video_2", video3Element]);

          // OR all videos on the page
          var myManyPlayers = VideoJS.setup("All");

          // After you have references to your players you can...(example)
          myPlayer.play(); // Starts playing the video for this player.
        });
        */

        /* ========= SETTING OPTIONS ========= */

        // Set options when setting up the videos. The defaults are shown here.

        /*
        VideoJS.setupAllWhenReady({
          controlsBelow: false, // Display control bar below video instead of in front of
          controlsHiding: true, // Hide controls when mouse is not over the video
          defaultVolume: 0.85, // Will be overridden by user's last volume if available
          flashVersion: 9, // Required flash version for fallback
          linksHiding: true // Hide download links when video is supported
        });
        */

        // Or as the second option of VideoJS.setup

        /*
        VideoJS.DOMReady(function(){
          var myPlayer = VideoJS.setup("example_video_1", {
            // Same options
          });
        });
        */

      </script>

 </template:addResources>


<template:addResources type="css" resources="video-js.css"/>

  <div class="video-js-box">

      <%--Test if it is a flv file--%>
    <c:set var="flv" value="${fn:endsWith(currentNode.properties.source.node.url,'.flv')}" />

     <%--If it is not a flv file, try to display it as a HTML 5 video--%>
    <c:if test="${!flv && !currentNode.properties.forceFlashPlayer.boolean}">
        <video id="${currentNode.UUID}" class="video-js" width="${currentNode.properties.width.string}" height="${currentNode.properties.height.string}" controls="controls" <c:if test="${currentNode.properties.autoplay.boolean}">autoplay="autoplay"</c:if> preload="auto" poster="" >

            <%--Prefer the HTML5 version--%>
            <source src="${currentNode.properties.source.node.url}" />
    </c:if>

            <%--If not available fallback to the flash player--%>
            <object id="${currentNode.UUID}" class="vjs-flash-fallback" width="${currentNode.properties.width.string}" height="${currentNode.properties.height.string}" type="application/x-shockwave-flash" data="${url.server}${url.context}/modules/assets/swf/flowplayer-3.2.6.swf">
                <param name="movie" value="${url.server}${url.context}/modules/assets/swf/flowplayer-3.2.6.swf" />
                <param name="allowfullscreen" value="true" />
                <param name="flashvars" value='config={"playlist":[{"url": "${url.server}${currentNode.properties.source.node.url}","autoPlay":${currentNode.properties.autoplay.boolean},"autoBuffering":true}]}' />

                <img src="" width="${currentNode.properties.width.string}" height="${currentNode.properties.height.string}" alt="Poster Image" title="No video playback capabilities." />
            </object>

     <c:if test="${!flv && !currentNode.properties.forceFlashPlayer.boolean}">
        </video>
     </c:if>

  </div>
