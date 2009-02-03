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

<%@ page language="java" buffer="8kb" autoFlush="true" isThreadSafe="true" isErrorPage="false"  %>
<%@ page import="java.util.*, java.text.*, com.jamonapi.*, com.jamonapi.utils.*, com.fdsapi.*, com.fdsapi.arrays.*" %>

<%

// Set formatting rules per the requests Locale (as opposed to the servers locale).
// This will format data per the users preference (note this sets it for the given thread/servlet)
LocaleContext.setLocale(request.getLocale());

// Assign request parameters to local variables.
String action    = getValue(request.getParameter("action"),"Refresh");
String outputType= getValue(request.getParameter("outputTypeValue"),"html");
String formatter = getValue(request.getParameter("formatterValue"), "#,###");
String arraySQL  = getValue(request.getParameter("ArraySQL"),"");
String sortOrder = getValue(request.getParameter("sortOrder"), "asc");
int sortCol      = getSortCol(request.getParameter("sortCol"), "1");

// Assign defaults to RangeName and displayTypeValue if they weren't part of the request.
String displayType=getValue(request.getParameter("displayTypeValue"), "BasicColumns");
String rangeName=getValue(request.getParameter("RangeName"),"AllMonitors");

// AllMonitors can't display ranges so allways change displayType to BasicColumns in this case
displayType=(rangeName.equalsIgnoreCase("AllMonitors")) ? "BasicColumns" : displayType;

// Assign defaults for arraysql.  If nothing is provided use 'select * from array'.  If the first word is not 
// 'select' assume they want to do a like on the first column (label).
String arraySQLExec = getValue(arraySQL,"select * from array");


if (arraySQLExec.trim().toLowerCase().startsWith("select")) 
  arraySQLExec=arraySQLExec;// noop all is ok full select entered
else if (arraySQLExec.trim().toLowerCase().startsWith("where")) 
   arraySQLExec="select * from array "+arraySQL;// where clause entered:  where hits>100 and total<50000
else
   arraySQLExec="select * from array where col0 like '"+arraySQL+"'";

arraySQLExec = (arraySQLExec.trim().toLowerCase().startsWith("select")) ? arraySQLExec : "select * from array where col0 like '"+arraySQL+"'";

// Build the request parameter query string that will be part of every clickable column.
String query="";
query+="&displayTypeValue="+displayType;
query+="&RangeName="+rangeName;
query+="&outputTypeValue="+outputType;
query+="&formatterValue="+java.net.URLEncoder.encode(formatter);
query+="&ArraySQL="+java.net.URLEncoder.encode(arraySQL);


if ("Reset".equals(action))
    MonitorFactory.reset();
else if ("Enable".equals(action)) 
    MonitorFactory.setEnabled(true);
else if ("Disable".equals(action))  
    MonitorFactory.setEnabled(false);

Map map=new HashMap();
// used for html page
map.put("sortPageName", "JAMonAdmin.jsp");
map.put("query", query);
map.put("imagesDir","images/");


// used for xml1 page.
map.put("rootElement", "JAMonXML");


String outputText;
MonitorComposite mc=MonitorFactory.getComposite(rangeName);

if (!MonitorFactory.isEnabled())
  outputText="<div align='center'><br><br><b>JAMon is currently disabled.  To enable monitoring you must select 'Enable'</b></div>";
else if (!mc.hasData())
  outputText="<div align='center'><br><br><b>No data was returned</b></div>";
else {
  ResultSetConverter rsc;  // contains the header, and data after ArraySQL is applied.  This is a thin wrapper for arrays.

  if (displayType.equalsIgnoreCase("BasicColumns") || rangeName.equalsIgnoreCase("AllMonitors")) 
     rsc=getResultSetConverter(mc.getBasicHeader(), mc.getBasicData(), arraySQLExec);
  else if (displayType.equalsIgnoreCase("RangeColumns")) 
     rsc=getResultSetConverter(mc.getDisplayHeader(), mc.getDisplayData(), arraySQLExec);
  else if (displayType.equalsIgnoreCase("AllColumns")) 
     rsc=getResultSetConverter(mc.getHeader(), mc.getData(), arraySQLExec);
  else 
     rsc=getResultSetConverter(mc.getBasicHeader(), mc.getBasicData(), arraySQLExec);

  if (rsc.isEmpty())
    outputText="<div align='center'><br><br><b>No data was returned</b></div>";
  else {
    ArrayConverter ac=getArrayConverter(formatter);
    fds.setArrayConverter(ac);

    if ("xml".equalsIgnoreCase(outputType)) {
      rsc=new ResultSetConverter(rsc.getMetaData(), ac.convert(rsc.getResultSet()));
      outputText=fds.getFormattedDataSet(rsc, map, "xml1");
    }
    else if ("excel".equalsIgnoreCase(outputType) || "spreadsheet".equalsIgnoreCase(outputType)) {
      rsc=new ResultSetConverter(rsc.getMetaData(), ac.convert(rsc.getResultSet()));
      outputText=fds.getFormattedDataSet(rsc, map, "basicHtmlTable");
    }
    else 
      outputText=fds.getSortedText(rsc.getMetaData(), rsc.getResultSet(), map, sortCol, sortOrder, getJAMonTemplate());

  }
}
%>

