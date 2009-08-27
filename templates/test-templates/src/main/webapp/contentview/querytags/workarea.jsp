<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="javax.jcr.PropertyType"%>
<%@page import="org.jahia.data.beans.SiteBean"%>
<%@page import="org.jahia.params.ParamBean"%>
<%@page import="org.jahia.registries.ServicesRegistry"%>
<%@page import="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"%>
<%@page import="org.jahia.services.content.nodetypes.NodeTypeRegistry"%>
<%@page import="org.jahia.services.pages.ContentPage"%>
<%@taglib prefix="s" uri="http://www.jahia.org/tags/search" %>

<%@include file="../../common/declarations.jspf"%>

<%
    ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    NodeTypeRegistry ntRegistry = NodeTypeRegistry.getInstance();
    Map<String, ExtendedPropertyDefinition> metadataPropertyDefinitions = ntRegistry.getNodeType("mix:lastModified").getDeclaredPropertyDefinitionsAsMap();
    metadataPropertyDefinitions.putAll(ntRegistry.getNodeType("mix:created").getDeclaredPropertyDefinitionsAsMap());
    metadataPropertyDefinitions.putAll(ntRegistry.getNodeType("mix:createdBy").getDeclaredPropertyDefinitionsAsMap());    
    Map<String, ExtendedPropertyDefinition> propertyDefinitions = ntRegistry.getNodeType("test_templates:allFields").getDeclaredPropertyDefinitionsAsMap();
    List<String> propertyNames = new ArrayList<String>(propertyDefinitions.keySet());
    Collections.sort(propertyNames);
    List<String> allPropertyNames = new ArrayList<String>(propertyDefinitions.keySet());
    Set<String> cleanedMetadataNames = new HashSet<String>();
    for (String metadataPropertyName : metadataPropertyDefinitions.keySet()) {
        int index = metadataPropertyName.lastIndexOf(':');
        cleanedMetadataNames.add(index == -1 ? metadataPropertyName : metadataPropertyName.substring(index));
    }
    allPropertyNames.addAll(cleanedMetadataNames);
    Collections.sort(allPropertyNames);    
%>
<%!
public boolean isNumeric (String propertyName, Map<String, ExtendedPropertyDefinition> propertyDefinitions) {
    boolean isNumeric = false;
    ExtendedPropertyDefinition propertyDef = propertyDefinitions.get(propertyName);
    
    if (propertyDef != null && propertyDef.getRequiredType() == PropertyType.DATE
        || propertyDef.getRequiredType() == PropertyType.BOOLEAN
        || propertyDef.getRequiredType() == PropertyType.DOUBLE || propertyDef
        .getRequiredType() == PropertyType.LONG) {
      isNumeric = true;
    }

    return isNumeric;
}
%>

<c:set var="propertyDefinitions" value="<%=propertyDefinitions%>"/>
<c:set var="propertyNames" value="<%=propertyNames%>"/>
<c:set var="allPropertyNames" value="<%=allPropertyNames%>"/>

<c:set var="params" value="${paramValues.listCategories}" scope="request"/>
<c:set var="listCategories" value="${fn:join(params, ',')}" scope="request"/>
<c:set var="createdDateFilter" value="${param.createdDateFilter}" scope="request"/>
<c:set var="modifiedDateFilter" value="${param.modifiedDateFilter}" scope="request"/>

<div class="expectedResultTitle">
  <fmt:message key="label.expected.result"/>:
</div> 
<div class="expectedResult">
  <fmt:message key="description.template.queryTags.expectedResult">
    <fmt:param><fmt:message key="submit"/></fmt:param>
  </fmt:message>
