<%@include file="/admin/include/header.inc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://www.jahia.org/tags/functions" prefix="functions" %>
<c:set var="theSite" value="${sessionScope['org.jahia.services.sites.jahiasite']}"/>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="label.htmlSettings"/>
    <c:if test="${not empty theSite}">:&nbsp;<fmt:message
            key="org.jahia.admin.site.label"/>&nbsp;${fn:escapeXml(theSite.title)}</c:if>
    </h2>
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
                                <div class="head headtop">
                                    <div class="object-title"><fmt:message key="label.htmlSettings"/>
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
                                      action='<%=JahiaAdministration.composeActionURL(request,response,"htmlSettings","&sub=update")%>'
                                      method="post">
                                    <table cellpadding="5" cellspacing="0" border="0">
                                        <tr>
                                            <td>
                                                <label for="wcagCompliance">
                                                    <fmt:message key="label.htmlSettings.wcagCompliance"/>:
                                                </label>
                                            </td>
                                            <td>
                                                <input class="input" type="checkbox" name="wcagCompliance"
                                                       id="wcagCompliance" value="true"
                                                       ${wcagCompliance ? 'checked="checked"' : ''}/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <label for="doTagFiltering">
                                                    <fmt:message key="label.htmlSettings.markupFiltering"/>:
                                                </label>
                                            </td>
                                            <td>
                                                <input class="input" type="checkbox" name="doTagFiltering"
                                                       id="doTagFiltering" value="true"
                                                       ${doTagFiltering ? 'checked="checked"' : ''}/>
                                                &nbsp;
                                                <input class="input" type="text" name="filteredTags"
                                                       id="filteredTags" size="70"
                                                       value="${fn:escapeXml(filteredTags)}"/>
                                                &nbsp;
                                                <fmt:message key="label.htmlSettings.markupFiltering.info" var="msg"/>
                                                <a href="#help" style="cursor: pointer;" onclick="alert('${functions:escapeJavaScript(msg)}'); return false;"><img src="${pageContext.request.contextPath}/engines/images/about.gif" alt="info"  style="cursor: pointer;"/></a>
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