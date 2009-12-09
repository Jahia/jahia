<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="Form taskForm"><!--start Form -->



            <form method="post" action="${url.base}${currentNode.path}/*">
                <input type="hidden" name="nodeType" value="jnt:task">
                <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}">
              <fieldset><legend><fmt message key="jnt_task.newTask"/></legend>


                <p><label for="task_title" class="left"><fmt message key="jnt_task.title"/></label>
                <input type="text" name="title" id="task_title" class="field" value="" tabindex="16" /></p>

                <p><label for="task_description" class="left"><fmt message key="jnt_task.description"/>:</label>
                <input type="text" name="description" id="task_description" class="field" value="" tabindex="17" /></p>

                <p><label for="task_priority" class="left"><fmt message key="jnt_task.priority"/>:</label>
                  <select name="priority" id="task_priority" class="combo" tabindex="21" >
                     <option value="very_low"> <fmt message key="jnt_task.veryLow"/> </option>
                     <option value="low"> <fmt message key="jnt_task.low"/> </option>
                     <option value="medium"> <fmt message key="jnt_task.medium"/> </option>
                     <option value="high"> <fmt message key="jnt_task.high"/> </option>
                     <option value="very_high"> <fmt message key="jnt_task.veryHigh"/> </option></select></p>

               <%--<p><label for="task_dueDate" class="left">Due date:</label>--%>
                  <%--<input type="text" name="dueDate" id="task_dueDate" class="field" value="" tabindex="17" /></p>--%>

                   <jcr:propertyInitializers nodeType="jnt:task" name="assignee" var="users"/>

                  <p><label for="task_assignee" class="left"><fmt message key="jnt_task.assignee"/>:</label>

                    <select name="assignee" id="task_assignee" class="combo" tabindex="21" >

                    <c:forEach items="${users}" var="user">
                        <option value="${user.value.string}"> ${user.displayName} </option>
                    </c:forEach>
                    </select>
                  </p>
              </fieldset>

              <div class="divButton"><input type="submit" id="submit" class="button" value="Create task" tabindex="28" />
              </div>
            </form>
          </div>
<div class='clear'></div>


