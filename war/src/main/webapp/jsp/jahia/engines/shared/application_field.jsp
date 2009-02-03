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
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.engines.rights.ManageRights" %>
<%@ page import="org.jahia.resourcebundle.JahiaResourceBundle" %>
<%@ page import="org.jahia.services.categories.Category" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<internal:gwtImport module="org.jahia.ajax.gwt.subengines.filepicker.FilePicker" />
<c:set var="jahia.engines.gwtModuleIncluded" value="true" scope="request"/>
<%!
    private final static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger("jsp.jahia.engines.shared.application_field"); %>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");

    final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + ".theField");
    final String definitionName = theField.getDefinition().getName();
    int appID = (Integer) engineMap.get(definitionName + "_appID");
    String selectedEntryPointDefName = (String) engineMap.get(definitionName + "_selectedEntryPointDefName");
    final Iterator appList = (Iterator) engineMap.get("appList");
    boolean appListIsEmpty = !appList.hasNext();
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
    final String theURL = jParams.settings().getJahiaEnginesHttpPath();
    final String selectUsrGrp = (String) engineMap.get("selectUsrGrp");

    final Integer userNameWidth = 15;
    request.getSession().setAttribute("userNameWidth", userNameWidth);
    boolean unAuthorizedOnCurrentlySelectedWebapp = false;
    if (engineMap.get(definitionName + "_unAuthorized") != null) {
        unAuthorizedOnCurrentlySelectedWebapp = true;
    }
    logger.debug("appID=" + appID);
    logger.debug("selectedEntryPointDefName=" + selectedEntryPointDefName);

    // Started path for entryPointInstance
    String path = (String) engineMap.get(definitionName+"_entryPointInstancePath");
    if (path == null || path.equals("<empty>")) {
        path = "/content/mashups/" ;
    }
    boolean displaySelectEngine = (Boolean) engineMap.get(definitionName+"_displaySelectInstance");
    int roleNb = 0; // Store the number of application roles
    int counter = 0;

    // categories
    String catIdValue = request.getParameter("catId");
    int selectedCategoryId = 1;
    boolean browseByCategory = false;
    if (catIdValue == null || catIdValue.equals("")) {
        catIdValue = "";
        browseByCategory = false;
    } else {
        browseByCategory = true;
        selectedCategoryId = Integer.parseInt(catIdValue);
    }
    Category selectedCategory = Category.getCategory(selectedCategoryId);
%>
<input name="catId" type="hidden" value="<%=catIdValue%>"/>
<script type="text/javascript" src="<%= theURL%>../javascript/selectbox.js"></script>
<script type="text/javascript">
    <!--//
    var formular = document.mainForm;

    var vKey = <%= ManageRights.getInstance().getVKey(userNameWidth) - 1 %>;

    var usrgrpname = new Array();
    var index = 0;

    var selectBoxName = null;

    function addOptions (text, value) {
        if (formular.elements[selectBoxName].options[0].value == "null") {
            formular.elements[selectBoxName].options[0] = null;
        }
        var i = formular.elements[selectBoxName].length;
        var pasteValue = value.substr(vKey);
        for (j = 0; j < i; j++) {
            var entity = formular.elements[selectBoxName].options[j].value;
            if (pasteValue == entity) {
                usrgrpname[index++] = entity;
                return;
            }
        }
        text = value.substr(vKey, 1) + text;
        formular.elements[selectBoxName].options[i] = new Option(text, pasteValue);
        formular.elements[selectBoxName].disabled = false;
    }

    function addOptionsBalance () {
        if (index > 0) {
            var badName = "\n";
            for (i = 0; i < index; i++) {
                badName += "- ";
                if (usrgrpname[i].substr(0, 1) == "u") {
                    badName += "<internal:engineResourceBundle resourceName="org.jahia.engines.shared.Application_Field.userName.label"/>";
                } else {
                    badName += "<internal:engineResourceBundle resourceName="org.jahia.engines.shared.Application_Field.groupName.label"/>";
                }
                badName += usrgrpname[i].substr(1, usrgrpname[i].lastIndexOf(':') - 1) + "\n";
            }
            alert("<%=JahiaTools.html2text(JahiaResourceBundle.getEngineResource("org.jahia.engines.shared.Application_Field.alertUsersGroupAlreadyMember.label",
                jParams, jParams.getLocale()))%>" + badName);
            index = 0;
        }
    }

    var myWin = 0;

    function puselectUsrGrp (url, _selectBoxName) {
        var params = "width=850,height=700,status=0,menubar=0,resizable=1,scrollbars=1";
        var name = "selectUsrGrp";

        myWin = window.open(url, name, params);
        myWin.focus();
        selectBoxName = _selectBoxName;
    }

    function puClose () {
        if (myWin != 0)
            myWin.close();
    }

    function setSelectedCategory (catId) {
        document.mainForm.catId.value = catId;
        handleActionChange('edit');
    }

    function viewAllPortlets () {
        document.mainForm.catId.value = '';
        handleActionChange('edit');
    }

    window.onunload = function() {
        puClose();
        closeTheWindow();
    };

    function selectStep (step) {
        if (step == "createInstance") {
            createInstance();
            /*var createInstance = document.getElementById("selectInstance");
            createInstance.style.display = "none";
            createInstance = document.getElementById("createInstance");
            createInstance.style.display = "block";*/
        } else if (step == "selectInstance") {
            var selectInstance = document.getElementById("createInstance");
            selectInstance.style.display = "none";
            selectInstance = document.getElementById("selectInstance");
            selectInstance.style.display = "block";
        } else {
            //un-select tab
            var active_tab = getElementsByClassName(document, "active");
            active_tab[0].className = "";

            // un-select panel
            var active_panel = getElementsByClassName(document, "activePanel");
            active_panel[0].className = "";
            active_panel[0].style.display = "none";

            // select tab
            var tab = document.getElementById('tab_' + step);
            tab.className = "active";

            //select panel
            var panel = document.getElementById('panel_' + step);
            panel.style.display = "block";
            panel.className = "activePanel";
        }
    }
    //-->
</script>

<div id="selectInstance" style="<%=displaySelectEngine?"display:block":"display:none"%>">
    <internal:fileManager rootPath="mashups" startPath="<%=path%>" enginemode="true" nodeTypes="jnt:portlet" conf="mashuppicker" mimeTypes="" filters=""/>
</div>
<script type="text/javascript">
    <!--//
    function check () {
    <% for (int i = 0; i < roleNb; i++) { %>
        selectAllOptionsSelectBox(document.mainForm.authMembers<%=i%>);
    <% } %>
        return true;
    }

    function saveContent () {
        check();
        if (workInProgressOverlay) workInProgressOverlay.launch();
    }
    //-->
</script>
