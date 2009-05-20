<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<%@ page import="org.jahia.content.*" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.data.fields.*" %>
<%@ page import="org.jahia.data.FormDataManager" %>
<%@ page import="org.jahia.resourcebundle.*" %>
<%@ page import="org.jahia.services.fields.*" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.services.version.*" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="org.jahia.views.engines.versioning.revisionsdetail.actions.*" %>
<%@ page import="org.jahia.views.engines.*" %>
<%@ page import="org.jahia.views.engines.datepicker.*" %>
<%@ page import="org.jahia.views.engines.datepicker.actions.*" %>
<%@ page import="org.jahia.views.engines.*" %>
<%@ page import="org.jahia.views.engines.versioning.*" %>
<%@ page import="org.jahia.utils.*" %>
<%@ page import="org.jahia.utils.textdiff.*" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>

<%@include file="/views/engines/common/taglibs.jsp" %>
<internal:gwtImport module="org.jahia.ajax.gwt.module.versioning.VersionComparison"/>
<%
  JahiaEngineCommonData engineCommonData =
    (JahiaEngineCommonData)request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
  ProcessingContext jParams = engineCommonData.getParamBean();
    final int pageDefID = jParams.getPage().getPageTemplateID();

  String engineView = (String)request.getAttribute("engineView");

  ContentVersioningViewHelper versViewHelper = (ContentVersioningViewHelper)
    request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

    ContainerCompareBean containerCompareBean = versViewHelper.getContainerCompareBean();

  String actionURL = (String)request.getAttribute("ContentVersioning.ActionURL");
  actionURL += "&engineview=" + engineView + "&method=containerVersionCompare";
  actionURL += "&version1=" + containerCompareBean.getOldRevision().toString() + "&version2=" + containerCompareBean.getNewRevision().toString();

    // Old engine system
    final Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
    String theScreen = (String) engineMap.get("screen");

    final EngineLanguageHelper elh = (EngineLanguageHelper)
        engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

    final boolean debug = request.getQueryString() != null && request.getQueryString().indexOf("debug=") > -1;


%>
<%@ include file="common-javascript.inc" %>
<%
  /*
  StringBuffer datas = new StringBuffer("{\"fieldDiffs\":[");
  Iterator iterator = containerCompareBean.getFields().iterator();
  JahiaFieldVersionCompare fvc = null;
  ContentField contentField = null;
  JahiaFieldDefinition def = null;
  while ( iterator.hasNext() ){
    fvc = (JahiaFieldVersionCompare)iterator.next();
    fvc.getCompareHandler().highLightDiff();
    contentField = ContentField.getField(fvc.getFieldId());
    def = (JahiaFieldDefinition)JahiaFieldDefinition.getChildInstance(String.valueOf(contentField.getFieldDefID()));
    datas.append("{\"fieldId\":").append("\"").append(fvc.getFieldId()).append("\",");
    datas.append("\"title\":").append("\"").append(def.getTitle(pageDefID, LanguageCodeConverters.languageCodeToLocale(fvc.getLanguageCode()))).append("\",");
    datas.append("\"type\":").append("\"").append(contentField.getType()).append("\",");
    datas.append("\"icon\":").append("\"").append(FieldTypes.getIconClassName(contentField.getType(), false)).append("\",");
    datas.append("\"displayMode\":").append("\"").append(fvc.getDisplayMode()).append("\",");
    datas.append("\"mergedDiffValue\":").append("'").append(StringEscapeUtils.escapeJavaScript(fvc.getCompareHandler().getMergedDiffText())).append("',");
    datas.append("\"oldValue\":").append("'").append(StringEscapeUtils.escapeJavaScript(fvc.getCompareHandler().getOldText())).append("',");
    datas.append("\"newValue\":").append("'").append(StringEscapeUtils.escapeJavaScript(fvc.getCompareHandler().getNewText())).append("'}");
    if ( iterator.hasNext() ){
      datas.append(",");
    }
  }
  datas.append("]}");
  */

  String datas = containerCompareBean.toJSON(jParams);

  String addedLegend = HunkTextDiffVisitor.getAddedText(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.added", jParams.getLocale(),"added"));
  String deletedLegend = HunkTextDiffVisitor.getDeletedText(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.deleted", jParams.getLocale(),"deleted"));
  String updatedLegend = HunkTextDiffVisitor.getChangedText(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.changed", jParams.getLocale(),"changed"));

%>
<script language="JavaScript">
<!--
    jahia.config.skipBodyResize = 'false';

    function sendFormCancel(){
      window.close();
    }
    /*
    function windowOnload(){
      scroll(0, 0);
      try {
          DBG = new AjxDebug(AjxDebug.NONE, null, false);
          <% if (debug){%>
          if (location.search && (location.search.indexOf("debug=") != -1)) {
              var m = location.search.match(/debug=(\\d+)/);
              if (m.length) {
                  var num = parseInt(m[1]);
                  var level = AjxDebug.DBG[num];
                  if (level) {
                      DBG.setDebugLevel(level);
                  }
    }
          }
          <% } %>
      } catch (ex) {
          return;
    }
    }

    function hideUserShell(){
      try {
        Dwt.setVisibility(document.getElementById("wrapper"),false);
      } catch (ex){
        return;
      }
    }

    function showUserShell(){
      try {
        Dwt.setVisibility(document.getElementById("wrapper"),true);
      } catch (ex){
        return;
      }
    }

    //window.onload = windowOnload;


    function launchContainerCompare(){
      //initZimbraShell("MainShell", true, null, null, true, true, "#336699");
      var str = <%=datas%>;
      //str.fieldDiffs
      var containerCompareController = new ContainerCompareController("fieldDiffsContainer");
      containerCompareController.addTab('<fmt:message key="org.jahia.engines.version.fieldsTab"/>',str.fieldDiffs);
      //It's an hack for IE ----------------------------------------------------
      containerCompareController.addTab('<fmt:message key="org.jahia.engines.version.metadatasTab"/>',str.metadataDiffs);
      //containerCompareController._tabView._tabBar._tbuttons[2].setVisible(false);
      // -----------------------------------------------------------------------
      containerCompareController.displayGUI();
      str = null;
      containerCompareController = null;
    }
    //AjxCore.addOnloadListener(launchContainerCompare);
    */
//-->
</script>
<div class="dex-TabPanelBottom-full">
  <div class="tabContent">
    <div id="content" class="full">
      <%if ( containerCompareBean.isCompareMode() ){%>
        <div id="versioncomparison" style="clear:both;" versionableUUID="<%=versViewHelper.getContentObject().getObjectKey()%>" version1="<%=containerCompareBean.getOldRevision().toString()%>" version2="<%=containerCompareBean.getNewRevision().toString()%>" lang="<%=elh.getCurrentLanguageCode()%>">&nbsp;</div>
      <% } else { RevisionEntrySet revisionEntrySet = containerCompareBean.getActiveRevision();%>
        <div id="versioncomparison" versionableUUID="<%=versViewHelper.getContentObject().getObjectKey()%>" version1="<%=containerCompareBean.getOldRevision().toString()%>" version2="<%=containerCompareBean.getNewRevision().toString()%>" lang="<%=elh.getCurrentLanguageCode()%>">&nbsp;</div>
      <% } %>
    </div>
  </div>
</div>