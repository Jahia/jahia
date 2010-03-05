/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.filter;

import java.util.Locale;

import org.jahia.params.ParamBean;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link URLFilter} User: toto Date: Nov 26, 2009 Time: 12:57:51 PM
 */
public class URLFilterTest {
    private final static String TESTSITE_NAME = "test";
    private final static String SITECONTENT_ROOT_NODE = "/sites/"
            + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME);

        ParamBean paramBean = (ParamBean) Jahia.getThreadParamBean();

        paramBean.getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

        JahiaData jData = new JahiaData(paramBean, false);
        paramBean.setAttribute(JahiaData.JAHIA_DATA, jData);

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);

        if (!siteNode.isCheckedOut()) {
            siteNode.checkout();
        }

        if (siteNode.hasNode("testPage")) {
            siteNode.getNode("testPage").remove();
        }
        JCRNodeWrapper pageNode = siteNode.addNode("testPage",
                Constants.JAHIANT_PAGE);
        pageNode.addNode("testContent", "jnt:mainContent");

        session.save();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testAssigningUrlMappings() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper pageNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/testPage");
        JCRNodeWrapper contentNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/testPage/testContent");

        VanityUrl vanityUrl = new VanityUrl("/testpage", TESTSITE_NAME, "en");
        vanityUrl.setDefaultMapping(true);
        vanityUrl.setActive(true);
        assertNull("URL mapping should not exist yet", getVanityUrlService()
                .findExistingVanityUrl(vanityUrl.getUrl(), vanityUrl.getSite()));
        getVanityUrlService().saveVanityUrlMapping(pageNode, vanityUrl);
        assertNotNull("URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrl(vanityUrl.getUrl(), vanityUrl.getSite()));
        try {
            getVanityUrlService().saveVanityUrlMapping(contentNode, vanityUrl);
            assertTrue("Exception should have been thrown", false);
        } catch (JahiaRuntimeException ex) {
            // expected
        }

        VanityUrl newVanityUrl = new VanityUrl("/testcontent", TESTSITE_NAME,
                "en");
        newVanityUrl.setDefaultMapping(true);
        newVanityUrl.setActive(true);        
        getVanityUrlService().saveVanityUrlMapping(contentNode, newVanityUrl);
        assertNotNull("New URL mapping should exist", getVanityUrlService()
                .findExistingVanityUrl(vanityUrl.getUrl(), vanityUrl.getSite()));

        assertTrue("Wrong page vanity URL returned", vanityUrl
                .equals(getVanityUrlService()
                        .getVanityUrlForWorkspaceAndLocale(pageNode,
                                session.getWorkspace().getName(),
                                session.getLocale())));

        assertTrue("Wrong container vanity URL returned", newVanityUrl
                .equals(getVanityUrlService().getVanityUrlsForCurrentLocale(
                        contentNode, session).get(0)));
    }

    @Test
    public void testUrlFilter() throws Exception {

    }

    private VanityUrlService getVanityUrlService() {
        return (VanityUrlService) SpringContextSingleton
                .getBean(VanityUrlService.class.getName());
    }

}
