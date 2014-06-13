/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.test.services.render.filter;

import static junit.framework.Assert.assertTrue;

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
        JahiaSite site = TestHelper.createSite("test", "localhost" + System.currentTimeMillis(), TestHelper.WEB_TEMPLATES, null, null,
                new String[] {"jahia-test-module"});
        
        setSessionSite(site);

        session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
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

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

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

