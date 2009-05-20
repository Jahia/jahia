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
 package org.jahia.services.timebasedpublishing;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 juil. 2005
 * Time: 13:01:41
 * To change this template use File | Settings | File Templates.
 */
public interface RetentionRuleDef extends Serializable {

    public Integer getId();

    public void setId(Integer id);

    public String getTitle();

    public void setTitle(String title);

    public RetentionRule getRule(Integer id);

    public RetentionRule createRule();

    public RetentionRule saveRule(RetentionRule rule) throws Exception;

    public void deleteRule(RetentionRule rule);

    public RetentionRuleDefHelper getHelper(); 

    /**
     * Returns the value of the associated ResourceBundle Key
     */
    public String getBundleKey();
}
