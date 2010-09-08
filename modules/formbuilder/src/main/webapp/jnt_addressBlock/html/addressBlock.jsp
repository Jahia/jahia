<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set value="${currentNode.propertiesAsString}" var="props"/>


<div>
<form>
<table cellpadding="4">
	<tr>
    	<td><label for="street"><fmt:message key="address.street"/></label></td>
        <td colspan="3"><input type="text" maxlength="50" size="40" name="street"></td>
	</tr>
	<tr>
		<td><label for="street2"><fmt:message key="address.street2"/></label></td>
        <td colspan="3"><input type="text" maxlength="50" size="40" name="street2"></td>
	</tr>
	<tr>
    	<td><label for="city"><fmt:message key="address.city"/></label></td>
        <td><input type="text" maxlength="40" size="18" name="city"></td>
        <td align="right"><label for="state"><fmt:message key="address.state"/>:</label></td>
        <td align="right"><input type="text" maxlength="15" size="6" name="state"></td>
	</tr>
	<tr>
    	<td><label for="zip">ZIP:</label></td>
        <td><input type="text" maxlength="10" size="6" name="zip"></td>
        <td align="right"><label for="country"><fmt:message key="address.country"/></label></td>
        <td align="right"><input type="text" maxlength="40" size="6" name="country"></td>
	</tr>
</table>
</form>
</div>