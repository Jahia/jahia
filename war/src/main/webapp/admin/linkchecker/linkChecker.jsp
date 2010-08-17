<%@include file="../include/header.inc" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<internal:gwtInit standalone="true"/>
<internal:gwtImport module="org.jahia.ajax.gwt.module.linkchecker.LinkChecker" />
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit">Jahia Link Checker</h2>
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
                           <fmt:message key="org.jahia.admin.linkChecker.label"/>
                      </div>
                  </div>
                  <div class="content-item-noborder">
                  <div id="linkchecker" class="evenOddTable"></div>
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
        </div>
      </div><%@include file="/admin/include/footer.inc" %>