<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page import="org.jahia.bin.Jahia" %>
<%@ page import="org.jahia.data.FormDataManager" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ page import="org.jahia.registries.EnginesRegistry"%>
<%@page import="org.jahia.registries.ServicesRegistry"%>
<%@ page import="org.jahia.engines.selectpage.SelectPage_Engine"%>
<%@page import="org.jahia.data.JahiaData"%>
<%@page import="org.jahia.data.fields.JahiaField"%>
<%@page import="org.jahia.services.acl.JahiaBaseACL"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%!
private static boolean isAuthorizedForToolbar(String toolbarSetName, ParamBean jParams) {
    return ServicesRegistry.getInstance().getJahiaACLManagerService().getPermission("org.jahia.actions.sites." + jParams.getSiteID()
        + ".htmlsettings.toolbar." + toolbarSetName, jParams.getUser(), org.jahia.services.acl.JahiaBaseACL.READ_RIGHTS, jParams.getSiteID(), true) > 0;
}
%>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    if (jData == null) {
        jData = (JahiaData) engineMap.get("jData");
    }

    final JahiaField theField = (JahiaField) engineMap.get("theField");
    //ProcessingContext  jParams  = (ProcessingContext) engineMap.get( "jParams" );
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean"); // for jahia405

    String theOldField = theField.getValue();
    String theNewField = "";

    theOldField = JahiaTools.replacePatternIgnoreCase(theOldField, "<br>", "<br/>");

    final String strToRemove[] = {"<jahia", "&lt;jahia", "_htmleditor>", "&lt;html>", "&lt;/html>", "<html>", "</html>"};

    for (int i = 0; i < strToRemove.length; i++) {
        final String lowerCaseField = theOldField.toLowerCase();
        int index = lowerCaseField.indexOf(strToRemove[i]);
        if (index != -1) {
            theNewField = theOldField.substring(0, index) +
                    theOldField.substring(index + strToRemove[i].length(),
                            theOldField.length());
            theOldField = theNewField;
        }
    }

    if (theOldField == null || "".equals(theOldField)) {
        theOldField = "<html><body></body></html>";
    }
    pageContext.setAttribute("theOldField", theOldField);

    final StringBuffer jahiaPath = new StringBuffer();

    if (Jahia.getContextPath() != null) {
        jahiaPath.append(Jahia.getContextPath());
    } else {
        jahiaPath.append(request.getContextPath());
    }

    if (Jahia.getServletPath() != null) {
        jahiaPath.append(Jahia.getServletPath());
    } else {
        // should only happen when the Jahia servlet hasn't been called at
        // least once !
        jahiaPath.append(request.getServletPath());
    }

    String htmlEditorCSSUrl = (String)engineMap.get("htmlEditorCSSUrl");
    if (htmlEditorCSSUrl.length() != 0 && pageContext.getServletContext().getResource(htmlEditorCSSUrl) == null) {
        htmlEditorCSSUrl = "";
    }
    String htmlEditorCSSDef = (String)engineMap.get("htmlEditorCSSDef");
    if (htmlEditorCSSDef == null || (htmlEditorCSSDef.length() != 0 && pageContext.getServletContext().getResource(htmlEditorCSSDef) == null)) {
        htmlEditorCSSDef = "";
    }

    StringBuffer buff = new StringBuffer();
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh == null) {
        elh = new EngineLanguageHelper(jParams.getLocale());
    }
    final String currentLanguageCode = elh.getCurrentLanguageCode();
    pageContext.setAttribute("currentLanguageCode", currentLanguageCode);
    pageContext.setAttribute(ParamBean.SESSION_LOCALE_ENGINE, elh.getCurrentLocale(), PageContext.SESSION_SCOPE);

    final String fckUrl = buff.append(request.getContextPath()).append("/htmleditors/fckeditor/").toString();
    buff.delete(0, buff.length());

    final Map params = new HashMap(4);
    params.put(SelectPage_Engine.OPERATION, "selectAnyPage");
    params.put(SelectPage_Engine.PARENT_PAGE_ID, new Integer(-1));
    params.put(SelectPage_Engine.PAGE_ID, new Integer(-1));
    params.put("callback","SetUrl");
    params.put(SelectPage_Engine.LANGUAGE, currentLanguageCode);
    String selectPageURL = EnginesRegistry.getInstance().getEngineByBeanName("selectPageEngine").renderLink(jParams, params);
    
    // determine, which toolbar set to use
    String toolbarSet = "Light";
    boolean authorizedForToolbar = isAuthorizedForToolbar("Full", jParams);
    if (authorizedForToolbar) {
        toolbarSet = "Full";
    } else if (isAuthorizedForToolbar("Basic", jParams)) {
        toolbarSet = "Basic";
    }
