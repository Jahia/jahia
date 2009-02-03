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
<%@page import="org.jahia.clipbuilder.html.*"%>
<%@page import="org.jahia.clipbuilder.html.bean.*"%>
<%@page import="java.util.*"%>
<!-- javaScript-->
<script language="javascript">
  // set the value of webClippinAction parameter
  function setAction(f, value) {
    if (value == "changeView") {
      f.action = '<%=response.encodeURL(request.getContextPath()+"/administration/edit?do=clipbuilder&webClippingAction=changeView")%>';
    }

    return;
  }

  //set action attribute of the given form
  function setActionAndSubmit(f, value) {
    setAction(f, value);
    f.submit();

    return;
  }
</script><!-- Main -->
  <html:form action="edit?do=clipbuilder" method="post">
    <!-- Header -->
    <table align="center" class="principal" width="100%">
      <!-- Title -->
      <tr>
        <td>
          <bean:message key="edit.title"/>
        </td>
        <% /* Help
        <td style="text-align:right" width="10%">
          <a href="javascript:popitup('=response.encodeURL(request.getContextPath()+"/jsp/clipbuilder/html/help/editHelp.jsp")')">
            <bean:message key="wizard.help"/>
          </a>
        </td>
        */ %>
      </tr>
    </table>
    <!-- Buttons -->
    <table align="center" class="principal" width="100%">
      <tr>
        <td class="topmenubuttons">
          <logic:messagesNotPresent>
            <html:submit styleClass="fancyButton" property="webClippingAction">
              <bean:message key="edit.validate"/>
            </html:submit>
            <html:submit styleClass="fancyButton" property="webClippingAction">
              <bean:message key="edit.reset"/>
            </html:submit>
            <html:submit styleClass="fancyButton" property="webClippingAction">
              <bean:message key="edit.resetAll"/>
            </html:submit>
          </logic:messagesNotPresent>
          <html:submit styleClass="fancyButton" property="webClippingAction">
            <bean:message key="edit.backToSelect"/>
          </html:submit>
        </td>
      </tr>
    </table>
    <!-- warning/information-->
    <logic:messagesPresent>
      <table align="center" bgcolor="#800000" cellpadding="0" cellspacing="1" width="100%">
        <tbody>
          <tr>
            <td>
              <table align="center" bgcolor="#ffffff" border="0" width="100%">
                <tbody>
                  <tr>
                    <td align="center" bgcolor="#dcdcdc">
                      <font color="#cc0000">
                        <bean:message key="error.warning"/>
                      </font>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <UL>
                        <html:messages id="error">
                          <LI> <bean:write name="error"/> </LI>
                        </html:messages>
                      </UL>
                    </td>
                  </tr>
                </tbody>
              </table>
            </td>
          </tr>
        </tbody>
      </table>
    </logic:messagesPresent>
    <logic:messagesNotPresent>
      <!-- Edit Information -->
      <table>
        <tr valign="middle">
          <td>
            <bean:message key="edit.selectUrl"/>
            <html:select name="editParamForm" property="selectedUrl" onchange="setActionAndSubmit(this.form,'changeView')">
              <!-- Get all the recorded url-->
             <%
            // strus bug
            String posStrg = request.getParameter("selectedUrl");
            if (posStrg == null) {
             posStrg = "0";
            }

            List urlListBean = SessionManager.getClipperBean(request).getUrlListBean();
            for (int i = 0; i < urlListBean.size() - 1; i++) {
              if(!posStrg.equalsIgnoreCase(String.valueOf(i))){
                %>
                <option value="<%=i%>"><%=((UrlBean) urlListBean.get(i)).getLabelForStrut()%>              </option>
                <%}//if
                else{%>
                <option selected value="<%=i%>"><%=((UrlBean) urlListBean.get(i)).getLabelForStrut()%>              </option>
                <%}//else
              }//for%>
            </html:select>
            <!-- Show hmtl-->
            <!--
              <html:checkbox property="showHTML" value="true" onclick="setActionAndSubmit(this.form,'changeView')">
              <bean:message key="edit.ShowHTML"/>
              </html:checkbox>
              <logic:equal name="editParamForm" property="showHTML" value="true">
            -->
            <!-- Show param label-->
            <!--
              <html:checkbox property="showLabel" value="true" onclick="setActionAndSubmit(this.form,'changeView')">
              <bean:message key="edit.ShowLabel"/>
              </html:checkbox>
              </logic:equal>
            -->
          </td>
        </tr>
      </table>
      <!-- Main -->
        <!-- Edit Form parameters in tabled view -->
          <!-- All parameter-->
            <table width="100%" class="clipInputTable">
              <!-- Labels-->
              <thead>
              <tr>
                <th>
                  <bean:message key="edit.name"/>
                </th>
                <th>
                  <bean:message key="edit.mappingName"/>
                </th>
                <th>
                  <bean:message key="edit.usedValue"/>
                </th>
                <th>
                  <bean:message key="edit.param.useAsDefaultValue"/>
                </th>
                <th>
                  <bean:message key="edit.visibility"/>
                </th>
                <th>
                  <bean:message key="edit.update"/>
                </th>
                <th>
                  <bean:message key="edit.type"/>
                </th>
             </tr>
             </thead>
             <tbody>

          <logic:iterate name="editParamForm" property="actifFormParamsList" id="currentParam">
            <!-- new parameter-->


        <tr>
          <!-- Name -->
          <td>
            <bean:write name="currentParam" property="name"/>
          </td>
          <!-- Mapping -->
          <td>
            <html:text name="currentParam" property="mapping"/>
          </td>
          <!-- Values-->
          <logic:notEqual name="currentParam" property="type" value="password">
            <td>
              <bean:write name="currentParam" property="usedValue"/>
            </td>
          </logic:notEqual>
          <logic:equal name="currentParam" property="type" value="password">
            <td>
              <bean:message key="edit.password.value"/>
            </td>
          </logic:equal>
          <!-- Use as default value -->
          <td>
            <html:select name="currentParam" property="useAsDefaultValue">
              <html:option value="true" key="edit.useAsDefault.true"/>
              <html:option value="false" key="edit.useAsDefault.false"/>
            </html:select>
          </td>
          <!-- Visibility -->
          <td>
            <html:select name="currentParam" property="visibility">
              <html:option value="true" key="edit.visibility.true"/>
              <html:option value="false" key="edit.visibility.false"/>
            </html:select>
          </td>
          <!-- Update -->
          <td>
            <html:select name="currentParam" property="update">
              <html:option value="true" key="edit.update.true"/>
              <html:option value="false" key="edit.update.false"/>
            </html:select>
          </td>
          <!-- form type-->
          <td>
            <bean:write name="currentParam" property="type"/>
          </td>
        </tr>
      </logic:iterate>
    </tbody>
      </table>
    </logic:messagesNotPresent>

</table>
<logic:messagesNotPresent>
 <table  width="100%">
     <iframe id="clip_builder" src="<%=response.encodeURL(request.getContextPath()+"/administration/webBrowser?do=clipbuilder")%>" width="100%" height="375" frameborder="1" scrolling="auto"></iframe>
 </table>
</logic:messagesNotPresent>
</html:form>

