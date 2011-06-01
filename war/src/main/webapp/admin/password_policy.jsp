<%@include file="include/header.inc" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
stretcherToOpen   = 0; %>
<script type="text/javascript">
  function doAction(action){
      if (!action) {
          action = 'reset';
      }
      document.jahiaAdmin.sub.value = action;
      document.jahiaAdmin.submit();
      return false;
  }
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="org.jahia.admin.passwordPolicies.label"/></h2>
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
                  <div class="head">
                      <div class="object-title">
                           <fmt:message key="org.jahia.admin.passwordPolicies.mainMenu.label"/>
                      </div>
                  </div>
                  <div  class="content-item-noborder">
                <c:if test="${not empty confirmationMessage}">
                  <% String msgKey = (String)request.getAttribute("confirmationMessage"); %>
                  <div class="blueColor">
                    <fmt:message key="<%= msgKey %>"/>
                  </div>
                </c:if>
                <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"passwordPolicies","")%>' method="post">
                  <input type="hidden" name="sub" value="reset" /><%--
                  <h2><c:out value="${policy.name}"/></h2>
                  --%>
                  <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                    <thead>
                      <tr>
                        <th width="7%">
                          <fmt:message key="org.jahia.admin.passwordPolicies.active.label"/>
                        </th>
                        <th width="50%">
                          <fmt:message key="label.name"/>
                        </th>
                        <th width="43%" class="lastCol">
                          <fmt:message key="label.parameters"/>
                        </th>
                      </tr>
                    </thead>
                    <c:forEach items="${policy.rules}" var="rule" varStatus="rlzStatus">
                      <tr class="<c:if test='${rlzStatus.index % 2 == 0}'>oddLine</c:if>">
                        <td align="center">
                          <input type="checkbox" name="rules[<c:out value='${rlzStatus.index}'/>].active"
                          <c:if test="${rule.active}">
                            checked="checked"
                          </c:if>
                          value="true"/>
                        </td>
                        <c:set var="i18nKey" value='org.jahia.admin.passwordPolicies.rule.${rule.name}'/>
                        <td>
                          <fmt:message key='<%= (String)pageContext.getAttribute("i18nKey") %>'/>
                        </td>
                        <td class="lastCol">
                          <table width="100%">
                            <c:forEach items="${rule.conditionParameters}" var="condParam" varStatus="paramsStatus">
                              <tr>
                                <c:set var="i18nKey" value='label.${condParam.name}'/>
                                <td width="45%" align="right">
                                  <fmt:message key='<%= (String)pageContext.getAttribute("i18nKey") %>'/>:
                                </td>
                                <td width="55%">
                                  <input type="text" name="rules[<c:out value='${rlzStatus.index}'/>].conditionParameters[<c:out value='${paramsStatus.index}'/>].value" value="<c:out value='${condParam.value}'/>"/>
                                </td>
                              </tr>
                            </c:forEach>
                          </table>
                        </td>
                      </tr>
                    </c:forEach>
                  </table>
                </form>
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
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-restore" href="#reset" onclick="return doAction('reset')"><fmt:message key="label.restore"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="#save" onclick="return doAction('save')"><fmt:message key="org.jahia.admin.saveChanges.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/admin/include/footer.inc" %>