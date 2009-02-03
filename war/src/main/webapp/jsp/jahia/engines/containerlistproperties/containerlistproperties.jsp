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

<%@ page language="java" %>
<%@ page import="org.jahia.data.containers.JahiaContainerList" %>
<%@ page import="org.jahia.data.fields.JahiaFieldDefinition" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.resourcebundle.JahiaResourceBundle" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>

<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>

<%
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final JahiaContainerList theContainerList = (JahiaContainerList) engineMap.get("theContainerList");
final String clistName;
if (theContainerList != null) {
    clistName = theContainerList.getDefinition().getName();
} else {
    clistName = "";
}

final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
final String fieldForm = (String) engineMap.get(fieldsEditCallingEngineName + ".fieldForm");
final String logForm = (String) engineMap.get("logForm");
final String engineUrl = (String) engineMap.get("engineUrl");
final String theScreen = (String) engineMap.get("screen");
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
final List containers = (List) engineMap.get("containers");
final List fields = (List) engineMap.get("fields");
String autSort = (String) engineMap.get("automatic");
if (autSort == null) {
    autSort = "";
}
final String cursorField = (String) engineMap.get("cursorField");

final Integer containerSelectedStr = (Integer) engineMap.get("containerSelected");
final int containerSelected;
if (containerSelectedStr != null) {
    containerSelected = containerSelectedStr.intValue();
} else {
    containerSelected = 0;
}

final List fieldInfoToDisplay = (List) engineMap.get("fieldInfoToDisplay");

final int pageDefID = jParams.getPage().getPageTemplateID();

final String aclFieldName = (String) engineMap.get("aclfieldname");
final boolean showEditMenu = (theScreen.equals("edit") || theScreen.equals("metadata") ||
        theScreen.equals("rightsMgmt") || theScreen.equals("timeBasedPublishing") ||
        theScreen.equals("ctneditview_rights"));
request.setAttribute("showEditMenu", new Boolean(showEditMenu));
final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

