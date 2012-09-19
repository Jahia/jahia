<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:if test="${not renderContext.editMode}">
    <template:addResources type="css" resources="jquery.jgrowl.css"/>
    <template:addResources type="javascript" resources="jquery.min.js,jquery.atmosphere.js,jquery.jgrowl.js"/>
    <c:set var="linked" value="${ui:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
    <template:addCacheDependency node="${linked}"/>
    <script type="text/javascript">

        function callbackNodeChannel(response) {
            // Websocket events.
            $.atmosphere.log('info', ["nodeChannel.state: " + response.state]);
            $.atmosphere.log('info', ["nodeChannel.transport: " + response.transport]);
            $.atmosphere.log('info', ["nodeChannel.status: " + response.status]);

            detectedTransport = response.transport;
            if (response.transport != 'polling' && response.state != 'connected' && response.state != 'closed') {
                $.atmosphere.log('info', ["nodeChannel.responseBody: " + response.responseBody]);
                if (response.status == 200) {
                    var data = $.parseJSON(response.responseBody);
                    if (data) {
                        var message = $("<p>" + data.body + "</p>").hide().append($("<br/><a href='" + data.url + "'>" + data.name + "</a> "));
                        $.jGrowl(message.html(), {sticky:true});
                    }
                }
            }
        }

        $(document).ready(function() {
            $.atmosphere.unsubscribe();
            $.atmosphere.subscribe("${url.server}${url.context}/atmosphere/pubsub/channel/${linked.identifier}", callbackNodeChannel,
                    $.atmosphere.request = { transport: "websocket" });
        });
    </script>
</c:if>