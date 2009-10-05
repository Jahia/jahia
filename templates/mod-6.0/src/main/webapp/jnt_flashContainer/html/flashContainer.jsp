<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jcr:nodeProperty node="${currentNode}" name="flashSourceFlashContainer" var="flashSource"/>
<jcr:nodeProperty node="${currentNode}" name="widthFlashContainer" var="widthFlash"/>
<jcr:nodeProperty node="${currentNode}" name="heightFlashContainer" var="heightFlash"/>
<jcr:nodeProperty node="${currentNode}" name="flashPlayerFlashContainer" var="flashPlayer"/>
<jcr:nodeProperty node="${currentNode}" name="idFlashContainer" var="idFlash"/>
<jcr:nodeProperty node="${currentNode}" name="nameFlashContainer" var="nameFlash"/>
<jcr:nodeProperty node="${currentNode}" name="swliveconnectFlashContainer" var="swliveconnectFlash"/>
<jcr:nodeProperty node="${currentNode}" name="playFlashContainer" var="playFlash"/>
<jcr:nodeProperty node="${currentNode}" name="loopFlashContainer" var="loopFlash"/>
<jcr:nodeProperty node="${currentNode}" name="menuFlashContainer" var="menuFlash"/>
<jcr:nodeProperty node="${currentNode}" name="qualityFlashContainer" var="qualityFlash"/>
<jcr:nodeProperty node="${currentNode}" name="scaleFlashContainer" var="scaleFlash"/>
<jcr:nodeProperty node="${currentNode}" name="alignFlashContainer" var="alignFlash"/>
<jcr:nodeProperty node="${currentNode}" name="salignFlashContainer" var="salignFlash"/>
<jcr:nodeProperty node="${currentNode}" name="wmodeFlashContainer" var="wmodeFlash"/>
<jcr:nodeProperty node="${currentNode}" name="bgcolorFlashContainer" var="bgcolorFlash"/>
<jcr:nodeProperty node="${currentNode}" name="baseFlashContainer" var="baseFlash"/>
<jcr:nodeProperty node="${currentNode}" name="flashvarsFlashContainer" var="flashvarsFlash"/>
	    <div id="flashcontent${currentNode.UUID}">
            <div id="flashcontent"><!--START FLASH -->
                <strong>You need to upgrade your Flash Player</strong><br />
                <br />
                <a href="http://www.adobe.com/go/getflashplayer"><img src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/160x41_Get_Flash_Player.jpg'/>" alt="get flash player" /></a>
                </div></div>
        <script type="text/javascript">
            var so = new SWFObject("${flashSource.node.path}", "${nameFlash.string}", "${widthFlash.long}", "${heightFlash.long}", "${flashPlayer.string}", "${bgcolorFlash.string}");
                    <c:if test="${not empty wmodeFlash.string}">
                          so.addParam("wmode", "${wmodeFlash.string}");
                    </c:if>
                    <c:if test="${not empty idFlash.string}">
                          so.addParam("id", "${idFlash.string}");
                    </c:if>
                    <c:if test="${not empty swliveconnectFlash.string}">
                          so.addParam("swliveconnect", "${swliveconnectFlash.string}");
                    </c:if>
                    <c:if test="${not empty playFlash.string}">
                          so.addParam("wmode", "${playFlash.string}");
                    </c:if>
                    <c:if test="${not empty menuFlash.string}">
                          so.addParam("menu", "${menuFlash.string}");
                    </c:if>
                    <c:if test="${not empty scaleFlash.string}">
                          so.addParam("scale", "${scaleFlash.string}");
                    </c:if>
                    <c:if test="${not empty qualityFlash.string}">
                          so.addParam("quality", "${qualityFlash.string}");
                    </c:if>
                    <c:if test="${not empty alignFlash.string}">
                          so.addParam("align", "${alignFlash.string}");
                    </c:if>
                    <c:if test="${not empty salignFlash.string}">
                          so.addParam("salign", "${salignFlash.string}");
                    </c:if>
                    <c:if test="${not empty baseFlash.string}">
                          so.addParam("base", "${baseFlash.string}");
                    </c:if>
                    <c:if test="${not empty flashvarsFlash.string}">
                          so.addParam("flashvars", "${flashvarsFlash.string}");
                    </c:if>
                  so.write("flashcontent${currentNode.UUID}");</script>