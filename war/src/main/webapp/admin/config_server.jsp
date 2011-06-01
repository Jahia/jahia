<%@page import="org.jahia.bin.*" %>
<%@taglib uri="http://www.jahia.org/tags/functions" prefix="functions" %>
<%@include file="/admin/include/header.inc" %>
<%
    stretcherToOpen = 0; %>
<script type="text/javascript" src="${pageContext.request.contextPath}/modules/assets/javascript/jquery.min.js"></script>
<script type="text/javascript">
<!--
function testSettings() {
    if (document.jahiaAdmin.host.value.length == 0) {
    	<fmt:message key="message.mailServer_mustSet.label" var="msg"/>
        alert("${functions:escapeJavaScript(msg)}");
        document.jahiaAdmin.host.focus();
    } else if (document.jahiaAdmin.to.value.length == 0) {
        <fmt:message key="message.mailAdmin_mustSet" var="msg"/>
        alert("${functions:escapeJavaScript(msg)}");
        document.jahiaAdmin.to.focus();
    } else if (document.jahiaAdmin.from.value.length == 0) {
    	<fmt:message key="message.mailFrom_mustSet.label" var="msg"/>
        alert("${functions:escapeJavaScript(msg)}");
        document.jahiaAdmin.from.focus();
    } else {
        if (typeof workInProgressOverlay != 'undefined') {
        	workInProgressOverlay.start();
        }
        
    	$.ajax({
            url: '${pageContext.request.contextPath}/cms/notification/testEmail',
            type: 'POST',
            dataType: 'text',
            cache: false,
            data: {
                host: document.jahiaAdmin.host.value,
                from: document.jahiaAdmin.from.value,
                to: document.jahiaAdmin.to.value
    		},
            success: function(data, textStatus) {
                if (typeof workInProgressOverlay != 'undefined') {
                    workInProgressOverlay.stop();
                }
            	if ("success" == textStatus) {
                    <fmt:message key="org.jahia.admin.server.ManageServer.testSettings.success" var="msg"/>
                    alert("${functions:escapeJavaScript(msg)}");
                } else {
                	<fmt:message key="org.jahia.admin.server.ManageServer.testSettings.failure" var="msg"/>
                    alert("${functions:escapeJavaScript(msg)}");
                }
            },
            error: function(xhr, textStatus, errorThrown) {
                if (typeof workInProgressOverlay != 'undefined') {
                    workInProgressOverlay.stop();
                }
                <fmt:message key="org.jahia.admin.server.ManageServer.testSettings.failure" var="msg"/>
                alert("${functions:escapeJavaScript(msg)}" + "\n" + xhr.status + " " + xhr.statusText + "\n" + xhr.responseText);
            }
        });
    }
}
//-->
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.emailSettings.label"/></h2>
</div>
<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
        <tbody>
            <tr>
                <td style="vertical-align: top;" align="left">
                    <%@include file="/admin/include/tab_menu.inc" %>
                </td>
            </tr>
            <tr>
                <td style="vertical-align: top;" align="left" height="100%">
                    <div class="dex-TabPanelBottom">
                        <div class="tabContent">
                            <jsp:include page="/admin/include/left_menu.jsp">
                                <jsp:param name="mode" value="server"/>
                            </jsp:include>
                            <div id="content" class="fit">
                                <div class="head headtop">
                                    <div class="object-title"><fmt:message key="org.jahia.admin.emailSettings.label"/>
                                    </div>
                                </div>
                                <div  class="content-item">
                                <c:if test="${not empty jahiaDisplayMessage}">
                                    <div class="redColor">
                                        <c:out value="${jahiaDisplayMessage}"/>
                                    </div>
                                    <br/>
                                </c:if>
                                <c:if test="${not empty jahiaDisplayInfo}">
                                    <div class="blueColor">
                                        <c:out value="${jahiaDisplayInfo}"/>
                                    </div>
                                    <br/>
                                </c:if>
                                <form name="jahiaAdmin"
                                      action='<%=JahiaAdministration.composeActionURL(request,response,"server","&sub=process")%>'
                                      method="post">
                                    <table cellpadding="5" cellspacing="0" border="0">
                                        <tr>
                                            <td>
                                                <label for="serviceActivated">
                                                    <fmt:message key="org.jahia.admin.server.ManageServer.serviceEnabled.label"/>&nbsp;:
                                                </label>
                                            </td>
                                            <td>
                                                <input class="input" type="checkbox" name="serviceActivated"
                                                       id="serviceActivated"
                                                        <c:if test='${jahiaMailSettings.serviceActivated}'>
                                                            checked="checked"
                                                        </c:if>/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <fmt:message key="org.jahia.admin.server.ManageServer.mailServer.label"/>&nbsp;:
                                            </td>
                                            <td>
                                                <input class="input" type="text" name="host" size="70"
                                                       maxlength="250"
                                                       value="<c:out value='${jahiaMailSettings.host}'/>"/>
                                                &nbsp;
                                                <a href="http://jira.jahia.org/browse/JKB-20" target="_blank" style="cursor: pointer;"><img src="${pageContext.request.contextPath}/engines/images/about.gif" alt="info"  style="cursor: pointer;"/></a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <fmt:message key="org.jahia.admin.server.ManageServer.mailAdministrator.label"/>&nbsp;:
                                            </td>
                                            <td>
                                                <input class="input" type="text" name="to" size="<%=inputSize%>"
                                                       maxlength="250" value="<c:out value='${jahiaMailSettings.to}'/>">
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <fmt:message key="org.jahia.admin.server.ManageServer.mailFrom.label"/>&nbsp;:
                                            </td>
                                            <td>
                                                <input class="input" type="text" name="from" size="<%=inputSize%>"
                                                       maxlength="250"
                                                       value="<c:out value='${jahiaMailSettings.from}'/>">
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <fmt:message key="org.jahia.admin.server.ManageServer.eventNotificationLevel.label"/>&nbsp;:
                                            </td>
                                            <td>
                                                <select class="input" name="notificationLevel">
                                                    <option value="Disabled" ${jahiaMailSettings.notificationLevel == 'Disabled' ? 'selected="selected"' : ''}><fmt:message key="org.jahia.admin.server.ManageServer.disabled.label"/></option>
                                                    <option value="Standard" ${jahiaMailSettings.notificationLevel == 'Standard' ? 'selected="selected"' : ''}><fmt:message key="org.jahia.admin.server.ManageServer.standard.label"/></option>
                                                    <option value="Wary" ${jahiaMailSettings.notificationLevel == 'Wary' ? 'selected="selected"' : ''}><fmt:message key="org.jahia.admin.server.ManageServer.wary.label"/></option>
                                                    <option value="Paranoid" ${jahiaMailSettings.notificationLevel == 'Paranoid' ? 'selected="selected"' : ''}><fmt:message key="org.jahia.admin.server.ManageServer.paranoid.label"/></option>
                                                </select>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="2" align="right">
                                              <span class="dex-PushButton">
                                                <span class="first-child">
                                                  <a class="ico-mail-test" href="#" onclick="testSettings(); return false;"><fmt:message key="org.jahia.admin.server.ManageServer.testSettings.label"/></a>
                                                </span>
                                              </span>

                                            </td>
                                        </tr>
                                    </table>
                                </form>
                            </div>
                            </div>
                        </div>
                </td>
            </tr>
        </tbody>
    </table>
</div>
<div id="actionBar">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-back"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:document.jahiaAdmin.submit();"><fmt:message key="org.jahia.admin.saveChanges.label"/></a>
            </span>
          </span>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>