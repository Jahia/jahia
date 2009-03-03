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
