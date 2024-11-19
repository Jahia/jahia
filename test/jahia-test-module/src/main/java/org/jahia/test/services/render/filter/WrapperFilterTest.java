/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test.services.render.filter;

import static org.junit.Assert.assertTrue;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.BaseAttributesFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.WrapperFilter;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.api.Constants;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Unit test for the {@link WrapperFilter}
 * User: toto
 * Date: Nov 26, 2009
 * Time: 12:57:51 PM
 */
public class WrapperFilterTest extends JahiaTestCase {
    private JCRSiteNode site;
    private JCRSessionWrapper session;
    private JCRNodeWrapper node;

    @Before
    public void setUp() throws Exception {
        session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);

        JahiaSite site = TestHelper.createSite("test", "localhost" + System.currentTimeMillis(), TestHelper.WEB_TEMPLATES, null, null,
                new String[] {"jahia-test-module"});

        this.site = (JCRSiteNode) session.getNode("/sites/"+site.getSiteKey());

        if (!this.site.isCheckedOut()) {
            session.checkout(this.site);
        }

        if (this.site.hasNode("testContent")) {
            this.site.getNode("testContent").remove();
        }
        node = this.site.addNode("testContent", "jnt:mainContent");

        session.save();
    }

    @After
    public void tearDown() throws Exception {
        TestHelper.deleteSite("test");
        session.save();
    }

    @Test
    public void testFullpageWrapper() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(null);

        RenderContext context = new RenderContext(getRequest(), getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        ChannelService channelService = (ChannelService) SpringContextSingleton.getInstance().getContext().getBean("ChannelService");
        context.setChannel(channelService.getChannel(Channel.GENERIC_CHANNEL));
        context.setServletPath("/cms/render");
        resource.pushWrapper("wrappertest");

        RenderChain chain = new RenderChain();
        BaseAttributesFilter attributesFilter = new BaseAttributesFilter();
        attributesFilter.setRenderService(RenderService.getInstance());
        attributesFilter.setConfigurationToSkipInResourceRenderedPath(new HashSet<String>(Arrays.asList("include", "wrapper")));
        chain.addFilter(attributesFilter);

        WrapperFilter filter = new WrapperFilter();
        filter.setRenderService(RenderService.getInstance());
        chain.addFilter(filter);

        chain.addFilter(new AbstractFilter() {
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return "test";
            }
        });
        String result = chain.doFilter(context,resource);

        assertTrue("Cannot find <body> tag", Pattern.compile("body").matcher(result).find());
        assertTrue("Cannot find test content", Pattern.compile("test").matcher(result).find());
    }

}

