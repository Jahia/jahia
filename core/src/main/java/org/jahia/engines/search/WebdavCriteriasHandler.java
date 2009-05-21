/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
