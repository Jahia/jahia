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
 package org.jahia.hibernate.manager;

import org.jahia.services.timebasedpublishing.RetentionRule;
import org.jahia.content.ObjectKey;
import org.jahia.content.TimeBasedPublishingState;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 5 ao�t 2005
 * Time: 15:42:36
 * To change this template use File | Settings | File Templates.
 */
public class JahiaObjectDelegate implements TimeBasedPublishingState, Cloneable, Serializable {

    private ObjectKey objectKey;
    private Integer timeBPState = IS_VALID_STATE;
    private Integer siteId = 0;
    private Long validFromDate = 0L;
    private Long validToDate = 0L;
    private RetentionRule rule;
    
    JahiaObjectDelegate(){}

    public ObjectKey getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(ObjectKey objectKey) {
        this.objectKey = objectKey;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public Integer getTimeBPState() {
        return timeBPState;
    }

    public void setTimeBPState(Integer timeBPState) {
        this.timeBPState = timeBPState;
    }

    public Long getValidFromDate() {
        return validFromDate;
    }

    public void setValidFromDate(Long validFromDate) {
        this.validFromDate = validFromDate;
    }

    public Long getValidToDate() {
        return validToDate;
    }

    public void setValidToDate(Long validToDate) {
        this.validToDate = validToDate;
    }

    public RetentionRule getRule() {
        return rule;
    }

    public void setRule(RetentionRule rule) {
        this.rule = rule;
    }

    public boolean isValid(){
        return (this.getTimeBPState() == IS_VALID_STATE);
    }

    public boolean isExpired(){
        return (this.getTimeBPState() == EXPIRED_STATE);
    }

    public boolean isNotValid(){
        return (this.getTimeBPState() == NOT_VALID_STATE);
    }

    public boolean willBecomeValid(long date){
        long validFrom = 0;
        if (this.getValidFromDate()!= null){
            validFrom = this.getValidFromDate();
        }
        return ( validFrom > date );
    }

    public boolean willExpire(long date){
        long validTo = 0;
        if (this.getValidToDate()!= null){
            validTo = this.getValidToDate();
        }
        return ( validTo > date );
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        final JahiaObjectDelegate clone = new JahiaObjectDelegate();
        clone.setObjectKey(getObjectKey());
        final RetentionRule rule = getRule();
        if (rule != null) clone.setRule((RetentionRule)getRule().clone());
        clone.setSiteId(getSiteId());
        clone.setTimeBPState(getTimeBPState());
        clone.setValidFromDate(getValidFromDate());
        clone.setValidToDate(getValidToDate());
        return clone;
    }

}
