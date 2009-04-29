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