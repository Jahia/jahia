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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.data.containers.JahiaContainer" %>
<%@ page import="org.jahia.data.containers.JahiaContainerDefinition" %>
<%@ page import="org.jahia.data.containers.JahiaContainerList" %>
<%@ page import="org.jahia.data.fields.LoadFlags" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.engines.addcontainer.AddContainer_Engine" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.jahia.services.lock.LockKey" %>
<%@ page import="org.jahia.services.lock.LockPrerequisites" %>
<%@ page import="org.jahia.services.lock.LockPrerequisitesResult" %>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%!
//  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("jsp.jahia.engines.navigation");

private static int getNextCtnID(final List<Integer> ids, final int currentID) {
  for (int i = 0; i < ids.size(); i++) {
    final int value = ((Integer) ids.get(i)).intValue();
    if (value == currentID) return ((Integer) ids.get(i + 1)).intValue();
  }
  return -1;
}

private static int getLastCtnID(final List<Integer> ids) {
  if (ids.size() == 0) return -1;
  return ((Integer) ids.get(ids.size() - 1)).intValue();
}

private static int getPreviousCtnID(final List<Integer> ids, final int currentID, final boolean isAddContainer) {
  if (isAddContainer) return getLastCtnID(ids);
  for (int i = 0; i < ids.size(); i++) {
    final int value = ((Integer) ids.get(i)).intValue();
    if (value == currentID) return ((Integer) ids.get(i - 1)).intValue();
  }
  return -1;
}

private static int getFirstCtnID(final List<Integer> ids) {
  if (ids.size() == 0) return -1;
  return ((Integer) ids.get(0)).intValue();
}

