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
package org.jahia.data.containers;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.bin.Jahia;
import org.jahia.data.fields.ExpressionMarker;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.hibernate.manager.JahiaContainerManager;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.resourcebundle.ResourceBundleMarker;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTypes;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.JahiaTools;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * Sort containers on metadata.
 * 
 * @see ContainerSorterBean
 * @see JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

@SuppressWarnings("serial")
public class ContainerMetadataSorterBean extends ContainerSorterBean {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(ContainerMetadataSorterBean.class);

    protected Integer[] siteIds;
    protected String[] containerDefinitionNames;
    protected JahiaUser user = null;
    protected JahiaSite site = null;
    protected boolean siteLevel = false;

    // --------------------------------------------------------------------------
    /**
     * Sort containers of a given container List.
     * 
     * @param ctnListID
     *            int
     * @param fieldName
     *            String
     * @param jParams
     *            ProcessingContext
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(int ctnListID, String fieldName,
            ProcessingContext jParams) throws JahiaException {
        this(ctnListID, fieldName, jParams, jParams.getEntryLoadRequest());
    }

    // --------------------------------------------------------------------------
    /**
     * Sort containers of a given container List
     * 
     * @param ctnListID
     *            , the container list id.
     * @param fieldName
     *            , field name, the field on which to sort.
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(int ctnListID, String fieldName,
            ProcessingContext jParams, EntryLoadRequest entryLoadRequest)
            throws JahiaException {
        this(ctnListID, fieldName, false, jParams, jParams
                .getEntryLoadRequest());
    }

    // --------------------------------------------------------------------------
    /**
     * Sort containers of a given container List
     * 
     * @param ctnListID
     *            , the container list id.
     * @param fieldName
     *            field name, the field on which to sort.
     * @param numberSort
     *            force field values to be converted to long representation before sorting ( if true ).
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(int ctnListID, String fieldName,
            boolean numberSort, ProcessingContext jParams,
            EntryLoadRequest entryLoadRequest) throws JahiaException {
        this(ctnListID, fieldName, numberSort, null, jParams, entryLoadRequest);
    }

    // --------------------------------------------------------------------------
    /**
     * Sort containers of a given container List
     * 
     * @param int ctnListID, the container list id.
     * @param String
     *            the field name, the field on which to sort.
     * @param boolean , force field values to be converted to long representation before sorting ( if true ).
     * @param numberFormat
     *            , @see NumberFormats , the default is NumberFormat.LONG_FORMAT
     * @param entryLoadRequest
     * @throws JahiaException
     */
    /**
     * 
     * @param ctnListID
     * @param fieldName
     * @param numberSort
     * @param jParams
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(int ctnListID, String fieldName,
            boolean numberSort, String numberFormat, ProcessingContext jParams,
            EntryLoadRequest entryLoadRequest) throws JahiaException {
        super(ctnListID, fieldName, numberSort, numberFormat, entryLoadRequest);
        this.site = jParams.getSite();
        this.user = jParams.getUser();
    }

    // --------------------------------------------------------------------------
    /**
     * Sort containers of a given container List.
     * 
     * @param containerListName
     *            , the container list name.
     * @param jParams
     *            param bean.
     * @param fieldName
     *            field name, the field on which to sort.
     */
    public ContainerMetadataSorterBean(String containerListName,
            ProcessingContext jParams, String fieldName) throws JahiaException {
        this(containerListName, jParams, fieldName, jParams
                .getEntryLoadRequest());
    }

    // --------------------------------------------------------------------------
    /**
     * Sort containers of a given container List.
     * 
     * @param containerListName
     *            , the container list name.
     * @param jParams
     *            param bean.
     * @param fieldName
     *            field name, the field on which to sort.
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(String containerListName,
            ProcessingContext jParams, String fieldName,
            EntryLoadRequest entryLoadRequest) throws JahiaException {
        super(containerListName, jParams, fieldName, entryLoadRequest);
        this.site = jParams.getSite();
        this.user = jParams.getUser();
    }

    // --------------------------------------------------------------------------
    /**
     * Sort containers of a given container List.
     * 
     * @param containerListName
     *            , the container list name.
     * @param jParams
     *            the param bean.
     * @param fieldName
     *            , the field on which to sort.
     * @param numberSort
     *            force field values to be converted to long representation before sorting ( if true ).
     */
    public ContainerMetadataSorterBean(String containerListName,
            ProcessingContext jParams, String fieldName, boolean numberSort)
            throws JahiaException {
        this(containerListName, jParams, fieldName, numberSort, jParams
                .getEntryLoadRequest());
    }

