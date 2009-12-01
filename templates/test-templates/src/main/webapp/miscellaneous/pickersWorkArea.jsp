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
<%@ include file="../common/declarations.jspf"%>

<div class="expectedResultTitle">
  <fmt:message key="label.expected.result"/>:
</div> 
<div class="expectedResult">
  <fmt:message key="description.template.pickers.expectedResult"/>
</div>

<h3>UI Components: file, page, date pickers</h3>
    <fieldset>
        <legend>Page selector</legend>
        <p>Page ID: <input type="text" name="pageId" id="pageId" value=""/><ui:pageSelector fieldId="pageId" displayIncludeChildren="false"/></p>
        <p>Page URL: <input type="text" name="pageUrl" id="pageUrl" value=""/><ui:pageSelector fieldId="pageUrl" useUrl="true" displayIncludeChildren="false"/></p>
        <p>Page ID with subpages: <input type="text" name="pageIdWithSubpages" id="pageIdWithSubpages" value=""/><ui:pageSelector fieldId="pageIdWithSubpages"/></p>
        <p>Page URL with subpages: <input type="text" name="pageUrlWithSubpages" id="pageUrlWithSubpages" value=""/><ui:pageSelector fieldId="pageUrlWithSubpages" useUrl="true"/></p>
        <p>Page URL with custom callback: <input type="text" name="pageUrlWithCallback" id="pageUrlWithCallback" value=""/><ui:pageSelector fieldId="pageUrlWithCallback" displayIncludeChildren="false" useUrl="true" onSelect="function (pid, url, title) { alert('Selected page [' + title + '] with ID ' + pid + ' and URL: ' + url); return true; }"/></p>
    </fieldset>
    <fieldset>
        <legend>Folder selector</legend>
        <p>Folder path: <input type="text" name="filePath" id="filePath" value=""/><ui:folderSelector fieldId="filePath" displayIncludeChildren="false"/></p>
        <p>Folder path with subfolders: <input type="text" name="filePathWithSubfolders" id="filePathWithSubfolders" value=""/><ui:folderSelector fieldId="filePathWithSubfolders"/></p>
        <p>Folder path with custom callback: <input type="text" name="filePathWithCallback" id="filePathWithCallback" value=""/><ui:folderSelector fieldId="filePathWithCallback" displayIncludeChildren="false" onSelect="function (path) { alert('Selected folder: ' + path); return true; }"/></p>
        <p>Folder path with custom root folder: <input type="text" name="filePathWithRoot" id="filePathWithRoot" value=""/><ui:folderSelector fieldId="filePathWithRoot" displayIncludeChildren="false" rootPath="/"/></p>
    </fieldset>
    <fieldset>
        <legend>File selector</legend>
        <p>File path: <input type="text" name="file" id="file" value=""/><ui:fileSelector fieldId="file"/></p>
        <p>File URL: <input type="text" name="fileUrl" id="fileUrl" value=""/><ui:fileSelector fieldId="fileUrl" useUrl="true" /></p>
        <p>File with custom callback: <input type="text" name="fileWithCallback" id="fileWithCallback" value=""/><ui:fileSelector fieldId="fileWithCallback" onSelect="function (path) { alert('Selected file: ' + path); return true; }"/></p>
        <p>File with custom root folder: <input type="text" name="fileWithRoot" id="fileWithRoot" value=""/><ui:fileSelector fieldId="fileWithRoot" rootPath="/"/></p>
        <p>File with wildcard filter for images: <input type="text" name="fileWithFilter" id="fileWithFilter" value=""/><ui:fileSelector fieldId="fileWithFilter" filters="*.bmp,*.gif,*.jpe,*.jpeg,*.jpg,*.png,*.tif,*.tiff"/></p>
        <p>File with MIME type for Word documents: <input type="text" name="fileWithMimiType" id="fileWithMimiType" value=""/><ui:fileSelector fieldId="fileWithMimiType" mimeTypes="application/msword,application/vnd.openxmlformats-officedocument"/></p>
    </fieldset>
    <fieldset>
        <legend>Category picker</legend>
        <p>Category: <input type="text" name="category" id="category" value=""/><ui:categorySelector fieldId="category" displayIncludeChildren="false" /></p>
        <p>Category with subcategories: <input type="text" name="categoryWithSubcategories" id="categoryWithSubcategories" value=""/><ui:categorySelector fieldId="categoryWithSubcategories"/></p>
        <p>Category with custom root: <input type="text" name="categoryWithRoot" id="categoryWithRoot" value=""/><ui:categorySelector fieldId="categoryWithRoot" root="Services" displayIncludeChildren="false" /></p>
        <p>Single category selection: ??? not supported yet</p>
    </fieldset>
    <fieldset>
        <legend>Date picker</legend>
        <p>Date picker (default): <ui:dateSelector/></p>
        <p>Date picker with dd-MM-yyyy format: <ui:dateSelector datePattern="dd-MM-yyyy"/></p>
        <p>Date picker with initial value: <ui:dateSelector datePattern="dd-MM-yyyy" value="09-05-1979"/></p>
    </fieldset>
    <fieldset>
        <legend>Time picker</legend>
        <p>Time picker: <ui:dateSelector datePattern="hh:mm" displayTime="true"/></p>
        <p>Time picker with initial value: <ui:dateSelector datePattern="hh:mm" value="12:45"/></p>
    </fieldset>
    <fieldset>
        <legend>Date and time picker</legend>
        <p>Date and time picker (default): <ui:dateSelector displayTime="true"/></p>
        <p>Date and time picker with dd-MM-yyyy/hh.mm format: <ui:dateSelector datePattern="dd-MM-yyyy/hh.mm" displayTime="true"/></p>
        <p>Date and time picker with initial value: <ui:dateSelector datePattern="dd-MM-yyyy/hh.mm" displayTime="true" value="09-05-1979/07.30"/></p>
    </fieldset>
    <fieldset>
        <legend>User/group picker</legend>
        <p>User (single selection): <input type="text" name="userGroup1" id="userGroup1" value=""/><ui:userGroupSelector mode="users" fieldId="userGroup1"/></p>
        <p>Group (single selection): <input type="text" name="userGroup2" id="userGroup2" value=""/><ui:userGroupSelector mode="groups" fieldId="userGroup2"/></p>
        <p>User or group (single selection): <input type="text" name="userGroup3" id="userGroup3" value=""/><ui:userGroupSelector mode="both" fieldId="userGroup3"/></p>
        <p>Users (multiple selection): <select name="userGroup4" id="userGroup4" multiple="multiple" size="3" style="width: 200px"></select><ui:userGroupSelector mode="users" fieldId="userGroup4" multiple="true"/></p>
        <p>Groups (multiple selection): <select name="userGroup5" id="userGroup5" multiple="multiple" size="3" style="width: 200px"></select><ui:userGroupSelector mode="groups" fieldId="userGroup5" multiple="true"/></p>
        <p>Users and groups (multiple selection): <select name="userGroup6" id="userGroup6" multiple="multiple" size="3" style="width: 200px"></select><ui:userGroupSelector mode="both" fieldId="userGroup6" multiple="true"/></p>
<script type="text/javascript">
function userGroupCallback(principalType, principalKey, principalName) {
    alert((principalType == 'u' ? 'User' : 'Group') + " '" + principalName + "' is selected. Key: '" + principalKey + "'.");
    return true;
} 
</script>        
        <p>Users and groups (multiple selection) with custom callback: <select name="userGroup7" id="userGroup7" multiple="multiple" size="3" style="width: 200px"></select><ui:userGroupSelector mode="both" fieldId="userGroup7" onSelect="userGroupCallback" multiple="true"/></p>
    </fieldset>