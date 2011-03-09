<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="gadgetUrl" value="${currentNode.propertiesAsString['j:url']}"/>
<c:set var="gadgetHeight" value="${currentNode.properties['j:height'].long}"/>
<c:set var="gadgetWidth" value="${currentNode.properties['j:width'].long}"/>
<c:if test="${not renderContext.editMode}">
    <c:if test="${empty requestScope['org.jahia.modules.shindig.containerResourcesIncluded']}">
        <c:set var="org.jahia.modules.shindig.containerResourcesIncluded" value="true" scope="request"/>
        <c:set var="org.jahia.modules.shindig.gadgetIndex" value="0" scope="request"/>
        <template:addResources type="css" resources="${url.currentModule}/container/gadgets.css"/>
        <template:addResources type="javascript" resources="${url.context}/gadgets/js/core:rpc:pubsub:shindig-container.js?c=1&debug=1"/>
        <template:addResources type="javascript" resources="jquery.js"/>

        <template:addResources>
            <script type="text/javascript">
                var jahiaGadgetUrls = new Array();

                $(document).ready(function () {
                    var ids = new Array();
                    var myGadgets = new Array();
                    for (var i = 0; i < jahiaGadgetUrls.length; i++) {
                        var gd = shindig.container.createGadget(jahiaGadgetUrls[i]);
                        gd.setServerBase('${url.context}/gadgets/');
                        myGadgets.push(gd);
                        shindig.container.addGadget(gd);
                        ids.push('gadget-chrome-' + i);
                    }
                    shindig.container.layoutManager.setGadgetChromeIds(ids);

                    for (var i = 0; i < myGadgets.length; i++) {
                        shindig.container.renderGadget(myGadgets[i]);
                    }
                });
            </script>
        </template:addResources>
    </c:if>
    <template:addResources>
        <script type="text/javascript">
            jahiaGadgetUrls.push({specUrl:'${gadgetUrl}'<c:if test="${gadgetHeight > 0}">, height:${gadgetHeight}</c:if><c:if test="${gadgetWidth > 0}">, width:${gadgetWidth}</c:if>,title:'${functions:escapeJavaScript(currentNode.displayableName)}'});
        </script>
    </template:addResources>
</c:if>

<div id="gadget-chrome-${requestScope['org.jahia.modules.shindig.gadgetIndex']}" class="gadgets-gadget-chrome${renderContext.editMode ? ' x-panel-linker' : ''}">
    <c:if test="${renderContext.editMode}">
        <h4><fmt:message key="jnt_openSocialGadget"/>&nbsp;${fn:escapeXml(functions:default(currentNode.propertiesAsString['jcr:title'], currentNode.name))}</h4>
        <p><strong><fmt:message key="jnt_openSocialGadget.j_url"/></strong>:&nbsp;${fn:escapeXml(gadgetUrl)}</p>
    </c:if>
</div>

<c:set var="org.jahia.modules.shindig.gadgetIndex" value="${requestScope['org.jahia.modules.shindig.gadgetIndex'] + 1}" scope="request"/>