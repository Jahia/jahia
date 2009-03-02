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
<%@ page import="java.util.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="javax.swing.JTree" %>
<%@ page import="javax.swing.tree.*" %>

<%@ page import="org.jahia.content.*" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.services.version.*" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="org.jahia.views.engines.versioning.revisionsdetail.actions.*" %>
<%@ page import="org.jahia.views.engines.*" %>
<%@ page import="org.jahia.views.engines.datepicker.*" %>
<%@ page import="org.jahia.views.engines.datepicker.actions.*" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="org.jahia.utils.GUITreeTools" %>

<%@include file="/views/engines/common/taglibs.jsp" %>
<%
  JahiaEngineCommonData engineCommonData =
    (JahiaEngineCommonData)request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
  ProcessingContext jParams = engineCommonData.getParamBean();
  String actionURL = (String)request.getAttribute(RevisionEntrySetDetailAction.ACTION_URL_REQUEST_ATTRIBUTE);
  String engineView = (String)request.getAttribute("engineView");

    JTree tree =
      (JTree)request.getAttribute(RevisionEntrySetDetailAction.REVISIONS_TREE);
    RevisionEntrySet revisionEntrySet =
      (RevisionEntrySet)request.getAttribute(RevisionEntrySetDetailAction.REVISIONENTRYSET);
    List flatRevisionsList =
      (List)request.getAttribute("flatRevisionsList");

  java.util.Date d = new java.util.Date(revisionEntrySet.getVersionID()*1000L);
  String dateStr = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG,java.text.DateFormat.MEDIUM,jParams.getLocale()).format(d);


  //----------------------------------------------------------------------------------------------------------
  // Diff Proof of concept !
  //
  RevisionEntry selRevEntry = (RevisionEntry)request.getAttribute(RevisionEntrySetDetailAction.REVISIONENTRY);

  String oldValue = (String)request.getAttribute("oldValue");
  if ( oldValue == null ){
    oldValue = "";
  }

  String newValue = (String)request.getAttribute("newValue");
  if ( newValue == null ){
    newValue = "";
  }

  String mergedValue = (String)request.getAttribute("mergedValue");
  if ( mergedValue == null ){
    mergedValue = "";
  }

  String oldValueStatus = "org.jahia.engines.version.valueStatus.";
  if ( selRevEntry != null && selRevEntry.getWorkflowState()>0 ){
    oldValueStatus += "2";
  } else {
    oldValueStatus += "1";
  }

  Boolean isStagingValue = (Boolean)request.getAttribute("isStagingValue");
  String newValueStatus = "org.jahia.engines.version.valueStatus.";
  if ( isStagingValue.booleanValue() ){
    newValueStatus += "3";
  } else {
    newValueStatus += "2";
  }

%>

<script language="javascript">
<!--
document.onkeydown = keyDown;
function keyDown() {
    if (document.all) {
        var ieKey = event.keyCode;
        if (ieKey == 13) { document.retryForm.submit(); }
        if (ieKey == 87 && event.ctrlKey) { window.close(); }
    }
}

function sendFormSave()
{
  window.close();
}

function sendFormCancel()
{
  window.close();
}

function sendForm(method,params)
{
    workInProgressOverlay.launch();
    document.mainForm.method.value = "POST";
    document.mainForm.action = "<%=actionURL%>&engineview=<%=engineView%>&method=" + method;
    if ( params.charAt(0) == "&" ){
      document.mainForm.action += params;
    } else {
      document.mainForm.action += "&" + params;
  }
    document.mainForm.submit();
}