</div>
<div>
  <template:jahiaPageForm name="searchForm" method="post">
    <fieldset>
      <legend>&nbsp;<fmt:message key="label.querySettings"/>&nbsp;</legend>
      <div class="search">
        <div class="searchCriteria">
          <fmt:message key="label.queryOn"/><br/>
          <input type="radio" name="searchFilter" value="database" <c:if test="${param.searchFilter == 'database'}">checked</c:if>/> <fmt:message key="label.searchFilter.database"/><br/>
          <input type="radio" name="searchFilter" value="lucene" <c:if test="${param.searchFilter == 'lucene'}">checked</c:if>/> <fmt:message key="label.searchFilter.lucene"/><br/>
          <input type="radio" name="searchFilter" value="automatic" <c:if test="${param.searchFilter != 'database' && param.searchFilter != 'lucene'}">checked</c:if>/> <fmt:message key="label.searchFilter.automatic"/><br/>          
        
          <fmt:message key="label.maxHits"/>
          <input type="text" name="maxHits" value="${param.maxHits}"/><br/>
          <fmt:message key="label.useBackendCache"/>          
          <input type="checkbox" name="useBackendCache" value="true" <c:if test="${param.useBackendCache == 'true'}">checked</c:if>/><br/>
          
          <fmt:message key="label.language"/>&nbsp;<s:language/><br/>
          
          <fmt:message key="label.sortByProperty"/>
          <select name="sortProperty">
            <option value=""/>          
            <c:forEach items="${allPropertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.sortProperty}">selected</c:if>>${property}</option>
            </c:forEach>
          </select><br/>
          <fmt:message key="label.sortOrder"/><br/>
          <input type="radio" name="sortOrder" value="${queryConstants.ORDER_ASCENDING}" <c:if test="${param.sortOrder != queryConstants.ORDER_DESCENDING}">checked</c:if>/> <fmt:message key="label.sortOrder.ascending"/><br/>
          <input type="radio" name="sortOrder" value="${queryConstants.ORDER_DESCENDING}" <c:if test="${param.sortOrder == queryConstants.ORDER_DESCENDING}">checked</c:if>/> <fmt:message key="label.sortOrder.descending"/><br/>          
          <fmt:message key="label.localeSensitiveSorting"/>          
          <input type="checkbox" name="localeSensitiveSorting" value="true" <c:if test="${param.localeSensitiveSorting == 'true'}">checked</c:if>/><br/>          
        </div>  
      </div>    
    </fieldset>  
    <fieldset>
      <legend>&nbsp;<fmt:message key="label.allFieldsContainerLocationSelection"/>&nbsp;</legend>
      <div class="search">
        <div class="searchCriteria">
          <input type="radio" name="location" value="onPage" <c:if test="${param.location == 'onPage'}">checked</c:if>> <fmt:message key="label.onPage"/><br>
          <input type="radio" name="location" value="onPageAndSubpages" <c:if test="${param.location == 'onPageAndSubpages'}">checked</c:if>> <fmt:message key="label.onPageAndSubpages"/><br>
          <input type="radio" name="location" value="onSite" <c:if test="${param.location != 'onPageAndSubpages' && param.location != 'onPage'}">checked</c:if>> <fmt:message key="label.onSite"/><br>
        
          <fmt:message key="label.pageKey"/>
          <input type="text" name="pageKey" value="${param.pageKey}"/><br/>
          <fmt:message key="label.pageId"/>
          <input type="text" name="pageId" value="${param.pageId}"/><br/>
          <fmt:message key="label.siteKey"/>
          <input type="text" name="querySiteKey" value="${param.querySiteKey}"/><br/>          
        </div>  
      </div>    
    </fieldset>
    
    <fieldset>
      <legend>&nbsp;<fmt:message key="label.fixedComparison"/>&nbsp;</legend>
      <div class="search">
        <div class="searchCriteria">
          <fmt:message key="label.propertyName"/>    
          <select name="equalToProperty">
            <option value=""/>
            <c:forEach items="${propertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.equalToProperty}">selected</c:if>>${property}</option>
            </c:forEach>
          </select>
          <fmt:message key="label.query.equalTo"/>
          <input type="text" name="equalToValue" value="${param.equalToValue}"/><br/>

          <fmt:message key="label.propertyName"/>    
          <select name="notEqualToProperty">
            <option value=""/>
            <c:forEach items="${propertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.notEqualToProperty}">selected</c:if>>${property}</option>
            </c:forEach>
          </select>
          <fmt:message key="label.query.notEqualTo"/>
          <input type="text" name="notEqualToValue" value="${param.notEqualToValue}"/><br/>

          <fmt:message key="label.propertyName"/>    
          <select name="greaterThanProperty">
            <option value=""/>          
            <c:forEach items="${propertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.greaterThanProperty}">selected</c:if>>${property}</option>
            </c:forEach>
          </select>
          <fmt:message key="label.query.greaterThan"/>
          <input type="text" name="greaterThanValue" value="${param.greaterThanValue}"/><br/>
                    
          <fmt:message key="label.propertyName"/>    
          <select name="greaterThanOrEqualToProperty">
            <option value=""/>          
            <c:forEach items="${propertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.greaterThanOrEqualToProperty}">selected</c:if>>${property}</option>
            </c:forEach>
          </select>
          <fmt:message key="label.query.greaterThanOrEqualTo"/>
          <input type="text" name="greaterThanOrEqualToValue" value="${param.greaterThanOrEqualToValue}"/><br/>
          
          <fmt:message key="label.propertyName"/>    
          <select name="lessThanProperty">
            <option value=""/>          
            <c:forEach items="${propertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.lessThanProperty}">selected</c:if>>${property}</option>
            </c:forEach>
          </select>
          <fmt:message key="label.query.lessThan"/>
          <input type="text" name="lessThanValue" value="${param.lessThanValue}"/><br/>
          
          <fmt:message key="label.propertyName"/>    
          <select name="lessThanOrEqualToProperty">
            <option value=""/>          
            <c:forEach items="${propertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.lessThanOrEqualToProperty}">selected</c:if>>${property}</option>
            </c:forEach>
          </select>
          <fmt:message key="label.query.lessThanOrEqualTo"/>
          <input type="text" name="lessThanOrEqualToValue" value="${param.lessThanOrEqualToValue}"/><br/>  
          
          <fmt:message key="label.propertyName"/>    
          <select name="likeProperty">
            <option value=""/>          
            <c:forEach items="${propertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.likeProperty}">selected</c:if>>${property}</option>
            </c:forEach>
          </select>
          <fmt:message key="label.query.like"/>
          <input type="text" name="likeValue" value="${param.likeValue}"/><br/>                  
        </div> 
      </div>           
    </fieldset>    
    
    <fieldset>
      <legend>&nbsp;<fmt:message key="label.dynamicComparison"/>&nbsp;</legend>
      <div class="search">
        <div class="searchCriteria">
          <fmt:message key="label.propertyName"/>    
          <select name="dynamic1Property">
            <option value=""/>          
            <c:forEach items="${propertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.dynamic1Property}">selected</c:if>>${property}</option>
            </c:forEach>
          </select>
          <select name="dynamic1Operator">
            <option value=""/>          
            <c:forEach items="${queryConstants.OPERATOR_LABELS}" var="operator">
              <option value="${operator.key}" <c:if test="${operator.key == param.dynamic1Operator}">selected</c:if>>${operator.value}</option>
            </c:forEach>
          </select>
          <input type="text" name="dynamic1Value" value="${param.dynamic1Value}"/><br/>
          
          <fmt:message key="label.propertyName"/>    
          <select name="dynamic2Property">
            <option value=""/>          
            <c:forEach items="${propertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.dynamic2Property}">selected</c:if>>${property}</option>
            </c:forEach>
          </select>
          <select name="dynamic2Operator">
            <option value=""/>          
            <c:forEach items="${queryConstants.OPERATOR_LABELS}" var="operator">
              <option value="${operator.key}" <c:if test="${operator.key == param.dynamic2Operator}">selected</c:if>>${operator.value}</option>
            </c:forEach>
          </select>
          <input type="text" name="dynamic2Value" value="${param.dynamic2Value}"/><br/>
          
          <fmt:message key="label.propertyName"/>    
          <select name="dynamic3Property">
            <option value=""/>          
            <c:forEach items="${propertyNames}" var="property">
              <option value="${property}" <c:if test="${property == param.dynamic3Property}">selected</c:if>>${property}</option>
            </c:forEach>
          </select>
          <select name="dynamic3Operator">
            <option value=""/>          
            <c:forEach items="${queryConstants.OPERATOR_LABELS}" var="operator">
              <option value="${operator.key}" <c:if test="${operator.key == param.dynamic3Operator}">selected</c:if>>${operator.value}</option>
            </c:forEach>
          </select>
          <input type="text" name="dynamic3Value" value="${param.dynamic3Value}"/><br/>          
        </div> 
      </div>
    </fieldset>  
    
    <fieldset>
      <legend>&nbsp;<fmt:message key="label.fullTextSearch"/>&nbsp;</legend>
      <div class="search">
        <div class="searchCriteria">
          <fmt:message key="label.fullTextSearchTerm"/>   
          <input type="text" name="fullTextSearchTerm" value="${param.fullTextSearchTerm}"/><br/>
        </div> 
      </div>
    </fieldset>
        
    <fieldset>
        <legend><fmt:message key="label.metadata"/></legend>
	    <fmt:message key="label.metadata.author"/>&nbsp;<s:createdBy/><br/>
	    <fmt:message key="label.metadata.createdDate"/>&nbsp;
	    <ui:dateSelector cssClassName="dateSelection" fieldName="createdDateFilter" value="${createdDateFilter}"/><br/>
	    <fmt:message key="label.metadata.lastEditor"/>&nbsp;<s:lastModifiedBy/><br/>
        <fmt:message key="label.metadata.modifiedDate"/>&nbsp;
	    <ui:dateSelector cssClassName="dateSelection" fieldName="modifiedDateFilter" value="${modifiedDateFilter}"/><br/>
        <fmt:message key="label.metadata.category"/>
        <query:categoryFilter startingCategoryKey=""
            display="selectBoxMultiple"
            name="listCategories"
            level="1"
            messageKey="noCategories"
            selected="${listCategories}"
            displayRootCategory="false"
            cssClassName="categorySelection"/><br/>
    </fieldset>
    
    <input type="submit" name="submitbutton" class="button"
        value="<fmt:message key="submit"/>"/>
                    
    <template:containerList name="allFieldsWithList" id="testContainerList" displayActionMenu="false">
      <query:containerQuery> 
        <query:selector selectorName="allFields" nodeTypeName="test_templates:allFields"/>
        <c:choose>
          <c:when test="${!empty param.pageId}">
            <c:set var="queryPageJCRPath" value='<%=ContentPage.getPage(Integer.parseInt(request.getParameter("pageId"))).getJCRPath(jParams)%>'/>
          </c:when>
          <c:when test="${!empty param.pageKey}">
            <c:set var="queryPageJCRPath"
                value='<%=ContentPage.getPage(
                    ServicesRegistry.getInstance().getJahiaPageService().getPageIDByURLKeyAndSiteID(
                    request.getParameter("pageKey"), request.getParameter("querySiteKey") != null && request.getParameter("querySiteKey").trim().length() > 0 ? ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(request.getParameter("querySiteKey")).getID() : jParams.getSiteID())).getJCRPath(jParams)%>' />
          </c:when>
        </c:choose>     

        <c:choose>
          <c:when test="${param.location == 'onPage'}">
            <query:childNode path="${queryPageJCRPath}"/>
          </c:when>
          <c:when test="${param.location == 'onPageAndSubpages'}">
            <query:descendantNode path="${queryPageJCRPath}"/>
          </c:when>        
          <c:when test="${param.location == 'onSite'}">
            <c:choose>
              <c:when test="${!empty param.querySiteKey}">
                <c:set var="querySite" value='<%=new SiteBean(ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(request.getParameter("querySiteKey")), jParams)%>'/>              
              </c:when>
              <c:otherwise>
                <c:set var="querySite" value="${currentSite}"/>
              </c:otherwise>
            </c:choose>  
            <query:descendantNode path="${querySite.JCRPath}"/>          
          </c:when>
        </c:choose>

        <c:choose>
          <c:when test="${param.searchFilter == 'database'}">
            <query:setProperty name="${queryConstants.FILTER_CREATORS}" value="JahiaDBFilterCreator"/>
            <query:setProperty name="${queryConstants.DB_MAX_RESULT}" value="${param.maxHits}"/>          
          </c:when>
          <c:when test="${param.searchFilter == 'lucene'}">
            <query:setProperty name="${queryConstants.FILTER_CREATORS}" value="JahiaSearchFilterCreator"/>
            <query:setProperty name="${queryConstants.SEARCH_MAX_HITS}" value="${param.maxHits}"/>
          </c:when>
          <c:otherwise>
            <query:setProperty name="${queryConstants.DB_MAX_RESULT}" value="${param.maxHits}"/>
            <query:setProperty name="${queryConstants.SEARCH_MAX_HITS}" value="${param.maxHits}"/>
          </c:otherwise>                  
        </c:choose>   
        
        <query:setProperty name="${queryConstants.USE_BACKEND_CACHE}" value="${param.useBackendCache}"/>  

        <query:setProperty name="${queryConstants.LANGUAGE_CODES}">
          <c:forTokens items="${src_languages.values}" delims="," var="lang">
            <query:propertyValue value="${lang}"/>
          </c:forTokens>  
        </query:setProperty>
        
        <c:if test="${!empty listCategories}">
          <query:equalTo propertyName="${queryConstants.CATEGORY_LINKS}" value="${listCategories}"
              metadata="true" multiValue="true"/>
        </c:if>              

        <c:if test="${!empty param.equalToProperty && !empty param.equalToValue}">
          <query:equalTo propertyName="${param.equalToProperty}" value="${param.equalToValue}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("equalToProperty"), propertyDefinitions))%>'/>
        </c:if>
        <c:if test="${!empty param.notEqualToProperty && !empty param.notEqualToValue}">
          <query:notEqualTo propertyName="${param.notEqualToProperty}" value="${param.notEqualToValue}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("notEqualToProperty"), propertyDefinitions))%>'/>
        </c:if>        
        <c:if test="${!empty param.greaterThanProperty && !empty param.greaterThanValue}">        
          <query:greaterThan propertyName="${param.greaterThanProperty}" value="${param.greaterThanValue}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("greaterThanProperty"), propertyDefinitions))%>'/>
        </c:if>
        <c:if test="${!empty param.greaterThanOrEqualToProperty && !empty param.greaterThanOrEqualToValue}">          
          <query:greaterThanOrEqualTo propertyName="${param.greaterThanOrEqualToProperty}" value="${param.greaterThanOrEqualToValue}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("greaterThanOrEqualToProperty"), propertyDefinitions))%>'/>
        </c:if>
        <c:if test="${!empty param.lessThanProperty && !empty param.lessThanValue}">          
          <query:lessThan propertyName="${param.lessThanProperty}" value="${param.lessThanValue}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("lessThanProperty"), propertyDefinitions))%>'/>
        </c:if>
        <c:if test="${!empty param.lessThanOrEqualToProperty && !empty param.lessThanOrEqualToValue}">          
          <query:lessThanOrEqualTo propertyName="${param.lessThanOrEqualToProperty}" value="${param.lessThanOrEqualToValue}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("lessThanOrEqualToProperty"), propertyDefinitions))%>'/>
        </c:if>
        <c:if test="${!empty param.likeProperty && !empty param.likeValue}">          
          <query:like propertyName="${param.likeProperty}" value="${param.likeValue}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("likeProperty"), propertyDefinitions))%>'/>
        </c:if>

        <c:if test="${!empty param.dynamic1Property && !empty param.dynamic1Operator && !empty param.dynamic1Value}">
          <query:comparison propertyName="${param.dynamic1Property}" operator="${param.dynamic1Operator}" value="${param.dynamic1Value}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("dynamic1Property"), propertyDefinitions))%>'/>
        </c:if>
        <c:if test="${!empty param.dynamic2Property && !empty param.dynamic2Operator && !empty param.dynamic2Value}">
          <query:comparison propertyName="${param.dynamic2Property}" operator="${param.dynamic2Operator}" value="${param.dynamic2Value}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("dynamic2Property"), propertyDefinitions))%>'/>
        </c:if>
        <c:if test="${!empty param.dynamic3Property && !empty param.dynamic3Operator && !empty param.dynamic3Value}">
          <query:comparison propertyName="${param.dynamic3Property}" operator="${param.dynamic3Operator}" value="${param.dynamic3Value}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("dynamic3Property"), propertyDefinitions))%>'/>
        </c:if>
        
        <c:if test="${!empty param.src_createdBy}">          
          <query:like propertyName="createdBy" value="${param.src_createdBy}" metadata="true"/>
        </c:if>                
        <c:if test="${!empty param.src_lastEditor}">          
          <query:like propertyName="lastModifiedBy" value="${param.src_lastEditor}" metadata="true"/>
        </c:if>        
        <c:if test="${!empty createdDateFilter}">
          <utility:dateUtil currentDate="${createdDateFilter}" var="createdDateFrom" hours="0" minutes="0"
              seconds="0"/>
          <utility:dateUtil currentDate="${createdDateFilter}" var="createdDateUntil" days="1" hours="0" minutes="0"
              seconds="0"/>
          <query:greaterThan propertyName="created" value="${createdDateFrom.time}" numberValue="true" />
          <query:lessThanOrEqualTo propertyName="created" value="${createdDateUntil.time}" numberValue="true" />
        </c:if>        
        <c:if test="${!empty modifiedDateFilter}">
          <utility:dateUtil currentDate="${modifiedDateFilter}" var="modifiedDateFrom" hours="0" minutes="0"
              seconds="0"/>
          <utility:dateUtil currentDate="${modifiedDateFilter}" var="modifiedDateTo" days="1" hours="0" minutes="0"
              seconds="0"/>
          <query:greaterThan propertyName="lastModified" value="${modifiedDateFrom.time}" numberValue="true" />
          <query:lessThanOrEqualTo propertyName="lastModified" value="${modifiedDateTo.time}" numberValue="true" />
        </c:if>
        
        <c:if test="${!empty param.fullTextSearchTerm}">        
            <query:fullTextSearch searchExpression="${param.fullTextSearchTerm}"/>
        </c:if>    
         
<%--
        <query:not/>
        <query:or/>
--%>

        <c:if test="${!empty param.sortProperty}">
          <query:sortBy propertyName="${param.sortProperty}" order="${param.sortOrder}" localeSensitive="${param.localeSensitiveSorting}" numberValue='<%=String.valueOf(isNumeric(request.getParameter("sortProperty"), propertyDefinitions))%>'  metadata="${propertyDefinitions[param.sortProperty] == null}"/>
        </c:if>          
      </query:containerQuery>
      
      <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true">
        <jsp:param name="1_listName" value="allFieldsWithSubList"/>
        <jsp:param name="2_listName" value="allFieldsWithSubSubList"/>
        <jsp:param name="3_listName" value="allFields"/>
        <jsp:param name="1_actionMenuName" value="testSubContainers"/>
        <jsp:param name="2_actionMenuName" value="testSubSubContainers"/>
        <jsp:param name="3_actionMenuName" value="testSubSubSubContainers"/>    
      </jsp:include>  
    </template:containerList>
  </template:jahiaPageForm>  
</div>