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
 package org.jahia.hibernate.dao;
/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 mai 2006
 * Time: 18:01:17
 * To change this template use File | Settings | File Templates.
 */

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.context.ConfigurableApplicationContext;
import org.jahia.hibernate.model.JahiaBigTextData;

public class JahiaBigTextDataDAOTest extends AbstractTransactionalDataSourceSpringContextTests {
    protected String[] getConfigLocations() {
        return new String[]{"spring/applicationContext-hibernate.xml", "spring/applicationContext-dao.xml"};
    }
    public void testLoad() throws Exception {
        ConfigurableApplicationContext context = getContext(getConfigLocations());
        JahiaBigTextDataDAO bigTextDataDAO = (JahiaBigTextDataDAO) context.getBean("jahiaBigTextDataDAO");
        JahiaBigTextData bigTextData = bigTextDataDAO.load("1-1-69-fr.jahia");
        assertNotNull(bigTextData);
        System.out.println("bigTextData.getRawValues() = " + bigTextData.getRawValues());
    }
}