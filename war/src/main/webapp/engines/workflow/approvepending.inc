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
<!-- Begin approvepending.inc -->
<script type="text/javascript">

// Verify if the box checked does not influe the others box, especially the
// mandatory or not mandatory languages dualism.
function excludeCheck(state, language, pageid) {
    var invState = (state == "accept" ? "refuse" : "accept");
    var invElement = "checkbox|" + language + "|" + pageid + "|" + invState;
    if (document.mainForm.elements[invElement] && document.mainForm.elements[invElement].checked) {
        document.mainForm.elements[invElement].checked = false;
    }
}

// Set or reset the check boxes on a given language.
function checkAllBoxes(state, language) {
    for (var i = 0; i < document.mainForm.length; i++) {
        var checkBoxName = document.mainForm.elements[i].name;
        if (!document.mainForm.elements[checkBoxName].disabled &&
             checkBoxName.indexOf(language + '|') != -1 && checkBoxName.indexOf(state) != -1) {
            document.mainForm.elements[checkBoxName].checked =
                !document.mainForm.elements[checkBoxName].checked;
            var checkBoxNameArray = checkBoxName.split('|');
            excludeCheck(state, language, checkBoxNameArray[2]); // pageid
        }
    }
}

</script>

<% if (!jContentPage.checkAdminAccess(user)) { %>
<table class="text" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        </td>
        <td><br><br>
            <b><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.theUser.label"/>
&nbsp;<i>'<%=user.getUsername()%>'</i>&nbsp;<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.noSuffisantRights.label"/></b>
        </td>
    </tr>
</table>
<% } else { %>
<table class="text" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        </td>
        <td>
            <internal:displayIcon src="workflowEditState" />&nbsp;<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.stateEditing.label"/>&nbsp;&nbsp;&nbsp;
        </td>
        <td>
            <internal:displayIcon src="workflowWaitingAnimState" />&nbsp;<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.stateApproval.label"/>&nbsp;&nbsp;&nbsp;
        </td>
        <td>
            <internal:displayIcon src="workflowActiveState" />&nbsp;<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.statePublished.label"/>&nbsp;&nbsp;&nbsp;
        </td>
    </tr>
</table>
<table class="text" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        </td>
        <td>
            <internal:displayIcon src="workflowAccept" />&nbsp;<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.acceptAndPublishPages.label"/>&nbsp;&nbsp;&nbsp;
        </td>
        <td>
            <internal:displayIcon src="workflowRefuse" />&nbsp;<fmt:message key="org.jahia.engines.workflow.WorkflowEngine.refusePages.label"/>
        </td>
    </tr>
</table>
<table class="text" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        </td>
        <td>
            <fmt:message key="org.jahia.engines.workflow.WorkflowEngine.mandatoryLanguage.label"/>
        </td>
    </tr>
</table>
<br>
<table class="text" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <%
        Set languageCodes = new HashSet();
        languageEnum = languageSettings.iterator();
        while (languageEnum.hasNext()) {
            SiteLanguageSettings siteLanguageSettings = (SiteLanguageSettings)languageEnum.next();
            String languageCode = siteLanguageSettings.getCode();
            languageCodes.add(languageCode);
            Locale loc = LanguageCodeConverters.languageCodeToLocale(languageCode);
            String alt = loc.getDisplayLanguage(loc) + " [" + loc.getDisplayName() + "]";
        %>
        <td align="center" colspan="3">
            &nbsp;&nbsp;<internal:displayLanguageFlag code="<%=loc.getLanguage()%>"
                                                   alt="<%=alt%>"
            /><%if (siteLanguageSettings.isMandatory()) {%>*<%} else {%>&nbsp;<%}%>&nbsp;
        </td>
        <td>&nbsp;&nbsp;</td>
        <% } %>
        <td>&nbsp;&nbsp;</td>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td><b><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.publishPages.label"/></b></td>
        <td>&nbsp;</td>
        <%
        languageEnum = languageSettings.iterator();
        while (languageEnum.hasNext()) {
            String languageCode = ((SiteLanguageSettings)languageEnum.next()).getCode();
        %>
        <td align="center">
            <a href="javascript:checkAllBoxes('accept', '<%=languageCode%>');">
                <internal:displayIcon src="workflowAccept" alt="Invert all pages acceptance"/></a>
        </td>
        <td>&nbsp;&nbsp;</td>
        <td align="center">
            <a href="javascript:checkAllBoxes('refuse', '<%=languageCode%>');">
                <internal:displayIcon src="workflowRefuse" alt="Invert all pages refusal"/></a>
        </td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <% } %>
        <td width="40%" valign="bottom">
            <b><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.comments.label"/></b>&nbsp;:
        </td>
    </tr>
    <% String[] resourceBundleState =
        {"", "workflowActiveState", "workflowEditState", "workflowWaitingAnimState"};
    TreeSiteMapViewHelper treeJahiaSiteMap = (TreeSiteMapViewHelper)engineMap.get("treeJahiaSiteMap");
    String screen = displayParam + tabOptions[activeTabOption.intValue()];
    List shouldDrawLineSegment = new ArrayList();
    for (int level = 0; level < treeJahiaSiteMap.getMaxLevel(); level++) {
        shouldDrawLineSegment.add(level, new Boolean(true));
    }
    int classNb = 0;
    for (int i = 0; i < treeJahiaSiteMap.size(); i++) {
        ContentPage contentPage = treeJahiaSiteMap.getContentPage(i);
        if (contentPage == null) continue;
        JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().
            getJahiaVersionService().getSiteSaveVersion(jParams.getSiteID());
        ActivationTestResults activationTestResults = contentPage.isValidForActivation(
            languageCodes, jParams, new StateModificationContext(new ContentPageKey(contentPage.getID()), languageCodes, false));
        boolean isAcceptCheckBoxEnabled = true;
        boolean isRefuseCheckBoxEnabled = true;
        %>
        <tr class='<%=(classNb ^= 5) == 0 ? "text" : "text5"%>'>
            <td style="{background-color: transparent}">&nbsp;&nbsp;&nbsp;&nbsp;</td>
            <td>
                &nbsp;&nbsp;<a name="<%=i%>"></a><%
                // Draw node line icons ----------------------------------------
                for (int level = 0; level < treeJahiaSiteMap.getPageLevel(i); level++) {
                    if (treeJahiaSiteMap.getPageLevel(i) > level + 1) {
                        if (((Boolean)shouldDrawLineSegment.get(level)).booleanValue()) {
                            %><internal:displayIcon src="vLineIcon" align="absmiddle"/><%
                        } else {
                            %><internal:displayIcon src="org.jahia.pix.image" width="14" height="0" align="absmiddle"/><%
                        }
                    } else {
                        if (treeJahiaSiteMap.isExpanded(i) || !treeJahiaSiteMap.hasChild(i)) {
                            if (treeJahiaSiteMap.isLastSister(i)) {
                                shouldDrawLineSegment.set(level, new Boolean(false));
                                %><internal:displayIcon src="lastNodeIcon" align="absmiddle"/><%
                            } else {
                                shouldDrawLineSegment.set(level, new Boolean(true));
                                %><internal:displayIcon src="lineNodeIcon" align="absmiddle"/><%
                            }
                        }
                    }
                }
                // -------------------------------------------------------------
                // Draw expand/collapse icons ----------------------------------
                if (treeJahiaSiteMap.isExpanded(i)) {
                    %><a href="javascript:handleActionChange('<%=screen%>&sitemap=collapse|<%=i%>#<%=i%>')"
                        ><internal:displayIcon src="minusNodeIcon" alt="Collapse" align="absmiddle" /></a><%
                } else if (treeJahiaSiteMap.hasChild(i)) {
                    %><a href="javascript:handleActionChange('<%=screen%>&sitemap=expandall|<%=i%>#<%=i%>')"
                        ><internal:displayIcon src="expandAllNodeIcon" alt="Expand all" align="absmiddle" /></a
                    ><a href="javascript:handleActionChange('<%=screen%>&sitemap=expand|<%=i%>#<%=i%>')"
                        ><internal:displayIcon src="plusNodeIcon" alt="Expand" align="absmiddle" /></a><%
                } else {
                    %><internal:displayIcon src="org.jahia.pix.image" width="14" height="0" align="absmiddle" /><%
                }
                // -------------------------------------------------------------
                // Draw info icon ----------------------------------------------
                if (!treeJahiaSiteMap.shouldShowInformation(i)) {
                    %><a href="javascript:handleActionChange('<%=screen%>&sitemap=showinfo|<%=i%>#<%=i%>')"><%
                        if ((activationTestResults.getErrors().size() == 0) && (activationTestResults.getWarnings().size() == 0)) {
                            %><internal:displayIcon src="infoIcon" alt="Display last page information" align="absmiddle"/><% } else { %><internal:displayIcon src="infoIconAnim" alt="Display last page information" align="absmiddle"/><%
                        } %></a><%
                } else {
                    %><a href="javascript:handleActionChange('<%=screen%>&sitemap=hideinfo|<%=i%>#<%=i%>')"
                        ><internal:displayIcon src="infoIconActive" alt="Hide last page information" align="absmiddle"/></a><%
                }
                // -------------------------------------------------------------
                // Write page title --------------------------------------------
                String pageTitle = treeJahiaSiteMap.getPageTitle(i, jParams.getUILocale().toString());
                boolean isPageMarkedForDeletion = contentPage.isMarkedForDelete();

                String style = "white-space: nowrap;";
                if (isPageMarkedForDeletion) {
                    style += "text-decoration: line-through;";
                }
                if (contentPage.getID() == jParams.getPageID()) {
                    style += " font-weight: bold;";
                } %>&nbsp;<span style="<%=style%>"
                    ><%=GuiBean.glueTitle(pageTitle,25) %></span><%
                // end write page title ----------------------------------------
                // Display lock if any page object is edited -------------------
                // #ifdef LOCK
                LockKey lockKey = LockKey.composeLockKey(LockKey.WORKFLOW_TYPE, contentPage.getID(), contentPage.getID());
                LockPrerequisites lockPrerequisites = LockPrerequisites.getInstance();
                LockPrerequisitesResult lockPrerequisitesResult = lockPrerequisites.getLockPrerequisitesResult(lockKey);
                boolean isLocked = lockPrerequisitesResult != null && lockPrerequisitesResult.getResultsList().size() > 0;
                if (isLocked) {
                    %>&nbsp;&nbsp;<a href="<%=jData.gui().html().drawLockEngineLauncher(lockKey)%>"
                        ><internal:displayIcon src="locked" align="absmiddle" alt="A page child object is already edited"/></a><%
                }
                // #endif
                %>
            </td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
            <%
            languageEnum = languageSettings.iterator();
            int mandLang = 0;
            Map languagesStates = contentPage.getLanguagesStates(true);
            Map pageOnlyLanguagesStates = contentPage.getLanguagesStates(false);

            while (languageEnum.hasNext()) {
                SiteLanguageSettings siteLanguageSettings = (SiteLanguageSettings)languageEnum.next();
                String languageCode = siteLanguageSettings.getCode();
                Integer languageState = (Integer)languagesStates.get(languageCode);
                Integer sharedLanguageState = (Integer)languagesStates.get(ContentObject.SHARED_LANGUAGE);
                Integer pageLanguageState = (Integer)pageOnlyLanguagesStates.get(languageCode);

                if (languageState != null && languageState.intValue() != -1) {

                    if ( sharedLanguageState != null && languageState.intValue() < sharedLanguageState.intValue() ){
                        languageState = sharedLanguageState;
                    }
                    if ( languageState.intValue() > EntryLoadRequest.STAGING_WORKFLOW_STATE ){
                        if ( pageLanguageState == null || pageLanguageState.intValue() < languageState.intValue() ){
                            // the page cannot be in waiting for approval if it has no entry ( jahia page info ) at waiting for approval state.
                            languageState = new Integer(EntryLoadRequest.STAGING_WORKFLOW_STATE);
                        }
                    }

                    boolean pointNEstBesoinDeContinuer = false;
                    boolean isWaitingState = languageState.intValue() == EntryLoadRequest.WAITING_WORKFLOW_STATE;
                    boolean isActiveState = languageState.intValue() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE;
                    if (contentPage.hasSameParentID() == treeJahiaSiteMap.getParentPageID(i)) { %>
                        <td colspan="<%=languageSettings.size() * 4 %>"><i><fmt:message key="org.jahia.engines.workflow.SelectPage_Engine.pageMoved.label"/></i></td>
                        <% break;
                    } else if ( contentPage.getTitles(true).containsKey(languageCode) ){
                    %><td align="center">
                        <input name="checkbox<% if (isActiveState) {%>Off<%}%>|<%=languageCode%>|<%=contentPage.getID()%>|accept"
                               type="checkbox"
                               onclick="javascript:excludeCheck('accept', '<%=languageCode%>', '<%=contentPage.getID()%>')"<%
                            if (!isWaitingState && !contentPage.checkWriteAccess(user) || isActiveState || isLocked) {
                                isAcceptCheckBoxEnabled = false;
                                %> disabled <%
                                if (isActiveState) {
                                    %>checked<%
                                }
                                pointNEstBesoinDeContinuer = true;
                            }
                            if (!pointNEstBesoinDeContinuer &&
                                activationTestResults.getErrors().size() != 0) {
                                // Disable the check box having errors.
                                for (int j = 0; j < activationTestResults.getErrors().size(); j++) {
                                    Object testResultObject = (Object)activationTestResults.getErrors().get(j);
                                    if (testResultObject instanceof IsValidForActivationResults) {
                                        IsValidForActivationResults ivfar = (IsValidForActivationResults)testResultObject;
                                        if (languageCode.equals(ivfar.getLanguageCode())) {
                                            isAcceptCheckBoxEnabled = false;
                                            %>disabled<%
                                            break;
                                        }
                                    }
                                }
                            }
                    %>></td>
                    <td align="center">
                        <internal:displayIcon src="<%=resourceBundleState[languageState.intValue()]%>" />
                    </td>
                    <td align="center">
                    <% if (isWaitingState && contentPage.getTitles(true).containsKey(languageCode) ) {
                        %><input name="checkbox|<%=languageCode%>|<%=contentPage.getID()%>|refuse"
                                 type="checkbox"
                                 onclick="javascript:excludeCheck('refuse', '<%=languageCode%>', '<%=contentPage.getID()%>')"><%
                            if (isLocked) {
                                isRefuseCheckBoxEnabled = false;
                                %>disabled<%
                            }
                        } else {
                            isRefuseCheckBoxEnabled = false;
                            %>&nbsp;<%
                        } %>
                    </td>
                    <td>&nbsp;&nbsp;</td>
                <%  } else { %>
                    <td>&nbsp;</td><td align="center"><internal:displayIcon src="<%=resourceBundleState[languageState.intValue()]%>" /></td><td align="center">&nbsp;</td><td>&nbsp;&nbsp;</td>
                <% } %>
            <%  } else { %>
                <td align="center" colspan="4">&nbsp;</td>
            <%  }
            } %>
            <td>
                <%
                if (isAcceptCheckBoxEnabled || isRefuseCheckBoxEnabled) { %>
                    <input name="input|<%=contentPage.getID()%>" type="input" size="40" maxlength="250" class="input" tabindex="<%=i + 1%>"><%
                } %>&nbsp;
            </td>
        </tr>
        <% if (treeJahiaSiteMap.shouldShowInformation(i)) { %>
            <%@ include file="informations.inc" %>
        <% } %>
    <% } %>
</table>
<% } %>
<!-- End approvepending.inc -->