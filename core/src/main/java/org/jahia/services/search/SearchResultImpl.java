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
 package org.jahia.services.search;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.compass.core.CompassHighlighter;
import org.compass.core.engine.SearchEngineHighlighter;
import org.jahia.bin.Jahia;
import org.jahia.content.CoreFilterNames;
import org.jahia.content.ObjectKey;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 17:48:29
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultImpl implements SearchResult {

    private List<SearchHit> results = new LinkedList<SearchHit>();

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(SearchResultImpl.class);

    private Map<String, Principal> guestUsers = new HashMap<String, Principal>();
    private boolean checkAccess = true;

    /**
     * Constructor without ACL and Time Based publisshing checks
     */
    public SearchResultImpl(){
        this(true);
    }

    /**
     *
     * @param checkAccess if true, apply ACL and Time Based publishing check
     */
    public SearchResultImpl(boolean checkAccess){
        this.checkAccess = checkAccess;
    }

    public boolean isCheckAccess() {
        return checkAccess;
    }

    public void setCheckAccess(boolean checkAccess) {
        this.checkAccess = checkAccess;
        
    }

    public List<SearchHit> results(){
        return results;
    }

    public boolean add(SearchHit hit){
        if ( hit != null && checkAccess(hit) ){
            results.add(hit);
        }
        return true;
    }

    public boolean checkAccess(SearchHit hit){
        if (!this.checkAccess){
            return true;
        }
        boolean accessAllowed = true;
        // acl check
        String fieldValue = hit.getValue(JahiaSearchConstant.ACL_ID);
        String siteIdValue = hit.getValue(JahiaSearchConstant.JAHIA_ID);
        JahiaUser user = Jahia.getThreadParamBean().getUser();
        if (user == null){
            user = (JahiaUser) guestUsers.get(siteIdValue);
            if (user == null){
                user = ServicesRegistry.getInstance().getJahiaUserManagerService()
                        .lookupUser(JahiaUserManagerService.GUEST_USERNAME);
                if (user != null){
                    guestUsers.put(siteIdValue,user);
                }
            }
        }
        if ( fieldValue != null ){
            try {
                int aclID = Integer.parseInt(fieldValue);
                final JahiaBaseACL acl = JahiaBaseACL.getACL(aclID);
                if (!acl.getPermission(user,
                        JahiaBaseACL.READ_RIGHTS)) {
                    accessAllowed = false;
                }
                fieldValue = hit.getValue(JahiaSearchConstant.OBJECT_KEY);
                if ( fieldValue != null ){
                    ObjectKey objectKey = ObjectKey.getInstance(fieldValue);
                    // Check for expired container
                    boolean disableTimeBasedPublishingFilter = Jahia.getThreadParamBean()
                            .isFilterDisabled(CoreFilterNames.
                            TIME_BASED_PUBLISHING_FILTER);
                    ProcessingContext context = Jahia.getThreadParamBean();
                    final TimeBasedPublishingService tbpServ = ServicesRegistry.getInstance().getTimeBasedPublishingService();
                    if ( !disableTimeBasedPublishingFilter ){
                        if ( ParamBean.NORMAL.equals(context.getOperationMode()) ){
                            accessAllowed = tbpServ.isValid(objectKey,
                                   context.getUser(),context.getEntryLoadRequest(),
                                    context.getOperationMode(),
                                    (Date)null);
                        } else if ( ParamBean.PREVIEW.equals(context.getOperationMode()) ){
                            accessAllowed = tbpServ.isValid(objectKey,
                                    context.getUser(),context.getEntryLoadRequest(),context.getOperationMode(),
                                    AdvPreviewSettings.getThreadLocaleInstance());
                        }
                    }
                }
            } catch ( Exception t){
                logger.warn("Exception checking hit access", t);
                return false;
            }
        }
        return accessAllowed;
    }

    public void remove(int index){
        try {
            results.remove(index);
        } catch ( Exception t ){
        }
    }

    /**
     * Returns an highlighter for the hits.
     */
    public SearchEngineHighlighter getHighlighter(){
        // by default, no highlighter
        return null;
    }

    /**
     * By Default no highlighter, return null
     *
     * @param index
     *            The n'th hit.
     * @return The highlighter.
     */
    public CompassHighlighter highlighter(int index) {
        return null;
    }
    
    /**
     * By Default no highlighter, return null
     *
     * @param searchHit
     * @return
     */
    public CompassHighlighter highlighter(SearchHit searchHit){
        return null;
    }


}
