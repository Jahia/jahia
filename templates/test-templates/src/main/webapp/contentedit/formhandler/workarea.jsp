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

<%@ include file="../../common/declarations.jspf" %>

<template:jahiaPageForm name="formHandler" method="post">

    <fmt:message key='display.name'/>: <input type="text"
                                                                                      name="formHandlerCL_Name"/><br/>
    <fmt:message key='display.adress'/>: <input type="text"
                                                                                          name="formHandlerCL_Adress"/><br/>
    <fmt:message key='display.zip'/>: <input type="text"
                                                                                    name="formHandlerCL_Zip">
    <fmt:message key='display.city'/>: <input type="text"
                                                                                      name="formHandlerCL_City"/>
    <br/>
    <input type="hidden" name="storeContact" value="1"/>
    <input type="submit" value="<fmt:message key='display.store'/>"
           name="save"/>
    <br/>

    <template:containerList name="formHandlerCL" id="formHandlerCL" actionMenuNamePostFix="textContainers"
                            actionMenuNameLabelKey="textContainers.add">

        <template:formContentMapperHandler listName="formHandlerCL" submitMarker="storeContact"/>
        <template:container id="formC">
            Contact:<br/>
            <fmt:message key='display.name'/>: <template:field
                name="Name"/><br/>
            <fmt:message key='display.adress'/>: <template:field
                name="Adress"/><br/>
            <fmt:message key='display.zip'/>: <template:field
                name="Zip"/> &nbsp;&nbsp;<fmt:message key='display.city'/>:
            <template:field name="City"/><br/><br/>


        </template:container>
    </template:containerList>

</template:jahiaPageForm>