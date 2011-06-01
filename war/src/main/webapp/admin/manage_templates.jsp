<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.bin.*" %>
<%@page import="org.jahia.params.*" %>
<%@page import="org.jahia.utils.*" %>
<%@page import="java.util.*,org.jahia.data.JahiaData" %>
<%
    String theURL = "";
    String requestURI = (String) request.getAttribute("requestURI");
    String contextRoot = (String) request.getContextPath();
    JahiaSite site = (JahiaSite) request.getAttribute("site");
    ProcessingContext jParams = jData.getProcessingContext(); %>

<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="label.manageTemplates"/><% if (site != null) { %><fmt:message
            key="org.jahia.admin.site.label"/>&nbsp;:&nbsp;<%=site.getServerName() %><%} %></h2>
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
                            <jsp:param name="mode" value="site"/>
                        </jsp:include>
                        <div id="content" class="fit">
                            <div class="head">
                                <div class="object-title">
                                    <fmt:message key="label.manageTemplates"/>
                                </div>
                            </div>


                            <div class="content-body">
                                <div id="operationMenu">
                                    <c:forEach items="${templates}" var="template">
                                        <div>

                <span class="dex-PushButton">
                  <span class="first-child"> 
                     <a class="ico-tpl-view"
                        href="<%=JahiaAdministration.composeActionURL(request, response, "templates", "&amp;sub=synchronize&amp;path=")%>${template.key}"
                        onclick="showWorkInProgress(); return true;"
                        alt="<fmt:message key="org.jahia.admin.templates.ManageTemplates.synchTemplates.label"/>"><fmt:message
                             key="label.templatesDeploy"/> : ${template.value}</a>
                  </span>
                </span>
                                        </div>
                                    </c:forEach>
                                    <c:if test="${not empty jahiaDisplayMessage}">
                                        <div class="redColor">
                                            <c:out value="${jahiaDisplayMessage}"/>
                                        </div>
                                    </c:if>

                                </div>
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
         href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message
              key="label.backToMenu"/></a>
    </span>
  </span>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>
