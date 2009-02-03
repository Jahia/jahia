<%--
Copyright 2002-2008 Jahia Ltd

Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
Version 1.0 (the "License"), or (at your option) any later version; you may 
not use this file except in compliance with the License. You should have 
received a copy of the License along with this program; if not, you may obtain 
a copy of the License at 

 http://www.jahia.org/license/

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ include file="header.inc" %>

<jsp:useBean id="input" class="java.lang.String" scope="request"/>

<%
    final Iterator scripts = (Iterator) request.getAttribute("scripts");
%>

<div class="head">
  <div class="object-title">
    <fmt:message key="org.jahia.advancedSettings.label"/>
  </div>
</div>
<div id="pagebody">
   <%@ include file="error.inc" %>
    	<br/>
    <table summary="<fmt:message key="org.jahia.databaseSettings.label" />">
    	<br/>
        <caption><fmt:message key="org.jahia.databaseSettings.label"/>:&nbsp;</caption>
        <tr>
            <th id="t1"><span>Parameter</span></th>
            <th id="t2"><span>Value</span></th>
        </tr>
        <tr>
            <td headers="t1" class="t5">
                <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.advsettings.database.label"/>
            </td>
            <td headers="t2" class="t6">
                <select class="choix" name="script" onChange="changeDatabase()">
                    <% while (scripts.hasNext()) {
                        final Map s = (Map) scripts.next(); %>
                    <option value='<%=s.get("jahia.database.script")%>'
                            <% if (s.get("jahia.database.script").equals((values.get("database_script")))) {%>
                            selected
                            <%}%> >
                        <%=s.get("jahia.database.name")%></option>
                    <% } %>
                </select>
            </td>
        </tr>
        <tr>
            <td headers="t1" class="t5">
                <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.advsettings.databaseDriver.label"/>
            </td>
            <td headers="t2" class="t6">
                <input class="inputtype" type="text" name="driver" value='<%=values.get("database_driver")%>'
                       size="<%=input%>" maxlength="250" />
            </td>
        </tr>
        <tr>
            <td headers="t1" class="t5"><fmt:message key="org.jahia.databaseURL.label"/></td>
            <td headers="t2" class="t6">
                <input class="inputtype" type="text" name="dburl" value='<%=values.get("database_url")%>'
                       size="<%=input%>" maxlength="250" />
            </td>
        </tr>
        <tr>
            <td headers="t1" class="t5">
                <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.advsettings.databaseUsername.label"/>
            </td>
            <td headers="t2" class="t6">
                <input class="inputtype" type="text" name="user" value='<%=values.get("database_user")%>'
                       size="<%=input%>" maxlength="250" />
            </td>
        </tr>
        <tr>
            <td headers="t1" class="t5">
                <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.advsettings.databasePassword.label"/>
            </td>
            <td headers="t2" class="t6">
                <input class="inputtype" type="password" name="pwd" value='<%=values.get("database_pwd")%>'
                       size="<%=input%>" maxlength="250" onfocus="this.select();"/>
            </td>
        </tr>
        <tr>
            <td headers="t5" class="t5">
                <fmt:message
                        key="org.jahia.bin.JahiaConfigurationWizard.advsettings.useExistingDb.label"/>
            </td>
        <td headers="t6" class="t6">
                <input type="checkbox" name="useExistingDb" id="useExistingDb" value="true"
                <% Object useExistingDb  = values.get("useExistingDb");
                    if (useExistingDb != null && useExistingDb.equals("true")) { %>
                       checked
                <% } %>
                />&nbsp;<label for="useExistingDb"><fmt:message key="org.jahia.configurationWizard.checkForTrue.label" /></label>
            </td>
            
        </tr>
        

    </table>
    <input type="hidden" name="starthsqlserver" value='<%=values.get("db_starthsqlserver")%>' />
    <input type="hidden" name="datasource" value='<%=values.get("datasource.name")%>' />
    <input type="hidden" name="hibernate_dialect" value='<%=values.get("hibernate_dialect")%>' />
    <input type="hidden" name="utf8Encoding" value='<%=values.get("utf8Encoding")%>' />
    <table summary="<fmt:message key="org.jahia.storeInDB.label"/>">
        <caption>
            <fmt:message key="org.jahia.storeInDB.label"/>:
        </caption>
        <tr>
            <th id="t5"><span>Parameter</span></th>
            <th id="t6"><span>Value</span></th>
        </tr>
        <tr>
            <td headers="t5" class="t5">
                <fmt:message
                        key="org.jahia.bin.JahiaConfigurationWizard.advsettings.acceptFilesInDB.label"/>
            </td>
            <td headers="t6" class="t6">
                <input type="checkbox" name="storeFilesInDB" id="storeFilesInDB" value="true"
                <% Object o = values.get("storeFilesInDB");
                    if (o != null && o.equals("true")) { %>
                       checked
                <% } %>
                />&nbsp;<label for="storeFilesInDB"><fmt:message key="org.jahia.configurationWizard.checkForTrue.label" /></label>
            </td>
        </tr>
        <tr>
            <td headers="t5" class="t5">
                <fmt:message
                        key="org.jahia.bin.JahiaConfigurationWizard.advsettings.acceptBigTextInDB.label"/>
            </td>
        <td headers="t6" class="t6">
                <input type="checkbox" name="storeBigTextInDB" id="storeBigTextInDB" value="true"
                <% Object storeBigTextInDB  = values.get("storeBigTextInDB");
                    if (storeBigTextInDB != null && storeBigTextInDB.equals("true")) { %>
                       checked
                <% } %>
                />&nbsp;<label for="storeBigTextInDB"><fmt:message key="org.jahia.configurationWizard.checkForTrue.label" /></label>
            </td>
            
        </tr>

    </table>   
</div>
<%@ include file="buttons.inc" %>
<%@ include file="footer.inc" %>

