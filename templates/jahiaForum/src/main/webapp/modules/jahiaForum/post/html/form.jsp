<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<form action="${baseUrl}${currentNode.path}/*" method="post">
    <input type="hidden" name="nodeType" value="jahiaForum:post"/>
    <div id="commentsForm"><!--start commentsForm-->
       <p></p>
       <fieldset>


           <p class="field">
             <input type="text" size="35" id="c_site" name="title" tabindex="1"/>
           </p>

           <p class="field">
             <textarea rows="7" cols="35" id="c_content" name="content" tabindex="2"></textarea>
           </p>
      <p class="commentsForm_button">
        <input type="reset" value="Annuler" class="button" tabindex="3" />

        <input type="submit" value="Sauvegarder" class="button" tabindex="4" />
      </p>
      </fieldset>
    </div>
</form>
