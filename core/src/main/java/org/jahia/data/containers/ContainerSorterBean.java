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
import java.util.*;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.bin.Jahia;
import org.jahia.data.fields.ExpressionMarker;
import org.jahia.data.fields.JahiaFileFieldWrapper;
import org.jahia.data.fields.LoadFlags;
import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.hibernate.manager.JahiaContainerManager;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.ResourceBundleMarker;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTypes;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.comparator.NumericStringComparator;
import org.springframework.context.ApplicationContext;

/**
 * Jahia Standard Containers Sort Handler for a given container list.
 * 
 * @see ContainerSorterBean
 * @see JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerSorterBean implements Serializable,
        ContainerSorterInterface, ValueProviderSorter {

    private static final long serialVersionUID = 3198953166963313827L;

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(ContainerSorterBean.class);

    protected int ctnListID = -1;

    protected String fieldName;

    protected boolean updated = false;

    protected long lastSortingTime = -1;

    protected boolean numberSort = false;

    protected String numberFormat = NumberFormats.LONG_FORMAT;

    protected boolean isValid = false;

    protected boolean ASC_Ordering = true; // by default ASCENDANT ORDER.

    // ** sorted ctnids **/
    protected List<Integer> result;

    protected EntryLoadRequest entryLoadRequest = EntryLoadRequest.CURRENT;

    protected ContainerSorterFieldValueProvider fieldValueProvider;

    protected String contextID = "";

    protected boolean optimizedMode = true;

    protected int dbMaxResult = -1;

    protected ContainerSorterBean() {
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param ctnListID
     *            , the container list id.
     * @param fieldName, the field on which to sort.
     * @deprecated
     */
    public ContainerSorterBean(int ctnListID, String fieldName)
            throws JahiaException {
        this(ctnListID, fieldName, null);
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param ctnListID, the container list id.
     * @param fieldName name, the field on which to sort.
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterBean(int ctnListID, String fieldName,
            EntryLoadRequest entryLoadRequest) throws JahiaException {
        this(ctnListID, fieldName, false, entryLoadRequest);
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param ctnListID, the container list id.
     * @param fieldName, the field on which to sort.
     * @param numberSort, force field values to be converted to long representation before sorting ( if true ).
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterBean(int ctnListID, String fieldName,
            boolean numberSort, EntryLoadRequest entryLoadRequest)
            throws JahiaException {
        this(ctnListID, fieldName, numberSort, null, entryLoadRequest);
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param ctnListID, the container list id.
     * @param fieldName, the field on which to sort.
     * @param numberSort, force field values to be converted to number representation before sorting ( if true ).
     * @param numberFormat, only used if numberSort is true. If null, the format used is NumberFormat.LONG_FORMAT
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterBean(int ctnListID, String fieldName,
            boolean numberSort, String numberFormat,
            EntryLoadRequest entryLoadRequest) throws JahiaException {
        if (ctnListID > 0 && fieldName != null && !fieldName.trim().equals("")) {
            this.ctnListID = ctnListID;
            this.fieldName = fieldName;
            this.numberSort = numberSort;
            if (NumberFormats.isValidFormat(numberFormat)) {
                this.numberFormat = numberFormat;
            }
            this.isValid = true;
        }

        this.optimizedMode = !this.numberSort;
        
        if (entryLoadRequest != null) {
            this.entryLoadRequest = entryLoadRequest;
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param String
     *            containerListName, the container list name.
     * @param ParamBean
     *            , the param bean.
     * @param String
     *            the field name, the field on which to sort.
     * @deprecated
     */
    public ContainerSorterBean(String containerListName,
            ProcessingContext params, String fieldName) throws JahiaException {
        this(containerListName, params, fieldName, params.getEntryLoadRequest());
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param String
     *            containerListName, the container list name.
     * @param ParamBean
     *            , the param bean.
     * @param String
     *            the field name, the field on which to sort.
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterBean(String containerListName,
            ProcessingContext params, String fieldName,
            EntryLoadRequest entryLoadRequest) throws JahiaException {
        this(containerListName, params, fieldName, false, entryLoadRequest);
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param String
     *            containerListName, the container list name.
     * @param ParamBean
     *            , the param bean.
     * @param String
     *            the field name, the field on which to sort.
     * @param boolean , force field values to be converted to long representation before sorting ( if true ).
     * @deprecated
     */
    public ContainerSorterBean(String containerListName,
            ProcessingContext params, String fieldName, boolean numberSort)
            throws JahiaException {
        this(containerListName, params, fieldName, numberSort, params
                .getEntryLoadRequest());
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param String
     *            containerListName, the container list name.
     * @param ParamBean
     *            , the param bean.
     * @param String
     *            the field name, the field on which to sort.
     * @param boolean , force field values to be converted to long representation before sorting ( if true ).
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterBean(String containerListName,
            ProcessingContext params, String fieldName, boolean numberSort,
            EntryLoadRequest entryLoadRequest) throws JahiaException {
        this(containerListName, params, fieldName, numberSort, null,
                entryLoadRequest);
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param String
     *            containerListName, the container list name.
     * @param ParamBean
     *            , the param bean.
     * @param String
     *            the field name, the field on which to sort.
     * @param boolean , force field values to be converted to number representation before sorting ( if true ).
     * @param numberFormat
     *            , only used if numberSort is true. If null, the format used is NumberFormat.LONG_FORMAT
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerSorterBean(String containerListName,
            ProcessingContext params, String fieldName, boolean numberSort,
            String numberFormat, EntryLoadRequest entryLoadRequest)
            throws JahiaException {
        logger.debug("Created container sort for clist: " + containerListName
                + " on the field " + fieldName);

        if (containerListName != null) {
            int clistID = ServicesRegistry.getInstance()
                    .getJahiaContainersService().getContainerListID(
                            containerListName, params.getPage().getID());
            if (clistID > 0 && fieldName != null
                    && !fieldName.trim().equals("")) {
                this.ctnListID = clistID;
                this.fieldName = fieldName;
                this.numberSort = numberSort;
                this.isValid = true;
                if (NumberFormats.isValidFormat(numberFormat)) {
                    this.numberFormat = numberFormat;
                }
                // JahiaConsole.println("ContainerSorterBean","Constructor sorter is valid ");
            }
        }
        if (entryLoadRequest != null) {
            this.entryLoadRequest = entryLoadRequest;
        } else if (params.getEntryLoadRequest() != null) {
            this.entryLoadRequest = params.getEntryLoadRequest();
        }
        if (this.numberSort) {
            this.optimizedMode = false;
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Do the sort. Optionally, you can provide a BitSet where each bit set correspond the a container id you want in the result. If you
     * want all containers in the result, give a null BitSet.
     * 
     * @param BitSet
     *            bits
     */
    public List<Integer> doSort(BitSet bits) {
        logger.debug("Started");

        this.result = null;

        try {
            if (this.isValid) {
                // get all container ids
                if (logger.isDebugEnabled())
                    logger.debug("Sorting : On field : "
                            + Arrays.toString(getSortingFieldNames()));
                if (this.optimizedMode) {
                    this.result = doOptimizedSort(bits);
                } else if (this.numberSort) {
                    this.result = doNumberSort(bits);
                } else {
                    this.result = doStringSort(bits);
                }
            }
        } catch (Exception t) {
            logger.error("Exception occured :" + t.getMessage(), t);
        }

        // Set search time
        this.lastSortingTime = System.currentTimeMillis();

        return this.result;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the List of sorted ctnids.
     * 
     */
    public List<Integer> result() {
        return this.result;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the order , true - > ASC, false -> DESC.
     * 
     */
    public boolean isAscOrdering() {
        return this.ASC_Ordering;
    }

    // --------------------------------------------------------------------------
    /**
     * Return true, if the values are converted to number before sorting.
     * 
     */
    public boolean isNumberOrdering() {
        return this.numberSort;
    }

    // --------------------------------------------------------------------------
    /**
     * Force or not value to be converted to number before doing the sort.
     * 
     */
    public boolean setNumberOrdering(boolean val) {
        this.numberSort = val;
        return this.optimizedMode = false;
    }

    // --------------------------------------------------------------------------
    /**
     * Set DESC ordering.
     * 
     */
    public void setDescOrdering() {
        this.ASC_Ordering = false;
    }

    // --------------------------------------------------------------------------
    /**
     * Set ASC ordering.
     * 
     */
    public boolean setAscOrdering() {
        return this.ASC_Ordering = true;
    }

    // --------------------------------------------------------------------------
    /**
     * Set ASC ordering.
     * 
     */
    public boolean setAscOrdering(boolean val) {
        return this.ASC_Ordering = val;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the container list id.
     * 
     * @return int ctnListID, the container list id.
     */
    public int getCtnListID() {
        return this.ctnListID;
    }

    public void setCtnListID(int listId) {
        this.ctnListID = listId;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the sorting field.
     * 
     * Only first element is defined for @see ContainerSorterBean (and the extending @see ContainerMetadataSorterBean) but more can be
     * defined for @see ContainerSorterByContainerDefinition.
     */
    public String[] getSortingFieldNames() {
        return new String[] { this.fieldName };
    }

    // --------------------------------------------------------------------------
    /**
     * Return the last sorting time.
     * 
     * @return long , the last sorting time
     */
    public long getLastSortingTime() {
        return this.lastSortingTime;
    }

    // --------------------------------------------------------------------------
    /**
     * Return true if the sorter initialited properly
     * 
     * @return boolean, the valid state value.
     */
    public boolean isValid() {
        return this.isValid;
    }

    // --------------------------------------------------------------------------
    /**
     * Return the update status. Each time the doSort method is called, this update status is set to true.
     * 
     * @return boolean, the internal updated status value.
     */
    public boolean getUpdateStatus() {
        return this.updated;
    }

    // --------------------------------------------------------------------------
    /**
     * Set the update status to true.
     * 
     */
    public void setUpdateStatus() {
        this.updated = true;
    }

    // --------------------------------------------------------------------------
    public EntryLoadRequest getEntryLoadRequest() {
        return this.entryLoadRequest;
    }

    // --------------------------------------------------------------------------
    /**
     * You can reset the internal update status by setting it to false
     * 
     */
    public void resetUpdateStatus() {
        this.updated = false;
    }

    public ContainerSorterFieldValueProvider getFieldValueProvider() {
        return fieldValueProvider;
    }

    public void setFieldValueProvider(
            ContainerSorterFieldValueProvider fieldValueProvider) {
        this.fieldValueProvider = fieldValueProvider;
        this.optimizedMode = false;
    }

    public String getContextID() {
        return this.contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }

    public boolean isOptimizedMode() {
        return optimizedMode;
    }

    /**
     * By default the sorter will work in optimized mode ( use db sort query ). In this optimized mode, multi-value, data provider, number
     * sort, jahia expression evaluation features are not supported.
     * 
     * If support for one of these features is required, this mode must ne set to false.
     * 
     * @param optimizedMode use the optimized mode
     */
    public void setOptimizedMode(boolean optimizedMode) {
        this.optimizedMode = optimizedMode;
    }

    /**
     * A dbMax Result may be defined and be used by the sorter if applicable.
     * 
     * @param dbMaxResult maximum number of results returned by the DB
     */
    public void setDBMaxResult(int dbMaxResult) {
        this.dbMaxResult = dbMaxResult;
    }

    public int getDBMaxResult() {
        return this.dbMaxResult;
    }

    // --------------------------------------------------------------------------
    /**
     * Load an Map of pair/value (ctnID,fieldValue) for a given ctnlist and a given fieldName.
     * 
     * 
     * @param ctnListID, the container list id
     * @param fieldName, the fieldName
     * @param convertValueAsLong number sort
     * @param bits, BitSet of results
     * @return List.
     */
    protected List<DataBean> getFieldValues(int ctnListID, String fieldName,
            boolean convertValueAsLong, BitSet bits) throws JahiaException {
        List<Integer> deletedCtns = ContainerFilterBean
                .getDeletedContainers(ctnListID);
        List<Integer> stagingFields = getStagingFields(ctnListID);
        Map<String, Object> parameters = new HashMap<String, Object>();

        StringBuffer buff = new StringBuffer(
                "select distinct b.containerId, b.comp_id.id, b.value, b.comp_id.workflowState, b.comp_id.languageCode, b.type from JahiaContainer a, JahiaFieldsData b, JahiaFieldsDef c ");
        buff
                .append("where a.listid=(:listId) and a.comp_id.id=b.containerId and b.fieldDefinition.id = c.id and c.ctnName=(:fieldName) and (");
        buff.append(ContainerFilterBean.buildMultilangAndWorlflowQuery(
                this.entryLoadRequest, true));
        buff.append(") order by ").append(ContainerFilterBean.FIELD_ID).append(
                ",").append(ContainerFilterBean.FIELD_WORKFLOW_STATE);

        parameters.put("listId", ctnListID);
        parameters.put("fieldName", fieldName);

        Locale locale = this.getEntryLoadRequest().getFirstLocale(true);
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        List<DataBean> datas = new ArrayList<DataBean>();

        boolean mixLanguageEnabled = JahiaSite
                .isMixLanguagesActiveForSite(Jahia.getThreadParamBean());
        List<String> languageCodes = this.entryLoadRequest
                .getNotSharedLanguageCodes();
        if (languageCodes.size() <= 1) {
            mixLanguageEnabled = false;
        }
        Map<String, TempField> maps = new HashMap<String, TempField>();
        Map<String, Map<String, TempField>> dataByLanguageCodeMaps = new HashMap<String, Map<String, TempField>>();
        Map<String, TempField> dataByLanguageCodeMap;
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context.getBean(JahiaFieldsDataManager.class.getName());
        List<Object[]> queryResult = fieldMgr.executeQuery(buff.toString(),parameters);

        for (Object[] row : queryResult) {
            int ctnID = (Integer) row[0];
            int fieldID = (Integer) row[1];
            String fieldValue = (String) row[2];
            int workflowState = (Integer) row[3];
            String languageCode = (String) row[4];
            int type = (Integer) row[5];

            if (type == ContentFieldTypes.PAGE) {
                try {
                    fieldValue = ContentPage.getPage(
                            Integer.parseInt(fieldValue)).getTitle(
                            entryLoadRequest);
                } catch (JahiaPageNotFoundException e) {
                    fieldValue = "";
                }
            } else if (type == ContentFieldTypes.FILE) {
                JahiaFileFieldWrapper field = (JahiaFileFieldWrapper)ContentField.getField(fieldID).getJahiaField(entryLoadRequest);
                if (field != null) {
                    field.load(LoadFlags.NOTHING, Jahia.getThreadParamBean(), entryLoadRequest);
                    JahiaFileField fileField = (JahiaFileField)field.getObject();
                    fieldValue = fileField.getFileFieldTitle();                    
                }
            } else if (type == ContentFieldTypes.BIGTEXT) {
                fieldValue = ContentField.getField(fieldID).getValue(
                        Jahia.getThreadParamBean(), entryLoadRequest);
                if (fieldValue != null) {
                    fieldValue = JahiaTools.html2text(fieldValue);
                    try {
                        fieldValue = (new RE("<(.*?)>")).subst(fieldValue, "");
                    } catch (RESyntaxException re) {
                        logger.error(re.toString(), re);
                    } catch (Exception t) {
                        logger.error(t.toString(), t);
                    }
                }
            }

            if (fieldValue != null && (bits == null || bits.get(ctnID))) {
                if (this.entryLoadRequest.isCurrent()
                        || !deletedCtns.contains(ctnID)) {
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
                            logger.debug("Problem while evaluating resource "
                                    + fieldValue, t);
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
        boolean hasDifferentTranslation = !dataByLanguageCodeMaps.isEmpty();
        for (TempField aField : maps.values()) {
            if (!addedIds.contains(aField.id)) {
                String key = aField.id + "_" + aField.workflowState + "_"
                        + locale.toString();
                if (!aField.languageCode.equals(ContentField.SHARED_LANGUAGE)
                        && maps.containsKey(key)
                        && !aField.languageCode.equals(locale.toString())) {
                    continue;
                } else if (!aField.languageCode
                        .equals(ContentField.SHARED_LANGUAGE)
                        && !maps.containsKey(key)) {
                    if (aField.workflowState >= 2
                            && maps.containsKey(aField.id + "_1_"
                                    + locale.toString())) {
                        continue;
                    }
                    // this field doesn't exist in the given language, so sort it as "" value
                    if (!mixLanguageEnabled) {
                        aField.value = "";
                    } else if (hasDifferentTranslation) {
                        for (String languageCode : languageCodes) {
                            if (languageCode.equals(aField.languageCode)) {
                                break;
                            }
                            dataByLanguageCodeMap = dataByLanguageCodeMaps
                                    .get(languageCode);
                            if (dataByLanguageCodeMap != null) {
                                TempField otherLanguageField = dataByLanguageCodeMap
                                        .get(aField.id + "_"
                                                + aField.workflowState + "_"
                                                + languageCode);
                                if (otherLanguageField != null) {
                                    aField.value = otherLanguageField.value;
                                    break;
                                } else if (otherLanguageField == null
                                        && aField.workflowState == EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                                    otherLanguageField = dataByLanguageCodeMap.get(aField.id
                                                    + "_"
                                                    + EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                                                    + "_" + languageCode);
                                    if (otherLanguageField != null) {
                                        aField.value = otherLanguageField.value;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                String valueToSort = aField.value;
                if (this.fieldValueProvider != null) {
                    valueToSort = this.fieldValueProvider.getFieldValue(
                            aField.id, Jahia.getThreadParamBean(), this
                                    .getEntryLoadRequest(), aField.value);
                    if (valueToSort == null) {
                        valueToSort = "";
                    }
                }
                DataBean obj = new DataBean(aField.ctnID,valueToSort);

                if (this.entryLoadRequest.isCurrent()) {
                    datas.add(obj);
                    addedIds.add(aField.id);
                } else if (this.entryLoadRequest.isStaging()
                        && aField.workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    datas.add(obj);
                    addedIds.add(aField.id);
                } else if (aField.workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                        && !stagingFields.contains(aField.id)) {
                    datas.add(obj);
                    addedIds.add(aField.id);
                }
            }
        }

        return datas;
    }

    // --------------------------------------------------------------------------
    /**
     * Containers are sorted after sorting field's data are loaded and converted to a long representation.
     * 
     * @param bits, any bit position sset to true must correspond to a ctn id to include in the result. if you want all ctn ids in the
     *            result, gieve a null BitSet.
     * @return List, List of sorted ctn ids.
     * @throws org.jahia.exceptions.JahiaException in case of error
     */
    protected List<Integer> doStringSort(BitSet bits) throws JahiaException {
        List<Integer> result = new ArrayList<Integer>();
        if (bits != null) {
            if (bits.length() == 0) {
                return result;
            } else if (bits.cardinality() == 1) {
                result.add(bits.nextSetBit(0));
                return result;
            }
        }

        List<DataBean> datas = new ArrayList<DataBean>(this.getFieldValues(this.ctnListID, this.fieldName, this
                .isNumberOrdering(), bits));

        // sort the datas
        if (datas.size() > 1) {
            // a dummy dataBean
            Collections.sort(datas, new StringComparator(ASC_Ordering));
        }
        // retrieve sorted ids
        BitSet sortedBitSet = new BitSet();
        for (DataBean dataBean : datas) {
            result.add(dataBean.ctnID);
            sortedBitSet.set(dataBean.ctnID);
        }
        // if bits is null, return all containers
        if (bits == null) {
            bits = new BitSet();
            List<Integer> ctnIds = ServicesRegistry.getInstance()
                    .getJahiaContainersService().getctnidsInList(
                            this.getCtnListID(), this.entryLoadRequest);
            for (Integer id : ctnIds) {
                bits.set(id);
            }
        }
        // missing container should be returned as well
        BitSet diffBitSet = new BitSet();
        diffBitSet.or(bits);
        diffBitSet.andNot(sortedBitSet);
        int l = diffBitSet.size();
        for (int i = 0; i < l; i++) {
            if (diffBitSet.get(i)) {
                result.add(i);
            }
        }
        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * Containers are sorted after sorting field's data are loaded and converted to a long representation.
     * 
     * @param bits, any bit position sset to true must correspond to a ctn id to include in the result. if you want all ctn ids in the
     *            result, gieve a null BitSet.
     * @return List, List of sorted ctn ids.
     * @throws org.jahia.exceptions.JahiaException in case of error
     */
    protected List<Integer> doNumberSort(BitSet bits) throws JahiaException {
        List<Integer> result = new ArrayList<Integer>();
        if (bits != null) {
            if (bits.length() == 0) {
                return result;
            } else if (bits.cardinality() == 1) {
                result.add(bits.nextSetBit(0));
                return result;
            }
        }

        List<DataBean> datas = new ArrayList<DataBean>(this.getFieldValues(this.ctnListID, this.fieldName, this
                .isNumberOrdering(), bits));

        // sort the datas
        if (datas.size() > 1) {
            // a dummy dataBean
            Collections.sort(datas, new NumberComparator(ASC_Ordering,numberFormat));
        }
        // retrieve sorted ids
        BitSet sortedBitSet = new BitSet();
        for (DataBean dataBean : datas) {
            result.add(dataBean.ctnID);
            sortedBitSet.set(dataBean.ctnID);
        }
        // if bits is null, return all containers
        if (bits == null) {
            bits = new BitSet();
            List<Integer> ctnIds = ServicesRegistry.getInstance()
                    .getJahiaContainersService().getctnidsInList(
                            this.getCtnListID(), this.entryLoadRequest);
            for (Integer id : ctnIds) {
                bits.set(id);
            }
        }
        // missing container should be returned as well
        BitSet diffBitSet = new BitSet();
        diffBitSet.or(bits);
        diffBitSet.andNot(sortedBitSet);
        int l = diffBitSet.size();
        for (int i = 0; i < l; i++) {
            if (diffBitSet.get(i)) {
                result.add(i);
            }
        }


        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * 
     * @param bits, any bit position sset to true must correspond to a ctn id to include in the result. if you want all ctn ids in the
     *            result, gieve a null BitSet.
     * @return List, List of sorted ctn ids.
     * @throws JahiaException in case of error
     */
    protected List<Integer> doOptimizedSort(BitSet bits) throws JahiaException {
        List<Integer> result = new ArrayList<Integer>();
        if (bits != null) {
            if (bits.length() == 0) {
                return result;
            } else if (bits.cardinality() == 1) {
                result.add(bits.nextSetBit(0));
                return result;
            }
        }
        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaContainerManager containerMgr = (JahiaContainerManager) context
                .getBean(JahiaContainerManager.class.getName());
        String[] fieldNames = new String[] { fieldName };
        List<Object[]> ctnIds = containerMgr.getSortedContainerIds(this.ctnListID, null, Boolean.FALSE, null, fieldNames, false,
                this.entryLoadRequest, false, false, this.ASC_Ordering, bits,
                this.dbMaxResult);
        Set<Integer> v = new LinkedHashSet<Integer>();
        BitSet sortedBitSet = new BitSet();
        if (ctnIds != null && !ctnIds.isEmpty()) {
            List<TmpData> orderedIds = new ArrayList<TmpData>();
            Set<Integer> stagedEntries = new HashSet<Integer>();
            for (Object[] row : ctnIds) {
                Integer workflowState = (Integer) row[1];
                Integer versionId = (Integer) row[2];
                Integer ctnId = (Integer) row[4];

                if (workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    workflowState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                }
                if (bits != null && !bits.get(ctnId)) {
                    continue;
                }
                if (workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    stagedEntries.add(ctnId);
                }
                orderedIds.add(new TmpData(ctnId, workflowState, versionId));
            }
            int wfs;
            for (TmpData data : orderedIds) {
                wfs = data.getWorkflowState();
                if (wfs == EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                        && stagedEntries.contains(data.getCtnId())) {
                    continue;
                } else if (wfs > EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                        && data.getVersionId() == -1) {
                    continue;
                }
                sortedBitSet.set(data.getCtnId());
                v.add(data.getCtnId());
            }
        }

        // if bits is null, return all containers
        if (bits == null) {
            bits = new BitSet();
            List<Integer> allCtnIds = ServicesRegistry.getInstance()
                    .getJahiaContainersService().getctnidsInList(
                            this.getCtnListID(), this.entryLoadRequest);
            for (Integer id : allCtnIds) {
                bits.set(id);
            }
        }

        // missing container should be returned as well
        BitSet diffBitSet = new BitSet();
        diffBitSet.or(bits);
        diffBitSet.andNot(sortedBitSet);
        int l = diffBitSet.size();
        for (int i = 0; i < l; i++) {
            if (diffBitSet.get(i)) {
                v.add(i);
            }
        }
        result.addAll(v);
        return result;
    }

    // --------------------------------------------------------------------------
    protected static class DataBean {
        int ctnID = 0;
        String value = null;

        public DataBean(int ctnID, String value) {
            this.ctnID = ctnID;
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    // --------------------------------------------------------------------------
    protected static class StringComparator extends NumericStringComparator {
        boolean ASC_Ordering = true;

        public StringComparator(boolean ASC_Ordering) {
            this.ASC_Ordering = ASC_Ordering;
        }

        public int compare(Object obj1, Object obj2) throws ClassCastException {
            if (obj1 instanceof DataBean) {
                DataBean dataBean1 = (DataBean) obj1;
                DataBean dataBean2 = (DataBean) obj2;
                if (this.ASC_Ordering) {
                    return super.compare(dataBean1.value, dataBean2.value);
                } else {
                    return super.compare(dataBean2.value, dataBean1.value);
                }
            } else {
                return super.compare(obj1, obj2);
            }
        }
    }

    protected static class NumberComparator implements Comparator<DataBean>,Serializable {
        boolean ASC_Ordering = true;
        String numberFormat = "";
        public NumberComparator(boolean ASC_Ordering,String numberFormat) {
            this.ASC_Ordering = ASC_Ordering;
            this.numberFormat = numberFormat;
        }

        public int compare(DataBean dataBean1, DataBean dataBean2) {
            if (ASC_Ordering) {
                return NumberFormats.compareNumber(dataBean1.value,
                        dataBean2.value, numberFormat);
            } else {
                return NumberFormats.compareNumber(dataBean2.value,
                        dataBean1.value, numberFormat);
            }
        }
    }

    protected static class TempField {
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

    protected static class TmpData {
        private Integer ctnId;
        private Integer workflowState;
        private Integer versionId;

        public TmpData(Integer ctnId, Integer workflowState, Integer versionId) {
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

    // --------------------------------------------------------------------------
    /**
     * Returns an array of fields that are in staging.
     * 
     * @param ctnListID containerlist to get fields ids from
     * @return List of staging fields ids
     * @throws JahiaException in case of error
     */
    protected List<Integer> getStagingFields(int ctnListID)
            throws JahiaException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ctnListId", ctnListID);

        StringBuffer buff = new StringBuffer(
                "select distinct b.comp_id.id from JahiaContainer a, JahiaFieldsData b where a.listid=(:ctnListId) and a.comp_id.id = b.containerId and b.comp_id.workflowState>1 and (");
        buff.append(ContainerFilterBean.buildMultilangAndWorlflowQuery(this
                .getEntryLoadRequest()));
        buff.append(" )");

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        return fieldMgr.executeQuery(buff.toString(), parameters);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (result != null) {
            buf.append("Result:");
            buf.append(result);
        }
        return buf.toString();
    }
}
