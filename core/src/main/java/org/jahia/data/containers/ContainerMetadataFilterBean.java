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
//
//
//

package org.jahia.data.containers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.data.fields.JahiaDateField;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SoapParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
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
public class ContainerMetadataFilterBean extends ContainerFilterBean {

    private static final long serialVersionUID = -8080480562281179540L;

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContainerMetadataFilterBean.class);

    private JahiaUser user = null;
    private JahiaSite site = null;

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param metadataName, the metadata name of the field on which to apply filtering.
     * @param entryLoadRequest
     */
    public ContainerMetadataFilterBean(String metadataName,
                                       ProcessingContext jParams,
                                       EntryLoadRequest entryLoadRequest){
        this(metadataName,false,jParams,entryLoadRequest);
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param metadataName, the metadata name of the field on which to apply filtering.
     * @param numberFiltering, if true force to convert filed value to long representation
     * @param entryLoadRequest
     */
    public ContainerMetadataFilterBean(String metadataName,
                                       boolean numberFiltering,
                                       ProcessingContext jParams,
                                       EntryLoadRequest entryLoadRequest){
        this(metadataName,numberFiltering,false,jParams,entryLoadRequest);
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param metadataName, the metadata name of the field on which to apply filtering.
     * @param numberFiltering, if true force to convert filed value to long representation
     * @param multipleFieldValue
     * @param entryLoadRequest
     */
    public ContainerMetadataFilterBean(String metadataName,
                                       boolean numberFiltering,
                                       boolean multipleFieldValue,
                                       ProcessingContext jParams,
                                       EntryLoadRequest entryLoadRequest){
        this(metadataName,numberFiltering,null,multipleFieldValue,jParams,entryLoadRequest);
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param metadataName, the metadata name of the field on which to apply filtering.
     * @param numberFiltering, if true force to convert filed value to long representation
     * @param multipleFieldValue
     * @param entryLoadRequest
     */
    public ContainerMetadataFilterBean(String metadataName,
                                       boolean numberFiltering,
                                       String numberFormat,
                                       boolean multipleFieldValue,
                                       ProcessingContext jParams,
                                       EntryLoadRequest entryLoadRequest){
        super(metadataName, numberFiltering, numberFormat,multipleFieldValue, entryLoadRequest);
        this.user = jParams.getUser();
        this.site = jParams.getSite();
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering.
     * The expected result is a bit set of matching container ids.
     *
     * @param ctnListID, the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    public BitSet doFilter(int ctnListID)
    throws JahiaException
    {
        if ( (this.getFieldName()==null)
              || (this.getFieldName().trim().equals(""))
              || (this.getClauses()==null)
              || (this.getClauses().size()==0) )
        {
            return null;
        }

        BitSet result = null;

        if ( this.getNumberFiltering() )
        {
            result = doNumberValueFiltering(ctnListID);
        } else if ( !this.multipleFieldValue && this.optimizedMode ) {
            result = this.doOptimizedFiltering(ctnListID);
        } else {
            result = this.doStringFiltering(ctnListID);
        }
        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering for NUMBER like fields.
     * Container field values are loaded and converted to long representation before
     * filtering comparison.
     *
     * The expected result is a bit set of matching container ids.
     *
     * @param ctnListID, the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    private BitSet doNumberValueFiltering(int ctnListID)
    throws JahiaException
    {

        //JahiaConsole.println(CLASS_NAME+".doNumberValueFiltering","Started for ctnlist ["+ctnListID+"]");
        BitSet result = new BitSet();

        if ( (this.getFieldName()==null)
              || (this.getFieldName().trim().equals(""))
              || (this.getClauses()==null)
              || (this.getClauses().size()==0) )
        {
            return null;
        }

        Map<ObjectKey, String> datas = getFieldValues(this.getFieldName());
        //JahiaConsole.println(CLASS_NAME+".doNumberValueFiltering","Nb datas : " + datas.size());

        if ( datas == null || datas.size()==0 ){
            return result;
        }

        FilterClause fClause = null;
        Iterator<ObjectKey> keys = datas.keySet().iterator();
        ObjectKey objectKey = null;
        String S = null;
        ContentObject contentObject = null;
        ContentContainerList containerList = null;
        Map<Integer, BitSet> bitsByClauseMap = new HashMap<Integer, BitSet>();
        BitSet bitsByClause = null;
        while ( keys.hasNext() )
        {
            objectKey = keys.next();
            S = datas.get(objectKey);
            boolean match = false;
            int size = this.getClauses().size();
            int i = 0;
            while ( (i<size) && !(this.isMultiClauseANDLogic() || this.isMultiClauseANDLogic() && !match) )
            {
                bitsByClause = bitsByClauseMap.get(new Integer(i));
                if (bitsByClause == null){
                    bitsByClause = new BitSet();
                    bitsByClauseMap.put(new Integer(i),bitsByClause);
                }
                fClause = (FilterClause)this .getClauses().get(i);
                if ( fClause != null && fClause.isValid() )
                {
                    match = fClause.compareNumber(S,this.numberFormat);
                    if ( match )
                    {
                        contentObject = ContentObject
                                        .getContentObjectFromMetadata(objectKey);
                        if ( contentObject != null &&
                             contentObject instanceof ContentContainer ){
                            containerList = (ContentContainerList)((ContentContainer)
                                                                   contentObject)
                                            .getParent(this.getEntryLoadRequest());
                            if ( containerList != null && containerList.getID()==ctnListID){
                                result.set(contentObject.getID());
                                bitsByClause.set(contentObject.getID());
                            }
                        }
                    }
                }
                i++;
            }
        }
        if (!this.isMultiClauseANDLogic()){
            return result;
        } else {
            Iterator<BitSet> it = bitsByClauseMap.values().iterator();
            result = null;
            while (it.hasNext()){
                bitsByClause = it.next();
                if (result == null){
                    result = bitsByClause;
                } else {
                    result.and(bitsByClause);
                }
            }
        }
        if (result == null){
            result = new BitSet();
        }

        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering by loading field values in memories first.
     *
     * The expected result is a bit set of matching container ids.
     *
     * @param ctnListID, the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    private BitSet doStringFiltering(int ctnListID)
    throws JahiaException
    {

        //JahiaConsole.println(CLASS_NAME+".doNumberValueFiltering","Started for ctnlist ["+ctnListID+"]");
        BitSet result = new BitSet();

        if ( (this.getFieldName()==null)
              || (this.getFieldName().trim().equals(""))
              || (this.getClauses()==null)
              || (this.getClauses().size()==0) )
        {
            return null;
        }

        Map<ObjectKey, String> datas = getFieldValues(this.getFieldName());
        //JahiaConsole.println(CLASS_NAME+".doNumberValueFiltering","Nb datas : " + datas.size());

        if ( datas == null || datas.size()==0 ){
            return result;
        }

        FilterClause fClause = null;
        Iterator<ObjectKey> keys = datas.keySet().iterator();
        ObjectKey objectKey = null;
        String S = null;
        ContentObject contentObject = null;
        ContentContainerList containerList = null;
        Map<Integer, BitSet> bitsByClauseMap = new HashMap<Integer, BitSet>();
        BitSet bitsByClause = null;
        while ( keys.hasNext() )
        {
            objectKey = keys.next();
            S = datas.get(objectKey);
            boolean match = false;
            String val = "";
            String[] vals = JahiaTools.getTokens(S,JahiaField.MULTIPLE_VALUES_SEP);
            int nbVals = vals.length;
            int i = 0;
            while ( (i<nbVals) && (this.isMultiClauseANDLogic() || (!this.isMultiClauseANDLogic() && !match)) ){
                val = vals[i];
                int size = this.getClauses().size();
                int j = 0;
                while ( (j<size) && (this.isMultiClauseANDLogic() || (!this.isMultiClauseANDLogic() && !match)) )
                {
                    bitsByClause = bitsByClauseMap.get(new Integer(j));
                    if (bitsByClause == null){
                        bitsByClause = new BitSet();
                        bitsByClauseMap.put(new Integer(j),bitsByClause);
                    }
                    fClause = (FilterClause)this.getClauses().get(j);
                    if ( fClause != null && fClause.isValid() )
                    {
                        match = fClause.compare(val);
                        if ( match )
                        {
                            contentObject = ContentObject
                                            .getContentObjectFromMetadata(objectKey);
                            if ( contentObject != null &&
                                 contentObject instanceof ContentContainer ){

                                containerList = (ContentContainerList) ( (
                                    ContentContainer)
                                    contentObject)
                                                .getParent(this.getEntryLoadRequest());
                                if (containerList != null &&
                                    containerList.getID() == ctnListID) {
                                    result.set(contentObject.getID());
                                    bitsByClause.set(contentObject.getID());
                                }
                            }
                        }
                    }
                    j++;
                }
                i++;
            }
        }
        if (!this.isMultiClauseANDLogic()){
            return result;
        } else {
            Iterator<BitSet> it = bitsByClauseMap.values().iterator();
            result = null;
            while (it.hasNext()){
                bitsByClause = it.next();
                if (result == null){
                    result = bitsByClause;
                } else {
                    result.and(bitsByClause);
                }
            }
        }
        if (result == null){
            result = new BitSet();
        }

        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Perform the filtering in optimized mode.
     *
     * The expected result is a bit set of matching container ids.
     *
     * @param ctnListID, the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    private BitSet doOptimizedFiltering(int ctnListID)
    throws JahiaException
    {

        //JahiaConsole.println(CLASS_NAME+".doNumberValueFiltering","Started for ctnlist ["+ctnListID+"]");
        BitSet result = new BitSet();

        if ( (this.getFieldName()==null)
              || (this.getFieldName().trim().equals(""))
              || (this.getClauses()==null)
              || (this.getClauses().size()==0) )
        {
            return null;
        }

        if ( this.entryLoadRequest.isCurrent() ){
            return doOptimizedQueryFiltering(ctnListID,true,false);
        } else if (this.entryLoadRequest.isStaging()){
            result = doOptimizedQueryFiltering(ctnListID,false,false);
            BitSet invertedStagingResult = doOptimizedQueryFiltering(ctnListID,false,true);
            BitSet liveResult = doOptimizedQueryFiltering(ctnListID,true,false);
            liveResult.andNot(invertedStagingResult);
            result.or(liveResult);
        }
        return result;
    }

    private BitSet doOptimizedQueryFiltering(int ctnListID, boolean useLiveMode, boolean invertComparator)
    throws JahiaException
    {
        BitSet result = new BitSet();
        StringBuffer buff = new StringBuffer(1024);
        Map<String, Object> parameters = new HashMap<String, Object>();

        List<Integer> fieldDefIDs = ServicesRegistry.getInstance().getJahiaFieldService()
                .getFieldDefinitionNameFromCtnType(this.fieldName,true);
        if ( fieldDefIDs == null || fieldDefIDs.isEmpty() ){
            return result;
        }
        parameters.put("contentType", ContentContainerKey.CONTAINER_TYPE);


        buff.append(" SELECT f.metadataOwnerId FROM JahiaFieldsData f ");
        buff.append(", JahiaContainer c ");
        parameters.put("ctListId",new Integer(ctnListID));
        buff.append(" WHERE (");
        int index = 0;
        for (Integer fieldDefID : fieldDefIDs){
            if (index > 0){
                buff.append(" OR ");
            }
            buff.append(" f.fieldDefinition.id = " + fieldDefID.toString());
            index++;
        }
        buff.append(" ) ");
        buff.append(" AND f.metadataOwnerType= :contentType ");

        buff.append(" AND (");
        Iterator<FilterClause> clauses = this.getClauses().iterator();
        FilterClause clause = null;
        index = 0;
        while(clauses.hasNext()){
            clause = clauses.next();
            if ( clause.isValid() ){
                if ( clause.isRangeClause() ){
                    buff.append("f.value ").append(FilterClause.getInvertedComp(clause.getLowerComp(),invertComparator))
                            .append( ":lowerFieldValue"+String.valueOf(index));
                    parameters.put("lowerFieldValue"+String.valueOf(index),clause.getLowerValue());
                    buff.append(" AND ");
                    buff.append("f.value ").append(FilterClause.getInvertedComp(clause.getUpperComp(),invertComparator))
                            .append( ":upperFieldValue"+String.valueOf(index));
                    parameters.put("upperFieldValue"+String.valueOf(index),clause.getUpperValue());
                } else if (clause.getComp().equals(ContainerFilterBean.COMP_STARTS_WITH)) {
                    String comp = " LIKE ";
                    if (invertComparator){
                        comp = " NOT LIKE ";
                    }
                    buff.append(" ( ");
                    for (int i=0; i<clause.getValues().length; i++){
                        buff.append(getLikeComparison(clause.getValues()[i],"f.value",comp));
                        if ( i<clause.getValues().length-1){
                            if (invertComparator){
                                buff.append(" AND ");
                            } else {
                                buff.append(" OR ");
                            }
                        }
                    }
                    buff.append(" ) ");
                } else {
                    buff.append(" ( ");
                    for (int i=0; i<clause.getValues().length; i++){
                        buff.append("f.value ").append(FilterClause.getInvertedComp(clause.getComp(),invertComparator))
                        .append( ":fieldValue"
                                +String.valueOf(index)+String.valueOf(i));
                        parameters.put("fieldValue"+String.valueOf(index)+String.valueOf(i),clause.getValues()[i]);
                        if ( i<clause.getValues().length-1){
                            if (invertComparator){
                                buff.append(" AND ");
                            } else {
                                buff.append(" OR ");
                            }
                        }
                    }
                    buff.append(" ) ");
                }
                if ( clauses.hasNext() ){
                    if (this.isMultiClauseANDLogic()){
                        buff.append(" AND ");
                    } else {
                        buff.append(" OR ");
                    }
                }
            }
            index++;
        }
        buff.append(") ");

        buff.append(" AND c.listid= :ctListId ");
        buff.append(" AND c.comp_id.id=f.metadataOwnerId ");
        boolean stagingOnly = this.entryLoadRequest.isStaging();
        appendFieldMultilangAndWorkflowParams(buff, parameters, this.entryLoadRequest, useLiveMode, false, stagingOnly);
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager)
                context.getBean(JahiaFieldsDataManager.class.getName());
        List<Integer> queryResult = fieldMgr.<Integer>executeQuery(buff.toString(),parameters);
        for (Integer ctnID : queryResult){
            result.set(ctnID.intValue());
        }
        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Load an Map of pair/value (objectKey,fieldValue) for a given metadataName.
     *
     * @param metadataName, the metadataName
     * @return Map.
     */
    private Map<ObjectKey, String> getFieldValues(String metadataName)
    throws JahiaException
    {

        Map<ObjectKey, String> datas = new HashMap<ObjectKey, String>();
        List<Integer> fieldDefIds = ServicesRegistry.getInstance().getJahiaFieldService()
                .getFieldDefinitionNameFromCtnType(metadataName,true);
        Integer defId = null;
        for (Integer id : fieldDefIds){
            defId = id;
            break;
        }
        List<ObjectKey> objectKeys = new ArrayList<ObjectKey>();
        if (defId != null){
            try {
                JahiaFieldDefinition fieldDef = (JahiaFieldDefinition)JahiaFieldDefinition.getChildInstance(String.valueOf(defId));
                if (fieldDef != null){
                    objectKeys = ServicesRegistry.getInstance().getMetadataService()
                                     .getMetadataByName(fieldDef.getNodeDefinition().getLocalName());
                }
            } catch ( Throwable t ){
                logger.debug(t);
            }
        }
        ObjectKey objectKey = null;
        String fieldValue = null;
        SoapParamBean jParams = new SoapParamBean(site, user);

        int fieldId = 0;
        JahiaField jahiaField = null;
        Object objectItem = null;
        for ( int i=0 ; i<objectKeys.size() ; i++ ){
            fieldValue = null;
            objectKey = objectKeys.get(i);
            fieldId = Integer.parseInt(objectKey.getIDInType());
            //field = ContentField.getField(fieldId);
            //fieldValue = field.getValue(coes);
            jahiaField = ServicesRegistry.getInstance().getJahiaFieldService()
                         .loadField(fieldId,LoadFlags.ALL,jParams,this.getEntryLoadRequest());
            if ( jahiaField != null ){
                if ( jahiaField instanceof JahiaDateField ){
                    objectItem = jahiaField.getObject();
                    if ( objectItem != null ){
                        fieldValue = (String)objectItem;
                    }
                } else {
                    fieldValue = jahiaField.getRawValue();
                }
                if (fieldValue != null) {
                    datas.put(objectKey, fieldValue);
                }
            }
        }
        return datas;
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     *
     * The expected result is a bit set of matching container ids.
     *
     * If siteId = -1 , returns results from all sites
     *
     * If the containerDefinitionName is null, return result from all containers
     * no regards to it definition !
     *
     * @param siteId
     * @param containerDefinitionName
     * @param listId
     *
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(int siteId, String containerDefinitionName, int listId)
    throws JahiaException
    {
        Integer[] siteIds = null;
        if (siteId != -1){
            siteIds = new Integer[]{new Integer(siteId)};
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null && !"".equals(containerDefinitionName.trim())){
            containerDefinitionNames = new String[]{containerDefinitionName};
        }
        return doFilterBySite(siteIds,containerDefinitionNames,listId);
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     *
     * The expected result is a bit set of matching container ids.
     *
     * @param siteIds
     * @param containerDefinitionNames
     * @param listId
     *
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(Integer[] siteIds, String[] containerDefinitionNames, int listId)
    throws JahiaException
    {
        if ( (this.getFieldName()==null)
              || (this.getFieldName().trim().equals(""))
              || (this.getClauses()==null)
              || (this.getClauses().size()==0) )
        {
            return null;
        }

        BitSet result = null;

        if ( this.getNumberFiltering() )
        {
            result = doNumberValueFilteringBySite(siteIds, containerDefinitionNames);
        } else if ( this.optimizedMode) {
            result = this.doOptimizedFiltering(siteIds,containerDefinitionNames);
        } else {
            result = this.doStringFiltering(siteIds,containerDefinitionNames);
        }
        return result;
    }

    protected BitSet doOptimizedFiltering(Integer[] siteIds, String[] containerDefinitionNames)
    throws JahiaException {
        BitSet result = null;
        if ( (this.getFieldName()==null)
              || (this.getFieldName().trim().equals(""))
              || (this.getClauses()==null)
              || (this.getClauses().size()==0) )
        {
            return null;
        }

        if ( this.entryLoadRequest.isCurrent() ){
            return doOptimizedQueryFiltering(siteIds,containerDefinitionNames,true,false);
        } else if (this.entryLoadRequest.isStaging()){
            result = doOptimizedQueryFiltering(siteIds,containerDefinitionNames,false,false);
            BitSet invertedStagingResult = doOptimizedQueryFiltering(siteIds,containerDefinitionNames,false,true);
            BitSet liveResult = doOptimizedQueryFiltering(siteIds,containerDefinitionNames,true,false);
            liveResult.andNot(invertedStagingResult);
            result.or(liveResult);
        }
        return result;
    }

    private BitSet doOptimizedQueryFiltering(Integer[] siteIDs, String[] containerDefinitionNames,
                                             boolean useLiveMode, boolean invertComparator)
    throws JahiaException
    {
        BitSet result = new BitSet();
        StringBuffer buff = new StringBuffer(1024);

        List<Integer> fieldDefIDs = ServicesRegistry.getInstance().getJahiaFieldService()
                .getFieldDefinitionNameFromCtnType(this.fieldName,true);
        if ( fieldDefIDs == null || fieldDefIDs.isEmpty() ){
            return result;
        }


        List<Integer> siteIdsList = null;
        if (siteIDs!=null && siteIDs.length>0){
            siteIdsList = Arrays.asList(siteIDs);
        }

        List<Integer> ctnDefIDs = null;
        if (containerDefinitionNames != null && containerDefinitionNames.length>0){
            ctnDefIDs = new ArrayList<Integer>();
            List<String> ctnDefNamesList = Arrays.asList(containerDefinitionNames);
            try {
                for (int defId : ServicesRegistry.getInstance().getJahiaContainersService()
                        .getAllContainerDefinitionIDs()) {
                    JahiaContainerDefinition currentDefinition =
                            ServicesRegistry.getInstance().getJahiaContainersService()
                                    .loadContainerDefinition (defId);
                    if (currentDefinition != null && ctnDefNamesList.contains(currentDefinition.getName())){
                        if ( siteIdsList==null || siteIdsList.contains(new Integer(currentDefinition.getJahiaID())) ){
                            ctnDefIDs.add(new Integer(currentDefinition.getID()));
                        }
                    }
                }
            } catch( Throwable t ){
                logger.debug("Exception occured retrieving container definition",t);
                return result;
            }
            if (ctnDefIDs.isEmpty()){
                return result;
            }
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        buff.append(" SELECT f.metadataOwnerId FROM JahiaFieldsData f ");

        boolean addSiteParam = false;
        boolean addContainerDefinitionNameParam = false;
        if ( containerDefinitionNames != null && containerDefinitionNames.length>0 ){
            addContainerDefinitionNameParam = true;
        }
        if (addSiteParam || addContainerDefinitionNameParam){
            buff.append(", JahiaContainer c ");
        }
        buff.append(" WHERE ");
        if (!fieldDefIDs.isEmpty()) {
            buff.append(" f.fieldDefinition.id IN (:fieldDefIDs)  ");
            parameters.put("fieldDefIDs", fieldDefIDs);
        }

        buff.append(" AND f.metadataOwnerType= :contentType ");
        parameters.put("contentType", ContentContainerKey.CONTAINER_TYPE);
        
        buff.append(" AND (");
        Iterator<FilterClause> clauses = this.getClauses().iterator();
        FilterClause clause = null;
        int index = 0;
        while(clauses.hasNext()){
            clause = (FilterClause)clauses.next();
            if ( clause.isValid() ){
                if ( clause.isRangeClause() ){
                    buff.append("f.value ").append(FilterClause.getInvertedComp(clause.getLowerComp(),invertComparator))
                            .append( ":lowerFieldValue"+String.valueOf(index));
                    parameters.put("lowerFieldValue"+String.valueOf(index),clause.getLowerValue());
                    buff.append(" AND ");
                    buff.append("f.value ").append(FilterClause.getInvertedComp(clause.getUpperComp(),invertComparator))
                            .append( ":upperFieldValue"+String.valueOf(index));
                    parameters.put("upperFieldValue"+String.valueOf(index),clause.getUpperValue());
                } else if (clause.getComp().equals(ContainerFilterBean.COMP_STARTS_WITH)) {
                    String comp = " LIKE ";
                    if (invertComparator){
                        comp = " NOT LIKE ";
                    }
                    buff.append(" ( ");
                    for (int i=0; i<clause.getValues().length; i++){
                        buff.append(getLikeComparison(clause.getValues()[i],"f.value",comp));
                        if ( i<clause.getValues().length-1){
                            if (invertComparator){
                                buff.append(" AND ");
                            } else {
                                buff.append(" OR ");
                            }
                        }
                    }
                    buff.append(" ) ");
                } else {
                    buff.append(" ( ");
                    for (int i=0; i<clause.getValues().length; i++){
                        buff.append("f.value ").append(FilterClause.getInvertedComp(clause.getComp(),invertComparator))
                        .append( ":fieldValue"
                                +String.valueOf(index)+String.valueOf(i));
                        parameters.put("fieldValue"+String.valueOf(index)+String.valueOf(i),clause.getValues()[i]);
                        if ( i<clause.getValues().length-1){
                            if (invertComparator){
                                buff.append(" AND ");
                            } else {
                                buff.append(" OR ");
                            }
                        }
                    }
                    buff.append(" ) ");
                }
                if ( clauses.hasNext() ){
                    if (this.isMultiClauseANDLogic()){
                        buff.append(" AND ");
                    } else {
                        buff.append(" OR ");
                    }
                }
            }
            index++;
        }
        buff.append(") ");
        
        if (siteIDs!=null && siteIDs.length>0){
            buff.append(" AND c.siteId IN (:siteIDs) ");
            parameters.put("siteIDs", siteIDs);            
        }
        
        if (addContainerDefinitionNameParam){
            buff.append(" AND c.comp_id.id=f.metadataOwnerId ");
        }
        
        if ( addContainerDefinitionNameParam ){
            buff.append(" AND c.ctndef.id IN (:ctnDefIDs) ");
            parameters.put("ctnDefIDs", ctnDefIDs);            
        }
        
        boolean stagingOnly = this.entryLoadRequest.isStaging();
        appendFieldMultilangAndWorkflowParams(buff, parameters, this.entryLoadRequest, useLiveMode, false, stagingOnly);
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager)
                context.getBean(JahiaFieldsDataManager.class.getName());
        
        for (Integer ctnID : fieldMgr.<Integer>executeQuery(buff.toString(),parameters)){
            result.set(ctnID.intValue());
        }
        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering for NUMBER like fields.
     * Container field values are loaded and converted to long representation before
     * filtering comparison.
     *
     * The expected result is a bit set of matching container ids for a given siteId
     * If siteId = -1 , return result from all sites
     *
     * @param siteId
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    protected BitSet doNumberValueFilteringBySite(int siteId,
            String containerDefinitionName)
    throws JahiaException
    {
        Integer[] siteIds = null;
        if (siteId != -1){
            siteIds = new Integer[]{new Integer(siteId)};
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null && !"".equals(containerDefinitionName.trim())){
            containerDefinitionNames = new String[]{containerDefinitionName};
        }
        return doNumberValueFilteringBySite(siteIds,containerDefinitionNames);
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering for NUMBER like fields.
     * Container field values are loaded and converted to long representation before
     * filtering comparison.
     *
     * @param siteIDs
     * @param containerDefinitionNames
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    protected BitSet doNumberValueFilteringBySite(Integer[] siteIDs,
            String[] containerDefinitionNames)
    throws JahiaException
    {

        BitSet result = new BitSet();

        if ( (this.getFieldName()==null)
              || (this.getFieldName().trim().equals(""))
              || (this.getClauses()==null)
              || (this.getClauses().size()==0) )
        {
            return null;
        }

        Map<ObjectKey, String> datas = this.getFieldValuesBySite(siteIDs, this.getFieldName(), containerDefinitionNames);

        if ( datas == null || datas.size()==0 ){
            return result;
        }

        FilterClause fClause = null;
        Iterator<ObjectKey> keys = datas.keySet().iterator();
        ObjectKey objectKey = null;
        String S = null;
        String v = null;
        ContentObject contentObject = null;

        List<Integer> siteIDsList = null;
        if (siteIDs!=null && siteIDs.length>0){
            siteIDsList = new ArrayList<Integer>();
            for(int i=0; i<siteIDs.length; i++){
                siteIDsList.add(siteIDs[i]);
            }
        }

        List<String> containerDefinitionNamesList = null;
        if (containerDefinitionNames!=null && containerDefinitionNames.length>0){
            containerDefinitionNamesList = new ArrayList<String>();
            for(int i=0; i<siteIDs.length; i++){
                containerDefinitionNamesList.add(containerDefinitionNames[i]);
            }
        }
        Map<Integer, BitSet> bitsByClauseMap = new HashMap<Integer, BitSet>();
        BitSet bitsByClause = null;
        while ( keys.hasNext() )
        {
            objectKey = keys.next();
            S = datas.get(objectKey);
            v = null;
            boolean match = false;
            String[] vals = JahiaTools.getTokens(S,JahiaField.MULTIPLE_VALUES_SEP);
            int nbVals = vals.length;
            int i = 0;
            while ( (i<nbVals) && (this.isMultiClauseANDLogic() || (!this.isMultiClauseANDLogic() && !match)) ){
                v = vals[i];
                i++;
                int size = this.getClauses().size();
                int j = 0;
                while ( (j<size) && (this.isMultiClauseANDLogic() || (!this.isMultiClauseANDLogic() && !match)) )
                {
                    bitsByClause = bitsByClauseMap.get(new Integer(j));
                    if (bitsByClause == null){
                        bitsByClause = new BitSet();
                        bitsByClauseMap.put(new Integer(j),bitsByClause);
                    }
                    fClause = (FilterClause)this.getClauses().get(j);
                    if ( fClause != null && fClause.isValid() )
                    {
                        match = fClause.compareNumber(v,this.numberFormat);
                        if ( match )
                        {
                            contentObject = ContentObject
                                            .getContentObjectFromMetadata(objectKey);
                            if ( contentObject != null &&
                                 contentObject instanceof ContentContainer ){
                                if ( siteIDsList == null || siteIDsList.contains(new Integer(contentObject.getSiteID())) ){
                                    try {
                                        ContentDefinition contentDefinition =
                                            ContentDefinition.
                                            getContentDefinitionInstance(
                                            contentObject.getDefinitionKey(this
                                            .getEntryLoadRequest()));
                                        if ( containerDefinitionNamesList == null
                                                || containerDefinitionNamesList.contains(contentDefinition.getName()) ){
                                            result.set(contentObject.getID());
                                            bitsByClause.set(contentObject.getID());
                                        }
                                    } catch ( Exception t ){
                                        logger.debug(t);
                                    }
                                }
                            }
                        }
                    }
                    j++;
                }
            }
        }
        if (!this.isMultiClauseANDLogic()){
            return result;
        } else {
            Iterator<BitSet> it = bitsByClauseMap.values().iterator();
            result = null;
            while (it.hasNext()){
                bitsByClause = it.next();
                if (result == null){
                    result = bitsByClause;
                } else {
                    result.and(bitsByClause);
                }
            }
        }
        if (result == null){
            result = new BitSet();
        }

        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering by loading field values in memories first.
     *
     * The expected result is a bit set of matching container ids.
     *
     * @param siteId
     * @param containerDefinitionName
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    protected BitSet doStringFiltering(int siteId,
            String containerDefinitionName)
    throws JahiaException
    {
        Integer[] siteIds = null;
        if (siteId != -1){
            siteIds = new Integer[]{new Integer(siteId)};
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null && !"".equals(containerDefinitionName.trim())){
            containerDefinitionNames = new String[]{containerDefinitionName};
        }
        return doStringFiltering(siteIds,containerDefinitionNames);
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering by loading field values in memories first.
     *
     * The expected result is a bit set of matching container ids.
     *
     * @param siteIDs
     * @param containerDefinitionNames
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    protected BitSet doStringFiltering(Integer[] siteIDs,
            String[] containerDefinitionNames)
    throws JahiaException
    {

        BitSet result = new BitSet();

        if ( (this.getFieldName()==null)
              || (this.getFieldName().trim().equals(""))
              || (this.getClauses()==null)
              || (this.getClauses().size()==0) )
        {
            return null;
        }

        Map<ObjectKey, String> datas = this.getFieldValuesBySite(siteIDs,this.getFieldName(),containerDefinitionNames);

        if ( datas == null || datas.size()==0 ){
            return result;
        }

        List<Integer> siteIDsList = null;
        if (siteIDs!=null && siteIDs.length>0){
            siteIDsList = new ArrayList<Integer>();
            for(int i=0; i<siteIDs.length; i++){
                siteIDsList.add(siteIDs[i]);
            }
        }

        List<String> containerDefinitionNamesList = null;
        if (containerDefinitionNames!=null && containerDefinitionNames.length>0){
            containerDefinitionNamesList = new ArrayList<String>();
            for(int i=0; i<siteIDs.length; i++){
                containerDefinitionNamesList.add(containerDefinitionNames[i]);
            }
        }

        FilterClause fClause = null;
        Iterator<ObjectKey> keys = datas.keySet().iterator();
        ObjectKey objectKey = null;
        String S = null;
        ContentObject contentObject = null;
        Map<Integer, BitSet> bitsByClauseMap = new HashMap<Integer, BitSet>();
        BitSet bitsByClause = null;
        while ( keys.hasNext() )
        {
            objectKey = keys.next();
            S = datas.get(objectKey);
            boolean match = false;
            String val = "";
            String[] vals = JahiaTools.getTokens(S,JahiaField.MULTIPLE_VALUES_SEP);
            int nbVals = vals.length;
            int i = 0;
            while ( (i<nbVals) && (this.isMultiClauseANDLogic() || (!this.isMultiClauseANDLogic() && !match)) ){
                val = vals[i];
                int size = this.getClauses().size();
                int j = 0;
                while ( (j<size) && (this.isMultiClauseANDLogic() || (!this.isMultiClauseANDLogic() && !match)) )
                {
                    bitsByClause = bitsByClauseMap.get(new Integer(j));
                    if (bitsByClause == null){
                        bitsByClause = new BitSet();
                        bitsByClauseMap.put(new Integer(j),bitsByClause);
                    }
                    fClause = (FilterClause)this.getClauses().get(j);
                    if ( fClause != null && fClause.isValid() )
                    {
                        match = fClause.compare(val);
                        if ( match )
                        {
                            contentObject = ContentObject
                                            .getContentObjectFromMetadata(objectKey);
                            if ( contentObject != null &&
                                 contentObject instanceof ContentContainer ){
                                if (siteIDsList == null || siteIDsList.contains(new Integer(contentObject.getSiteID())) ){
                                    try {
                                        ContentDefinition contentDefinition =
                                            ContentDefinition.
                                            getContentDefinitionInstance(
                                            contentObject.getDefinitionKey(this
                                            .getEntryLoadRequest()));
                                        if ( containerDefinitionNamesList == null
                                                || containerDefinitionNamesList.contains(contentDefinition.getName())){
                                            result.set(contentObject.getID());
                                            bitsByClause.set(contentObject.getID());
                                        }
                                    } catch ( Exception t ){
                                        logger.debug(t);
                                    }
                                }
                            }
                        }
                    }
                    j++;
                }
                i++;
            }
        }
        if (!this.isMultiClauseANDLogic()){
            return result;
        } else {
            Iterator<BitSet> it = bitsByClauseMap.values().iterator();
            result = null;
            while (it.hasNext()){
                bitsByClause = it.next();
                if (result == null){
                    result = bitsByClause;
                } else {
                    result.and(bitsByClause);
                }
            }
        }
        if (result == null){
            result = new BitSet();
        }

        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Load an Map of pair/value (objectKey,fieldValue) for a given metadataName.
     *
     * If siteId = -1 , return result from all sites
     *
     * @param siteId
     * @param metadataName
     * @param containerDefinitionName
     * @return
     * @throws JahiaException
     */
    protected Map<ObjectKey, String> getFieldValuesBySite(int siteId,
            String metadataName,
            String containerDefinitionName)
    throws JahiaException
    {
        Integer[] siteIds = null;
        if (siteId != -1){
            siteIds = new Integer[]{new Integer(siteId)};
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null && !"".equals(containerDefinitionName.trim())){
            containerDefinitionNames = new String[]{containerDefinitionName};
        }
        return getFieldValuesBySite(siteIds,metadataName,containerDefinitionNames);
    }

    //--------------------------------------------------------------------------
    /**
     * Load an Map of pair/value (objectKey,fieldValue) for a given metadataName.
     *
     * @param siteIDs
     * @param metadataName
     * @param containerDefinitionNames
     * @return
     * @throws JahiaException
     */
    protected Map<ObjectKey, String> getFieldValuesBySite(Integer[] siteIDs,
            String metadataName,
            String[] containerDefinitionNames)
    throws JahiaException
    {


        Map<ObjectKey, String> datas = new HashMap<ObjectKey, String>();

        List<Integer> siteIDsList = null;
        if (siteIDs!=null && siteIDs.length>0){
            siteIDsList = new ArrayList<Integer>();
            for(int i=0; i<siteIDs.length; i++){
                siteIDsList.add(siteIDs[i]);
            }
        }

        List<Integer> fieldDefIds = ServicesRegistry.getInstance().getJahiaFieldService()
                .getFieldDefinitionNameFromCtnType(metadataName,true);
        Integer defId = null;
        for (Integer id : fieldDefIds){
            defId = id;
            break;
        }
        List<ObjectKey> objectKeys = new ArrayList<ObjectKey>();
        if (defId != null){
            try {
                JahiaFieldDefinition fieldDef = (JahiaFieldDefinition)JahiaFieldDefinition.getChildInstance(String.valueOf(defId));
                if (fieldDef != null){
                    objectKeys = ServicesRegistry.getInstance().getMetadataService()
                                     .getMetadataByName(fieldDef.getNodeDefinition().getLocalName());
                }
            } catch ( Throwable t ){
                logger.debug(t);
            }
        }

        ObjectKey objectKey = null;
        String fieldValue = null;
        SoapParamBean jParams = new SoapParamBean(site, user);
        int fieldId = 0;
        JahiaField jahiaField = null;
        Object objectItem = null;
        for ( int i=0 ; i<objectKeys.size() ; i++ ){
            objectKey = objectKeys.get(i);
            fieldId = Integer.parseInt(objectKey.getIDInType());
            jahiaField = ServicesRegistry.getInstance().getJahiaFieldService()
                         .loadField(fieldId,LoadFlags.ALL,jParams,this.getEntryLoadRequest());
            if ( jahiaField != null ){
                if (siteIDsList == null || siteIDsList.contains(new Integer(jahiaField.getSiteID()))) {
                    if ( jahiaField instanceof JahiaDateField ){
                        objectItem = jahiaField.getObject();
                        if ( objectItem != null ){
                            fieldValue = objectItem.toString();
                        }
                    } else {
                    fieldValue = jahiaField.getRawValue();
                    }
                    if (fieldValue != null) {
                        datas.put(objectKey, fieldValue);
                    }
                }
            }
        }
        return datas;

    }
}
