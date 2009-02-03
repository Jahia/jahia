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

<%@taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="http://struts.apache.org/tags-nested" prefix="nested"%>
<%@taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>

<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<div class="head">
    <div class="object-title">
      <bean:message key="wizard.title"/>
    </div>
</div>
<table width="100%" cellpadding="8" cellspacing="0" border="0">
  <!-- Head wizard-->
  <logic:present name="descriptionClipperForm">
    <tr>      
      <!-- Quit -->
      <td align="right">
        <div class="buttonList">
          <div class="button">
            <html:link action="/manageAction?do=clipbuilder&webClippingAction=init">
              <bean:message key="wizard.quit"/>
            </html:link>
          </div>
        </div>
      </td>
    </tr>
    </logic:present>

    <!-- Wizar action-->
    <tr valign="middle">
      <td>
        <table style="text-align: center" cellpadding="10" cellspacing="0" border="0">
          <!-- Show only the step that has been performed -->
          <tr>
            <!-- Manage -->
            <!-- description -->
            <logic:present name="descriptionClipperForm">
              <td class="wizardStep">
                <html:form action="/beginExtractWizard?do=clipbuilder" method="post">
                  <html:image styleClass="wizard" pageKey="image.description" altKey="image.description.alt" />
                  <br/>
                  <bean:message key="image.description.alt"/>
                  <html:hidden property="webClippingAction" value="wizard"/>
                </html:form>
              </td>
            </logic:present>
            <!-- Browse -->
            <logic:present name="browseForm">
              <td>
                <html:img page="/jsp/clipbuilder/html/images/arrow_right_green.gif" />
              </td>
              <td class="wizardStep">
                <html:form action="/browse?do=clipbuilder" method="post">
                  <html:image styleClass="wizard" pageKey="image.browse" altKey="image.browse.alt" />
                  <br/>
                  <bean:message key="image.browse.alt"/>
                  <html:hidden property="webClippingAction" value="wizard"/>
                </html:form>
              </td>
            </logic:present>
            <!-- Select -->
            <logic:present name="selectPartForm">
              <td>
                <html:img page="/jsp/clipbuilder/html/images/arrow_right_green.gif" />
              </td>
              <td class="wizardStep">
                <html:form action="/selectPart?do=clipbuilder" method="post">
                  <html:image styleClass="wizard" pageKey="image.selectPart" altKey="image.selectPart.alt" />
                  <br/>
                  <bean:message key="image.selectPart.alt"/>
                  <html:hidden property="webClippingAction" value="wizard"/>
                </html:form>
              </td>
            </logic:present>
            <!-- Edit Param-->
            <logic:present name="editParamForm">
              <td>
                <html:img page="/jsp/clipbuilder/html/images/arrow_right_green.gif" />
              </td>
              <td class="wizardStep">
                <html:form action="/edit?do=clipbuilder" method="post">
                  <html:image styleClass="wizard" pageKey="image.editParams" altKey="image.editParams.alt" />
                  <br/>
                  <bean:message key="image.editParams.alt"/>
                  <html:hidden property="webClippingAction" value="wizard"/>
                </html:form>
              </td>
            </logic:present>
            <!-- Preview -->
            <logic:present name="previewForm">
              <td>
                <html:img page="/jsp/clipbuilder/html/images/arrow_right_green.gif" />
              </td>
              <td class="wizardStep">
                <html:form action="/saveClipper?do=clipbuilder" method="post">
                  <html:image styleClass="wizard" pageKey="image.preview" altKey="image.preview.alt" />
                  <br/>
                  <bean:message key="image.preview.alt"/>
                  <html:hidden property="webClippingAction" value="wizard"/>
                </html:form>
              </td>
            </logic:present>
          </tr>
        </table>
      </td>
    </tr>

</table>
<br/>