<%
if ("html".equalsIgnoreCase(outputType)) {
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<META http-equiv="Content-Type" content="text/html"; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="css/JAMonStyles.css">
<title>JAMon - Administration/Reporting - <%=now()%></title>
<script type="text/javascript">
<!--
// Row highlighter
var objClass

function rollOnRow(obj, txt) {
    objClass = obj.className
    obj.className = "rowon";
    obj.title = txt;
}

function rollOffRow(obj) {
    obj.className = objClass;
}

function selectAll(obj, numRows) {
    state = (obj.checked) ? true : false;

    for (var i = 1; i < numRows + 1; i ++) {
        currRow = eval("obj.form.row_" + i);
        currRow.checked = state;
    }
}

function helpWin() {
    newWin = window.open('JAMonHelp.htm', 'helpWin', 'resizable=no,scrollbars=yes,height=550,width=450,screenX=100,screenY=100');
    if (newWin.opener == null) newWin.opener = self;
}
// -->
</script>

</head>
<body>

<!-- arraySQLExec=<%=arraySQLExec%>-->

<form action="JAMonAdmin.jsp" method="post">

<table border="0" cellpadding="1" cellspacing="0" align="center">
<tr>
<td><h2 style="color:#03487F;">JAMon - Administration/Reporting - <%=now()%></h2></td>
</tr>
<tr>
<td><table class="layoutmain" border="0" cellpadding="4" cellspacing="0" width="750" align="left">
    <tr class="sectHead">
    <th>Action</th>
    <th>Output</th>
    <th>Range/Units</th>
    <th>Display Columns</th>
    <th>Cell Format</th>
    <th>Filter (optional)</th>
    <th align="right"><a href="javascript:helpWin();" style="color:#C5D4E4;">Help</a></th>
    </tr>
    <tr class="even">
    <th><%=fds.getDropDownListBox(actionHeader, actionBody, action)%></th>
    <th><%=fds.getDropDownListBox(outputTypeHeader, outputTypeBody, outputType)%></th>
    <th><%=fds.getDropDownListBox(MonitorFactory.getRangeHeader(), getRangeNames(), rangeName)%></th>
    <th><%=fds.getDropDownListBox(displayTypeHeader, displayTypeBody, displayType)%></th>
    <th><%=fds.getDropDownListBox(formatHeader, formatBody, formatter)%></th>
    <th><input type='text' name='ArraySQL' value="<%=arraySQL%>" size="45"></th>
    <td><input type="submit" name="actionSbmt" value="Go !" ></td>
    </tr>
</table></td>
</tr>
<tr>
<td><table border="0" cellpadding='0' cellspacing='0' align="center">
    <tr>
    <td><%=outputText%></td>
    </tr>
</table></td>
</tr>
</table>

</form>

<hr width="75%">

<td><table border='0' align='center' width='25%'>
    <tr>
    <th nowrap><a href="http://www.jamonapi.com"><img src="images/jamon_small.jpg" id="monLink" border="0" /></a></th>
    <th nowrap>JAMon <%=MonitorFactory.getVersion()%></th>
    <th nowrap><a href="http://www.fdsapi.com"><img height=40 width=80 src="images/fds_logo_small.jpg" id="monLink" border="0" /></a></th>
    </tr>
</table></td>


</body>
</html>

<%
} else if ("xml".equalsIgnoreCase(outputType)) {
%>
<?xml version="1.0"?>
<%=outputText%>
<%
} else {
  response.setContentType("application/vnd.ms-excel");
%>


<%=outputText%>

<%
}
%>

<%!

String[] actionHeader={"action","actionDisplay"};
Object[][] actionBody={
                 {"Refresh", "Refresh"}, 
                 {"Reset", "Reset"}, 
	           {"Enable","Enable"}, 
	           {"Disable","Disable"}, 
                };


String[] displayTypeHeader={"displayTypeValue","displayType"};
Object[][] displayTypeBody={
                 {"RangeColumns", "Basic/Range Cols"}, 
                 {"BasicColumns", "Basic Cols Only"}, 
	           {"AllColumns","All Cols"}, 
                };

