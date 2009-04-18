/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//
//
//
//
// 27.05.2002 NK Creation

package org.jahia.data.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ObjectKey;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaContainerManager;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.JahiaTools;
import org.springframework.context.ApplicationContext;

/**
 * Jahia Standard container filtering.
 * 
 * 
 * @see FilterClause
 * @see ContainerFilters
 * @see JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */
public class ContainerFilterBean implements Serializable,
        ContainerFilterInterface, MergeableFilter, ValueProviderFilter {

    private static final long serialVersionUID = -8051105649253889492L;

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(ContainerFilterBean.class);

    /**
     * "=" Comparator
     */
    public static final String COMP_EQUAL = "=";
/**
     * "<" Comparator
     */
    public static final String COMP_SMALLER = "<";
    /**
     * "<=" Comparator
     */
    public static final String COMP_SMALLER_OR_EQUAL = "<=";
    /**
     * ">=" Comparator
     */
    public static final String COMP_BIGGER_OR_EQUAL = ">=";
    /**
     * ">" Comparator
     */
    public static final String COMP_BIGGER = ">";

    public static final String COMP_NOT_EQUAL = "<>";

    public static final String COMP_NOTNULL = "NOTNULL";

    public static final String COMP_ISNULL = "ISNULL";

    public static final String COMP_STARTS_WITH = "STARTS_WITH";

    public static final String FIELD_ID = "b.comp_id.id";
    public static final String FIELD_VALUE = "b.value";
    public static final String FIELD_VERSION_ID = "b.comp_id.versionId";
    public static final String FIELD_WORKFLOW_STATE = "b.comp_id.workflowState";
    public static final String FIELD_LANGUAGE_CODE = "b.comp_id.languageCode";

    private static final String DELETED_FIELDS_QUERY = "select distinct b.comp_id.id from JahiaContainer a, JahiaFieldsData b where a.listid=(:ctnListId) and a.comp_id.id = b.containerId and b.comp_id.workflowState>1 and b.comp_id.versionId=-1";
    private static final String DELETED_CONTAINERS_IN_LIST_QUERY = "select distinct a.comp_id.id from JahiaContainer a where a.comp_id.workflowState>1 and a.comp_id.versionId=-1 and a.listid=(:ctnListId)";
    private static final String ALL_DELETED_CONTAINERS_QUERY = "select distinct a.comp_id.id from JahiaContainer a where a.comp_id.workflowState>1 and a.comp_id.versionId=-1";

    protected String fieldName;

    protected boolean numberFiltering = false;

    protected String numberFormat = NumberFormats.LONG_FORMAT;

    protected boolean multipleFieldValue = false;

    /** The list of FilterClause bean **/
    protected List<FilterClause> clauses = new ArrayList<FilterClause>();

    protected EntryLoadRequest entryLoadRequest = EntryLoadRequest.CURRENT;

    protected ContainerFilters containerFilters = null;

    protected JahiaContainerManager containerManager;

    protected ContainerFilterFieldValueProvider fieldValueProvider;

    protected boolean optimizedMode = true;

    protected boolean multiClauseANDLogic = false;

    public ContainerFilterBean() {
        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        containerManager = (JahiaContainerManager) context
                .getBean(JahiaContainerManager.class.getName());
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param fieldName
     *            , the field name of the field on which to apply filtering.
     * @deprecated use ContainerFilterBean(String fieldName, EntryLoadRequest entryLoadRequest)
     */
    public ContainerFilterBean(String fieldName) {
        this(fieldName, null);
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param fieldName
     *            , the field name of the field on which to apply filtering.
     * @param entryLoadRequest
     */
    public ContainerFilterBean(String fieldName,
            EntryLoadRequest entryLoadRequest) {
        this(fieldName, false, entryLoadRequest);
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param fieldName
     *            , the field name of the field on which to apply filtering.
     * @param numberFiltering
     *            , if true force to convert filed value to long representation
     * @deprecated use ContainerFilterBean(String fieldName, EntryLoadRequest entryLoadRequest)
     */
    public ContainerFilterBean(String fieldName, boolean numberFiltering) {
        this(fieldName, numberFiltering, null);
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param fieldName
     *            , the field name of the field on which to apply filtering.
     * @param numberFiltering
     *            , if true force to convert filed value to long representation
     * @param entryLoadRequest
     */
    public ContainerFilterBean(String fieldName, boolean numberFiltering,
            EntryLoadRequest entryLoadRequest) {
        this(fieldName, numberFiltering, false, entryLoadRequest);
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param fieldName
     *            , the field name of the field on which to apply filtering.
     * @param numberFiltering
     *            , if true force to convert filed value to long representation
     * @param multipleFieldValue
     * @param entryLoadRequest
     */
    public ContainerFilterBean(String fieldName, boolean numberFiltering,
            boolean multipleFieldValue, EntryLoadRequest entryLoadRequest) {
        this(fieldName, numberFiltering, null, multipleFieldValue,
                entryLoadRequest);
    }

    /**
     * 
     * @param fieldName
     * @param numberFiltering
     *            , if true force to convert filed value to number representation
     * @param numberFormat
     *            , only used if numberSort is true. If null, the format used is NumberFormat.LONG_FORMAT
     * @param multipleFieldValue
     * @param entryLoadRequest
     */
    public ContainerFilterBean(String fieldName, boolean numberFiltering,
            String numberFormat, boolean multipleFieldValue,
            EntryLoadRequest entryLoadRequest) {
        this();
        this.fieldName = fieldName;
        this.numberFiltering = numberFiltering;
        this.multipleFieldValue = multipleFieldValue;
        if (NumberFormats.isValidFormat(numberFormat)) {
            this.numberFormat = numberFormat;
        }
        if (entryLoadRequest != null) {
            this.entryLoadRequest = entryLoadRequest;
        }
        if (this.numberFiltering || this.multipleFieldValue) {
            this.optimizedMode = false;
        }
        logger.debug("Created with field name : " + fieldName);
    }

    // --------------------------------------------------------------------------
    /**
     * Add a simple comparison clause with a single value
     * 
     * <pre>
     *                                  		I.E : comparator = ContainerFilterBean.COMP_BIGGER (&gt;)
     *                       	  value	   	 = '1'
     * 
     *                       	  will be used to generate the WHERE clause :
     * 
     *                                                 			WHERE (fieldvalue&gt;'1')
     * 
     * </pre>
     * 
     * @param comparator
     *            , the comparator used to compareNumber the field value.
     * @param value
     *            , a single value
     */
    public void addClause(String comparator, String value) {

        if ((value == null) || !checkComparator(comparator))
            return;

        FilterClause fClause = new FilterClause(comparator, value);

        if (fClause.isValid()) {
            clauses.add(fClause);
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Add a simple comparison clause with multiple values
     * 
     * An OR comparison is added between each clause.
     * 
     * <pre>
     *                                  		I.E : comparator = ContainerFilterBean.COMP_EQUAL (=)
     *                       	  values	   = {'1','3','1000'}
     * 
     *                       	  will be used to generate the WHERE clause :
     * 
     *                                                 			WHERE (fieldvalue='1' OR fieldvalue='3' OR fieldvalue='1000')
     * 
     * </pre>
     * 
     * @param comparator
     *            , the comparator used to compareNumber the field value with each value of the values array.
     * @param values
     *            , an array of values as String
     */
    public void addClause(String comparator, String[] values) {

        if (values == null || (values.length == 0)
                || !checkComparator(comparator))
            return;

        FilterClause fClause = new FilterClause(comparator, values);

        if (fClause.isValid()) {
            clauses.add(fClause);
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Add a simple equality comparison clause with a single value
     * 
     * <pre>
     *                                  		I.E :
     *                       	  value	   	 = '1'
     * 
     *                       	  will be used to generate the WHERE clause :
     * 
     *                                                 			WHERE (fieldvalue='1')
     * 
     * </pre>
     * 
     * @param value
     *            , a single value
     */
    public void addEqualClause(String value) {

        if ((value == null))
            return;

        FilterClause fClause = new FilterClause(COMP_EQUAL, value);

        if (fClause.isValid()) {
            clauses.add(fClause);
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Add a simple equality comparison clause with multiple value
     * 
     * <pre>
     *                                  		I.E :
     *                       	  values	   = {'1','3','1000'}
     * 
     *                       	  will be used to generate the WHERE clause :
     * 
     *                                                 			WHERE (fieldvalue='1' OR fieldvalue='3' OR fieldvalue='1000')
     * 
     * </pre>
     * 
     * @param values
     *            , a single value
     */
    public void addEqualClause(String[] values) {

        if (values == null || (values.length == 0))
            return;

        FilterClause fClause = new FilterClause(COMP_EQUAL, values);

        if (fClause.isValid()) {
            clauses.add(fClause);
        }
    }

    // --------------------------------------------------------------------------
/**
     * Constructs a range clause matching values between
     * <code>lowerVal</code> and <code>upperVal</code>.
     *
     * <pre>
	 *                                  		I.E : lowerComp = ContainerFilterBean.COMP_BIGGER_OR_EQUAL (>=)
	 *                       	  upperComp = ContainerFilterBean.COMP_SMALLER (<)
	 *                       	  lowerVal	= '1'
	 *                       	  upperVal	= '1000'
     *
	 *                       	  will be used to generate the WHERE clause :
     *
	 *                                                 			WHERE (fieldvalue>='1' AND fielValue<'10001')
     *
     * </pre>
     *
     * @param lowerComp, the lower comparator
     * @param upperComp, the upper comparator
     * @param lowerVal, 	the lower value
     * @param upperVal, 	the upper value
     */
    public void addRangeClause(String lowerComp, String upperComp,
            String lowerVal, String upperVal) {

        if (lowerVal == null || upperVal == null || !checkComparator(lowerComp)
                || !checkComparator(upperComp)) {

            return;
        }

        FilterClause fClause = new FilterClause(lowerComp, upperComp, lowerVal,
                upperVal);

        if (fClause.isValid()) {
            clauses.add(fClause);
        }
    }

    // --------------------------------------------------------------------------
/**
     * Constructs a range clause matching date values between
     * <code>lower</code> and <code>upper</code>.
     *
     * Available only with field of type JahiaDateField ( date field ).
     *
     * <pre>
	 *                                  		I.E : lowerComp = ContainerFilterBean.COMP_SMALLER (<)
	 *                       	  upperComp = ContainerFilterBean.COMP_BIGGER_OR_EQUAL (>=)
	 *                       	  lowerVal	= '1020038400100' ( long representation )
	 *                       	  upperVal	= '1020038400000' ( long representation )
     *
	 *                       	  will be used to generate the WHERE clause :
     *
	 *                                                 			WHERE (fieldvalue<'1020038400100' AND fielValue>='1020038400000')
     *
     * </pre>
     *
     * @param lowerComp, the lower comparator
     * @param upperComp, the upper comparator
     * @param lowerVal,	the lower date
     * @param upperVal, 	the upper date
     */
    public void addDateClause(String lowerComp, String upperComp,
            Date lowerVal, Date upperVal) {

        if (lowerVal == null || upperVal == null || !checkComparator(lowerComp)
                || !checkComparator(upperComp)) {
            return;
        }

        addRangeClause(lowerComp, upperComp,
                String.valueOf(lowerVal.getTime()), String.valueOf(upperVal
                        .getTime()));
    }

    // --------------------------------------------------------------------------
/**
     * Constructs a range clause matching date values between
     * <code>lower</code> and <code>upper</code>.
     *
     * Available only with field of type JahiaDateField ( date field ).
     *
     * <pre>
	 *                                  		I.E : lowerComp = ContainerFilterBean.COMP_SMALLER (<)
	 *                       	  upperComp = ContainerFilterBean.COMP_BIGGER_OR_EQUAL (>=)
	 *                       	  lowerVal	= '1020038400100' ( long representation )
	 *                       	  upperVal	= '1020038400000' ( long representation )
     *
	 *                       	  will be used to generate the WHERE clause :
     *
	 *                                                 			WHERE (fieldvalue<'1020038400100' AND fielValue>='1020038400000')
     *
     * </pre>
     *
     * @param lowerComp, the lower comparator
     * @param upperComp, the upper comparator
     * @param lowerVal,	the lower date
     * @param upperVal, 	the upper date
     */
    public void addDateClause(String lowerComp, String upperComp,
            long lowerVal, long upperVal) {

        addRangeClause(lowerComp, upperComp, String.valueOf(lowerVal), String
                .valueOf(upperVal));
    }

    // --------------------------------------------------------------------------
    /**
     * Constructs a range clause matching date for X day ago.
     * 
     * Available only with field of type JahiaDateField ( date field ).
     * 
     * @param nbDays
     */
    public void addXDayMaxDateClause(int nbDays) {

        // Now
        long nowLong = System.currentTimeMillis();

        addDateClause(COMP_SMALLER_OR_EQUAL, COMP_BIGGER_OR_EQUAL, nowLong,
                nowLong - (nbDays * 24 * 60 * 60 * 1000L));
    }

    // --------------------------------------------------------------------------
    /**
     * Constructs a range clause matching date values that are in Today date.
     * 
     * Available only with field of type JahiaDateField ( date field ).
     * 
     */
    public void addTodayDateClause() {

        // Now
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Calendar cal = Calendar.getInstance(tz);
        Date nowDate = cal.getTime();
        long nowLong = nowDate.getTime();

        int year = cal.get(Calendar.YEAR);
        int mon = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(year, mon, date, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date todayDate = cal.getTime();
        long todayLong = todayDate.getTime();

        // Today
        addDateClause(COMP_SMALLER_OR_EQUAL, COMP_BIGGER_OR_EQUAL, nowLong,
                todayLong);
    }

    // --------------------------------------------------------------------------
    /**
     * Return the field name
     * 
     * @return String, the field name.
     */
    public String getFieldName() {
        return this.fieldName;
    }

    /**
     * If true an AND logic must be applied between all Clause otherwise an OR. False by default.
     * 
     * @return
     */
    public boolean isMultiClauseANDLogic() {
        return multiClauseANDLogic;
    }

    public void setMultiClauseANDLogic(boolean multiClauseANDLogic) {
        this.multiClauseANDLogic = multiClauseANDLogic;
    }

    // --------------------------------------------------------------------------
    /**
     * You can force field values to be corverted to long representation for filtering comparison Prefer String comparison when possible,
     * because it is faster ( only one DB query needed ).
     * 
     * @param val
     *            filtering status value.
     */
    public void setNumberFiltering(boolean val) {
        this.numberFiltering = val;
        if (this.numberFiltering) {
            this.optimizedMode = false;
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Return the number filtering status. If true, field values are converted to long representation before filtering comparison
     * 
     * @return boolean the number filtering status.
     */
    public boolean getNumberFiltering() {
        return this.numberFiltering;
    }

    public boolean isMultipleFieldValue() {
        return multipleFieldValue;
    }

    public void setMultipleFieldValue(boolean multipleFieldValue) {
        this.multipleFieldValue = multipleFieldValue;
        if (this.multipleFieldValue) {
            this.optimizedMode = false;
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Return the List of clauses.
     * 
     * @return List, the list of FilterClause bean.
     */
    public List<FilterClause> getClauses() {
        return this.clauses;
    }

    /**
     * An optional fiel value provider can be used to format field value to filter
     * 
     * @return
     */
    public ContainerFilterFieldValueProvider getFieldValueProvider() {
        return fieldValueProvider;
    }

    public void setFieldValueProvider(
            ContainerFilterFieldValueProvider fieldValueProvider) {
        this.fieldValueProvider = fieldValueProvider;
        if (this.fieldValueProvider != null) {
            this.optimizedMode = false;
        }
    }

    public boolean isOptimizedMode() {
        return optimizedMode;
    }

    public void setOptimizedMode(boolean optimizedMode) {
        this.optimizedMode = optimizedMode;
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering. The expected result is a bit set of matching container ids.
     * 
     * @param ctnListID
     *            , the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     */
    public BitSet doFilter(int ctnListID) throws JahiaException {
        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null)
                || (this.getClauses().isEmpty())) {
            return null;
        }

        BitSet result = null;

        if (this.getNumberFiltering()) {
            result = doNumberValueFiltering(ctnListID);
        } else if (!this.multipleFieldValue && this.optimizedMode) {
            result = this.doOptimizedFiltering(ctnListID);
        } else {
            result = this.doStringFiltering(ctnListID);
        }
        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * Perform the filtering in optimized mode.
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * @param ctnListID
     *            , the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     */
    private BitSet doOptimizedFiltering(int ctnListID) throws JahiaException {

        // JahiaConsole.println(CLASS_NAME+".doNumberValueFiltering","Started for ctnlist ["+ctnListID+"]");
        BitSet result = new BitSet();

        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null)
                || (this.getClauses().isEmpty())) {
            return null;
        }

        if (this.entryLoadRequest.isCurrent()) {
            return doOptimizedQueryFiltering(ctnListID, true, false);
        } else if (this.entryLoadRequest.isStaging()) {
            result = doOptimizedQueryFiltering(ctnListID, false, false);
            BitSet invertedStagingResult = doOptimizedQueryFiltering(ctnListID,
                    false, true);
            BitSet liveResult = doOptimizedQueryFiltering(ctnListID, true,
                    false);
            liveResult.andNot(invertedStagingResult);
            result.or(liveResult);
        }
        return result;
    }

    private BitSet doOptimizedQueryFiltering(int ctnListID,
            boolean useLiveMode, boolean invertComparator)
            throws JahiaException {
        BitSet result = new BitSet();
        StringBuffer buff = new StringBuffer(1024);
        Map<String, Object> parameters = new HashMap<String, Object>();

        List<Integer> fieldDefIDs = ServicesRegistry.getInstance()
                .getJahiaFieldService().getFieldDefinitionNameFromCtnType(this.fieldName,
                        false);
        if (fieldDefIDs == null || fieldDefIDs.isEmpty()) {
            return result;
        }

        buff.append(" SELECT f.containerId FROM JahiaFieldsData f ");
        buff.append(", JahiaContainer c ");
        parameters.put("ctListId", new Integer(ctnListID));
        buff.append(" WHERE (");
        if (!fieldDefIDs.isEmpty()) {
            buff.append(" f.fieldDefinition.id IN (:fieldDefIds) ");
            parameters.put("fieldDefIds", fieldDefIDs);
        }
        buff.append(" ) ");
        buff.append(" AND f.containerId= c.comp_id.id ");
        buff.append(" AND (");
        int index = 0;
        for (FilterClause clause : this.getClauses()) {
            if (clause.isValid()) {
                if (index > 0) {
                    if (this.isMultiClauseANDLogic()) {
                        buff.append(" AND ");
                    } else {
                        buff.append(" OR ");
                    }
                }
                if (clause.isRangeClause()) {
                    buff.append("f.value ").append(
                            FilterClause.getInvertedComp(clause.getLowerComp(),
                                    invertComparator)).append(
                            ":lowerFieldValue" + String.valueOf(index));
                    parameters.put("lowerFieldValue" + String.valueOf(index),
                            clause.getLowerValue());
                    buff.append(" AND ");
                    buff.append("f.value ").append(
                            FilterClause.getInvertedComp(clause.getUpperComp(),
                                    invertComparator)).append(
                            ":upperFieldValue" + String.valueOf(index));
                    parameters.put("upperFieldValue" + String.valueOf(index),
                            clause.getUpperValue());
                } else if (clause.getComp().equals(
                        ContainerFilterBean.COMP_STARTS_WITH)) {
                    String comp = " LIKE ";
                    if (invertComparator) {
                        comp = " NOT LIKE ";
                    }
                    buff.append(" ( ");
                    for (int i = 0; i < clause.getValues().length; i++) {
                        buff.append(getLikeComparison(clause.getValues()[i],
                                "f.value", comp));
                        if (i < clause.getValues().length - 1) {
                            if (invertComparator) {
                                buff.append(" AND ");
                            } else {
                                buff.append(" OR ");
                            }
                        }
                    }
                    buff.append(" ) ");
                } else {
                    buff.append(" ( ");
                    for (int i = 0; i < clause.getValues().length; i++) {
                        buff.append("f.value ").append(
                                FilterClause.getInvertedComp(clause.getComp(),
                                        invertComparator)).append(
                                ":fieldValue" + String.valueOf(index)
                                        + String.valueOf(i));
                        parameters.put("fieldValue" + String.valueOf(index)
                                + String.valueOf(i), clause.getValues()[i]);
                        if (i < clause.getValues().length - 1) {
                            if (invertComparator) {
                                buff.append(" AND ");
                            } else {
                                buff.append(" OR ");
                            }
                        }
                    }
                    buff.append(" ) ");
                }
                index++;                
            }
        }
        buff.append(") ");

        buff.append(" AND c.listid= :ctListId ");
        boolean stagingOnly = this.entryLoadRequest.isStaging();
        appendFieldMultilangAndWorkflowParams(buff, parameters,
                this.entryLoadRequest, useLiveMode, false, stagingOnly);
        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());
        List<Integer> queryResult = fieldMgr.executeQuery(buff.toString(),
                parameters);
        for (Integer ctnID : queryResult) {
            result.set(ctnID.intValue());
        }
        return result;
    }

    /**
     * 
     * @param query
     * @param params
     * @param entryLoadRequest
     * @param useLiveMode
     * @param ignoreLang
     * @param stagingOnly
     */
    protected void appendFieldMultilangAndWorkflowParams(StringBuffer query,
            Map<String, Object> params, EntryLoadRequest entryLoadRequest,
            boolean useLiveMode, boolean ignoreLang, boolean stagingOnly) {

        if (useLiveMode || entryLoadRequest.isCurrent()) {
            query.append(" AND f.comp_id.workflowState = ");
            query.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        } else if (entryLoadRequest.isStaging()) {
            query.append(" AND f.comp_id.workflowState > ");
            if (stagingOnly) {
                query.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
            } else {
                query.append(EntryLoadRequest.VERSIONED_WORKFLOW_STATE);
            }
        } else {
            query.append(" AND f.comp_id.versionId = ");
            query.append(entryLoadRequest.getVersionID());
        }
        if (!ignoreLang) {
            String languageCode = entryLoadRequest.getFirstLocale(true)
                    .toString();
            query.append(" AND (");
            query
                    .append("f.comp_id.languageCode = :languageCode OR f.comp_id.languageCode= :sharedLanguageCode ");
            params.put("languageCode", languageCode);
            params.put("sharedLanguageCode", ContentField.SHARED_LANGUAGE);
            query.append(" ) ");
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for all container list of the site.
     * 
     * @param ctnListID
     *            , the container list id
     * @return String , the sql statement. Null on error
     */
    public String getSelect(int ctnListID, int filterId, Map<String, Object> parameters) {
        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null) || (this.getClauses().isEmpty())) {
            return null;
        }

        StringBuffer buff = new StringBuffer(
                "select distinct a.comp_id.id, b.comp_id.id, b.comp_id.workflowState from JahiaContainer a, JahiaFieldsData b, JahiaFieldsDef c where ");
        buff
                .append(" a.listid=(:ctnListId_").append(filterId).append(") AND ( a.comp_id.id = b.containerId AND b.fieldDefinition.id = c.id AND c.ctnName=(:fieldName_").append(filterId).append(") and (");
        parameters.put("ctnListId_" + filterId, ctnListID);
        parameters.put("fieldName_" + filterId, this.getFieldName());
        int i = 0;
        for (FilterClause fClause : clauses) {
            if (fClause != null && fClause.isValid()) {
                if (i > 0) {
                    if (this.isMultiClauseANDLogic()) {
                        buff.append(" and ");
                    } else {
                        buff.append(" or ");
                    }
                }
                StringBuffer clauseBuff = new StringBuffer("(");
                if (!fClause.isRangeClause()) {
                    int j = 0;
                    for (String val : fClause.getValues()) {
                        if (j > 0) {
                            clauseBuff.append(" or ");
                        }
                        // TODO : hollis
                        // need to review encode/decode special char
                        clauseBuff.append("(");
                        clauseBuff
                                .append(buildMultilangAndWorlflowQuery(this.entryLoadRequest));
                        clauseBuff.append(" and ");
                        // SCSE: add STARTS_WITH comparator
                        if (COMP_STARTS_WITH.equals(fClause.getComp())) {
                            String lowerValue = val.toLowerCase();
                            clauseBuff.append(" lower(" + FIELD_VALUE
                                    + ") like (:lowerValue_" + filterId + "_" + i + ")");
                            parameters.put("lowerValue_" + filterId + "_" + i, lowerValue + "%");
                        } else {
                            clauseBuff.append(FIELD_VALUE);
                            clauseBuff.append(fClause.getComp());
                            clauseBuff.append("(:value_" + filterId + "_" + i + ")");
                            parameters.put("value_" + filterId + "_" + i, val);
                        }

                        clauseBuff.append(")");
                        j++;
                    }
                } else {
                    clauseBuff
                            .append(buildMultilangAndWorlflowQuery(this.entryLoadRequest));
                    clauseBuff.append(" and ");
                    clauseBuff.append(FIELD_VALUE);
                    clauseBuff.append(fClause.getLowerComp());
                    clauseBuff.append("(:lowerValue_" + filterId + "_" + i + ") and ");
                    clauseBuff.append(FIELD_VALUE);
                    clauseBuff.append(fClause.getUpperComp());
                    clauseBuff.append("(:upperValue_" + filterId + "_" + i + ") ");
                    parameters.put("lowerValue_" + filterId + "_" + i, fClause.getLowerValue());
                    parameters.put("upperValue_" + filterId + "_" + i, fClause.getUpperValue());
                }
                clauseBuff.append(")");
                buff.append(clauseBuff.toString());
                i++;                
            }
        }
        buff.append(")) order by ");
        buff.append(ContainerFilterBean.FIELD_ID);
        buff.append(",");
        buff.append(ContainerFilterBean.FIELD_WORKFLOW_STATE);

        logger.debug("field filter query : " + buff.toString());

        return buff.toString();
    }

    // --------------------------------------------------------------------------
    /**
     * Set reference to a containerFilters
     * 
     * @return
     * @throws JahiaException
     */
    public void setContainerFilters(ContainerFilters containerFilters) {
        this.containerFilters = containerFilters;
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering for NUMBER like fields. Container field values are loaded and converted to long representation before filtering
     * comparison.
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * @param ctnListID
     *            , the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     */
    private BitSet doNumberValueFiltering(int ctnListID) throws JahiaException {

        // JahiaConsole.println(CLASS_NAME+".doNumberValueFiltering","Started for ctnlist ["+ctnListID+"]");
        BitSet result = new BitSet();

        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null)
                || (this.getClauses().isEmpty())) {
            return null;
        }

        Map<Integer, String> datas = getFieldValues(ctnListID, fieldName);

        if (datas == null || datas.isEmpty()) {
            return result;
        }

        FilterClause fClause = null;

        Map<Integer, BitSet> bitsByClauseMap = new HashMap<Integer, BitSet>();

        for (Map.Entry<Integer, String> entry : datas.entrySet()) {
            boolean match = false;
            int i = 0;
            for (Iterator<FilterClause> it = this.clauses.iterator(); it.hasNext()
                    && (this.isMultiClauseANDLogic() || (!this
                            .isMultiClauseANDLogic() && !match));) {
                BitSet bitsByClause = (BitSet) bitsByClauseMap.get(new Integer(
                        i));
                if (bitsByClause == null) {
                    bitsByClause = new BitSet();
                    bitsByClauseMap.put(new Integer(i), bitsByClause);
                }
                fClause = it.next();
                if (fClause != null && fClause.isValid()) {
                    match = fClause.compareNumber(entry.getValue(),
                            this.numberFormat);
                    if (match) {
                        bitsByClause.set(entry.getKey().intValue());
                        result.set(entry.getKey().intValue());
                    }
                }
                i++;
            }
        }
        if (!this.isMultiClauseANDLogic()) {
            return result;
        } else {
            result = null;
            for (BitSet bitsByClause : bitsByClauseMap.values()) {
                if (result == null) {
                    result = bitsByClause;
                } else {
                    result.and(bitsByClause);
                }
            }
        }
        if (result == null) {
            result = new BitSet();
        }

        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering by loading field values in memories first.
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * @param ctnListID
     *            , the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     */
    private BitSet doStringFiltering(int ctnListID) throws JahiaException {

        // JahiaConsole.println(CLASS_NAME+".doNumberValueFiltering","Started for ctnlist ["+ctnListID+"]");
        BitSet result = new BitSet();

        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null)
                || (this.getClauses().isEmpty())) {
            return null;
        }

        Map<Integer, String> datas = getFieldValues(ctnListID, fieldName);

        if (datas == null || datas.isEmpty()) {
            return result;
        }

        FilterClause fClause = null;
        Map<Integer, BitSet> bitsByClauseMap = new HashMap<Integer, BitSet>();
        for (Map.Entry<Integer, String> entry : datas.entrySet()) {
            boolean match = false;
            String val = "";
            String[] vals = JahiaTools.getTokens(entry.getValue(),
                    JahiaField.MULTIPLE_VALUES_SEP);
            int nbVals = vals.length;
            int i = 0;
            while ((i < nbVals)
                    && (this.isMultiClauseANDLogic() || (!this
                            .isMultiClauseANDLogic() && !match))) {
                val = vals[i];
                int j = 0;
                for (Iterator<FilterClause> it = this.clauses.iterator(); it.hasNext() 
                        && (this.isMultiClauseANDLogic() || (!this
                                .isMultiClauseANDLogic() && !match));) {
                    BitSet bitsByClause = (BitSet) bitsByClauseMap
                            .get(new Integer(j));
                    if (bitsByClause == null) {
                        bitsByClause = new BitSet();
                        bitsByClauseMap.put(new Integer(j), bitsByClause);
                    }
                    fClause = it.next();
                    if (fClause != null && fClause.isValid()) {
                        match = fClause.compare(val);
                        if (match) {
                            result.set(entry.getKey().intValue());
                            bitsByClause.set(entry.getKey().intValue());
                        }
                    }
                    j++;
                }
                i++;
            }
        }
        if (!this.isMultiClauseANDLogic()) {
            return result;
        } else {
            result = null;
            for (BitSet bitsByClause : bitsByClauseMap.values()) {
                if (result == null) {
                    result = bitsByClause;
                } else {
                    result.and(bitsByClause);
                }
            }
        }
        if (result == null) {
            result = new BitSet();
        }

        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * Load an Map of pair/value (ctnID,fieldValue) for a given ctnlist and a given fieldName.
     * 
     * 
     * @param ctnListID
     *            , the container list id
     * @param fldName
     *            , the fieldName
     * @return Map.
     */
    private Map<Integer, String> getFieldValues(int ctnListID, String fldName)
            throws JahiaException {
        List<Integer> deletedCtns = getDeletedContainers(ctnListID);
        List<Integer> stagingFields = this.getStagingFields(ctnListID);
        Map<String, Object> parameters = new HashMap<String, Object>();
        StringBuffer buff = new StringBuffer(
                "select distinct b.containerId, b.comp_id.id, b.value, b.comp_id.workflowState from JahiaContainer a, JahiaFieldsData b, JahiaFieldsDef c where ");
        buff
                .append(" a.listid=(:ctnListId) AND ( a.comp_id.id = b.containerId AND b.fieldDefinition.id = c.id AND c.ctnName=(:fieldName) and (");
        buff.append(buildMultilangAndWorlflowQuery(this.entryLoadRequest))
                .append(
                        ") ) order by " + ContainerFilterBean.FIELD_ID + ","
                                + ContainerFilterBean.FIELD_WORKFLOW_STATE);
        parameters.put("ctnListId", ctnListID);
        parameters.put("fieldName", fldName);

        Map<Integer, String> datas = new HashMap<Integer, String>();
        Map<String, TempField> maps = new HashMap<String, TempField>();
        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        List<Object[]> queryResults = fieldMgr.executeQuery(buff.toString(),
                parameters);

        for (Object[] result : queryResults) {
            int ctnID = ((Integer) result[0]).intValue();
            int fieldID = ((Integer) result[1]).intValue();
            String fieldValue = (String) result[2];
            int workflowState = ((Integer) result[3]).intValue();

            if (this.entryLoadRequest.isCurrent()
                    || !deletedCtns.contains(new Integer(ctnID))) {
                if (workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    workflowState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                }
                if (this.fieldValueProvider != null) {
                    fieldValue = this.fieldValueProvider.getFieldValue(fieldID,
                            Jahia.getThreadParamBean(), this
                                    .getEntryLoadRequest(), fieldValue);
                    if (fieldValue == null) {
                        fieldValue = "";
                    }
                }
                TempField aField = new TempField(fieldID, ctnID, 0,
                        workflowState, "", fieldValue);
                String key = fieldID + "_" + workflowState;
                maps.put(key, aField);
            }
        }

        for (TempField aField : maps.values()) {
            if (aField.value != null) {
                if (this.entryLoadRequest.isCurrent()) {
                    datas.put(new Integer(aField.ctnID), aField.value);
                } else if (this.entryLoadRequest.isStaging()
                        && aField.workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    datas.put(new Integer(aField.ctnID), aField.value);
                } else if (aField.workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                        && !stagingFields.contains(new Integer(aField.id)) && !datas.containsKey(new Integer(aField.ctnID))) {
                    datas.put(new Integer(aField.ctnID), aField.value);
                }
            }
        }

        return datas;
    }

    // --------------------------------------------------------------------------
    /**
     * 
     * @return
     */
    public static String buildMultilangAndWorlflowQuery(
            EntryLoadRequest entryLoadRequest) {
        return buildMultilangAndWorlflowQuery(entryLoadRequest, false);
    }

    // --------------------------------------------------------------------------
    /**
     * 
     * @return
     */
    public static String buildMultilangAndWorlflowQuery(
            EntryLoadRequest entryLoadRequest, boolean ignoreLang) {
        return buildMultilangAndWorlflowQuery(entryLoadRequest, ignoreLang,
                false);
    }

    // --------------------------------------------------------------------------
    /**
     * 
     * @return
     */
    public static String buildMultilangAndWorlflowQuery(
            EntryLoadRequest entryLoadRequest, boolean ignoreLang,
            boolean stagingOnly) {

        StringBuffer strBuf = new StringBuffer(96);
        if (entryLoadRequest.isCurrent()) {
            strBuf.append(FIELD_WORKFLOW_STATE);
            strBuf.append(COMP_EQUAL);
            strBuf.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        } else if (entryLoadRequest.isStaging()) {
            strBuf.append(FIELD_WORKFLOW_STATE);
            strBuf.append(COMP_BIGGER);
            if (stagingOnly) {
                strBuf.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
            } else {
                strBuf.append(EntryLoadRequest.VERSIONED_WORKFLOW_STATE);
            }
            strBuf.append(" AND ");
            strBuf.append(FIELD_VERSION_ID);
            strBuf.append(" > -1 ");
        } else {
            strBuf.append(FIELD_VERSION_ID);
            strBuf.append("=");
            strBuf.append(entryLoadRequest.getVersionID());
        }
        if (!ignoreLang) {
            strBuf.append(" AND (");
            strBuf.append(FIELD_LANGUAGE_CODE);
            strBuf.append(" IN (");
            int i = 0;
            for (Locale locale : entryLoadRequest.getLocales()) {
                if (i > 0) {
                    strBuf.append(",");
                }
                strBuf.append("'").append(locale.toString()).append("'");
                i++;
            }
            if (i > 0) {
                strBuf.append(",");
            }
            strBuf.append("'").append(ContentField.SHARED_LANGUAGE).append("') ) ");
        }

        return strBuf.toString();

    }

    // --------------------------------------------------------------------------
    /**
     * 
     * @return
     */
    public static String buildMultilangAndWorlflowQuery(
            EntryLoadRequest entryLoadRequest, String fieldWorkflowName,
            String fieldLanguageCodeName, String fieldVersionName) {

        StringBuffer strBuf = new StringBuffer(" ");
        if (entryLoadRequest.isCurrent()) {
            strBuf.append(fieldWorkflowName);
            strBuf.append(COMP_EQUAL);
            strBuf.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        } else {
            strBuf.append(fieldWorkflowName);
            strBuf.append(COMP_BIGGER);
            strBuf.append(EntryLoadRequest.VERSIONED_WORKFLOW_STATE);
            strBuf.append(" AND ");
            strBuf.append(fieldVersionName);
            strBuf.append(" <> -1 ");
        }
        String languageCode = entryLoadRequest.getFirstLocale(true).toString();
        strBuf.append(" AND (");
        strBuf.append(fieldLanguageCodeName);
        strBuf.append("='");
        strBuf.append(JahiaTools.quote(languageCode));
        strBuf.append("' OR ");
        strBuf.append(fieldLanguageCodeName);
        strBuf.append("='");
        strBuf.append(ContentField.SHARED_LANGUAGE);
        strBuf.append("') ");
        return strBuf.toString();

    }

    // --------------------------------------------------------------------------
    /**
     * Returns an array of field ids that are workflow=staging and versionid=-1 (deleted)
     * 
     * @param ctnListID
     *            , the container list id
     * @return
     * @throws JahiaException
     */
    public static List<Integer> getDeletedFields(int ctnListID)
            throws JahiaException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ctnListId", ctnListID);

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        List<Integer> queryResults = fieldMgr.executeQuery(
                DELETED_FIELDS_QUERY, parameters);
        List<Integer> datas = new ArrayList<Integer>(queryResults);

        return datas;
    }

    // --------------------------------------------------------------------------
    /**
     * Returns an array of cntids that are workflow=staging and versionid=-1 (deleted)
     * 
     * @param ctnListID
     * @return
     * @throws JahiaException
     */
    public static List<Integer> getDeletedContainers(int ctnListID)
            throws JahiaException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ctnListId", ctnListID);

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        List<Integer> queryResults = fieldMgr.executeQuery(
                DELETED_CONTAINERS_IN_LIST_QUERY, parameters);

        List<Integer> datas = new ArrayList<Integer>(queryResults);

        return datas;
    }

    // --------------------------------------------------------------------------
    /**
     * Returns an array of cntids that are workflow=staging and versionid=-1 (deleted)
     * 
     * @param ctnListID
     * @return
     * @throws JahiaException
     */
    public static Set<Integer> getDeletedContainersSet(int ctnListID)
            throws JahiaException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ctnListId", ctnListID);

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        List<Integer> queryResults = fieldMgr.executeQuery(
                ctnListID > 0 ? DELETED_CONTAINERS_IN_LIST_QUERY
                        : ALL_DELETED_CONTAINERS_QUERY, parameters);
        Set<Integer> datas = new HashSet<Integer>(queryResults);
        return datas;
    }

    // --------------------------------------------------------------------------
    /**
     * Returns an array of fields that are in staging.
     * 
     * @param ctnListID
     * @return
     * @throws JahiaException
     */
    private List<Integer> getStagingFields(int ctnListID) throws JahiaException {
        if (this.containerFilters != null) {
            return this.containerFilters.getStagingFields(false, (String) null,
                    this.entryLoadRequest);
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ctnListId", ctnListID);
        
        StringBuffer buff = new StringBuffer("select distinct b.comp_id.id from JahiaContainer a, JahiaFieldsData b where a.listid=(:ctnListId) and a.comp_id.id = b.containerId and b.comp_id.workflowState>1 and a.comp_id.versionId=-1 and (");
        buff.append(buildMultilangAndWorlflowQuery(this.entryLoadRequest,
                false, true));
        buff.append(" )");

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        List<Integer> queryResults = fieldMgr.executeQuery(buff.toString(),
                parameters);

        return queryResults;
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * If siteId = -1 , returns results from all sites
     * 
     * If the containerDefinitionName is null, return result from all containers no regards to it definition !
     * 
     * @param siteId
     * @param containerDefinitionName
     * @param listId
     * @deprecated for oldMode support. Should use doFilterBySite(int[] siteIds, String[] containerDefinitionNames, int listId)
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(int siteId, String containerDefinitionName,
            int listId) throws JahiaException {
        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null)
                || (this.getClauses().isEmpty())) {
            return null;
        }

        BitSet result = null;

        Integer[] siteIds = null;
        if (siteId != -1) {
            siteIds = new Integer[] { new Integer(siteId) };
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && containerDefinitionName.trim().length() > 0) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        if (this.getNumberFiltering()) {
            result = doNumberValueFilteringBySite(siteIds,
                    containerDefinitionNames);
        } else if (this.optimizedMode) {
            result = this.doOptimizedFiltering(siteIds,
                    containerDefinitionNames);
        } else {
            result = this.doStringFiltering(siteIds, containerDefinitionNames);
        }
        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * @param siteIds
     *            if null or empty all sites are allowed
     * @param containerDefinitionNames
     *            if null or empty all definitions are allowed
     * @param listId
     * 
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(Integer[] siteIds,
            String[] containerDefinitionNames, int listId)
            throws JahiaException {
        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null)
                || (this.getClauses().isEmpty())) {
            return null;
        }

        BitSet result = null;

        if (this.getNumberFiltering()) {
            result = doNumberValueFilteringBySite(siteIds,
                    containerDefinitionNames);
        } else if (this.optimizedMode) {
            result = this.doOptimizedFiltering(siteIds,
                    containerDefinitionNames);
        } else {
            result = this.doStringFiltering(siteIds, containerDefinitionNames);
        }
        return result;
    }

    protected BitSet doOptimizedFiltering(Integer[] siteIds,
            String[] containerDefinitionNames) throws JahiaException {
        BitSet result = null;
        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null)
                || (this.getClauses().isEmpty())) {
            return null;
        }

        if (this.entryLoadRequest.isCurrent()) {
            return doOptimizedQueryFiltering(siteIds, containerDefinitionNames,
                    true, false);
        } else if (this.entryLoadRequest.isStaging()) {
            result = doOptimizedQueryFiltering(siteIds,
                    containerDefinitionNames, false, false);
            BitSet invertedStagingResult = doOptimizedQueryFiltering(siteIds,
                    containerDefinitionNames, false, true);
            BitSet liveResult = doOptimizedQueryFiltering(siteIds,
                    containerDefinitionNames, true, false);
            liveResult.andNot(invertedStagingResult);
            result.or(liveResult);
        }
        return result;
    }

    private BitSet doOptimizedQueryFiltering(Integer[] siteIDs,
            String[] containerDefinitionNames, boolean useLiveMode,
            boolean invertComparator) throws JahiaException {
        BitSet result = new BitSet();
        StringBuffer buff = new StringBuffer(1024);
        Map<String, Object> parameters = new HashMap<String, Object>();

        List<Integer> fieldDefIDs = ServicesRegistry.getInstance()
                .getJahiaFieldService().getFieldDefinitionNameFromCtnType(this.fieldName,
                        false);
        if (fieldDefIDs == null || fieldDefIDs.isEmpty()) {
            return result;
        }
        boolean allSitesAllowed = (siteIDs == null || siteIDs.length == 0);
        List<Integer> siteIDsList = new ArrayList<Integer>();
        if (!allSitesAllowed) {
            siteIDsList = Arrays.asList(siteIDs);
        }
        List<Integer> ctnDefIDs = null;
        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            ctnDefIDs = new ArrayList<Integer>();
            List<String> ctnDefNamesList = Arrays.asList(containerDefinitionNames);
            try {
                List<Integer> defIDs = ServicesRegistry.getInstance()
                        .getJahiaContainersService()
                        .getAllContainerDefinitionIDs();
                for (Integer defId : defIDs) {
                    JahiaContainerDefinition currentDefinition = ServicesRegistry
                            .getInstance().getJahiaContainersService()
                            .loadContainerDefinition(defId.intValue());
                    if (currentDefinition != null
                            && ctnDefNamesList.contains(currentDefinition
                                    .getName())) {
                        if (!allSitesAllowed) {
                            if (siteIDsList.contains(new Integer(
                                    currentDefinition.getJahiaID()))) {
                                ctnDefIDs.add(new Integer(currentDefinition
                                        .getID()));
                            }
                        } else {
                            ctnDefIDs
                                    .add(new Integer(currentDefinition.getID()));
                        }
                    }
                }
            } catch (Exception t) {
                logger.debug(
                        "Exception occured retrieving container definition", t);
                return result;
            }
            if (ctnDefIDs.isEmpty()) {
                return result;
            }
        }

        buff.append(" SELECT f.containerId FROM JahiaFieldsData f ");

        boolean addContainerDefinitionNameParam = false;
        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            addContainerDefinitionNameParam = true;
        }
        // if (addSiteParam || addContainerDefinitionNameParam){
        buff.append(", JahiaContainer c ");
        // }
        buff.append(" WHERE (");
        if (!fieldDefIDs.isEmpty()) {
            buff.append(" f.fieldDefinition.id IN (:fieldDefIds) ");
            parameters.put("fieldDefIds", fieldDefIDs);
        }
        buff.append(" ) ");
        buff.append(" AND f.containerId= c.comp_id.id ");
        buff.append(" AND (");
        int index = 0;
        for (FilterClause clause : this.getClauses()) {
            if (clause.isValid()) {
                if (index > 0) {
                    if (this.isMultiClauseANDLogic()) {
                        buff.append(" AND ");
                    } else {
                        buff.append(" OR ");
                    }
                }
                if (clause.isRangeClause()) {
                    buff.append("f.value ").append(
                            FilterClause.getInvertedComp(clause.getLowerComp(),
                                    invertComparator)).append(
                            ":lowerFieldValue" + String.valueOf(index));
                    parameters.put("lowerFieldValue" + String.valueOf(index),
                            clause.getLowerValue());
                    buff.append(" AND ");
                    buff.append("f.value ").append(
                            FilterClause.getInvertedComp(clause.getUpperComp(),
                                    invertComparator)).append(
                            ":upperFieldValue" + String.valueOf(index));
                    parameters.put("upperFieldValue" + String.valueOf(index),
                            clause.getUpperValue());
                } else if (clause.getComp().equals(
                        ContainerFilterBean.COMP_STARTS_WITH)) {
                    String comp = " LIKE ";
                    if (invertComparator) {
                        comp = " NOT LIKE ";
                    }
                    buff.append(" ( ");
                    for (int i = 0; i < clause.getValues().length; i++) {
                        buff.append(getLikeComparison(clause.getValues()[i],
                                "f.value", comp));
                        if (i < clause.getValues().length - 1) {
                            if (invertComparator) {
                                buff.append(" AND ");
                            } else {
                                buff.append(" OR ");
                            }
                        }
                    }
                    buff.append(" ) ");
                } else {
                    buff.append(" ( ");
                    for (int i = 0; i < clause.getValues().length; i++) {
                        buff.append("f.value ").append(
                                FilterClause.getInvertedComp(clause.getComp(),
                                        invertComparator)).append(
                                ":fieldValue" + String.valueOf(index)
                                        + String.valueOf(i));
                        parameters.put("fieldValue" + String.valueOf(index)
                                + String.valueOf(i), clause.getValues()[i]);
                        if (i < clause.getValues().length - 1) {
                            if (invertComparator) {
                                buff.append(" AND ");
                            } else {
                                buff.append(" OR ");
                            }
                        }
                    }
                    buff.append(" ) ");
                }
                index++;                
            }
        }
        buff.append(") ");

        if (!siteIDsList.isEmpty()) {
            if (siteIDsList.size() == 1) {
                buff.append(" AND c.siteId = (:siteID) ");
                parameters.put("siteID", siteIDsList.get(0).toString());
            } else {
                buff.append(" AND c.siteId IN (:siteIDsList) ");
                parameters.put("siteIDsList", siteIDsList);
            }
        }
        if (addContainerDefinitionNameParam && !ctnDefIDs.isEmpty()) {
            buff.append(" AND c.ctndef.id IN (:ctnDefIds)");
            parameters.put("ctnDefIds", ctnDefIDs);
        }
        boolean stagingOnly = this.entryLoadRequest.isStaging();
        appendFieldMultilangAndWorkflowParams(buff, parameters,
                this.entryLoadRequest, useLiveMode, false, stagingOnly);
        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());
        List<Integer> queryResult = fieldMgr.<Integer>executeQuery(buff.toString(), parameters);

        for (Integer ctnID : queryResult) {
            result.set(ctnID.intValue());
        }
        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for a given site. If siteId = -1 -> build query for all sites
     * 
     * If the containerDefinitionName is null, return result from all containers no regards to it definition !
     * 
     * @param siteId
     * @param containerDefinitionName
     * @deprecated for use with old mode
     * @return
     */
    public String getSelectBySiteID(int siteId, String containerDefinitionName, int filterId, Map<String, Object> parameters) {
        Integer[] siteIds = null;
        if (siteId != -1) {
            siteIds = new Integer[] { new Integer(siteId) };
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && containerDefinitionName.trim().length() > 0) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        return getSelectBySiteID(siteIds, containerDefinitionNames, filterId, parameters);
    }

    // --------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for a given site.
     * 
     * @param siteIds
     *            if null or empty all sites are allowed
     * @param containerDefinitionNames
     *            if null or empty all definitions are allowed.
     * @return
     */
    public String getSelectBySiteID(Integer[] siteIds,
            String[] containerDefinitionNames, int filterId, Map<String, Object> parameters) {

        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null)
                || (this.getClauses().isEmpty())) {
            return null;
        }
        
        StringBuffer buff = new StringBuffer(
                "select distinct b.containerId, b.comp_id.id, b.comp_id.workflowState from JahiaContainer a, JahiaFieldsData b, JahiaFieldsDef c, JahiaCtnDef d where ");

        if (siteIds != null && siteIds.length > 0) {
            if (siteIds.length == 1) {
                buff.append(" c.siteId = (:siteIds_").append(filterId).append(")");
                parameters.put("siteIds_" + filterId, siteIds[0]);
            } else {
                buff.append(" c.siteId in (:siteIds_").append(filterId).append(")");
                parameters.put("siteIds_" + filterId, siteIds);
            }
            buff.append(" and ");
        }

        buff.append(" ( a.comp_id.id = b.containerId and b.fieldDefinition.id = c.id and c.ctnName=(:fieldName").append(filterId).append(") ");
        parameters.put("fieldName_" + filterId, this.getFieldName());        

        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            buff.append(" and a.ctndef.id = d.id and d.name in (:defNames").append(filterId).append(") ");
            parameters.put("defNames_" + filterId, containerDefinitionNames);
        }

        buff.append(" and (");
        int i = 0;
        for (FilterClause fClause : this.clauses) {
            if (fClause != null && fClause.isValid()) {
                if (i > 0) {
                    if (this.isMultiClauseANDLogic()) {
                        buff.append(" and ");
                    } else {
                        buff.append(" or ");
                    }
                }
                StringBuffer clauseBuff = new StringBuffer("(");
                if (!fClause.isRangeClause()) {
                    int j = 0;
                    for (String val : fClause.getValues()) {
                        if (j > 0) {
                            clauseBuff.append(" or ");
                        }
                        // TODO : hollis
                        // need to review encode/decode special car
                        clauseBuff.append("(");
                        clauseBuff
                                .append(buildMultilangAndWorlflowQuery(this.entryLoadRequest));
                        clauseBuff.append(" and ");
                        // SCSE: add STARTS_WITH comparator
                        if (COMP_STARTS_WITH.equals(fClause.getComp())) {
                            String lowerValue = val.toLowerCase();

                            clauseBuff.append(" lower(" + FIELD_VALUE
                                    + ") like (:lowerValue_" + filterId + "_" + i + "_" + j + ")");
                            parameters.put("lowerValue_" + filterId + "_" + i + "_" + j, lowerValue + "%");         
                        } else {
                            clauseBuff.append(FIELD_VALUE);
                            clauseBuff.append(fClause.getComp());
                            clauseBuff.append("(:value_" + filterId + "_"+ i + "_" + j + ")");
                            parameters.put("value_" + filterId + "_" + i + "_" + j, val);
                        }
                        clauseBuff.append(")");
                        j++;
                    }
                    clauseBuff.append(")");
                } else {
                    clauseBuff
                            .append(buildMultilangAndWorlflowQuery(this.entryLoadRequest));
                    clauseBuff.append(" and ");
                    clauseBuff.append(FIELD_VALUE);
                    clauseBuff.append(fClause.getLowerComp());
                    clauseBuff.append("(:lowerValue_" + filterId + "_" + i + ") and ");
                    clauseBuff.append(FIELD_VALUE);
                    clauseBuff.append(fClause.getUpperComp());
                    clauseBuff.append("(:upperValue_" + filterId + "_" + i + ")");

                    parameters.put("lowerValue_" + filterId + "_" + i, fClause.getLowerValue());
                    parameters.put("upperValue_" + filterId + "_" + i, fClause.getUpperValue());                    
                }
                buff.append(clauseBuff.toString());
                i++;
            }
        }
        buff.append(")) order by ");
        buff.append(ContainerFilterBean.FIELD_ID);
        buff.append(",");
        buff.append(ContainerFilterBean.FIELD_WORKFLOW_STATE);
        if (logger.isDebugEnabled()) {
            logger.debug("field filter query : " + buff.toString());
        }

        return buff.toString();
    }

    public EntryLoadRequest getEntryLoadRequest() {
        return this.entryLoadRequest;
    }

    public boolean mergeAnd(ContainerFilterInterface filter) {
        if (!(filter instanceof ContainerFilterBean)) {
            return false;
        }
        ContainerFilterBean filterBean = (ContainerFilterBean) filter;
        if (this.getFieldName().equals(filterBean.getFieldName())) {
            if (this.isMultiClauseANDLogic() != filterBean
                    .isMultiClauseANDLogic()) {
                return false;
            } else if (this.isMultiClauseANDLogic()) {
                this.getClauses().addAll(filterBean.getClauses());
                return true;
            } else if (this.getClauses().size() == 1
                    && filterBean.getClauses().size() == 1) {
                this.setMultiClauseANDLogic(true);
                this.getClauses().addAll(filterBean.getClauses());
                return true;
            }
        }
        return false;
    }

    public boolean mergeOr(ContainerFilterInterface filter) {
        if (!(filter instanceof ContainerFilterBean)) {
            return false;
        }
        ContainerFilterBean filterBean = (ContainerFilterBean) filter;
        if (this.getFieldName().equals(filterBean.getFieldName())) {
            if (this.clauses.size() == 1 && filterBean.clauses.size() == 1) {
                FilterClause clause1 = (FilterClause) this.clauses.get(0);
                FilterClause clause2 = (FilterClause) filterBean.clauses.get(0);
                if (clause1.isNullOrNotNullClause()
                        && clause2.isNullOrNotNullClause()) {
                    return true;
                } else if (clause1.isRangeClause() || clause2.isRangeClause()) {
                    return false;
                } else if (clause1.getComp().equals(clause2.getComp())) {
                    String[] values1 = clause1.getValues();
                    String[] values2 = clause2.getValues();
                    List<String> values = new ArrayList<String>();
                    values.addAll(Arrays.asList(values1));
                    values.addAll(Arrays.asList(values2));
                    clause1 = new FilterClause(clause1.getComp(),
                            values.toArray(new String[] {}));
                    this.clauses.set(0, clause1);
                    return true;
                }
            }
        }
        return false;
    }

    // --------------------------------------------------------------------------
    /**
     * 
     * The expected result is a bit set of matching container ids for a given siteId. if siteId = -1 , return result from all sites
     * 
     * If the containerDefinitionName is null, return result from all containers no regards to it definition !
     * 
     * @param siteId
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     * @throws JahiaException
     */
    protected BitSet doQueryFilteringBySite(int siteId,
            String containerDefinitionName) throws JahiaException {
        Map<String, Object> parameters = new HashMap<String, Object>();        
        String fieldFilterQuery = this.getSelectBySiteID(siteId,
                containerDefinitionName, 0, parameters);
        if (StringUtils.isEmpty(fieldFilterQuery)) {
            return null;
        }

        BitSet bits = new BitSet();

        List<Integer> deletedCtns = containerManager
                .getDeletedContainerIdsBySiteAndCtnDef(new Integer(siteId),
                        containerDefinitionName);
        List<Integer> stagingFields = getStagingFieldsBySite(siteId,
                containerDefinitionName);
        Map<String, TempField> maps = new HashMap<String, TempField>();

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());
        List<Object[]> queryResults = fieldMgr.executeQuery(fieldFilterQuery,
                parameters);

        for (Object[] result : queryResults) {
            int ctnID = ((Integer) result[0]).intValue();
            int fieldID = ((Integer) result[1]).intValue();
            int workflowState = ((Integer) result[2]).intValue();
            if (this.entryLoadRequest.isCurrent()
                    || !deletedCtns.contains(new Integer(ctnID))) {
                if (workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    workflowState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                }
                TempField aField = new TempField(fieldID, ctnID, 0,
                        workflowState, "", "");
                String key = fieldID + "_" + workflowState;
                maps.put(key, aField);
            }
        }

        for (TempField aField : maps.values()) {
            if (this.entryLoadRequest.isCurrent()) {
                bits.set(aField.ctnID);
            } else if (this.entryLoadRequest.isStaging()
                    && aField.workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                bits.set(aField.ctnID);
            } else if (aField.workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                    && !stagingFields.contains(new Integer(aField.id))) {
                bits.set(aField.ctnID);
            }
        }
        return bits;
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering for NUMBER like fields. Container field values are loaded and converted to long representation before filtering
     * comparison.
     * 
     * The expected result is a bit set of matching container ids for a given siteId If siteId = -1 , return result from all sites
     * 
     * @param siteId
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     * @throws JahiaException
     */
    protected BitSet doNumberValueFilteringBySite(int siteId,
            String containerDefinitionName) throws JahiaException {
        Integer[] siteIds = null;
        if (siteId != -1) {
            siteIds = new Integer[] { new Integer(siteId) };
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && containerDefinitionName.trim().length() > 0) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        return doNumberValueFilteringBySite(siteIds, containerDefinitionNames);
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering for NUMBER like fields. Container field values are loaded and converted to long representation before filtering
     * comparison.
     * 
     * The expected result is a bit set of matching container ids for a given siteId
     * 
     * @param siteIds
     *            all sites are allowed if null or empty
     * @param containerDefinitionNames
     *            all definitions are allowed if null or empty
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     * @throws JahiaException
     */
    protected BitSet doNumberValueFilteringBySite(Integer[] siteIds,
            String[] containerDefinitionNames) throws JahiaException {

        BitSet result = new BitSet();

        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null)
                || (this.getClauses().isEmpty())) {
            return null;
        }

        Map<ObjectKey, String> datas = getFieldValuesBySite(siteIds, fieldName,
                containerDefinitionNames);

        if (datas == null || datas.isEmpty()) {
            return result;
        }

        Map<Integer, BitSet> bitsByClauseMap = new HashMap<Integer, BitSet>();
        for (Map.Entry<ObjectKey, String> entry : datas.entrySet()) {
            boolean match = false;
            String[] vals = JahiaTools.getTokens(entry.getValue(),
                    JahiaField.MULTIPLE_VALUES_SEP);
            int nbVals = vals.length;
            int i = 0;
            while ((i < nbVals)
                    && (this.isMultiClauseANDLogic() || (!this
                            .isMultiClauseANDLogic() && !match))) {
                String v = vals[i];
                int j = 0;
                for (Iterator<FilterClause> it = clauses.iterator(); it
                        .hasNext()
                        && (this.isMultiClauseANDLogic() || (!this
                                .isMultiClauseANDLogic() && !match));) {
                    BitSet bitsByClause = (BitSet) bitsByClauseMap
                            .get(new Integer(j));
                    if (bitsByClause == null) {
                        bitsByClause = new BitSet();
                        bitsByClauseMap.put(new Integer(j), bitsByClause);
                    }
                    FilterClause fClause = it.next();
                    if (fClause != null && fClause.isValid()) {
                        match = fClause.compareNumber(v, this.numberFormat);
                        if (match) {
                            result.set(entry.getKey().getIdInType());
                            bitsByClause.set(entry.getKey().getIdInType());
                        }
                    }
                    j++;
                }
                i++;
            }
        }
        if (!this.isMultiClauseANDLogic()) {
            return result;
        } else {
            result = null;
            for (BitSet bitsByClause : bitsByClauseMap.values()) {
                if (result == null) {
                    result = bitsByClause;
                } else {
                    result.and(bitsByClause);
                }
            }
        }
        if (result == null) {
            result = new BitSet();
        }

        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering by loading field values in memories first.
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * @param siteId
     * @param containerDefinitionName
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     */
    protected BitSet doStringFiltering(int siteId,
            String containerDefinitionName) throws JahiaException {
        Integer[] siteIds = null;
        if (siteId != -1) {
            siteIds = new Integer[] { new Integer(siteId) };
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && containerDefinitionName.trim().length() > 0) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        return doStringFiltering(siteIds, containerDefinitionNames);

    }

    // --------------------------------------------------------------------------
    /**
     * Perform filtering by loading field values in memories first.
     * 
     * The expected result is a bit set of matching container ids.
     * 
     * @param siteIds
     * @param containerDefinitionNames
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn
     *         ids.
     */
    protected BitSet doStringFiltering(Integer[] siteIds,
            String[] containerDefinitionNames) throws JahiaException {

        BitSet result = new BitSet();

        if ((this.getFieldName() == null)
                || (this.getFieldName().trim().length() == 0)
                || (this.getClauses() == null)
                || (this.getClauses().isEmpty())) {
            return null;
        }

        Map<ObjectKey, String> datas = this.getFieldValuesBySite(siteIds, fieldName,
                containerDefinitionNames);

        if (datas == null || datas.isEmpty()) {
            return result;
        }

        Map<Integer, BitSet> bitsByClauseMap = new HashMap<Integer, BitSet>();
        for (Map.Entry<ObjectKey, String> entry : datas.entrySet()) {
            boolean match = false;
            String val = "";
            String[] vals = JahiaTools.getTokens(entry.getValue(),
                    JahiaField.MULTIPLE_VALUES_SEP);
            int nbVals = vals.length;
            int i = 0;
            while ((i < nbVals)
                    && (this.isMultiClauseANDLogic() || (!this
                            .isMultiClauseANDLogic() && !match))) {
                val = vals[i];
                int j = 0;
                for (Iterator<FilterClause> it = this.clauses.iterator(); it.hasNext()
                        && (this.isMultiClauseANDLogic() || (!this
                                .isMultiClauseANDLogic() && !match));) {
                    BitSet bitsByClause = (BitSet) bitsByClauseMap.get(new Integer(j));
                    if (bitsByClause == null) {
                        bitsByClause = new BitSet();
                        bitsByClauseMap.put(new Integer(j), bitsByClause);
                    }
                    FilterClause fClause = it.next();
                    if (fClause != null && fClause.isValid()) {
                        match = fClause.compare(val);
                        if (match) {
                            result.set(entry.getKey().getIdInType());
                            bitsByClause.set(entry.getKey().getIdInType());
                        }
                    }
                    j++;
                }
                i++;
            }
        }
        if (!this.isMultiClauseANDLogic()) {
            return result;
        } else {
            result = null;
            for (BitSet bitsByClause : bitsByClauseMap.values()) {
                if (result == null) {
                    result = bitsByClause;
                } else {
                    result.and(bitsByClause);
                }
            }
        }
        if (result == null) {
            result = new BitSet();
        }
        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * Returns an array of cntids that are workflow=staging and versionid=-1 (deleted) for a given site
     * 
     * @param siteId
     *            If siteId = -1 , return deleted containers from all sites
     * @param containerDefinitionName
     *            if null or empty, all definitions allowed
     * @return
     * @throws JahiaException
     */
    public static List<Integer> getDeletedContainersBySite(int siteId,
            String containerDefinitionName) throws JahiaException {
        Integer[] siteIds = null;
        if (siteId != -1) {
            siteIds = new Integer[] { new Integer(siteId) };
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && containerDefinitionName.trim().length() > 0) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        return getDeletedContainersBySite(siteIds, containerDefinitionNames);
    }

    // --------------------------------------------------------------------------
    /**
     * Returns an array of cntids that are workflow=staging and versionid=-1 (deleted) for a given site
     * 
     * @param siteIds
     *            if null or empty all sites are allowed
     * @param containerDefinitionNames
     *            if null or empty all definition are allowed
     * @return
     * @throws JahiaException
     */
    public static List<Integer> getDeletedContainersBySite(Integer[] siteIds,
            String[] containerDefinitionNames) throws JahiaException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        
        StringBuffer buff = new StringBuffer("select distinct a.comp_id.id from JahiaContainer a, JahiaCtnDef b where a.comp_id.workflowState>1 and a.comp_id.versionId=-1");
        if (siteIds != null && siteIds.length > 0) {
            buff.append(" and a.siteId in (:siteIds) ");
            parameters.put("siteIds", siteIds);
        }

        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            buff.append(" AND a.ctndef.id = b.id and b.name in (:defNames) ");
            parameters.put("defNames", containerDefinitionNames);            
        }

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        List<Integer> queryResults = fieldMgr.executeQuery(buff.toString(),
                parameters);

        return queryResults;
    }

    // --------------------------------------------------------------------------
    /**
     * Returns an array of fields that are in staging.
     * 
     * If siteId = -1 , return result from all sites
     * 
     * @param siteId
     * @return
     * @throws JahiaException
     */
    protected List<Integer> getStagingFieldsBySite(int siteId,
            String containerDefinitionName) throws JahiaException {
        Integer[] siteIds = null;
        if (siteId != -1) {
            siteIds = new Integer[] { new Integer(siteId) };
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null
                && containerDefinitionName.trim().length() > 0) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        return getStagingFieldsBySite(siteIds, containerDefinitionNames);
    }

    // --------------------------------------------------------------------------
    /**
     * Returns an array of fields that are in staging.
     * 
     * @param siteIds
     *            if null or empty all sites allowed
     * @param containerDefinitionNames
     *            if null or empty all definitions allowed
     * @return
     * @throws JahiaException
     */
    protected List<Integer> getStagingFieldsBySite(Integer[] siteIds,
            String[] containerDefinitionNames) throws JahiaException {
        if (this.containerFilters != null) {
            return this.containerFilters.getStagingFields(false,
                    containerDefinitionNames, this.entryLoadRequest);
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        StringBuffer buff = new StringBuffer(
                "select distinct b.comp_id.id from JahiaContainer a, JahiaFieldsData b, JahiaCtnDef c where ");
        if (siteIds != null && siteIds.length > 0) {
            buff.append(" a.siteId in (:siteIds) and ");
            parameters.put("siteIds", siteIds);
        }

        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            buff.append(" a.ctndef.id = c.id and c.name in (:defNames) and ");
            parameters.put("defNames", containerDefinitionNames);
        }

        buff.append(" a.comp_id.id=b.containerId AND b.comp_id.workflowState>1 and (");
        buff.append(buildMultilangAndWorlflowQuery(entryLoadRequest, false,
                true));
        buff.append(" )");

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        List<Integer> queryResults = fieldMgr.executeQuery(buff.toString(),
                parameters);
        return queryResults;
    }

    // --------------------------------------------------------------------------
    /**
     * Load an Map of pair/value (ctnID,fieldValue) for a given ctnlist and a given fieldName.
     * 
     * If siteId = -1 , return result from all sites
     * 
     * @param siteId
     * @param fieldName
     * @param containerDefinitionName
     * @return
     * @throws JahiaException
     */
    protected Map<ObjectKey, String> getFieldValuesBySite(int siteId, String fieldName,
            String containerDefinitionName) throws JahiaException {
        Integer[] siteIDs = null;
        String[] containerDefinitionNames = null;
        if (siteId != -1) {
            siteIDs = new Integer[] { new Integer(siteId) };
        }
        if (containerDefinitionName != null
                && containerDefinitionName.trim().length() > 0) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        return getFieldValuesBySite(siteIDs, fieldName,
                containerDefinitionNames);
    }

    // --------------------------------------------------------------------------
    /**
     * Load an Map of pair/value (ctnID,fieldValue) for a given ctnlist and a given fieldName.
     * 
     * If siteId = -1 , return result from all sites
     * 
     * @param siteIds
     *            all sites allowed if null or empty
     * @param fieldName
     * @param containerDefinitionNames
     *            all definitions allowed if null or empty
     * @return
     * @throws JahiaException
     */
    protected Map<ObjectKey, String> getFieldValuesBySite(Integer[] siteIds,
            String fieldName, String[] containerDefinitionNames)
            throws JahiaException {

        List<Integer> deletedCtns = getDeletedContainersBySite(siteIds,
                containerDefinitionNames);
        List<Integer> stagingFields = this.getStagingFieldsBySite(siteIds,
                containerDefinitionNames);

        Map<String, Object> parameters = new HashMap<String, Object>();
        StringBuffer buff = new StringBuffer(
                "select distinct b.containerId, b.comp_id.id, b.value, b.comp_id.workflowState from JahiaContainer a, JahiaFieldsData b, JahiaFieldsDef c, JahiaCtnDef d where ");

        if (siteIds != null && siteIds.length > 0) {
            buff.append(" a.siteId in (:siteIds) and ");
            parameters.put("siteIds", siteIds);
        }

        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            buff.append(" a.ctndef.id = d.id and d.name in (:defNames) and ");
            parameters.put("defNames", containerDefinitionNames);
        }

        buff.append(" a.comp_id.id = b.containerId AND b.fieldDefinition.id = c.id AND c.ctnName=(:fieldName) and (");
        parameters.put("fieldName", fieldName);
        buff.append(buildMultilangAndWorlflowQuery(this.entryLoadRequest));
        buff.append(") order by ");
        buff.append(ContainerFilterBean.FIELD_ID);
        buff.append(",");
        buff.append(ContainerFilterBean.FIELD_WORKFLOW_STATE);

        Map<String, TempField> maps = new HashMap<String, TempField>();

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        List<Object[]> queryResults = fieldMgr.executeQuery(buff.toString(),
                parameters);

        for (Object[] result : queryResults) {
            int ctnID = ((Integer) result[0]).intValue();
            int fieldID = ((Integer) result[1]).intValue();
            String fieldValue = (String) result[2];
            int workflowState = ((Integer) result[3]).intValue();

            if (!this.entryLoadRequest.isCurrent()
                    && !(this.entryLoadRequest.isStaging() && workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE)
                    && !(workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE && !stagingFields
                            .contains(new Integer(ctnID)))) {
                continue;
            }

            if (this.entryLoadRequest.isCurrent()
                    || !deletedCtns.contains(new Integer(ctnID))) {
                if (workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    workflowState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                }

                if (this.fieldValueProvider != null) {
                    fieldValue = this.fieldValueProvider.getFieldValue(fieldID,
                            Jahia.getThreadParamBean(), this
                                    .getEntryLoadRequest(), fieldValue);
                    if (fieldValue == null) {
                        fieldValue = "";
                    }
                }

                TempField aField = new TempField(fieldID, ctnID, 0,
                        workflowState, "", fieldValue);
                String key = fieldID + "_" + workflowState;
                maps.put(key, aField);
            }
        }

        Map<ObjectKey, String> datas = new HashMap<ObjectKey, String>();
        for (TempField aField : maps.values()) {
            if (aField.value != null) {
                datas.put(new ContentContainerKey(aField.ctnID), aField.value);
            }
        }
        return datas;
    }

    // --------------------------------------------------------------------------
    /**
     * Check if the comparator is valid
     * 
     * @param comparator
     *            the comparator
     */
    private boolean checkComparator(String comparator) {
        if (comparator == null)
            return false;
        return (comparator.equals(COMP_EQUAL)
                || comparator.equals(COMP_SMALLER)
                || comparator.equals(COMP_SMALLER_OR_EQUAL)
                || comparator.equals(COMP_BIGGER_OR_EQUAL)
                || comparator.equals(COMP_BIGGER)
                || comparator.equals(COMP_NOTNULL)
                || comparator.equals(COMP_ISNULL)
                || comparator.equals(COMP_NOT_EQUAL) || comparator
                .equals(COMP_STARTS_WITH));
    }

    // -------------------------------------------------------------------------

    private class TempField {
        public int id;
        public int ctnID;
        public int versionID;
        public int workflowState;
        public String languageCode;
        public String value;

        public TempField(int id, int ctnID, int versionID, int workflowState,
                String languageCode, String value) {
            this.id = id;
            this.ctnID = ctnID;
            this.versionID = versionID;
            this.workflowState = workflowState;
            this.languageCode = languageCode;
            this.value = value;
        }
    }

    protected static String getLikeComparison(String value, String fieldName,
            String operator) {
        String lowerValue = value.toLowerCase();
        StringBuffer clauseBuff = new StringBuffer();

        clauseBuff.append(" lower(" + fieldName + ") ").append(operator)
                .append(" '");
        clauseBuff.append(JahiaTools.quote(lowerValue));
        clauseBuff.append("%'");
        return clauseBuff.toString();
    }
}