//-->
</script>
<div class="dex-TabPanelBottom-full">
  <div class="tabContent">
    <div id="content" class="full">
      <div class="head">
        <div class="object-title"><fmt:message key="org.jahia.engines.version.revisionDetail" /></div>
      </div>
      <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
        <tr>
          <th width="100"><fmt:message key="org.jahia.engines.version.dateOfRevision" />&nbsp;:&nbsp;</th>
          <td align="left" valign="top">
            <%=dateStr%>
          </td>
        </tr>
      </table>
      <table border="0" cellpadding="0" cellspacing="0" width="100%">
        <tr>
          <td colspan="2" align="right" valign="top" nowrap>
            <br />
            <fmt:message key="org.jahia.engines.version.legend" />&nbsp;:&nbsp;<span style='color:aqua'><fmt:message key="org.jahia.engines.version.added" /></span>&nbsp;|&nbsp;<span style='color:lime'><fmt:message key="org.jahia.engines.version.changed" /></span>&nbsp;|&nbsp;<span style='color:red;text-decoration:line-through;'><fmt:message key="org.jahia.engines.version.deleted" /></span>&nbsp;&nbsp;&nbsp;<br />
            <br />
          </td>
        </tr>
        <tr>
          <td align="left" width="50%" valign="top">
            <div class="headtop">
              <div class="object-title"><fmt:message key="org.jahia.engines.version.revisionTree" /></div>
            </div>
            <table class="text" border="0" cellspacing="0" cellpadding="0">
              <%
              for (int i = 0; i < flatRevisionsList.size(); i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)flatRevisionsList.get(i);
                %>
                <tr class="sitemap1">
                  <td>&nbsp;&nbsp;</td>
                  <td class="text" nowrap>
                    <%

                    List verticalLineCells = GUITreeTools.getLevelsWithVerticalLine(node);
                    int nodeLevel = node.getLevel();
                    for (int level = 0; level<nodeLevel; level++) {
                      DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)node.getParent();
                      boolean isLastChild = ( parentNode!=null && node.equals(parentNode.getLastChild()) );

                      if ( level<nodeLevel-1 ){
                        if (verticalLineCells.contains(new Integer(level+1))) {%>
                          <internal:displayIcon src="vLineIcon" align="absmiddle"/>
                        <%} else {%>
                          <internal:displayIcon src="org.jahia.pix.image" width="14" height="0" align="absmiddle"/>
                        <%}
                      } else {
                        if ( !isLastChild ){%>
                          <internal:displayIcon src="lineNodeIcon" align="absmiddle"/>
                        <%} else if (isLastChild || node.isLeaf()) {%>
                          <internal:displayIcon src="lastNodeIcon" align="absmiddle"/>
                        <%} else {%>
                          <internal:displayIcon src="org.jahia.pix.image" width="14" height="0" align="absmiddle"/>
                        <%}
                      }
                    }

                    if ( !node.isLeaf() && tree.isExpanded(new TreePath(node.getPath()))) {
                        %><a href="javascript:sendForm('revisionsDetail','&guitree=collapse&nodeindex=<%=i%>')" onclick="javascript:flagSubmit();"
                        ><internal:displayIcon src="minusNodeIcon" alt="Collapse" align="absmiddle" /></a><%
                    } else if (!node.isLeaf()) {
                        %><a href="javascript:sendForm('revisionsDetail','&guitree=expandall&nodeindex=<%=i%>')" onclick="javascript:flagSubmit();"
                          ><internal:displayIcon src="expandAllNodeIcon" alt="Expand all" align="absmiddle" /></a
                        ><a href="javascript:sendForm('revisionsDetail','&guitree=expand&nodeindex=<%=i%>')" onclick="javascript:flagSubmit();"
                            ><internal:displayIcon src="plusNodeIcon" alt="Expand" align="absmiddle" /></a><%
                    } else {
                        %><internal:displayIcon src="org.jahia.pix.image" width="14" height="0" align="absmiddle" /><%
                    }

                    Object nodeInfo = node.getUserObject();
                    if ( nodeInfo instanceof ContentObject ){
                      ContentObject contentObject = (ContentObject)nodeInfo;
                      String objectID = "[" + contentObject.getObjectKey().getIDInType() + "]";
                      ContentObjectEntryState entryState = new ContentObjectEntryState(ContentObjectEntryState.WORKFLOW_STATE_ACTIVE, 0,LanguageCodeConverters.localeToLanguageTag(jParams.getLocale()));
                      String objectTitle = ContentDefinition.getObjectTitle(contentObject,entryState);
                      if ( objectTitle == null ){
                        objectTitle = contentObject.getObjectKey().getType() + objectID;
                      } else {
                        objectTitle += objectID;
                      }
                    %>
                    <b><%=objectTitle%></b><br>
                  <% } else {
                    RevisionEntry revEntry = (RevisionEntry)nodeInfo;
                    String languageCode = revEntry.getLanguageCode();
                  %>
                  <input type="radio" class="input" name="revisionEntry" value="<%=revEntry.toString()%>" onclick="javascript:flagSubmit();sendForm('revisionsDetail','')"
                  <% if ( selRevEntry != null && selRevEntry.equals(revEntry) ){ %>checked<% } %> ><fmt:message key='<%= "org.jahia.services.version.entryState." + revEntry.getWorkflowState()%>'
                  /><% if ( !ContentObject.SHARED_LANGUAGE.equals(languageCode) ){ %>&nbsp;<internal:displayLanguageFlag code="<%=languageCode%>" /><% } %><br>
                <% } %>
                  </td>
                  <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
                </tr><%
              } %>
            </table>
          </td>
          <td class="split" valign="top" align="left">
            <div class="headtop">
              <div class="object-title"><fmt:message key="<%=oldValueStatus%>" />&nbsp;:&nbsp;</div>
            </div>
            <div class="padded">
              <br><%=oldValue%><br /><br />
            </div>
            <% if ( selRevEntry != null && selRevEntry.getWorkflowState()>0 && !isStagingValue.booleanValue() ){ %>
              <div class="head">
                <div class="object-title"><fmt:message key="org.jahia.engines.version.valueStatus.3" />&nbsp;:&nbsp;</div>
              </div>
              <div class="padded">
                <br>No Staging<br /><br />
              </div>
            <% } else { %>
              <div class="head">
                <div class="object-title"><fmt:message key="<%=newValueStatus%>" />&nbsp;:&nbsp;</div>
              </div>
              <div class="padded">
                <br><%=newValue%><br /><br />
              </div>
            <% } %>
            <div class="head">
              <div class="object-title"><fmt:message key="org.jahia.engines.version.mergedDifference" />&nbsp;:&nbsp;</div>
            </div>
            <div class="padded">
              <br><%=mergedValue%><br /><br />
            </div>
          </td>
        </tr>
      </table>
    </div>
  </div>
</div>