%>
<script type="text/javascript">
<!--//
function getHtmlEditorText(jahiaTextHiddenInput)
{
  try
  {
    var oEditor = FCKeditorAPI.GetInstance('jahiaEditor') ;
    jahiaTextHiddenInput.value = oEditor.GetXHTML( true );
  }
  catch (e)
  {
    alert(e);
  }
  if ( jahiaTextHiddenInput.value.toLowerCase().indexOf("<html>",0)==-1 )
  {
    jahiaTextHiddenInput.value = "<html>" + jahiaTextHiddenInput.value + "</html>";
  }
}

function FCKeditor_OnComplete(editorInstance) {
    window.status = editorInstance.Description;
    if (typeof workInProgressOverlay != 'undefined') {
        workInProgressOverlay.stop();
    }
    jahia.config.startWorkInProgressOnLoad=false;
}
jahia.config.startWorkInProgressOnLoad=true;
//-->
</script>

<script type="text/javascript" src="<%=fckUrl%>fckeditor.js"></script>
<script type="text/javascript">
    var oFCKeditor = null;

    window.onload = function() {
        oFCKeditor = new FCKeditor('jahiaEditor', '100%', '400');
        oFCKeditor.BasePath = "<%=fckUrl%>";
        oFCKeditor.Config.basePath = "<%=fckUrl%>";
        
    <%if (htmlEditorCSSUrl.length() == 0) {%>
        oFCKeditor.Config.EditorAreaCSS = '<%=fckUrl%>editor/css/fck_editorarea.css';        
    <% } else if (htmlEditorCSSUrl.startsWith(request.getContextPath())) { %>
        oFCKeditor.Config.EditorAreaCSS = '<%=htmlEditorCSSUrl%>';
    <% } else { %>
        oFCKeditor.Config.EditorAreaCSS = '${pageContext.request.contextPath}<%=htmlEditorCSSUrl%>';
    <% } 
    if (htmlEditorCSSDef.length() == 0) {%>
        oFCKeditor.Config.StylesXmlPath = '<%=fckUrl%>fckstyles.xml';
    <%} else if (htmlEditorCSSDef.startsWith(request.getContextPath())) {%>
        oFCKeditor.Config.StylesXmlPath = '<%=htmlEditorCSSDef%>';
    <%} else {%>
        oFCKeditor.Config.StylesXmlPath = '${pageContext.request.contextPath}<%=htmlEditorCSSDef%>';
    <%}%>

		oFCKeditor.Config.ImageBrowserURL = "<c:url value='/engines/webdav/filePicker.jsp?callback=SetUrl&callbackType=url&lang=${currentLanguageCode}'><c:param name='filters' value='*.bmp,*.gif,*.jpe,*.jpeg,*.jpg,*.png,*.tif,*.tiff'/></c:url>";

		oFCKeditor.Config.FileBrowserURL = "<c:url value='/engines/webdav/filePicker.jsp?callback=SetUrl&callbackType=url&lang=${currentLanguageCode}'/>";
		
		oFCKeditor.Config.FlashBrowserURL = "<c:url value='/engines/webdav/filePicker.jsp?callback=SetUrl&callbackType=url&filters=*.swf&lang=${currentLanguageCode}'/>";
		
        oFCKeditor.Config.LinkBrowserURL = "<%=selectPageURL%>";

        oFCKeditor.Config["DefaultLanguage"] = "<%=currentLanguageCode%>";
        oFCKeditor.Config["CustomConfigurationsPath"] = "<c:url value='/htmleditors/fckeditor/fckconfig_jahia.js'/>"
        oFCKeditor.ToolbarSet = '<%=toolbarSet%>';
        oFCKeditor.ReplaceTextarea();
    }
</script>

<input type="hidden" name="_<%=theField.getID()%>"
       value="<c:out value='${theOldField}'/>" />

<span class="htmleditor">
  <textarea id="jahiaEditor" name="jahiaEditor" rows="20" cols="75">
  <c:out value="${theOldField}"/>
  </textarea>
</span>