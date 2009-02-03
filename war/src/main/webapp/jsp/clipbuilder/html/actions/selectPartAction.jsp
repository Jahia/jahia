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
<script language="javascript" type="text/javascript">
  // set the selected content
  function setSelectContent(f) {
    var txt     = '';
    var foundIn = '';
    var iFrameWindow = frames['webBrowser'].window;
    var iFrameDocument = frames['webBrowser'].document;
    if (window.getSelection) {
      txt = iFrameWindow.window.getSelection();
      foundIn = 'window.getSelection()';

    } else if (document.getSelection) {
      txt = iFrameDocument.getSelection();
      foundIn = 'document.getSelection()';

    } else if (document.selection) {
      txt = iFrameDocument.selection.createRange().htmlText;
      foundIn = 'document.selection.createRange()';

    } else {
      txt = 'Enable to validate selection!!!';
      alert(txt);
      f.selectedContent.value = "<p> Enable to validate selection!!! </p>";
      return;
    }
    f.selectedContent.value = txt;
  }

  // set the value of webClippinAction parameter
  function setAction(f, value) {
    if(value == "autoCut"){
    	f.action = '<%=response.encodeURL(request.getContextPath()+"/administration/selectPart?do=clipbuilder&webClippingAction=autoCut")%>';
    }
    if(value == "changeClippingMethod"){
    	f.action = '<%=response.encodeURL(request.getContextPath()+"/administration/selectPart?do=clipbuilder&webClippingAction=changeClippingMethod")%>';
    }
    return;
  }

  //set action attribute of the given form
  function setActionAndSubmit(f, value) {
    setAction(f, value);
    f.submit();
    return;
  }
</script><%
  String options = "\n";
  javax.swing.text.html.HTML.Tag[] tags = javax.swing.text.html.HTML.getAllTags();
  String selectedTagValue = request.getParameter("webClippingTagName");
  if (selectedTagValue == null) {
    selectedTagValue = "table";
  }
  for (int i = 0; i < tags.length; i++) {
    String name = tags[i].toString();
    options = options + "\n" + "<option value=" + name;
    if (name.equalsIgnoreCase(selectedTagValue)) {
      options = options + " selected=selected" + " ";
    }
    options = options + " > " + name + "</option>";
  }
%>
<table align="center" width="100%">
  <html:form action="selectPart?do=clipbuilder">
    <html:hidden property="selectedContent"/>
    <!-- Header -->
    <table align="center" class="principal" width="100%">
      <!-- Title -->
      <tr>
        <td>
          <bean:message key="select.title"/>
        </td>
        <!-- Help -->
        <td style="text-align:right">
          <a href="javascript:popitup('<%=response.encodeURL(request.getContextPath()+"/jsp/clipbuilder/html/help/selectPartHelp.jsp")%>')">
            <bean:message key="wizard.help"/>
          </a>
        </td>
      </tr>
    </table>
    <!-- Buttons -->
    <table align="center" class="principal" width="100%">
      <tr>
        <td class="topmenubuttons">
          <!-- Buttons depending on the clipping method-->
          <logic:notEqual name="selectPartForm" value="1" property="clippingMethod">
            <!-- Manual -->
            <html:submit styleClass="fancyButton" property="webClippingAction" onclick="javascript:setSelectContent(this.form);">
              <bean:message key="edit.button.extract"/>
            </html:submit>
          </logic:notEqual>
          <logic:equal name="selectPartForm" value="1" property="clippingMethod">
            <!-- chew -->
            <html:submit styleClass="fancyButton" property="webClippingAction">
              <bean:message key="edit.button.chew.cut"/>
            </html:submit>
          </logic:equal>
          <!-- Edit param -->
          <html:submit styleClass="fancyButton" property="webClippingAction">
            <bean:message key="select.editParams"/>
          </html:submit>
        </td>
      </tr>
    </table>
    <!-- Select option  -->
    <table class="clipInputTable" style="width:100%; text-align:center">
      <tr>
        <!-- Selecting the clipping method-->
        <td>
          <bean:message key="select.clipbuilderMethod"/>
          <html:select property="clippingMethod" size="1" onchange="setActionAndSubmit(this.form,'changeClippingMethod');">
            <html:option value="1">
              <bean:message key="select.clipbuilderMethod.chew"/>
            </html:option>
            <html:option value="3">
              <bean:message key="select.clipbuilderMethod.form"/>
            </html:option>
            <html:option value="4">
              <bean:message key="select.clipbuilderMethod.xPath"/>
            </html:option>
            <html:option value="5">
              <bean:message key="select.clipbuilderMethod.all"/>
            </html:option>
            <html:option value="0">
              <bean:message key="select.clipbuilderMethod.manual"/>
            </html:option>
          </html:select>
        </td>
        <!-- Selecting the type of the content-->
        <logic:equal name="selectPartForm" value="0" property="clippingMethod">
          <!-- case: manual-->
          <td>
            <bean:message key="select.typeContent"/>
            <html:select property="webClippingTypeContent" size="1" value="0">
              <html:option value="0">
                <bean:message key="select.typeContent.dynamic"/>
              </html:option>
              <html:option value="1">
                <bean:message key="select.typeContent.static"/>
              </html:option>
            </html:select>
          </td>
        </logic:equal>
        <logic:equal name="selectPartForm" value="1" property="clippingMethod">
          <!-- case: chew-->
          <td>
            <bean:message key="select.tagName"/>
            <html:select property="webClippingTagName" size="1" onchange="setActionAndSubmit(this.form,'autoCut');"><%=options %>            </html:select>
          </td>
        </logic:equal>
        <logic:equal name="selectPartForm" value="4" property="clippingMethod">
          <!-- case: chew-->
          <td>
            <bean:message key="select.xpath.enter"/>
            <html:text property="webClippingXPath" size="10"/>
          </td>
        </logic:equal>
        <!-- all cases-->
        <td>
          <bean:message key="select.showCss"/>
          <html:checkbox property="webClippingShowCss"/>
        </td>
      </tr>
    </table>
    <iframe name="webBrowser" id="clip_builder" src="<%=response.encodeURL(request.getContextPath()+"/administration/webBrowser?do=clipbuilder")%>" width="100%" height="375" frameborder="1" scrolling="auto">      </iframe>
  </html:form>
</table>