Integer nbTotalOfContainers = (Integer)engineMap.get("nbTotalOfContainers");
Integer maxContainersToDisplay = (Integer)engineMap.get("maxContainersToDisplay");
Boolean useOptimizedMode = (Boolean)engineMap.get("automatic_sort_useOptimizedMode");
%>
<script type="text/javascript">
<!--

  function handleActionChanges(action, params) {

    // alert("action: " + action + ", params: " + params);

    var actionUrl = "<%=engineUrl%>";
    <% if ("".equals(aclFieldName)) { %>
      document.mainForm.lastaclfield.value = "";
    <% } %>
    if (handleActionChanges.arguments.length > 1) {
        actionUrl += handleActionChanges.arguments[1];
    }
    document.mainForm.screen.value = action;
    document.mainForm.action = actionUrl;
    check();
    teleportCaptainFlam(document.mainForm);
  }

  <%if ( containers != null && !containers.isEmpty() ){%>
  function check() {
    if (document.mainForm.lastscreen.value == "edit") {
      for (var i = 0; i < document.mainForm.manRank.length; i++) {
        document.mainForm.manRank.options[i].selected = true;
      }
    }
    return true;
  }
  <%}else{%>
  function check() {
    return true;
  }
  <%}%>

  function sendForm() {
    check();
    document.mainForm.screen.value = "save";
    teleportCaptainFlam(document.mainForm);
  }

  document.onkeydown = keyDown;
  function keyDown() {
    if (document.all) {
      var ieKey = event.keyCode;
      if (ieKey == 13 && event.ctrlKey) {
        sendForm();
      }
      if (ieKey == 87 && event.ctrlKey) {
        window.close();
      }
    }
  }

  function changeCursorField() {
    document.mainForm.screen.value = "edit";
    document.mainForm.updMode.value = "cursor";
    teleportCaptainFlam(document.mainForm);
  }

  function changeMaxContainersToDisplay() {
    document.mainForm.screen.value = "edit";
    document.mainForm.updMode.value = "maxContainersToDisplay";
    teleportCaptainFlam(document.mainForm);
  }

  function changeUseOptimizedMode() {
    document.mainForm.screen.value = "edit";
    document.mainForm.updMode.value = "useOptimizedMode";
    teleportCaptainFlam(document.mainForm);
  }

  function automaticRanking() {
    document.mainForm.screen.value = "edit";
    document.mainForm.updMode.value = "automatic";
    teleportCaptainFlam(document.mainForm);
  }

  function manualRanking(move) {
    document.mainForm.screen.value = "edit";
    document.mainForm.updMode.value = "manual";
    document.mainForm.move.value = move;
    teleportCaptainFlam(document.mainForm);
  }

  function moveUp() {
    moveItem(document.mainForm.manRank, "up");
  }

  function moveDown() {
    moveItem(document.mainForm.manRank, "down");
  }

  function moveTop() {
    moveItem(document.mainForm.manRank, "top");
  }

  function moveBottom() {
    moveItem(document.mainForm.manRank, "bottom");
  }

  function moveItem(element, move) {
    document.mainForm.autRank.selectedIndex = 0;
    index = element.selectedIndex;
    if (index != -1 && element.options[index].value > "") {
      aText = element.options[index].text;
      aValue = element.options[index].value;
      if (element.options[index].value > "" && index > 0 && move == "up") {
        element.options[index].text = element.options[index - 1].text;
        element.options[index].value = element.options[index - 1].value;
        element.options[index - 1].text = aText;
        element.options[index - 1].value = aValue;
        element.selectedIndex--;
      } else if (index < element.length - 1 && element.options[index + 1].value > "" && move == "down") {
        element.options[index].text = element.options[index + 1].text;
        element.options[index].value = element.options[index + 1].value;
        element.options[index + 1].text = aText;
        element.options[index + 1].value = aValue;
        element.selectedIndex++;
      } else if (index > 0 && move == "top") {
        element.options[element.length] = new Option(aText, aValue, false, false);
        for (var i = element.length - 2; i >= 0; i--) {
            element.options[i + 1].text = element.options[i].text;
            element.options[i + 1].value = element.options[i].value;
            element.options[i + 1].selected = false;
        }
        element.options[0] = new Option(aText, aValue, false, true);
        element.options[index + 1] = null;
      } else if (index < element.length && move == "bottom") {
        element.options[element.length] = new Option(aText, aValue, false, true);
        element.options[index] = null;
      }
    } else {
      alert("<%=JahiaTools.html2text(JahiaResourceBundle.getEngineResource("org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.alertSelectValue.label", jParams, jParams.getLocale()))%>");
    }
  }
//-->
</script>
<div id="header">
  <h1>Jahia</h1>
  <h2 class="list"><internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.containerListSettings.label"/></h2>
