<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<div class="contactForm">
    <form method="post" action="">
        <fieldset><legend>1. CONTACT DETAILS</legend>

<c:if test="${currentNode.properties.firstname.boolean}">
<p>
  <label class="left" for="firstname">first name</label>  <input id="firstname" type="text" name="firstname">
</p>
</c:if>

<c:if test="${currentNode.properties.lastname.boolean}">
<p>
    <label class="left" for="lastname">last name</label><input id="lastname" type="text" name="lastname">
</p>
</c:if>
<c:if test="${currentNode.properties.title.boolean}">
<p>
    <label class="left" for="title">title</label><input type="text" name="title" id="title">
</p>
</c:if>
<c:if test="${currentNode.properties.age.boolean}">
<p>
<label class="left" for="age">age</label><input type="text" id="age" name="age">
</p>
</c:if>
<c:if test="${currentNode.properties.birthdate.boolean}">
<p>
<label class="left" for="birthdate">birthdate</label><input type="text" id="birthdate" name="birthdate">
</p>
</c:if>
<c:if test="${currentNode.properties.gender.boolean}">
<p>
<label class="left" for="gender">gender</label><input type="text" id="gender" name="gender">
</p>
</c:if>
<c:if test="${currentNode.properties.profession.boolean}">
<p>
<label class="left" for="profession">profession</label><input type="text" id="profession" name="profession">
</p>
</c:if>
<c:if test="${currentNode.properties.maritalStatus.boolean}">
<p>
<label class="left" for="maritalStatus">maritalStatus</label><input type="text" id="maritalStatus" name="maritalStatus">
</p>
</c:if>
<c:if test="${currentNode.properties.hobbies.boolean}">
<p>
<label class="left" for="hobbies">hobbies</label><input type="hobbies" id="hobbies" name="hobbies">
</p>
</c:if>
<c:if test="${currentNode.properties.contact.boolean}">
<p>
<label class="left" for="contact">contact</label><input type="text" id="contact" name="contact">
</p>
</c:if>
<c:if test="${currentNode.properties.address.boolean}">
<p>
<label class="left" for="address">address</label><input type="text" id="address" name="address">
</p>
</c:if>
<c:if test="${currentNode.properties.city.boolean}">
<p>
<label class="left" for="city">city</label><input type="text" id="city" name="city">
</p>
</c:if>
<c:if test="${currentNode.properties.state.boolean}">
<p>
<label class="left" for="state">state</label><input type="text" id="state" name="state">
</p>
</c:if>
<c:if test="${currentNode.properties.zip.boolean}">
<p>
<label class="left" for="zip">zip</label><input type="text" id="zip" name="zip">
</p>
</c:if>
<c:if test="${currentNode.properties.country.boolean}">
<p>
<label class="left" for="country">country</label><input type="text" id="country" name="country">
</p>
</c:if>
<c:if test="${currentNode.properties.remarks.boolean}">
<p>
<label class="left" for="remarks">remarks</label><input type="text" id="remarks" name="remarks">
</p>
</c:if>
            </fieldset>
</form>        
</div>