    // --------------------------------------------------------------------------
    /**
     * Sort containers of a given container List.
     * 
     * @param containerListName
     *            , the container list name.
     * @param jParams
     *            the param bean.
     * @param fieldName
     *            the field name, the field on which to sort.
     * @param numberSort
     *            force field values to be converted to long representation before sorting ( if true ).
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(String containerListName,
            ProcessingContext jParams, String fieldName, boolean numberSort,
            EntryLoadRequest entryLoadRequest) throws JahiaException {
        this(containerListName, jParams, fieldName, numberSort, null,
                entryLoadRequest);
    }

    // --------------------------------------------------------------------------
    /**
     * Sort containers of a given container List.
     * 
     * @param containerListName
     *            , the container list name.
     * @param jParams
     *            the param bean.
     * @param fieldName
     *            , the field name, the field on which to sort.
     * @param numberSort
     *            force field values to be converted to long representation before sorting ( if true ).
     * @param numberFormat
     *            , @see NumberFormats , the default is NumberFormat.LONG_FORMAT
     * @param entryLoadRequest
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(String containerListName,
            ProcessingContext jParams, String fieldName, boolean numberSort,
            String numberFormat, EntryLoadRequest entryLoadRequest)
            throws JahiaException {
        super(containerListName, jParams, fieldName, numberSort, numberFormat,
                entryLoadRequest);
        this.site = jParams.getSite();
        this.user = jParams.getUser();
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor for sorting container from a whole Jahia site or from all sites, given the container definition name.
     * 
     * @param siteId
     * @param fieldName
     * @param containerDefinitionName
     * @deprecated
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(int siteId, ProcessingContext jParams,
            String fieldName, String containerDefinitionName,
            EntryLoadRequest entryLoadRequest, boolean numberSort)
            throws JahiaException {
        this(siteId, jParams, fieldName, containerDefinitionName, numberSort,
                null, entryLoadRequest);
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor for sorting container from a whole Jahia site or from all sites, given the container definition name.
     * 
     * 
     * @param siteId
     * @param jParams
     * @param fieldName
     * @param containerDefinitionName
     * @param entryLoadRequest
     * @param numberSort
     *            , force field values to be converted to long representation before sorting ( if true ).
     * @param numberFormat
     *            , @see NumberFormats , the default is NumberFormat.LONG_FORMAT
     * @deprecated
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(int siteId, ProcessingContext jParams,
            String fieldName, String containerDefinitionName,
            boolean numberSort, String numberFormat,
            EntryLoadRequest entryLoadRequest) throws JahiaException {
        super(null, jParams, fieldName, numberSort, numberFormat,
                entryLoadRequest);
        this.fieldName = fieldName;
        this.numberSort = numberSort;
        this.isValid = true;
        if (NumberFormats.isValidFormat(numberFormat)) {
            this.numberFormat = numberFormat;
        }
        if (siteId > 0) {
            this.siteIds = new Integer[] {siteId};
        }
        if (containerDefinitionName != null
                && !"".equals(containerDefinitionName.trim())) {
            containerDefinitionNames = new String[] { containerDefinitionName };
        }
        this.siteLevel = true;
        this.user = jParams.getUser();
        this.site = jParams.getSite();
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor for sorting container from a whole Jahia site or from all sites, given the container definition name.
     * 
     * 
     * @param siteIds
     * @param jParams
     * @param fieldName
     * @param containerDefinitionNames
     * @param entryLoadRequest
     * @param numberSort
     *            , force field values to be converted to long representation before sorting ( if true ).
     * @param numberFormat
     *            , @see NumberFormats , the default is NumberFormat.LONG_FORMAT
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(int[] siteIds,
            ProcessingContext jParams, String fieldName,
            String[] containerDefinitionNames, boolean numberSort,
            String numberFormat, EntryLoadRequest entryLoadRequest)
            throws JahiaException {
        super(null, jParams, fieldName, numberSort, numberFormat,
                entryLoadRequest);
        this.fieldName = fieldName;
        this.numberSort = numberSort;
        this.isValid = true;
        if (NumberFormats.isValidFormat(numberFormat)) {
            this.numberFormat = numberFormat;
        }
        this.siteIds = new Integer[siteIds.length];
        int i = 0;
        for (int siteId : siteIds) {
            this.siteIds[i] = siteId;
            i++;
        }
        this.containerDefinitionNames = containerDefinitionNames.clone();
        this.siteLevel = true;
        this.user = jParams.getUser();
        this.site = jParams.getSite();
    }

    // --------------------------------------------------------------------------
    /**
     * Constructor for sorting container from a whole Jahia site or from all sites, given the container definition name.
     * 
     * 
     * @param siteIds
     * @param jParams
     * @param fieldName
     * @param containerDefinitionNames
     * @param entryLoadRequest
     * @param numberSort
     *            , force field values to be converted to long representation before sorting ( if true ).
     * @param numberFormat
     *            , @see NumberFormats , the default is NumberFormat.LONG_FORMAT
     * @throws JahiaException
     */
    public ContainerMetadataSorterBean(List<Integer> siteIds,
            ProcessingContext jParams, String fieldName,
            List<String> containerDefinitionNames, boolean numberSort,
            String numberFormat, EntryLoadRequest entryLoadRequest)
            throws JahiaException {
        super(null, jParams, fieldName, numberSort, numberFormat,
                entryLoadRequest);
        this.fieldName = fieldName;
        this.numberSort = numberSort;
        this.isValid = true;
        if (NumberFormats.isValidFormat(numberFormat)) {
            this.numberFormat = numberFormat;
        }
        this.siteIds = new Integer[siteIds.size()];
        siteIds.toArray(this.siteIds);
        this.containerDefinitionNames = new String[containerDefinitionNames
                .size()];
        containerDefinitionNames.toArray(this.containerDefinitionNames);
        this.siteLevel = true;
        this.user = jParams.getUser();
        this.site = jParams.getSite();
    }