</div>
<div id="mainContent">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left" width="50%">
          <div class="dex-TabBar">
            <jsp:include page="../menuBar.jsp" flush="true" />
          </div>
        </td>
       <td class="dex-langItem-wrapper-empty" style="vertical-align: top;" align="right" nowrap="nowrap" width="50%">
          <jsp:include page="../multilanguage_links.jsp" flush="true" />
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%" colspan="2">

          <input type="hidden" name="lastaclfield" value=""/>
          <input type="hidden" name="updMode" value=""/>
          <input type="hidden" name="move" value=""/>
          <%if (theScreen.equals("edit")) {
            if (!clistName.equals("PortletList") && containers != null && !containers.isEmpty() && fieldInfoToDisplay != null) { %>
              <div class="dex-TabPanelBottom">
                <div class="tabContent">
                  <%@ include file="../menu.inc" %>
                  <div id="content" class="fit w2">
                    <div class="head">
                       <div class="object-title"><internal:engineResourceBundle  resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.containerListTitle.label"/>: <%=theContainerList.getDefinition().getTitle(elh.getCurrentLocale())%></div>
                    </div>
                    <%if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.EDIT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) {%>
                      <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
                        <tr>
                          <th width="200">
                            <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.cursorField.label"/>
                          </th>
                          <td>
                            <select name="cursorField" onchange="changeCursorField()">
                              <%
                              for (Iterator it = fields.iterator(); it.hasNext();) {
                                JahiaFieldDefinition jcd = ((JahiaFieldDefinition) it.next());
                                String defName = "";
                                if (jcd.getIsMetadata()) defName = "metadata_" + jcd.getName();
                                else defName = jcd.getName();%>
                                <option value="<%=defName%>" <% if(cursorField.equals(defName)){%> selected="selected" <% } %>>
                                  <%=jcd.getTitle(elh.getCurrentLocale())%>
                                </option>
                              <% } %>
                            </select>
                          </td>
                        </tr>
                        <tr>
                          <th width="200">
                            <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.automaticRanking.label"/>
                            (<internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.sortBy.label"/>)
                          </th>
                          <td>
                            <% if (autSort.equals("alphAsc")) {%>
                              <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.alphabeticalAscending.label"/>
                            <% } else if (autSort.equals("alphDesc")) { %>
                              <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.alphabeticalDescending.label"/>
                            <% } %>
                          </td>
                        </tr>
                        <tr>
                          <th width="200">
                            <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.moveAContainerUpOrDown.label"/>
                            (<internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.manualRanking.label"/>)
                          </th>
                          <td>
                            <div class="selectContainer">
                              <select name="manRank" size="10" multiple="multiple" disabled="disabled">
                                <%
                                for (int i = 0; i < containers.size(); i++) {
                                  String value = (String) fieldInfoToDisplay.get(i);
                                  try {
                                    if (value.startsWith("isDate;")) {
                                      String[] strings = value.split(";");
                                      Long aLong;
                                      if (strings.length == 2) {
                                        aLong = new Long(strings[1]);
                                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                        calendar.setTime(new Date(aLong.longValue()));
                                        Locale locale = null;
                                        try {
                                          locale = elh.getCurrentLocale();
                                        } catch (Throwable t) {
                                        }

                                        DateFormat sdf = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.SHORT, locale);
                                        value = sdf.format(calendar.getTime());
                                      } else {
                                          value = JahiaResourceBundle.getEngineResource("org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.notPublished.label", jParams, elh.getCurrentLocale());
                                      }
                                    }
                                  } catch (NumberFormatException e) {
                                  }%>
                                  <option value="<%=i%>" title="<%=org.jahia.utils.JahiaTools.replacePattern(value,"$$$", " - ")%>"<% if (i == containerSelected) {%> selected="selected" <%}%>>
                                    <%=org.jahia.utils.JahiaTools.replacePattern(value,"$$$", " - ")%>
                                  </option>
                                <%}%>
                              </select>
                            </div>
                          </td>
                        </tr>
                      </table>
                    <% } else { %>
                      <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
                        <% if (nbTotalOfContainers.intValue()>maxContainersToDisplay.intValue()){%>
                          <tr>
                            <th width="200">
                              <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.maxContainersToDisplay.label" defaultValue="Nb. of Containers to display"/>
                            </th>
                            <td>
                              <select name="maxContainersToDisplay" onchange="changeMaxContainersToDisplay()">
                                <option value="100" <% if(maxContainersToDisplay.intValue()==100){%> selected="selected" <% } %>>100
                                <option value="200" <% if(maxContainersToDisplay.intValue()==200){%> selected="selected" <% } %>>200
                                <option value="300" <% if(maxContainersToDisplay.intValue()==300){%> selected="selected" <% } %>>300
                                <option value="500" <% if(maxContainersToDisplay.intValue()==500){%> selected="selected" <% } %>>500
                                <option value="1000" <% if(maxContainersToDisplay.intValue()==1000){%> selected="selected" <% } %>>1000
                              </select>
                              <br/>
                              <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.only" defaultValue="only"/>&nbsp;<%=maxContainersToDisplay%>&nbsp;
                              <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.outOf" defaultValue="out of"/>&nbsp;<%=nbTotalOfContainers%>&nbsp;
                              <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.areDisplayed" defaultValue="are&nbsp; displayed"/>
                            </td>
                          </tr>
                        <% } %>
                        <tr>
                          <th width="200">
                            <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.cursorField.label"/>
                          </th>
                          <td>
                            <select name="cursorField" onchange="changeCursorField()">
                              <%
                              for (Iterator it = fields.iterator(); it.hasNext();) {
                                JahiaFieldDefinition jcd = ((JahiaFieldDefinition) it.next());
                                String defName = "";
                                if (jcd.getIsMetadata()) defName = "metadata_" + jcd.getName();
                                else defName = jcd.getName(); %>
                                <option value="<%=defName%>"<% if(cursorField.equals(defName)){%> selected="selected" <% } %>>
                                  <%=jcd.getTitle(elh.getCurrentLocale())%>
                                </option>
                              <% } %>
                            </select>
                          </td>
                        </tr>
                        <tr>
                          <th>
                            <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.automaticRanking.label"/>
                            (<internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.sortBy.label"/>)
                          </th>
                          <td>
                            <select name="autRank" onchange="automaticRanking()">
                              <option value="none">
                                <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.chooseAnAutomaticSort.label"/>
                              </option>
                              <option value="alphAsc" <% if (autSort.equals("alphAsc")) {%> selected="selected"<%}%>>
                                <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.alphabeticalAscending.label"/>
                              </option>
                              <option value="alphDesc" <% if (autSort.equals("alphDesc")) {%> selected="selected"<%}%>>
                                <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.alphabeticalDescending.label"/>
                              </option>
                            </select>
                          </td>
                        </tr>
                        <tr>
                          <th valign="top">
                            <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.moveAContainerUpOrDown.label"/>
                            (<internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.manualRanking.label"/>)
                          </th>
                          <td>
                            <table cellpadding="0" cellspacing="5" border="0">
                              <tr>
                                <td>
                                  <div class="selectContainer">
                                    <select name="manRank" size="10" multiple="multiple">
                                      <%
                                      for (int i = 0; i < containers.size(); i++) {
                                        String value = (String) fieldInfoToDisplay.get(i);
                                        try {
                                          if (value.startsWith("isDate;")) {
                                            String[] strings = value.split(";");
                                            Long aLong;
                                            if (strings.length == 2) {
                                              aLong = new Long(strings[1]);
                                              Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                              calendar.setTime(new Date(aLong.longValue()));
                                              Locale locale = null;
                                              try {
                                                locale = elh.getCurrentLocale();
                                              } catch (Throwable t) {
                                              }

                                              DateFormat sdf = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.SHORT, locale);
                                              value = sdf.format(calendar.getTime());
                                            } else {
                                              value = JahiaResourceBundle.getEngineResource("org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.notPublished.label", jParams, elh.getCurrentLocale());
                                            }
                                          }
                                        } catch (NumberFormatException e) {
                                        }%>
                                        <option value="<%=i%>" title="<%=org.jahia.utils.JahiaTools.replacePattern(value,"$$$", " - ")%>" <% if (i == containerSelected) {%> selected="selected" <%}%>>
                                          <%=org.jahia.utils.JahiaTools.replacePattern(value,"$$$", " - ")%>
                                        </option>
                                      <% } %>
                                    </select>
                                  </div>
                                </td>
                                <td width="100%" class="text" align="left" valign="top">
                                  <a href="javascript:moveTop()" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Top','','${pageContext.request.contextPath}<internal:engineResourceBundle resourceName="org.jahia.moveTopOn.button"/>',1)">
                                    <img name="Top" src="${pageContext.request.contextPath}<internal:engineResourceBundle resourceName="org.jahia.moveTopOff.button"/>"  width="16" height="16" border="0" alt="<internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.moveOnTop.label"/>"></a>
                                  <br>
                                  <a href="javascript:moveUp()" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Up','','${pageContext.request.contextPath}<internal:engineResourceBundle resourceName="org.jahia.moveUpOn.button"/>',1)">
                                    <img name="Up" src="${pageContext.request.contextPath}<internal:engineResourceBundle resourceName="org.jahia.moveUpOff.button"/>" width="16" height="16" border="0" alt="<internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.moveUp.label"/>"></a>
                                  <br>
                                  <a href="javascript:moveDown()" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Down','','${pageContext.request.contextPath}<internal:engineResourceBundle resourceName="org.jahia.moveDownOn.button"/>',1)">
                                    <img name="Down" src="${pageContext.request.contextPath}<internal:engineResourceBundle resourceName="org.jahia.moveDownOff.button"/>" width="16" height="16" border="0" alt="<internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.moveDown.label"/>"></a>
                                  <br>
                                  <a href="javascript:moveBottom()" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Bottom','','${pageContext.request.contextPath}<internal:engineResourceBundle resourceName="org.jahia.moveBottomOn.button"/>',1)">
                                    <img name="Bottom" src="${pageContext.request.contextPath}<internal:engineResourceBundle resourceName="org.jahia.moveBottomOff.button"/>" width="16" height="16" border="0" alt="<internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.moveAtBottom.label"/>"></a>
                                  <br>
                                </td>
                              </tr>
                            </table>
                          </td>
                        <tr>
                        <tr>
                          <th valign="top">
                            <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.useOptimizedSort.label" defaultValue="Use DB optimized sort"/>
                          </th>
                          <td>
                            <input type="checkbox" name="useOptimizedMode" onchange="changeUseOptimizedMode()" <%if(useOptimizedMode.booleanValue()){%>checked<%}%> value="true">
                            <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.useOptimizedSortExplanation.label" defaultValue="Uncheck only if you need language \"natural\" sorting on that field, to work properly with special characters like '�','�', OR if the MIXIN LANGUAGES is enabled (in Administration's language setting section) and not all translations are present. But performance can be bad."/>
                          </td>
                        </tr>
                      </table>
                    <%}%>
                  </div>
                </div>
              </div>
            <%}else{%>
              <%if ((containers == null) || (containers.isEmpty())) {%>
                <div class="dex-TabPanelBottom">
                  <div class="tabContent">
                    <%@ include file="../menu.inc" %>
                    <div id="content" class="fit w2">
                      <div class="head">
                         <div class="object-title"><internal:engineResourceBundle  resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.containerListTitle.label"/>: <%=theContainerList.getDefinition().getTitle(elh.getCurrentLocale())%></div>
                      </div>
                      <p class="error">
                        <internal:engineResourceBundle resourceName="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.emptyContainerListWarning.label"/>
                      </p>
                    </div>
                  <div style="clear:both"></div></div>
                <div style="clear:both"></div></div>
              <%}
              }%>
          <% } else if (theScreen.equals("metadata")) { %>
            <jsp:include page="../containeredit/containeredit.jsp" flush="true"/>
          <% } else if (theScreen.equals("logs")) {
            if (logForm != null) {%>
              <%=logForm%>
            <%} else {%>
              <div class="dex-TabPanelBottom">
                <div class="tabContent">
                   <%@ include file="../tools.inc" %>
                  <div id="content" class="fit w2">
                    <div id="editor">
                      <p class="error"><internal:engineResourceBundle resourceName="org.jahia.engines.noLogs.label"/></p>
                    </div>
                  </div>
                <div style="clear:both"></div></div>
              <div style="clear:both"></div></div>
            <%}
          } else if (! theScreen.equals("notools") && ! theScreen.equals("edit")) {%>
            <%=fieldForm%>
          <%}%>
        <div style="clear:both"></div></td>
      </tr>
    </tbody>
  </table>
  <jsp:include page="../buttons.jsp" flush="true" />
</div>