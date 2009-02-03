<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@include file="include/header.inc" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
stretcherToOpen   = 0; %>
<script src="${pageContext.request.contextPath}/jsp/jahia/javascript/prototype/prototype-150-compressed.js">
</script>
<script type="text/javascript">
  
      freqCall = 1;
      watchcount = 0;
      var results = new Array();
  
      function getElementValueFromXmlDoc(xdoc,id,def){
          if(!xdoc || !xdoc.getElementsByTagName(id)) return def;
          var currentagobject = xdoc.getElementsByTagName(id);
          if(currentagobject.length>0) return currentagobject[0].firstChild.data;
          return def;
      }
  
      function monitor()
      {
          var url = '<%=request.getContextPath()%>/ajaxaction/GetPatchesStatus';
          do_ajax = new Ajax.Request(url, {onSuccess: function(tr) {
              var xdoc = tr.responseXML;
              var elements = xdoc.getElementsByTagName("script");
              for (i=0; i<elements.length; i++) {
                  var result = getElementValueFromXmlDoc(elements[i],"result",'-1');
                  var divElementIcon = document.getElementById( getElementValueFromXmlDoc(elements[i],"name","")+"Icon" );
                  var divElementStatus = document.getElementById( getElementValueFromXmlDoc(elements[i],"name","")+"Status" );
  
                  if (result != results[i]) {
                      if (result==-1) {
                          divElementIcon.src="<%=request.getContextPath()%>/jsp/jahia/engines/images/waiting.gif";
                      } else if (result==0) {
                          divElementIcon.src="<%=request.getContextPath()%>/jsp/jahia/engines/images/icons/workflow/accept.gif";
                      } else {
                          divElementIcon.src="<%=request.getContextPath()%>/jsp/jahia/engines/images/icons/workflow/errors.gif";
                      }
                      results[i] = result;
                  }
  
                  if (result==-1) {
                      divElementStatus.innerHTML = getElementValueFromXmlDoc(elements[i],"substatus","") + " - " + getElementValueFromXmlDoc(elements[i],"completed",'0') + "% ( "+ getElementValueFromXmlDoc(elements[i],"remaining",'0') + " )";
                  } else if (result==0) {
                      divElementStatus.innerHTML = '<internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.success.label"/>';
                  } else {
                      divElementStatus.innerHTML = '<internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.failed.label"/>';
                  }
              }
          }
          });
      }
  
      function watch(){
          watcher = new PeriodicalExecuter(monitor, freqCall);
      }
  
      function doAction(action) {
          if (!action) {
              action = 'reset';
          }
          var divElement = document.getElementById('installLink');
          divElement.innerHTML = "<internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.ongoingInstall.label"/>";
          monitor();
          watch();
          document.jahiaAdmin.sub.value = action;
          document.jahiaAdmin.submit();
          return false;
      }
  
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.label"/></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/jsp/jahia/administration/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
            <jsp:include page="/jsp/jahia/administration/include/left_menu.jsp">
                <jsp:param name="mode" value="server"/>
            </jsp:include>
              <div id="content" class="fit">
                <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"patches","")%>' method="post">
                  <input type="hidden" name="sub" value="reset" />
                  <c:forEach items="${installedPatches}" var="version" varStatus="versionStatus">
                    <c:if test="${versionStatus.index == 0}">
                      <div class="head">
                        <div class="object-title">
                          <c:out value="${version.key.buildNumber}"/>- <c:out value="${version.key.releaseNumber}"/>( <internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.initialInstall.label"/>)
                        </div>
                      </div>
                    </c:if>
                    <c:if test="${versionStatus.index > 0}">
                      <div class="head">
                        <div class="object-title">
                          <c:out value="${version.key.buildNumber}"/>- <c:out value="${version.key.releaseNumber}"/>( <fmt:formatDate value="${version.key.installationDate}" type="both" timeStyle="long" dateStyle="long" />)
                        </div>
                      </div>
                      <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                        <thead>
                          <tr>
                            <th>
                              <internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.patchName.label"/>
                            </th>
                            <th>
                              <internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.installationDate.label"/>
                            </th>
                            <th class="lastCol">
                              <internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.status.label"/>
                            </th>
                          </tr>
                        </thead>
                        <c:forEach items="${version.value}" var="patch" varStatus="patchStatus">
                        <tr class="<c:if test='${patchStatus.index % 2 == 0}'>oddLine</c:if>">
                          <td>
                            <c:out value="${patch.name}"/>
                          </td>
                          <td>
                          <fmt:formatDate value="${patch.installationDate}" type="both" timeStyle="long" dateStyle="long" />
                          <td class="lastCol">
                            <c:if test="${patch.resultCode == 0}">
                              <internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.success.label"/>
                            </c:if>
                            <c:if test="${patch.resultCode != 0}">
                              <internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.failed.label"/>
                            </c:if>
                          </td>
                        </tr>
                        </c:forEach>
                      </table>
                    </c:if>
                  </c:forEach>
                  <c:if test="${isPatchesAvailable}">
                    <div class="head">
                      <div class="object-title">
                        <internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.available.label"/>
                      </div>
                    </div>
                    <div id="installLink">
                      <a href="javascript:doAction('install');"><internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.install.label"/></a>
                    </div>
                    <br/>
                    <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                      <thead>
                        <tr>
                          <th>
                            <internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.patchName.label"/>
                          </th>
                          <th colspan="2" class="lastCol">
                            <internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.status.label"/>
                          </th>
                        </tr>
                      </thead>
                      <c:forEach items="${availablePatches}" var="patch" varStatus="patchStatus">
                        <tr class="<c:if test='${patchStatus.index % 2 == 0}'>oddLine</c:if>">
                          <td>
                            <c:out value="${patch.key.name}"/>
                          </td>
                          <td class="lastCol">
                            <img id="<c:out value='${patch.key.name}'/>Icon" src="<%=request.getContextPath()%>/jsp/jahia/engines/images/pix.gif" />
                          </td>
                          <td>
                          <div id="<c:out value='${patch.key.name}'/>Status">
                          </div>
                          </div>
                          <div id="<c:out value='${patch.key.name}'/>Percent">
                          </div>
                        </td>
                        </tr>
                      </c:forEach>
                    </table>
                  </c:if>
                  <c:if test="${!isPatchesAvailable}">
                    <p>
                      &nbsp;&nbsp;<internal:adminResourceBundle resourceName="org.jahia.admin.patchmanagement.nopatch.label"/>
                    </p>
                  </c:if>
                </form>
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
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><internal:adminResourceBundle resourceName="org.jahia.admin.backToMenu.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/jsp/jahia/administration/include/footer.inc" %>
