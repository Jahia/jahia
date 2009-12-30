<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set value="${currentNode.propertiesAsString}" var="props"/>


<p class="field">
          <table>
            <tr><td>Street</td><td colspan="3"><input type="text" maxlength="50" size="15" name="street"></td></tr>
            <tr><td>Street 2</td><td colspan="3"><input type="text" maxlength="50" size="15" name="street2"></td></tr>
            <tr><td>City</td><td><input type="text" maxlength="40" size="10" name="city"></td><td>State</td><td><input type="text" maxlength="15" size="5" name="state"></td></tr>
            <tr><td>ZIP</td><td><input type="text" maxlength="10" size="6" name="zip"></td><td>Country</td><td><input type="text" maxlength="40" size="6" name="zip"></td></tr>
          </table>
</p>