private static int getCtnIndex(final List<Integer> ids, final int currentID) {
  for (int i = 0; i < ids.size(); i++) {
    final int value = ((Integer) ids.get(i)).intValue();
    if (value == currentID) return (i + 1);
  }
  return -1;
}
%>
<%
final Map<String, Object> engineMap = (Map<String, Object>) request.getAttribute("org.jahia.engines.EngineHashMap");
final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
final JahiaContainer theEditedContainer = (JahiaContainer) engineMap.get("theContainer");
int parentListID = -1;
if (theEditedContainer != null) {
  parentListID = theEditedContainer.getListID();
}
if (parentListID > -1) {
  final JahiaContainerList theParentList = ServicesRegistry.getInstance().getJahiaContainersService().loadContainerList(parentListID, LoadFlags.ALL, jData.getProcessingContext());
  final int listType = Integer.parseInt(theParentList.getDefinition().getProperty(JahiaContainerDefinition.CONTAINER_LIST_TYPE_PROPERTY));
  if (theEditedContainer != null && listType == 0) {
    List<Integer> ctnIds = (List<Integer>) session.getAttribute("getSorteredAndFilteredCtnIds" + parentListID);
    if (ctnIds == null) {
        ctnIds = new ArrayList<Integer>();
    }
    final boolean enableForAdd = ctnIds.size() > 0;
    final boolean enableForUpdate = ctnIds.size() > 1;
    final boolean enableNext = enableForAdd && theEditedContainer.getID() != ((Integer) ctnIds.get(ctnIds.size() - 1)).intValue();
    final boolean enablePrevious = enableForAdd && theEditedContainer.getID() != ((Integer) ctnIds.get(0)).intValue();
    final boolean isAddContainer = engineMap.get(JahiaEngine.ENGINE_NAME_PARAM).equals(AddContainer_Engine.ENGINE_NAME);
    final boolean enableNavigation = isAddContainer ? enableForAdd : enableForUpdate;
    final boolean isLocked = jspSource.equals("lock");
    final LockKey lockKey = (LockKey) engineMap.get("LockKey");
    final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult(lockKey);
    %>
    <script type="text/javascript">
      function navigate(goTo) {
        document.mainForm.navigation.value = goTo;
        <% if (isAddContainer) { %>
          sendFormSaveAndAddNew();
        <% } else { %>
          if (goTo == "new") {
            <% if(isLocked){ %>
              submittedCount++;
              document.location.href = '<%=jData.gui().drawAddContainerUrl(theParentList)%>';
            <% } else { %>
              sendFormApply();
            <% } %>
          } else {
              sendFormApply();
          }
        <% } %>
      }

      function keyDown(e) {
        var code;
        if (window.event)
            code = window.event.keyCode;
        else if (e)
            code = e.which;
        else
            code = null;

        if (code == 13) {
        <% if (isAddContainer) { %>
          var currentId = <%=ctnIds.size() + 1%>;
        <% } else { %>
          var currentId = <%=getCtnIndex(ctnIds, theEditedContainer.getID())%>;
        <% } %>
          try {
            var id = parseInt(document.mainForm.goToId.value, 10);
            if (id > 0 && id < (<%=ctnIds.size()%> + 1) && id != currentId) {
              document.mainForm.first.value = getCtnId(id);
              navigate("first");
            } else {
              document.mainForm.goToId.value = currentId;
            }
          } catch (e) {
            delete code;
            document.mainForm.goToId.value = currentId;
            return false;
          }
        }
        delete code;
        return true;
      }

      var ctnIDs = <%=ctnIds.toString()%>;
      function getCtnId(index) {
        return ctnIDs[index - 1];
      }
    </script>
    <!-- navigationBar (start) -->
    <div id="navigationBar">
      <input type="hidden" name="navigation"/>
      <table cellpadding="0" cellspacing="0" border="0">
      <% if (enableNavigation) { %>
        <tr>
          <td valign="top">
          <% if (enablePrevious) { %>
            <a href="#" onclick="navigate('first'); return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toFirst.button"/>" class="navigationButton ico-nav-first<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
            <input type="hidden" name="first" value="<%= getFirstCtnID(ctnIds)%>"/>
          <% } else { %>
            <a href="#" onclick="return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toFirst.button"/> (<internal:engineResourceBundle resourceName="org.jahia.disabled"/>)" class="navigationButton ico-nav-first<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
          <% } %>
          </td>
          <td valign="top">
          <% if (enablePrevious) { %>
            <a href="#" onclick="navigate('previous'); return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toPrevious.button"/>" class="navigationButton ico-nav-previous<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
            <input type="hidden" name="previous" value="<%=getPreviousCtnID(ctnIds, theEditedContainer.getID(), isAddContainer)%>"/>
          <% } else { %>
            <a href="#" onclick="return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toPrevious.button"/> (<internal:engineResourceBundle resourceName="org.jahia.disabled"/>)" class="navigationButton ico-nav-previous<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
          <% } %>
          </td>
          <% if (isAddContainer) { %>
            <td valign="top">
              &nbsp;&nbsp;<input <%=enableNavigation ? "" : "disabled=disabled "%> size="4" type="text" name="goToId" onkeypress="return keyDown(event)" value="<%=ctnIds.size() + 1%>"/>&nbsp;/&nbsp;<%=ctnIds.size() + 1%>&nbsp;&nbsp;
            </td>
          <% } else { %>
            <td valign="top">
              &nbsp;&nbsp;<input <%=enableNavigation ? "" : "disabled=disabled "%> size="4" type="text" name="goToId" onkeypress="return keyDown(event)" value="<%=getCtnIndex(ctnIds, theEditedContainer.getID())%>"/>&nbsp;/&nbsp;<%=ctnIds.size()%>&nbsp;&nbsp;
            </td>
          <% } %>
          <td valign="top">
          <% if (!isAddContainer && enableNext) { %>
            <a href="#" onclick="navigate('next'); return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toNext.button"/>" class="navigationButton ico-nav-next<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
            <input type="hidden" name="next" value="<%=getNextCtnID(ctnIds, theEditedContainer.getID())%>"/>
          <% } else { %>
            <a href="#" onclick="return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toNext.button"/> (<internal:engineResourceBundle resourceName="org.jahia.disabled"/>)" class="navigationButton ico-nav-next<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
          <% } %>
          </td>
          <td valign="top">
          <% if (!isAddContainer && enableNext) { %>
            <a href="#" onclick="navigate('last'); return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toLast.button"/>" class="navigationButton ico-nav-last<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
            <input type="hidden" name="last" value="<%=getLastCtnID(ctnIds)%>"/>
          <% } else { %>
            <a href="#" onclick="return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toLast.button"/> (<internal:engineResourceBundle resourceName="org.jahia.disabled"/>)" class="navigationButton ico-nav-last<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
          <% } %>
          </td>
            <td valign="top">
              <a href="#" onclick="navigate('new'); return false;" title="<internal:engineResourceBundle resourceName="org.jahia.engines.addcontainer.AddContainer.label"/>" class="navigationButton ico-nav-add<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
            </td>
        </tr>
      <% } else { %>
        <tr>
          <td valign="top">
            <a href="#" onclick="return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toFirst.button"/> (<internal:engineResourceBundle resourceName="org.jahia.disabled"/>)" class="navigationButton ico-nav-first<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
          </td>
          <td valign="top">
            <a href="#" onclick="return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toPrevious.button"/> (<internal:engineResourceBundle resourceName="org.jahia.disabled"/>)" class="navigationButton ico-nav-previous<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
          </td>
          <% if (isAddContainer) { %>
            <td valign="top">
              &nbsp;&nbsp;<input <%=enableNavigation ? "" : "disabled=disabled "%> size="4" type="text" name="goToId" onkeypress="return keyDown(event)" value="<%=ctnIds.size() + 1%>"/>&nbsp;/&nbsp;<%=ctnIds.size() + 1%>&nbsp;&nbsp;
            </td>
          <% } else { %>
            <td valign="top">
              &nbsp;&nbsp;<input <%=enableNavigation ? "" : "disabled=disabled "%> size="4" type="text" name="goToId" onkeypress="return keyDown(event)" value="<%=getCtnIndex(ctnIds, theEditedContainer.getID())%>"/>&nbsp;/&nbsp;<%=ctnIds.size()%>&nbsp;&nbsp;
            </td>
          <% } %>
          <td valign="top">
            <a href="#" onclick="return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toNext.button"/> (<internal:engineResourceBundle resourceName="org.jahia.disabled"/>)" class="navigationButton ico-nav-next<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
          </td>
          <td valign="top">
            <a href="#" onclick="return false;" title="<internal:engineResourceBundle resourceName="org.jahia.navigation.toLast.button"/> (<internal:engineResourceBundle resourceName="org.jahia.disabled"/>)" class="navigationButton ico-nav-last<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
          </td>
            <td valign="top">
              <a href="#" onclick="navigate('new'); return false;" title="<internal:engineResourceBundle resourceName="org.jahia.engines.addcontainer.AddContainer.label"/>" class="navigationButton ico-nav-add<%=(results != null ? " disabled" : "")%>">&nbsp;</a>
            </td>
        </tr>
      <% } %>
      </table>
    </div>
    <!-- navigationBar (start) -->
  <% } %>
<% } %>