<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
                <table width="100%" class="table tableTasks " summary="Mes taches en cour en table">
                    <caption>
                        My tasks
                    </caption>
                    <colgroup>
                        <col span="1" width="10%" class="col1"/>
                        <col span="1" width="50%" class="col2"/>
                        <col span="1" width="10%" class="col3"/>
                        <col span="1" width="10%" class="col4"/>
                        <col span="1" width="15%" class="col5"/>
                        <col span="1" width="15%" class="col6"/>
                    </colgroup>
                    <thead>
                    <tr>
                        <th class="center" id="Type" scope="col">Type <a href="#" title="sort up"><img
                                src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a title="sort down"
                                                                                                     href="#"> <img
                                src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
                        <th id="Title" scope="col">Title <a href="#" title="sort up"><img
                                src="${url.currentModule}/images/sort-arrow-up.png"
                                alt="up"/></a><a
                                title="sort down" href="#"> <img src="${url.currentModule}/images/sort-arrow-down.png"
                                                                 alt="down"/></a></th>
                        <th class="center" id="State" scope="col">State <a href="#" title="sort up"><img
                                src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a title="sort down"
                                                                                                     href="#"> <img
                                src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
                        <th class="center" id="Priority" scope="col">Priority <a href="#" title="sort up"><img
                                src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a title="sort down"
                                                                                                     href="#"> <img
                                src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
                        <th id="Tags" scope="col">Tags <a href="#" title="sort up"><img
                                src="${url.currentModule}/images/sort-arrow-up.png"
                                alt="up"/></a><a
                                title="sort down" href="#"> <img src="${url.currentModule}/images/sort-arrow-down.png"
                                                                 alt="down"/></a></th>
                        <th id="Date" scope="col">Due Date <a href="#" title="sort up"><img
                                src="${url.currentModule}/images/sort-arrow-up.png"
                                alt="up"/></a><a
                                title="sort down" href="#"> <img src="${url.currentModule}/images/sort-arrow-down.png"
                                                                 alt="down"/></a></th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${jcr:getNodes(currentNode,'jnt:task')}" var="task"
                               begin="1" varStatus="status">
                        <c:choose>
                            <c:when test="${status.count % 2 == 0}">
                                <tr class="odd">
                            </c:when>
                            <c:otherwise>
                                <tr class="even">
                            </c:otherwise>
                        </c:choose>
                        <td class="center" headers="Type"><img alt="" src="${url.currentModule}/images/flag_16.png"/>
                        </td>
                        <td headers="Title"><a href="${url.base}${task.path}.html">${task.properties.title.string}</a></td>
                        <td class="center" headers="State"><img alt=""
                                                                src="${url.currentModule}/images/right_16.png"/>${currentNode.properties.status.string}
                        </td>
                        <td class="center" headers="Priority">${currentNode.properties.priority.string}</td>
                        <td headers="Tags"></td>
                        <td headers="Date"><fmt:formatDate value="${currentNode.properties['dueDate'].date.time}"
                                                           dateStyle="short" type="date"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <div class="pagination"><!--start pagination-->
                    <div class="paginationPosition"><span>Page 2 of 2 - 450 results</span></div>
                    <div class="paginationNavigation"><a href="#" class="previousLink">Previous</a> <span><a href="#"
                                                                                                             class="paginationPageUrl">1</a></span>
                        <span><a href="#" class="paginationPageUrl">2</a></span> <span><a href="#"
                                                                                          class="paginationPageUrl">3</a></span>
                        <span><a href="#" class="paginationPageUrl">4</a></span> <span><a href="#"
                                                                                          class="paginationPageUrl">5</a></span>
                        <span class="currentPage">6</span> <a href="#" class="nextLink">Next</a></div>
                    <div class="clear"></div>
                </div>
                <!--stop pagination-->

                <template:module node="${currentNode}" forcedTemplate="newTask" />
