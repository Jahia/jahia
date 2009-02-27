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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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