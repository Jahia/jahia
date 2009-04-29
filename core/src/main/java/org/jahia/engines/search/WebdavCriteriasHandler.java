/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.engines.search;

import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.JahiaTools;
import org.jahia.engines.calendar.CalendarHandler;

import java.util.*;

/**
 * @author Khue NGuyen
 */
public class WebdavCriteriasHandler {

    // search operation
    public static final String MSG = "WebdavCriteriasHandler.msgs";
    public static final String OPERATION_PARAM = "operation";
    public static final String ADD_OPERATION = "add";
    public static final String SAVE_OPERATION = "save";
    public static final String DELETE_OPERATION = "delete";

    public static final String OPERATION_VIEW = "WebdavCriteriasHandler.view";

    private List<CriteriaBean> criterias = new ArrayList<CriteriaBean>();
    private String operation;

    public WebdavCriteriasHandler(){
    }

    /**
     *
     * @param jParams
     * @param engineMap
     * @throws org.jahia.exceptions.JahiaException
     */
    public void handleActions (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {

        operation = JahiaTools.getStrParameter(jParams, WebdavCriteriasHandler.OPERATION_PARAM,"");
        engineMap.put(WebdavCriteriasHandler.OPERATION_VIEW,"save");

        engineMap.remove(WebdavCriteriasHandler.MSG);
        if ( WebdavCriteriasHandler.ADD_OPERATION.equals(operation) ) {
            handleAddCriteria(jParams,engineMap);
        } else if ( WebdavCriteriasHandler.SAVE_OPERATION.equals(operation) ){
            handleSaveCriteria(jParams,engineMap);
        } else if ( WebdavCriteriasHandler.DELETE_OPERATION.equals(operation) ){
            handleDeleteCriteria(jParams,engineMap);
        }
    }

    /**
     *
     * @param jParams
     * @param engineMap
     * @throws org.jahia.exceptions.JahiaException
     */
    public void handleAddCriteria (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {

        int order = 1;
        if ( !this.criterias.isEmpty() ){
            CriteriaBean criteria = this.criterias.get(this.criterias.size()-1);
            order = criteria.getOrder() + 1;
        }
        CriteriaBean criteriaBean = new CriteriaBean("none","","",order,false,false);
        this.criterias.add(criteriaBean);
    }

    /**
     *
     * @param jParams
     * @param engineMap
     * @throws org.jahia.exceptions.JahiaException
     */
    public void handleSaveCriteria (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {


        Iterator<CriteriaBean> iterator = this.criterias.iterator();
        CriteriaBean criteriaBean = null;
        boolean propertyNameChanged = false;
        while ( iterator.hasNext() ){
            propertyNameChanged = false;
            criteriaBean = iterator.next();
            String value = jParams.getParameter("criteriaName_"+criteriaBean.getOrder());
            if ( value != null ){
                propertyNameChanged = !criteriaBean.getName().equals(value);
                criteriaBean.setName(value.trim());
            }
            value = jParams.getParameter("criteriaComparator_"+criteriaBean.getOrder());
            if ( value != null ){
                criteriaBean.setComparator(value.trim());
            }
            value = jParams.getParameter("criteriaValue_"+criteriaBean.getOrder());
            if ( value != null ){
                criteriaBean.setValue(value.trim());
            }
            value = jParams.getParameter("calendar_"+criteriaBean.getOrder()+"Timestamp");
            if ( value != null ){
                criteriaBean.setDate(true);
                if ( propertyNameChanged ){
                    criteriaBean.setValue("");
                } else {
                    // it's a date criteria
                    CalendarHandler criteriaCalHandler =
                        new CalendarHandler(org.jahia.settings.SettingsBean.getInstance().getJahiaEnginesHttpPath(),
                                        "calendar_"+criteriaBean.getOrder(),
                                        "advSearchForm",
                                        CalendarHandler.DEFAULT_DATE_FORMAT,
                                        0L,
                                        jParams.getLocale(),
                                        0L);
                    criteriaCalHandler.update(jParams);
                    criteriaBean.setValue(String.valueOf(criteriaCalHandler.getDateLong().longValue()));
                }
            } else {
                criteriaBean.setDate(false);
            }
        }
    }

    /**
     *
     * @param jParams
     * @param engineMap
     * @throws org.jahia.exceptions.JahiaException
     */
    public void handleDeleteCriteria (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {
        String item = jParams.getParameter("criteriaItem");
        if ( item != null ){
            try {
                int itemOrder = Integer.parseInt(item);
                Iterator<CriteriaBean> it = this.criterias.iterator();
                CriteriaBean cr = null;
                int index = 0;
                while ( it.hasNext() ){
                    cr = it.next();
                    if ( cr.getOrder() == itemOrder ){
                        this.criterias.remove(index);
                    }
                    index++;
                }
            } catch ( Exception t ){
            }
        }
    }

    public List<CriteriaBean> getCriterias() {
        return criterias;
    }

    public void setCriterias(List<CriteriaBean> criterias) {
        this.criterias = criterias;
    }

    protected CriteriaBean getCriteria(int order){
        Iterator<CriteriaBean> it = this.criterias.iterator();
        CriteriaBean cr = null;
        while ( it.hasNext() ){
            cr = it.next();
            if ( cr.getOrder() == order ){
                return cr;
            }
        }
        return null;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