    public String getContextID() {
        return this.contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }

    // --------------------------------------------------------------------------
    /**
     * 
     * @param bits
     *            , any bit position sset to true must correspond to a ctn id to include in the result. if you want all ctn ids in the
     *            result, gieve a null BitSet.
     * @return List, List of sorted ctn ids.
     */
    protected List<Integer> doOptimizedSort(BitSet bits)
            throws JahiaException {
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
        String[] fieldNames = new String[] { fieldName };
        JahiaContainerManager containerMgr = (JahiaContainerManager) context
                .getBean(JahiaContainerManager.class.getName());
        List<Object[]> ctnIds = containerMgr.getSortedContainerIds(this.ctnListID, siteIds, siteLevel,
                containerDefinitionNames, fieldNames, true,
                this.entryLoadRequest, false, false, this.ASC_Ordering, bits,
                this.getDBMaxResult());
        Set<Integer> v = new LinkedHashSet<Integer>();
        if (ctnIds != null && !ctnIds.isEmpty()) {
            List<TmpData> orderedIds = new ArrayList<TmpData>();
            Set<Integer> stagedEntries = new HashSet<Integer>();
            for (Object[] row : ctnIds) {
                Integer workflowState = (Integer) row[1];
                Integer versionId = (Integer) row[2];
                Integer ctnId = (Integer) row[3];
                
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
                v.add(data.getCtnId());
            }
        }
        result.addAll(v);
        return result;
    }

