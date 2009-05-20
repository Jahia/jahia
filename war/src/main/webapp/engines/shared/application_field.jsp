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
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.engines.rights.ManageRights" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<internal:gwtImport module="org.jahia.ajax.gwt.module.filepicker.FilePicker" />
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
%>
<utility:setBundle basename="JahiaInternalResources"/>
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
                    badName += "<fmt:message key="org.jahia.engines.shared.Application_Field.userName.label"/>";
                } else {
                    badName += "<fmt:message key="org.jahia.engines.shared.Application_Field.groupName.label"/>";
                }
                badName += usrgrpname[i].substr(1, usrgrpname[i].lastIndexOf(':') - 1) + "\n";
            }
            alert("<%=JahiaTools.html2text(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field.alertUsersGroupAlreadyMember.label",jParams.getLocale()))%>" + badName);
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
        if (typeof workInProgressOverlay != 'undefined') workInProgressOverlay.launch();
    }
    //-->
</script>
