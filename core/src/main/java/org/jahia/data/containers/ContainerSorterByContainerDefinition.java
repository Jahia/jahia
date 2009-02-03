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
//
// 29.05.2002 NK added in Jahia


package org.jahia.data.containers;

import java.io.Serializable;
import java.text.Collator;
import java.util.*;

import org.jahia.bin.Jahia;
import org.jahia.data.fields.ExpressionMarker;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaContainerManager;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.resourcebundle.ResourceBundleMarker;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.comparator.NumericStringComparator;
import org.springframework.context.ApplicationContext;

/**
 * Jahia Standard Containers Sort Handler sorts the list of containers, defined by one or more definition
 * names and with the given field name(s).
 *
 * The sorting is done according to the values in the given field name(s) in alphanumeric or numeric in
 * descending or ascending order.
 *
 * @see ContainerSorterByContainerDefinition
 * @see JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 * @author MC
 */

public class ContainerSorterByContainerDefinition implements Serializable, ContainerSorterInterface,
        ValueProviderSorter {

    private static final long serialVersionUID = 3435781167635642116L;

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContainerSorterByContainerDefinition.class);

    protected int listId = -1;

    protected Integer[] siteIds;

    protected String[] fieldNames;

    protected String[] containerDefinitionNames;

    protected boolean updated = false;

    protected long lastSortingTime = -1;

    protected boolean numberSort = false;

    private String numberFormat = NumberFormats.LONG_FORMAT;

    protected boolean isValid = false;

    protected boolean ASC_Ordering = true; // by default ASCENDANT ORDER.

    //** sorted ctnids **/
    protected List<Integer> result;

    protected ContainerSorterFieldValueProvider fieldValueProvider;

    protected EntryLoadRequest entryLoadRequest = EntryLoadRequest.CURRENT;

    protected String contextID = "";

    protected boolean optimizedMode = true;

    protected int dbMaxResult = -1;

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param siteId
     * @param fieldName, field name which containers must have and on which sorting is carried out
     * @param containerDefinitionName, definition name which containers must have
     * @throws JahiaException
     * @deprecated, inverted numbersort and entryLoadRequest parameter
     *
     */
    public ContainerSorterByContainerDefinition(int siteId, String fieldName,
            String containerDefinitionName, EntryLoadRequest entryLoadRequest,
            boolean numberSort)
    throws JahiaException
    {
        this(siteId,new String[]{fieldName},new String[]{containerDefinitionName},numberSort,null,entryLoadRequest);
    }

    /**
     *
     *
     * @param siteId
     * @param fieldNames, the container must have one of the field listed in this array.
     * The sorting will be carried out on these fields.
     * @param containerDefinitionName, definition name which containers must have
     * @throws JahiaException
     * @deprecated, inverted numbersort and entryLoadRequest parameter
     *
     */
    public ContainerSorterByContainerDefinition(int siteId, String[] fieldNames,
            String containerDefinitionName, EntryLoadRequest entryLoadRequest,
            boolean numberSort)
    throws JahiaException
    {
       this(siteId,fieldNames,new String[]{containerDefinitionName},numberSort,null,entryLoadRequest);
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @param siteId
     * @param fieldName, field name which containers must have and on which sorting is carried out
     * @param containerDefinitionName, definition name which containers must have
     * @param numberSort
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterByContainerDefinition(int siteId, String fieldName,
            String containerDefinitionName, boolean numberSort,
            EntryLoadRequest entryLoadRequest)
    throws JahiaException
    {
        this(siteId,new String[]{fieldName},new String[]{containerDefinitionName},numberSort,null,entryLoadRequest);
    }


    /**
     *
     * @param siteId
     * @param fieldName, field name which containers must have and on which sorting is carried out
     * @param containerDefinitionName, definition name which containers must have
     * @param numberSort force field values to be converted to number representation before sorting ( if true ).
     * @param numberFormat, only used if numberSort is true. If null, the format used is NumberFormat.LONG_FORMAT
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterByContainerDefinition(int siteId, String fieldName,
            String containerDefinitionName,
            boolean numberSort,
            String numberFormat,
            EntryLoadRequest entryLoadRequest )
        throws JahiaException
    {
       this(siteId,new String[]{fieldName},new String[]{containerDefinitionName},numberSort,numberFormat,entryLoadRequest);
    }

    /**
        *
        * @param siteId
        * @param fieldNames, the container must have one of the field listed in this array.
        * The sorting will be carried out on these fields.
        * @param containerDefinitionName, definition name which containers must have
        * @param numberSort force field values to be converted to number representation before sorting ( if true ).
        * @param numberFormat, only used if numberSort is true. If null, the format used is NumberFormat.LONG_FORMAT
        * @param entryLoadRequest
        * @throws JahiaException
     */
    public ContainerSorterByContainerDefinition(int siteId, String[] fieldNames,
            String containerDefinitionName,
            boolean numberSort,
            String numberFormat,
            EntryLoadRequest entryLoadRequest )
    throws JahiaException
    {
        this(siteId,fieldNames,new String[]{containerDefinitionName},numberSort,numberFormat,entryLoadRequest);
    }

    /**
     *
     * @param siteId
     * @param fieldNames, sorting can be done over more than one field (as long as they are the same type)
     * @param containerDefinitionNames, containers must be of one of these container definition names.
     * @param numberSort force field values to be converted to number representation before sorting ( if true ).
     * @param numberFormat, only used if numberSort is true. If null, the format used is NumberFormat.LONG_FORMAT
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterByContainerDefinition(int siteId, String[] fieldNames,
            String[] containerDefinitionNames,
            boolean numberSort,
            String numberFormat,
            EntryLoadRequest entryLoadRequest )
    throws JahiaException
    {

        //Must have at least one field correctly defined
        for (int i = 0; i < fieldNames.length; i++) {
            if (fieldNames[i] != null && !fieldNames[i].trim().equals("")) {
                //Must have at least one container definition correctly defined
                for (int d = 0; d < containerDefinitionNames.length; d++) {
                    if (containerDefinitionNames[d] != null &&
                        !containerDefinitionNames[d].trim().equals("")) {
                        if (siteId > 0){
                            this.siteIds = new Integer[]{new Integer(siteId)};
                        }
                        this.fieldNames = fieldNames;
                        this.containerDefinitionNames = containerDefinitionNames;
                        this.isValid = true;
                        this.numberSort = numberSort;
                        if (NumberFormats.isValidFormat(numberFormat)) {
                            this.numberFormat = numberFormat;
                        }
                        break;
                    }
                }
                if (this.isValid) break;
            }
        }
    /*  WAS: if ( fieldName != null && !fieldName.trim().equals("") )
        {
            this.siteId = siteId;
            this.fieldName = fieldName;
            this.containerDefinitionName = containerDefinitionName;
            this.isValid = true;
            this.numberSort = numberSort;
            if ( NumberFormats.isValidFormat(numberFormat) ){
                this.numberFormat = numberFormat;
            }
        }*/
        if (entryLoadRequest != null){
            this.entryLoadRequest = entryLoadRequest;
        }
        if ( this.numberSort ){
            this.optimizedMode = false;
        }
    }

    /**
     *
     * @param siteIds
     * @param fieldNames, sorting can be done over more than one field (as long as they are the same type)
     * @param containerDefinitionNames, containers must be of one of these container definition names.
     * @param numberSort force field values to be converted to number representation before sorting ( if true ).
     * @param numberFormat, only used if numberSort is true. If null, the format used is NumberFormat.LONG_FORMAT
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterByContainerDefinition(List<Integer> siteIds, String[] fieldNames,
            List<String> containerDefinitionNames,
            boolean numberSort,
            String numberFormat,
            EntryLoadRequest entryLoadRequest )
    throws JahiaException
    {
        //Must have at least one field correctly defined
        for (int i = 0; i < fieldNames.length; i++) {
            if (fieldNames[i] != null && !fieldNames[i].trim().equals("")) {
                //Must have at least one container definition correctly defined
                if (siteIds != null && !siteIds.isEmpty()){
                    this.siteIds = siteIds.toArray(new Integer[]{});
                }
                if (containerDefinitionNames != null && !containerDefinitionNames.isEmpty()){
                    this.containerDefinitionNames = new String[containerDefinitionNames.size()];
                    int j = 0;
                    for (String containerDefName :containerDefinitionNames){
                        this.containerDefinitionNames[j] = containerDefName;
                        j++;
                    }
                }
                this.fieldNames = fieldNames;
                this.isValid = true;
                this.numberSort = numberSort;
                if (NumberFormats.isValidFormat(numberFormat)) {
                    this.numberFormat = numberFormat;
                }
                if (this.isValid) break;
            }
        }
    /*  WAS: if ( fieldName != null && !fieldName.trim().equals("") )
        {
            this.siteId = siteId;
            this.fieldName = fieldName;
            this.containerDefinitionName = containerDefinitionName;
            this.isValid = true;
            this.numberSort = numberSort;
            if ( NumberFormats.isValidFormat(numberFormat) ){
                this.numberFormat = numberFormat;
            }
        }*/
        if (entryLoadRequest != null){
            this.entryLoadRequest = entryLoadRequest;
        }
        if ( this.numberSort ){
            this.optimizedMode = false;
        }

    }

    public int getCtnListID () {
        return this.listId;
    }

    public void setCtnListID (int id) {
        this.listId = id;
    }

    //--------------------------------------------------------------------------
    /**
     * Do the sort. Optionally, you can provide a BitSet where each bit set correspond the a container id you want in the result.
     * If you want all containers in the result, give a null BitSet.
     *
     * @param bits
     */
    public List<Integer> doSort(BitSet bits)
    {
        this.result = null;

        try {
            if ( this.isValid ){
                // get all container ids
                if ( this.optimizedMode ){
                    this.result = doOptimizedSort(bits);
                } else if ( this.numberSort )
                {
                    this.result = doNumberSort(bits);
                } else {
                    this.result = doStringSort(bits);
                }
            }
        } catch ( Exception t ){
            logger.error("Exception occured :" + t.getMessage(), t);
        }

        // Set search time
        this.lastSortingTime = System.currentTimeMillis();

        return this.result;
    }


    //--------------------------------------------------------------------------
    /**
     * Return the List of sorted ctnids.
     *
     */
    public List<Integer> result()
    {
        return this.result;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the order , true - > ASC, false -> DESC.
     *
     */
    public boolean isAscOrdering()
    {
        return this.ASC_Ordering;
    }

    //--------------------------------------------------------------------------
    /**
     * Return true, if the values are converted to number before sorting.
     *
     */
    public boolean isNumberOrdering()
    {
        return this.numberSort;
    }

    //--------------------------------------------------------------------------
    /**
     * Force or not value to be converted to number before doing the sort.
     *
     */
    public boolean setNumberOrdering(boolean val)
    {
        return this.numberSort = val;
    }

    //--------------------------------------------------------------------------
    /**
     * Set DESC ordering.
     *
     */
    public void setDescOrdering()
    {
        this.ASC_Ordering = false;
    }

    //--------------------------------------------------------------------------
    /**
     * Set ASC ordering.
     *
     */
    public boolean setAscOrdering()
    {
        return this.ASC_Ordering = true;
    }

    //--------------------------------------------------------------------------
    /**
     * Set ASC ordering.
     *
     */
    public boolean setAscOrdering(boolean val)
    {
        return this.ASC_Ordering = val;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the site id.
     * @deprecated used getSiteIds
     * @return
     */
    public int getSiteId()
    {
        if (this.siteIds==null || this.siteIds.length==0){
            return -1;
        }
        return this.siteIds[0].intValue();
    }

    //--------------------------------------------------------------------------
    /**
     * Return the sorting fields.
     *
     * @return String[] , the name of fields used for sorting.
     */
    public String[] getSortingFieldNames ()
    {
        return this.fieldNames;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the last sorting time.
     *
     * @return long , the last sorting time
     */
    public long getLastSortingTime()
    {
        return this.lastSortingTime;
    }

    //--------------------------------------------------------------------------
    /**
     * Return true if the sorter initialited properly
     *
     * @return boolean, the valid state value.
     */
    public boolean isValid()
    {
        return this.isValid;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the update status. Each time the doSort method is called, this update status is set to true.
     *
     * @return boolean, the internal updated status value.
     */
    public boolean getUpdateStatus()
    {
        return this.updated;
    }

    //--------------------------------------------------------------------------
    /**
     * Set the update status to true.
     *
     */
    public void setUpdateStatus()
    {
        this.updated = true;
    }

    //--------------------------------------------------------------------------
    public EntryLoadRequest getEntryLoadRequest(){
        return this.entryLoadRequest;
    }

    //--------------------------------------------------------------------------
    /**
     * You can reset the internal update status by setting it to false
     *
     */
    public void resetUpdateStatus()
    {
        this.updated = false;
    }

    public ContainerSorterFieldValueProvider getFieldValueProvider() {
        return fieldValueProvider;
    }

    public void setFieldValueProvider(ContainerSorterFieldValueProvider fieldValueProvider) {
        this.fieldValueProvider = fieldValueProvider;
        this.optimizedMode = false;
    }

    public String getContextID(){
        return this.contextID;
    }

    public void setContextID(String contextID){
        this.contextID = contextID;
    }

    public boolean isOptimizedMode() {
        return optimizedMode;
    }

    /**
     * By default the sorter will work in optimized mode ( use db sort query ). In this optimized mode,
     * multi-value, data provider, number sort, jahia expression evaluation features are not supported.
     *
     * If support for one of these features is required, this mode must ne set to false.
     *
     * @param optimizedMode
     */
    public void setOptimizedMode(boolean optimizedMode) {
        this.optimizedMode = optimizedMode;
    }

    /**
     * A dbMax Result may be defined and be used by the sorter if applicable.
     *
     * @param dbMaxResult
     */
    public void setDBMaxResult(int dbMaxResult){
        this.dbMaxResult = dbMaxResult;
    }

    public int getDBMaxResult(){
        return this.dbMaxResult;
    }

    //--------------------------------------------------------------------------
    /**
     * Load an Map of pair/value (ctnID,fieldValue) .
     *
     *
     * @param bits
     * @return
     * @throws JahiaException
     */
    protected List<Object> getFieldValues(BitSet bits) throws JahiaException {

        List<Integer> deletedCtns = ContainerFilterBean
                .getDeletedContainersBySite(this.siteIds,
                        this.containerDefinitionNames);
        List<Integer> stagingFields = getStagingFields();

        StringBuffer buff = new StringBuffer(
                "select distinct b.containerId, b.comp_id.id, b.value, b.comp_id.workflowState, b.comp_id.languageCode from JahiaContainer a, JahiaFieldsData b, JahiaFieldsDef c, JahiaCtnDef d where ");
        Map<String, Object> parameters = new HashMap<String, Object>();

        if (siteIds != null && siteIds.length > 0) {
            buff.append(" b.siteId in (:siteIds) and ");
            parameters.put("siteIds", siteIds);
        }

        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            buff.append(" a.ctndef.id = d.id and d.name in (:ctnDefNames)");
            parameters.put("ctnDefNames", containerDefinitionNames);
        }

        buff.append(" and a.id=b.containerId and b.fieldDefinition.id = c.id and c.ctnName in (:fieldNames) and");
        parameters.put("fieldNames", fieldNames);

        buff.append(ContainerFilterBean.buildMultilangAndWorlflowQuery(
                this.entryLoadRequest, true));
        buff.append(" order by ");
        buff.append(ContainerFilterBean.FIELD_ID);
        buff.append(",");
        buff.append(ContainerFilterBean.FIELD_WORKFLOW_STATE);

        Locale locale = this.getEntryLoadRequest().getFirstLocale(true);
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        List<Object> datas = new ArrayList<Object>();

        boolean mixLanguageEnabled = JahiaSite
                .isMixLanguagesActiveForSite(Jahia.getThreadParamBean());
        List<String> languageCodes = this.entryLoadRequest.getNotSharedLanguageCodes();
        if (languageCodes.size() <= 1) {
            mixLanguageEnabled = false;
        }
        Map<String, TempField> maps = new HashMap<String, TempField>();
        Map<String, Map<String, TempField>> dataByLanguageCodeMaps = new HashMap<String, Map<String, TempField>>();
        Map<String, TempField> dataByLanguageCodeMap = null;
        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        List<Object[]> result = fieldMgr.executeQuery(buff.toString(),
                parameters);
        for (Object[] objects : result) {
            int ctnID = ((Integer) objects[0]).intValue();
            int fieldID = ((Integer) objects[1]).intValue();
            String fieldValue = (String) objects[2];
            int workflowState = ((Integer) objects[3]).intValue();
            String languageCode = (String) objects[4];

            if (bits == null || bits.get(ctnID)) {
                if (!this.entryLoadRequest.isCurrent()
                        && !(this.entryLoadRequest.isStaging() && workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE)
                        && !(workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE && !stagingFields
                                .contains(new Integer(ctnID)))) {
                    continue;
                }

                if (this.entryLoadRequest.isCurrent() || deletedCtns.isEmpty()
                        || !deletedCtns.contains(new Integer(ctnID))) {
                    if (workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                        workflowState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                    }

                    if (Jahia.getThreadParamBean() != null) {
                        // expression marker
                        ExpressionMarker exprMarker = ExpressionMarker
                                .parseMarkerValue(fieldValue, Jahia
                                        .getThreadParamBean());
                        if (exprMarker != null) {
                            try {
                                String value = exprMarker.getValue();
                                if (value != null && !"".equals(value)) {
                                    fieldValue = value;
                                }
                            } catch (Exception t) {
                                logger.debug(
                                        "Problem while evaluating expression "
                                                + exprMarker.getExpr(), t);
                            }
                        }
                    }

                    // resbundle marker
                    ResourceBundleMarker resMarker = ResourceBundleMarker
                            .parseMarkerValue(fieldValue);
                    if (resMarker != null) {
                        try {
                            String value = resMarker.getValue(locale);
                            if (value != null && !"".equals(value)) {
                                fieldValue = value;
                            }
                        } catch (Exception t) {
                        }
                    }

                    TempField aField = new TempField(fieldID, ctnID, 0,
                            workflowState, languageCode, fieldValue);
                    String key = fieldID + "_" + workflowState + "_"
                            + languageCode;
                    if (languageCode.equals(ContentField.SHARED_LANGUAGE)
                            || languageCode.equals(locale.toString())) {
                        maps.put(key, aField);
                    } else if (mixLanguageEnabled) {
                        dataByLanguageCodeMap = dataByLanguageCodeMaps
                                .get(languageCode);
                        if (dataByLanguageCodeMap == null) {
                            dataByLanguageCodeMap = new HashMap<String, TempField>();
                            dataByLanguageCodeMaps.put(languageCode,
                                    dataByLanguageCodeMap);
                        }
                        dataByLanguageCodeMap.put(key, aField);
                        maps.put(key, aField);
                    }
                }
            }
        }
 

        Set<Integer> addedIds = new HashSet<Integer>();
        Iterator<TempField> iterator = maps.values().iterator();
        String valueToSort = null;
        Iterator<String> languageIterator = null;
        String languageCode = null;
        TempField otherLanguageField = null;
        boolean hasDifferentTranslation = !dataByLanguageCodeMaps.isEmpty();
        while ( iterator.hasNext() ){
            TempField aField = iterator.next();
            if ( !addedIds.contains(new Integer(aField.id)) ){
                String key = aField.id + "_" + aField.workflowState + "_" +
                             locale.toString();
                if (!aField.languageCode.equals(ContentField.SHARED_LANGUAGE) &&
                    maps.containsKey(key) &&
                    !aField.languageCode.equals(locale.toString())) {
                    continue;
                } else if (!aField.languageCode.equals(ContentField.SHARED_LANGUAGE) && !maps.containsKey(key)) {
                    if (aField.workflowState>=2 && maps.containsKey(aField.id + "_1_" +locale.toString())) {
                        continue;
                    }
                    // this field doesn't exist in the given language, so sort it as "" value
                    if (!mixLanguageEnabled){
                        aField.value = "";
                    } else if (hasDifferentTranslation) {
                        languageIterator = languageCodes.iterator();
                        while (languageIterator.hasNext()){
                            languageCode = languageIterator.next();
                            if ( languageCode.equals(aField.languageCode) ){
                                break;
                            }
                            dataByLanguageCodeMap = dataByLanguageCodeMaps.get(languageCode);
                            if (dataByLanguageCodeMap != null){
                                otherLanguageField = dataByLanguageCodeMap.get(aField.id + "_"
                                        + aField.workflowState + "_" + languageCode);
                                if (otherLanguageField != null){
                                    aField.value = otherLanguageField.value;
                                    break;
                                } else if (otherLanguageField == null && aField.workflowState
                                        == EntryLoadRequest.STAGING_WORKFLOW_STATE){
                                    otherLanguageField = dataByLanguageCodeMap.get(aField.id + "_"
                                            + EntryLoadRequest.ACTIVE_WORKFLOW_STATE + "_" + languageCode);
                                    if (otherLanguageField != null){
                                        aField.value = otherLanguageField.value;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                Object obj = null;
                valueToSort = aField.value;
                if ( this.fieldValueProvider != null ){
                    valueToSort = this.fieldValueProvider.getFieldValue(aField.id, Jahia.getThreadParamBean(),
                            this.getEntryLoadRequest(), aField.value);
                    if ( valueToSort == null ){
                        valueToSort = "";
                    }
                }
                if (this.numberSort) {
                    obj = new DataBean(aField.ctnID, valueToSort);
                } else {
                    obj = new StrDataBean(aField.ctnID, valueToSort);
                }
                datas.add(obj);
                addedIds.add(new Integer(aField.id));
            }
        }

        return datas;
    }

    //--------------------------------------------------------------------------
    /**
     * Containers are sorted after sorting field's data are loaded and converted to
     * a long representation.
     *
     * @param bits, 	any bit position sset to true must correspond to a ctn id to include in the result.
     *						if you want all ctn ids in the result, gieve a null BitSet.
     * @return List, List of sorted ctn ids.
     */
    protected List<Integer> doStringSort(BitSet bits) throws JahiaException
    {
        if ( bits != null ){
            if (bits.length()== 0){
                return new ArrayList<Integer>();
            } else if(bits.cardinality()==1){
                List<Integer> result = new ArrayList<Integer>();
                result.add(new Integer(bits.nextSetBit(0)));
                return result;
            }
        }
        List<Integer> results = new ArrayList<Integer>();

        List<Object> datas = this.getFieldValues(bits);

        // sort the datas
        if ( datas.size()>1 ){
            // a dummy dataBean
            StrDataBean dummyDataBean = new StrDataBean(ASC_Ordering);
            Collections.sort(datas,dummyDataBean);
        }
        // retrieve sorted ids
        int size = datas.size();
        BitSet sortedBitSet = new BitSet();
        StrDataBean dataBean = null;
        for ( int i=0; i<size ; i++ ){
            dataBean = (StrDataBean)datas.get(i);
            results.add(new Integer(dataBean.ctnID));
            sortedBitSet.set(dataBean.ctnID);
        }

        // missing container should be returned as well
        if ( bits != null ){
            BitSet diffBitSet = new BitSet();
            diffBitSet.or(bits);
            diffBitSet.andNot(sortedBitSet);
            int l = diffBitSet.size();
            for ( int i=0; i<l ; i++ ){
                if (diffBitSet.get(i)) {
                    results.add(new Integer(i));
                }
            }
        }

        return results;
    }

    //--------------------------------------------------------------------------
    /**
     * Containers are sorted after sorting field's data are loaded and converted to
     * a long representation.
     *
     * @param bits, 	any bit position sset to true must correspond to a ctn id to include in the result.
	 *                                                                                                                     			if you want all ctn ids in the result, gieve a null BitSet.
     * @return List, List of sorted ctn ids.
     */
    protected List<Integer> doNumberSort(BitSet bits) throws JahiaException
    {
        if ( bits != null ){
            if (bits.length()== 0){
                return new ArrayList<Integer>();
            } else if(bits.cardinality()==1){
                List<Integer> result = new ArrayList<Integer>();
                result.add(new Integer(bits.nextSetBit(0)));
                return result;
            }
        }
        List<Integer> results = new ArrayList<Integer>();

        List<Object> datas = this.getFieldValues(bits);
        // sort the datas
        if ( datas.size()>1 ){
            // a dummy dataBean
            DataBean dummyDataBean = new DataBean(ASC_Ordering);
            Collections.sort(datas,dummyDataBean);
        }
        // retrieve sorted ids
        int size = datas.size();
        BitSet sortedBitSet = new BitSet();
        DataBean dataBean = null;
        for ( int i=0; i<size ; i++ ){
            dataBean = (DataBean)datas.get(i);
            results.add(new Integer(dataBean.ctnID));
            sortedBitSet.set(dataBean.ctnID);
        }

        // missing container should be returned as well
        if ( bits != null ){
            BitSet diffBitSet = new BitSet();
            diffBitSet.or(bits);
            diffBitSet.andNot(sortedBitSet);
            int l = diffBitSet.size();
            for ( int i=0; i<l ; i++ ){
                if (diffBitSet.get(i)) {
                    results.add(new Integer(i));
                }
            }
        }

        return results;
    }

    //--------------------------------------------------------------------------
    /**
     *
     * @param bits, 	any bit position sset to true must correspond to a ctn id to include in the result.
     *						if you want all ctn ids in the result, gieve a null BitSet.
     * @return List, List of sorted ctn ids.
     */
    protected List<Integer> doOptimizedSort(BitSet bits) throws JahiaException
    {
        if ( bits != null ){
            if (bits.length()== 0){
                return new ArrayList<Integer>();
            } else if(bits.cardinality()==1){
                List<Integer> result = new ArrayList<Integer>();
                result.add(new Integer(bits.nextSetBit(0)));
                return result;
            }
        }
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        JahiaContainerManager containerMgr = (JahiaContainerManager) context.getBean(JahiaContainerManager.class.getName());
        List<Object[]> ctnIds = containerMgr.getSortedContainerIds(new Integer(this.listId),siteIds,
                Boolean.TRUE,containerDefinitionNames, fieldNames, false, this.entryLoadRequest,false,false,
                this.ASC_Ordering, bits, this.getDBMaxResult());
        List<Integer> v = new ArrayList<Integer>();
        BitSet sortedBitSet = new BitSet();
        if ( ctnIds != null && !ctnIds.isEmpty() ){
            Integer workflowState = null;
            Integer versionId = null;
            Integer ctnId = null;
            List<TmpData> orderedIds = new ArrayList<TmpData>();
            Set<Integer> stagedEntries = new HashSet<Integer>();
            for ( Object[] row : ctnIds ){
                workflowState = (Integer)row[1];
                versionId = (Integer)row[2];
                ctnId = (Integer)row[4];
                if ( workflowState.intValue() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                    workflowState = new Integer(EntryLoadRequest.STAGING_WORKFLOW_STATE);
                }
                if (bits != null && !bits.get(ctnId.intValue())){
                    continue;
                }
                if ( workflowState.intValue() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                    stagedEntries.add(ctnId);
                }
                orderedIds.add(new TmpData(ctnId,workflowState,versionId));
            }
            int wfs = 0;
            for (TmpData data : orderedIds){
                wfs = data.getWorkflowState().intValue();
                if ( wfs == EntryLoadRequest.ACTIVE_WORKFLOW_STATE && stagedEntries.contains(data.getCtnId()) ) {
                    continue;
                } else if ( wfs > EntryLoadRequest.ACTIVE_WORKFLOW_STATE && data.getVersionId().intValue() == -1 ){
                    continue;
                }
                sortedBitSet.set(data.getCtnId().intValue());
                v.add(data.getCtnId());
            }
        }
        // missing container should be returned as well
        if ( bits != null ){
            BitSet diffBitSet = new BitSet();
            diffBitSet.or(bits);
            diffBitSet.andNot(sortedBitSet);
            int l = diffBitSet.size();
            for ( int i=0; i<l ; i++ ){
                if (diffBitSet.get(i)) {
                    v.add(new Integer(i));
                }
            }
        }
        return v;
    }

    protected class DataBean implements Comparator
    {
        int ctnID = 0;
        String value = null;
        boolean ASC_Ordering = true;

        public DataBean (int ctnID, String value)
        {
            this.ctnID = ctnID;
            this.value = value;
        }

        public DataBean (boolean ASC_Ordering)
        {
            this.ASC_Ordering = ASC_Ordering;
        }

        public int compare(Object obj1, Object obj2) throws ClassCastException {

            DataBean dataBean1 = (DataBean)obj1;
            DataBean dataBean2 = (DataBean)obj2;
            if ( ASC_Ordering ){
                return NumberFormats.compareNumber(dataBean1.value,dataBean2.value, numberFormat);
            } else {
                return NumberFormats.compareNumber(dataBean2.value,dataBean1.value, numberFormat);
            }
        }
    }

    //--------------------------------------------------------------------------
    protected class StrDataBean extends NumericStringComparator {
        int ctnID = 0;
        String value = "";
        boolean ASC_Ordering = true;

        public StrDataBean (int ctnID, String value)
        {
            this.ctnID = ctnID;
            this.value = value;
        }

        public StrDataBean (boolean ASC_Ordering)
        {
            this.ASC_Ordering = ASC_Ordering;
        }

        public int compare(Object obj1, Object obj2) throws ClassCastException {

            StrDataBean dataBean1 = (StrDataBean)obj1;
            StrDataBean dataBean2 = (StrDataBean)obj2;
            String valueBean1 = "";
            if ( dataBean1 != null ){
                valueBean1 = dataBean1.value;
            }

            String valueBean2 = "";

            if ( dataBean2 != null ){
                valueBean2 = dataBean2.value;
            }

            if ( valueBean1 == null ){
                valueBean1 = "";
            }
            if ( valueBean2 == null ){
                valueBean2 = "";
            }

            if ( this.ASC_Ordering ){
                return super.compare(valueBean1,valueBean2);
            } else {
                return super.compare(valueBean2,valueBean1);
            }
        }
    }

    protected class TempField{
        public int id;
        public int ctnID;
        public int versionID;
        public int workflowState;
        public String languageCode;
        public String value;

        public TempField(int id, int ctnID, int versionID, int workflowState,
                         String languageCode, String value){
            this.id = id;
            this.ctnID = ctnID;
            this.versionID = versionID;
            this.workflowState = workflowState;
            this.languageCode = languageCode;
            this.value = value;
        }
    }

    protected class TmpData {
        private Integer ctnId;
        private Integer workflowState;
        private Integer versionId;

        public  TmpData(Integer ctnId, Integer workflowState, Integer versionId) {
            this.ctnId = ctnId;
            this.workflowState = workflowState;
            this.versionId = versionId;
        }

        public Integer getCtnId() {
            return ctnId;
        }

        public void setCtnId(Integer ctnId) {
            this.ctnId = ctnId;
        }

        public Integer getWorkflowState() {
            return workflowState;
        }

        public void setWorkflowState(Integer workflowState) {
            this.workflowState = workflowState;
        }

        public Integer getVersionId() {
            return versionId;
        }

        public void setVersionId(Integer versionId) {
            this.versionId = versionId;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Returns an array of fields that are in staging.
     *
     * @return
     * @throws JahiaException
     */
    protected List<Integer> getStagingFields() throws JahiaException {

        StringBuffer buff = new StringBuffer(
                "select distinct b.comp_id.id from JahiaContainer a, JahiaFieldsData b, JahiaCtnDef c where ");
        Map<String, Object> parameters = new HashMap<String, Object>();

        if (siteIds != null && siteIds.length > 0) {
            buff.append(" b.siteId in (:siteIds) and ");
            parameters.put("siteIds", siteIds);
        }

        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            buff.append(" a.ctndef.id = c.id and c.name in (:ctnDefNames)");
            parameters.put("ctnDefNames", containerDefinitionNames);
        }

        buff.append(" AND a.id=b.containerId AND b.comp_id.workflowState > 1 AND a.comp_id.workflowState > 1");
        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        return new ArrayList<Integer>(fieldMgr.<Integer>executeQuery(buff.toString(), parameters));
    }

    /**
     *  Return the collator instantiated with the first locale from the internal EntryLoadRequest.
     *  If the entryLoadRequest is null, the localtor is instantiated with the default locale of the system
     * @return
     */
    protected Collator getCollator(){
        Collator collator = Collator.getInstance();
        if ( this.getEntryLoadRequest() != null ){
            Locale locale = null;
            locale = this.getEntryLoadRequest().getFirstLocale(true);
            if ( locale != null ){
                collator = Collator.getInstance(locale);
            }
        }
        return collator;
    }

}
