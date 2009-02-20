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

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.data.FormDataManager" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.data.fields.ExpressionMarker" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.data.fields.JahiaFieldDefinitionProperties"%>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.gui.GuiBean" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.resourcebundle.ResourceBundleMarker" %>
<%@ page import="org.jahia.services.metadata.FieldDefinition" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="org.jahia.exceptions.JahiaException" %>
<%@ page import="org.jahia.data.containers.JahiaContainer" %>
<%@ page import="javax.jcr.Value" %>
<%@ page import="javax.jcr.PropertyType" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.utils.comparator.NumericStringComparator" %>
<%@ page import="org.jahia.services.content.nodetypes.*" %>
<%@ page import="org.jahia.data.templates.JahiaTemplatesPackage" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="java.io.File" %>
<%@ page import="org.jahia.bin.Jahia" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>

<!-- Begin Smalltext_field.jsp -->

<%!
    final static private org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger("jsp.jahia.engines.shared.smalltext_field");  %>

<%

        final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
        final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");

        final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + ".theField");

        try {
        final boolean readOnly = ("true".equals(theField.getDefinition().getProperty(FieldDefinition.READ_ONLY)));

        final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        final Locale processingLocale = elh.getCurrentLocale();

        final ProcessingContext jParams;
        final String theURL;
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (jData != null) {
            jParams = jData.getProcessingContext();
            theURL = jParams.settings().getJahiaEnginesHttpPath();
        } else {
            jParams = null;
            theURL = null;
        }

        final int pageID = theField.getPageID();

            ExtendedPropertyDefinition propDef = theField.getDefinition().getPropertyDefinition();
        final String contextID = JahiaTools.replacePattern(String.valueOf(theField.getID()), "-", "_");
%>
<logic:present name='<%=theField.getID()+".engineMessages"%>'>
    <br>Errors : <br>
    <ul>
        <logic:iterate name='<%=theField.getID()+".engineMessages"%>' property="messages" id="curMessage">
            <li><internal:message name="curMessage"/></li>
        </logic:iterate>
    </ul>
</logic:present>
<%  if ("true".equals(theField.getDefinition().getProperty(JahiaFieldDefinitionProperties.COLOR_PICKER_PROP))) { %>
<logic:notEqual name="dynapiInitialized" value="true">
    <bean:define id="dynapiInitialized" value='true' toScope="request"/>
    <script type="text/javascript" src="<%=theURL%>jahiatools/javascript/dynapi.js"></script>

    <script type="text/javascript">
    <!--
        DynAPI.setLibraryPath('<%=theURL%>jahiatools/javascript/lib/');
        DynAPI.include('dynapi.api.*');

        function getPageOffsetTop(el) {
            // Return the y coordinate of an element relative to the page.
            if (el.offsetParent != null)
                return el.offsetTop + getPageOffsetTop(el.offsetParent);
            else
                return el.offsetTop;
        }
    //-->
    </script>
</logic:notEqual>

<script type="text/javascript">
<!--
    var ViewColorBG_<%=contextID%>;
    var ViewColorPick_<%=contextID%>;

    function addDynLayer_<%=contextID%>() {
        y = getPageOffsetTop(document.getElementById("imgcolmap_<%=contextID%>"));
        if (document.layers) {
            ViewColorBG_<%=contextID%> = new DynLayer(null, 569, y, 67, 67, '#000000');
            ViewColorPick_<%=contextID%> = new DynLayer(null, 570, y + 1, 65, 65, '#000000');
        } else if (parseInt(navigator.appVersion) == 5) {
            ViewColorBG_<%=contextID%> = new DynLayer(null, 564, y, 67, 67, '#000000');
            ViewColorPick_<%=contextID%> = new DynLayer(null, 565, y + 1, 65, 65, '#000000');
        } else {
            ViewColorBG_<%=contextID%> = new DynLayer(null, 560, y + 1, 67, 67, '#000000');
            ViewColorPick_<%=contextID%> = new DynLayer(null, 561, y + 2, 65, 65, '#000000');
        }

        DynAPI.document.addChild(ViewColorBG_<%=contextID%>);
        DynAPI.document.addChild(ViewColorPick_<%=contextID%>);

        setTextColor_<%=contextID%>('<%=theField.getValue()%>');
    }

    DynAPI.addLoadFunction("addDynLayer_<%=contextID%>();");

    function setTextColor_<%=contextID%>(color) {
        document.getElementById("field_<%=theField.getID()%>").value = color;
        ViewColorPick_<%=contextID%>.setBgColor(color);
    }
//-->
</script>

<table width="377" border="1" cellpadding="5" cellspacing="0" bgcolor="#4D4E6D">
    <tr>
        <td>
            <map id="colmap_<%=contextID%>" name="colmap_<%=contextID%>">
                <%
                    String hexNb[] = {"00", "33", "66", "99", "CC", "FF"};
                    byte R = 0;
                    byte G = 5;
                    byte B = 0;
                    String color;
                    for (int j = 1; j <= 56; j += 11) {
                        for (int i = 1; i <= 281; i += 8) {
                            color = "#" + hexNb[R] + hexNb[G] + hexNb[B];
                %>
                <area shape="rect"
                      coords="<%=Integer.toString(i)%>, <%=Integer.toString(j)%>, <%=Integer.toString(i + 6)%>, <%=Integer.toString(j + 9)%>"
                      href="javascript:setTextColor_<%=contextID%>('<%=color%>')"
                      alt="<%=color%>">
                <%
                            if (B < 5) {
                                B++;
                            } else {
                                B = 0;
                                if (R < 5) {
                                    R++;
                                } else {
                                    R = 0;
                                    G--;
                                }
                            }
                        }
                    }
                %>
            </map><img id="imgcolmap_<%=contextID%>" name="imgcolmap_<%=contextID%>" usemap="#colmap_<%=contextID%>"
                       src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.jahiaTools.colorTable.image"/>"
                       border="0" width="289" height="67"><img alt=""
                                                               src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.pix.image"/>"
                                                               border="0"
                                                               width="73" height="1"><br><img alt=""
                                                                                              src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.jahiaTools.pix.image"/>"
                                                                                              border="0" width="1"
                                                                                              height="4"><br>
            <input id="field_<%=theField.getID()%>" name="_<%=theField.getID()%>"
                   type="text" size="12" value="<%=theField.getValue()%>"
                   onchange="setTextColor_<%=contextID%>(document.getElementById('field_<%=theField.getID()%>').value)" <% if ( readOnly ){%>
                   readonly="readonly"<%}%>>
        </td>
    </tr>
</table>

<% } else if (propDef.getSelector() == SelectorType.CHOICELIST || propDef.getName().equals("jcr:primaryType")) {

    String theSelectedField = theField.getValue();
    Map<String,String> opts = propDef.getSelectorOptions();
    final boolean multipleChoice = propDef.isMultiple();
    final boolean unsorted = !opts.containsKey("sort");
     boolean putRawValue = false;
    Map<String,String> rawValues = new HashMap<String,String>();

    String theList = "";
    if (propDef.getSelector() == SelectorType.CHOICELIST) {
        Value[] cons = propDef.getValueConstraintsAsValue();
        for (int i = 0; i < cons.length; i++) {
            if (i>0) theList += ":";
            if (propDef.getRequiredType() == PropertyType.LONG || propDef.getRequiredType() == PropertyType.DOUBLE) {
                String s = cons[i].getString();
                boolean lowerBoundIncluded = s.charAt(0) == '[';
                boolean upperBoundIncluded = s.charAt(s.length()-1) == ']';
                String lowerBound = s.substring(1,s.indexOf(','));
                String upperBound = s.substring(s.indexOf(',')+1, s.length()-1);
                double l = Double.parseDouble(lowerBound);
                double u = Double.parseDouble(upperBound);
                if (!lowerBoundIncluded) l++;
                if (!upperBoundIncluded) u--;
                for (double j=l; j<=u; j++) {
                    if (j>l) theList += ":";
                    if (propDef.getRequiredType() == PropertyType.LONG) {
                        theList += (long) j;
                    } else {
                        theList += j;
                    }
                }
            } else {
                theList += cons[i].getString();
            }
        }
    } else if (propDef.getName().equals("jcr:primaryType")) {
        putRawValue = true;
        ExtendedNodeDefinition containerDefinition = ((JahiaContainer)engineMap.get("theContainer")).getDefinition().getContainerListNodeDefinition();
        ExtendedNodeType nt = ((JahiaContainer)engineMap.get("theContainer")).getDefinition().getNodeType();
        String[] types;
        opts = containerDefinition.getSelectorOptions();
        if (opts.containsKey("availableTypes")) {
            types = opts.get("availableTypes").split(",");
        } else {
            types = new String[] { ((JahiaContainer)engineMap.get("theContainer")).getDefinition().getNodeDefinition().getRequiredPrimaryTypes()[0].getName() };
        }
        for (int i = 0; i < types.length; i++) {
            ExtendedNodeType currentType = NodeTypeRegistry.getInstance().getNodeType(types[i]);
            if (i>0) theList += ":";
            if (currentType.isAbstract()) {
                ExtendedNodeType[] subs = currentType.getSubtypes();
                for (int j = 0; j < subs.length; j++) {
                    if (j>0) theList += ":";
                    ExtendedNodeType subType = subs[j];
                    System.out.println("--->"+subType.getSystemId());
                    if (!subType.getSystemId().equals("system-standard")) {
                        JahiaTemplatesPackage defPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(subType.getSystemId());
                        System.out.println("---1>"+subType.getSystemId());
                        if (defPackage != null)  {
                            System.out.println("---2>"+subType.getSystemId());
                            JahiaTemplatesPackage parentPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(containerDefinition.getDeclaringNodeType().getSystemId());
                            System.out.println("---3>"+parentPackage + "/ "+nt.getSystemId());
                            if (parentPackage != null && !parentPackage.getInvertedHierarchy().contains(defPackage.getName())) {
                                System.out.println("---4>"+subType.getSystemId());
                                continue;
                            }
                        }
                    }
                    if (!subType.isAbstract()) {
                        String resourceBundleMarker = subType.getResourceBundleMarker();
                        theList += resourceBundleMarker;
                        rawValues.put(resourceBundleMarker, subType.getName());
                    }
                }
            } else {
                String resourceBundleMarker = currentType.getResourceBundleMarker();
                theList += resourceBundleMarker;
                rawValues.put(resourceBundleMarker, currentType.getName()) ;
            }
        }
    }

    //System.out.println("smalltext_field.jsp: field.getValue()=" + theField.getValue());
    final String fieldValue = theField.getValue();
    //System.out.println("smalltext_field.jsp: field.getValue()=" + theField.getValue());

    String[] fieldValues = JahiaTools.getTokens(fieldValue, JahiaField.MULTIPLE_VALUES_SEP);
    if ((fieldValues == null || fieldValues.length == 0) && theSelectedField != null) {
        fieldValues = new String[]{theSelectedField};
    }

    List fieldValuesList = new ArrayList();
    if (fieldValues != null) {
        for (int i = 0; i < fieldValues.length; i++) {
            String val = fieldValues[i];
            fieldValuesList.add(FormDataManager.formDecode(val));
            //System.out.println("smalltext_field.jsp: added multi value[" + val +"]");
        }
    }


        if (theList.indexOf("<jahia-expression") == -1) {
            List resolvedList = new ArrayList(fieldValuesList.size());
            for (Iterator it = fieldValuesList.iterator(); it.hasNext(); ){
                String currentFieldValue = (String)it.next();
                if (currentFieldValue.indexOf("<jahia-expression") != -1) {
                    resolvedList.add(ExpressionMarker.getValue(currentFieldValue, jParams));
                } else {
                    resolvedList.add(currentFieldValue);
                }
            }
            fieldValuesList = resolvedList;

            List markers = ResourceBundleMarker.buildResourceBundleMarkers(theList, processingLocale, !unsorted);
            boolean imageChooser = (propDef.getSelectorOptions().get("image") != null);
            String changeField = "";
            if (imageChooser) {
                changeField = "changeField("+theField.getID()+");";
            }
%>

<select name="listSelection_<%=contextID%>"
        onChange="handleListSelectionChange(document.mainForm.listSelection_<%=contextID%>,document.mainForm.elements['_<%=theField.getID()%>']);<%=changeField%>"  <% if (multipleChoice) {%>
        multiple size="15"<% } %> >
    <%
        int size = markers.size();
        ResourceBundleMarker marker;
        String resourceMarker;
        String selected ="";
        for (int i = 0; i < size; i++) {
            marker = (ResourceBundleMarker) markers.get(i);
            if ("".equals(marker.getResourceBundleID())) {
                resourceMarker = marker.getValue(jParams.getLocale());
            } else {
                resourceMarker = marker.drawMarker();
            }
            logger.debug("fieldValue=[" + fieldValue + "]");
            logger.debug("fieldValuesList=" + fieldValuesList.toString() + " size=" + fieldValuesList.size());

            // We select the first entry if no entry was ever selected, because browsers don't all handle the default selection the same way (http://www.jahia.net/jira/browse/JAHIA-3674)
            boolean selectFirstByDefault = (("".equals(fieldValue)) && (i == 0) && (!multipleChoice));

            String value;
            boolean isSelected;
            if (putRawValue) {
                value  = rawValues.get(resourceMarker);
                isSelected = fieldValuesList.contains(value);
            } else {
                value = FormDataManager.formEncode(resourceMarker);
                isSelected = fieldValuesList.contains(resourceMarker);
            }
 
            if (isSelected ||
                selectFirstByDefault) { %>
    <option id="<%=marker.getDefaultValue()%>" value="<%=value%>"
            selected="selected"><%=GuiBean.glueTitle(marker.getValue(jParams.getLocale()), 70)%></option>
    <%
    selected = marker.getDefaultValue();
    } else { %>
    <option id="<%=marker.getDefaultValue()%>" value="<%=value%>"><%=GuiBean.glueTitle(marker.getValue(jParams.getLocale()), 70)%></option>
    <% }
    }
    %>
</select>

<%
   if (imageChooser) {
       String path = null;
       String tplPkgName = jParams.getSite().getTemplatePackageName();
       JahiaTemplatesPackage pkg = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(tplPkgName);
       for (Iterator iterator = pkg.getLookupPath().iterator(); iterator.hasNext();) {
           String rootFolderPath = (String) iterator.next();
           File f = new File(Jahia.getStaticServletConfig().getServletContext().getRealPath(rootFolderPath+"/"+propDef.getName()+"s/"+selected+".png"));
           if (f.exists()) {
               path = rootFolderPath+"/"+propDef.getName()+"s/"+selected+".png";
           }
       }
       if (path != null) {
%>
   <br>
    <img src="<%=jParams.getContextPath()+ path%>" alt="<%=selected%>" />
<%
        }
    }


} else {
    // Expression markers found, we must evaluate them.
    List markers = ExpressionMarker.buildExpressionMarkers(theList, jParams);

%>
<select name="listSelection_<%=contextID%>"
        onchange="handleListSelectionChange(document.mainForm.listSelection_<%=contextID%>,document.mainForm.elements['_<%=theField.getID()%>']);"  <% if (multipleChoice) {%>
        multiple="multiple" size="15"<% } %> >
    <%
        int size = markers.size();
        ExpressionMarker marker;
        String expressionMarker;
        for (int i = 0; i < size; i++) {
            marker = (ExpressionMarker) markers.get(i);
            expressionMarker = marker.drawMarker();
            logger.debug("expressionMarker=" + expressionMarker);
            logger.debug("fieldValue=" + fieldValue);
            logger.debug("fieldValuesList=" + fieldValuesList.toString());
            logger.debug("expressionMarker=" + FormDataManager.formDecode(expressionMarker));

            // We select the first entry if no entry was ever selected, because browsers don't all handle the default selection the same way (http://www.jahia.net/jira/browse/JAHIA-3674)
            boolean selectFirstByDefault = (("".equals(fieldValue)) && (i == 0) && (!multipleChoice));

            if (fieldValuesList.contains(FormDataManager.formDecode(expressionMarker)) ||
                selectFirstByDefault) { %>
    <option value="<%=FormDataManager.formEncode(expressionMarker)%>"
            selected="selected"><%=GuiBean.glueTitle(marker.getValue(), 70)%></option>
    <% } else { %>
    <option value="<%=FormDataManager.formEncode(expressionMarker)%>"><%=GuiBean.glueTitle(marker.getValue(), 70)%></option>
    <% }
    }
    %>
</select>
<%
        }
%>
<input name="_<%=theField.getID()%>" type="hidden" value="<%=theField.getValue()%>"/>
<script type="text/javascript">

    function concatMultipleFieldValues(selectBox) {

        var sep = "<%=JahiaField.MULTIPLE_VALUES_SEP%>";
        var result = "";
        for (i = 0; i < selectBox.options.length; i++) {
            if (selectBox.options[i].selected) {
                if (result.length == 0) {
                    result = selectBox.options[i].value;
                } else {
                    result = result + sep + selectBox.options[i].value;
                }
            }
        }
        return result;
    }

    function handleListSelectionChange(selectBox, fieldInput) {
        fieldInput.value = concatMultipleFieldValues(selectBox);
        // alert('handleListSelection=' + fieldInput.value);
    }

    document.mainForm.elements['_<%=theField.getID()%>'].value = concatMultipleFieldValues(document.mainForm.listSelection_<%=contextID%>);
</script>
<% } else { %>
<%
    final String val;
    if(theField.getRawValue().startsWith("<jahia-expression")) {
        val = ExpressionMarker.getValue(theField.getRawValue(), jParams);
    } else if (theField.getRawValue().startsWith("<jahia-resource")) {
        val = ResourceBundleMarker.getValue(theField.getRawValue(), jParams.getLocale());
    } else {
        val = JahiaTools.replacePattern(theField.getValue(), "\"", "\\\"");
    }
    if ("true".equals(theField.getDefinition().getProperty(JahiaFieldDefinitionProperties.FIELD_MULTILINE_SMALLTEXT_PROP))) {
%>
<textarea id="field_<%=theField.getID()%>" name="_<%=theField.getID()%>" rows="3" cols="100" style="width:98%" <% if ( readOnly ){%> readonly="readonly"<% } %>><%=val%></textarea>
<%} else { %>
<input id="field_<%=theField.getID()%>" name="_<%=theField.getID()%>" style="width:550px" type="text" maxlength="250" value="<%=val%>"
<% if ( readOnly ){%> readonly="readonly"<% } %> >
<%
}
        }
    } catch (final JahiaException je) {
        logger.error("Error displaying SmallText: " + je.getMessage(), je);
    }
%>

<!-- End Smalltext_field.jsp -->

