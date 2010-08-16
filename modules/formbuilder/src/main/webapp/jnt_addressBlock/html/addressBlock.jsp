<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>


<c:set value="${currentNode.propertiesAsString}" var="props"/>


<div>
<form>
<table cellpadding="4">
	<tr>
    	<td><label for="street">Street:</label></td>
        <td colspan="3"><input type="text" maxlength="50" size="40" name="street"></td>
	</tr>
	<tr>
		<td><label for="street2">Street 2:</label></td>
        <td colspan="3"><input type="text" maxlength="50" size="40" name="street2"></td>
	</tr>
	<tr>
    	<td><label for="city">City:</label></td>
        <td><input type="text" maxlength="40" size="18" name="city"></td>
        <td align="right"><label for="state">State:</label></td>
        <td align="right"><input type="text" maxlength="15" size="6" name="state"></td>
	</tr>
	<tr>
    	<td><label for="zip">ZIP:</label></td>
        <td><input type="text" maxlength="10" size="6" name="zip"></td>
        <td align="right"><label for="country">Country:</label></td>
        <td align="right"><input type="text" maxlength="40" size="6" name="country"></td>
	</tr>
</table>
</form>
</div>