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
<%@ include file="header.inc" %>
<jsp:useBean id="selectedJahiaEdition" class="java.lang.String" scope="request"/>
<div class="head">
    <div class="object-title">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.chooseEdition.label" />
    </div>
</div>
<div id="pagebody">
    <%@ include file="error.inc" %>
    <p>
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.chooseedition.selectEditionIntro.label" />.
    </p>
    
    <br/>
    <fieldset>
        <table>
            <tr>
                <td>
                    <label for="standardEdition">
                        <img src="<%=url%>/configuration_wizard/images/editions/bigbox_standard.gif" alt="" />
                    </label>
                </td>
                <td>
                    <input name="selectedJahiaEdition" type="radio" value="standard" id="standardEdition"<% if ("standard".equals(selectedJahiaEdition)) { %>checked="true"<% } %>/>
                </td>
                <td>
                    <h4>
                        <label for="standardEdition">
                            <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.chooseedition.standardEditionTitle.label" />
                        </label>
                    </h4>
                    <br/>
                    <br/>
                    <label for="standardEdition">
                        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.chooseedition.standardEditionDescription.label" />
                        <br/>
                        <br/>
            <span class="warning">
            	<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.chooseedition.standardEditionExpiration.label" />
            </span>
                    </label>
                </td>
            </tr>
        </table>
    </fieldset>
    <br/>
    <fieldset>
        <table>
            <tr>
                <td>
                    <label for="proEdition">
                        <img src="<%=url%>/configuration_wizard/images/editions/bigbox_pro.gif" alt="" />
                    </label>
                </td>
                <td>
                    <input name="selectedJahiaEdition" type="radio" value="pro" id="proEdition"<% if ("pro".equals(selectedJahiaEdition)) { %>checked="true"<% } %>/>
                </td>
                <td>
                    <h4>
                        <label for="proEdition">
                            <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.chooseedition.proEditionTitle.label" />
                        </label>
                    </h4>
                    <br/>
                    <br/>
                    <label for="proEdition">
                        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.chooseedition.proEditionDescription.label" />
                        <br/>
                        <br/>
            <span class="warning">
              <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.chooseedition.proEditionExpiration.label" />
            </span>
                    </label>
                </td>
            </tr>
        </table>
    </fieldset>
    <br/>
</div>
<%@ include file="buttons.inc" %>
<%@ include file="footer.inc" %>