String[] outputTypeHeader={"outputTypeValue","outputType"};
Object[][] outputTypeBody={
                 {"html", "HTML"}, 
                 {"xml", "XML"}, 
	           {"excel","MS Excel"}, 
                };


FormattedDataSet fds=new FormattedDataSet();
Template jamonTemplate;

String[] formatHeader={"formatterValue","formatterDisplay"};
Object[][] formatBody= {
                       {"#,###","#,###"},
                       {"#,###.#","#,###.#"},
                       {"#,###.##","#,###.##"},
                       {"#,###.###","#,###.###"},
                       {"#,###.####","#,###.####"},
                       {"raw","No Format"},  
                      };

// Format time String in current locale.
private String now() {
  return LocaleContext.getDateFormatter().format(new Date());
}


private synchronized Template getJAMonTemplate() {
   // start from sortedHTMLTable template and add some jamon display capabilities (highlighting and descriptive text appearing where the mouse is)
   if (jamonTemplate==null) {
     jamonTemplate=fds.getTemplate("sortedHTMLTable").copy();

     // highlighting for odd and even rows of the JAMon report.  Affects coloring, 
     // and what happens when the rows is highlighted with the mouse.
     String odd="Odd==   <tr class='odd' onMouseOver='rollOnRow(this, \"##1\")' onMouseOut='rollOffRow(this)'>\n";
     String even="Even==   <tr class='even' onMouseOver='rollOnRow(this, \"##1\")' onMouseOut='rollOffRow(this)'>\n";
     jamonTemplate.initialize("BODY_ROW_PREFIX",0,0,"Type==Alternating "+odd+" "+even);
   }

   return jamonTemplate;
}


// if the value is null then return the passed in default else return the value
private static String getValue(String value, String defaultValue) {
  return (value==null || "".equals(value.trim())) ? defaultValue: value;
}

// convert arg to an int or return the default
private static int getSortCol(String value, String defaultValue) {
  String col=getValue(value, defaultValue);
  return Integer.parseInt(col);
}

private static ResultSetConverter getResultSetConverter(String[] header, Object[][] data, String arraySQLExec) {
     ArraySQL asql=new ArraySQL(header, arraySQLExec );
     ArrayConverter ac=new ArrayConverter();

     // replace all booleans with strings as you can't sort booleans (they don't support Comparable interface for some reason)
     Map map=new HashMap();
     map.put(new Boolean(true), "true");
     map.put(new Boolean(false), "false");

     ac.setDefaultConverter(new ConverterMapValue(map));
     asql.setArrayConverter(ac);

     ResultSetConverter rsc = new ResultSetConverter(header, asql.convert(data));
     //MonitorFactory.add("cellCount","count",rsc.getColumnCount()*rsc.getRowCount());
     return rsc;
}

private static Map getDefaultsMap() {
  Map map=new HashMap();

  Double max=new Double(-Double.MAX_VALUE);
  Double min=new Double(Double.MAX_VALUE);
  Date noDate=new Date(0);

  map.put(min,"");
  map.put(max,"");
  map.put(noDate,"");

  return map;
}


private static ArrayConverter getArrayConverter(String pattern) {
  if (pattern==null || "".equals(pattern))
    pattern="#,###";
  else if ("raw".equalsIgnoreCase(pattern)) {
    ArrayConverter ac=new ArrayConverter();
    ac.setDefaultConverter(new NullConverter());
    return ac;
  }

  // used to format the data in the report
  ArrayConverter ac=new ArrayConverter();
  // replace the normal extreme values of default min,max, and acccess date with an empty default.
  // If the key is in the data stream the value will be used instead of the key.
  Map map=getDefaultsMap();

  DecimalFormat decimalFormat=LocaleContext.getFloatingPointFormatter();
  decimalFormat.applyPattern(pattern);
  DateFormat dateFormat=LocaleContext.getDateFormatter();

  // Set the converter to take action on data passed to the FormattedDataSet
  ac.setDefaultConverter(new ConverterNumToString(decimalFormat, new ConverterDateToString(dateFormat, new ConverterMapValue(map))));
 
  return ac;

}


// This version of the FormattedDataSet requires a value as well as the display value.  The same column is used for both 
// below.
private static Object[][] getRangeNames() {
   Object[][] range=MonitorFactory.getRangeNames();
   Object[][] data=new Object[range.length][];
   for (int i=0;i<range.length;i++) {
      data[i]=new Object[2];
      data[i][0]=data[i][1]=range[i][0];
   }
  
   return data;
     

}
%>