    // --------------------------------------------------------------------------
    /**
     * Load an Map of pair/value (ctnID,fieldValue) for a given ctnlist and a given fieldName.
     * 
     * 
     * @param ctnListID
     *            , the container list id
     * @param fieldName
     *            , the fieldName
     * @return List.
     */
    protected List<DataBean> getFieldValues(int ctnListID, String fieldName,
            boolean convertValueAsLong, BitSet bits) throws JahiaException {

        Set<Integer> deletedCtns = ContainerFilterBean
                .getDeletedContainersSet(ctnListID);
        Set<Integer> stagingFields = getStagingFields(ctnListID, siteIds,
                containerDefinitionNames, fieldName, this.entryLoadRequest);
        Map<String, Object> parameters = new HashMap<String, Object>();

        StringBuffer buff = new StringBuffer(
                "select distinct b.metadataOwnerId, b.comp_id.id, b.value, b.comp_id.workflowState, b.comp_id.languageCode, b.type from JahiaContainer a, JahiaFieldsData b, JahiaFieldsDef c ");
        if (this.containerDefinitionNames != null
                && this.containerDefinitionNames.length > 0) {
            buff.append(", JahiaCtnDef d ");
        }
        buff.append(" where ");

        if (!this.siteLevel && this.ctnListID > 0) {
            buff.append(" a.listid=(:listId) and ");
            parameters.put("listId", ctnListID);
        }
        if (this.siteIds != null && this.siteIds.length > 0) {
            buff
                    .append(" a.siteId in (:siteIds) and b.siteId in (:siteIds) and ");
            parameters.put("siteIds", siteIds);
        }
        if (this.containerDefinitionNames != null
                && this.containerDefinitionNames.length > 0) {
            buff.append(" a.ctndef.id=d.id and d.name in (:defNames) and ");
            parameters.put("defNames", containerDefinitionNames);
        }

        buff
                .append(" a.comp_id.id = b.metadataOwnerId AND b.type='ContentContainer' AND b.fieldDefinition.id = c.id and c.ctnName=(:fieldName) and (");
        parameters.put("fieldName", fieldName);
        buff.append(ContainerFilterBean.buildMultilangAndWorlflowQuery(
                this.entryLoadRequest, true));
        buff.append(") order by  ");
        buff.append(ContainerFilterBean.FIELD_ID);
        buff.append(",");
        buff.append(ContainerFilterBean.FIELD_WORKFLOW_STATE);

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
                String[] arrays = fieldValue.split("/");
                fieldValue = arrays[arrays.length - 1];
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
                                    otherLanguageField = dataByLanguageCodeMap
                                            .get(aField.id
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
     * Returns an array of fields that are in staging.
     * 
     * @param ctnListID
     * @param siteIds
     * @param containerDefinitionNames
     * @param fieldName
     * @param loadRequest
     * @return
     * @throws JahiaException
     */
    protected Set<Integer> getStagingFields(int ctnListID, Integer[] siteIds,
            String[] containerDefinitionNames, String fieldName,
            EntryLoadRequest loadRequest) throws JahiaException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        StringBuffer buff = new StringBuffer(
                "select distinct b.comp_id.id from JahiaContainer a, JahiaFieldsData b, JahiaFieldsDef c ");
        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            buff.append(", JahiaCtnDef d ");
        }
        buff.append(" where ");

        if (!this.siteLevel && ctnListID > 0) {
            buff.append(" a.listid=(:listId) and ");
            parameters.put("listId", ctnListID);
        }
        if (this.siteIds != null && this.siteIds.length > 0) {
            buff.append(" a.siteId=(:siteIds) and b.siteId=(:siteIds) and ");
            parameters.put("siteIds", siteIds);
        }
        if (this.containerDefinitionNames != null
                && this.containerDefinitionNames.length > 0) {
            buff.append(" a.ctndef.id=d.id and d.name in (:defNames) and ");
            parameters.put("defNames", containerDefinitionNames);
        }

        buff
                .append(" a.comp_id.id = b.metadataOwnerId and b.type='ContentContainer' and b.fieldDefinition.id = c.id and c.ctnName=(:fieldName) and b.comp_id.workflowState>1 and (");
        parameters.put("fieldName", fieldName);
        buff.append(ContainerFilterBean
                .buildMultilangAndWorlflowQuery(entryLoadRequest));
        buff.append(") ");

        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                .getBean(JahiaFieldsDataManager.class.getName());

        List<Integer> queryResults = fieldMgr.executeQuery(buff.toString(),
                parameters);

        return new HashSet<Integer>(queryResults);
    }

}
