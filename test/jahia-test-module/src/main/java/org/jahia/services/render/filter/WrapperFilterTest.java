/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter;

import org.jahia.params.ParamBean;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.RenderService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.TestHelper;

import java.util.Locale;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Unit test for the {@link WrapperFilter} 
 * User: toto
 * Date: Nov 26, 2009
 * Time: 12:57:51 PM
 */
public class WrapperFilterTest extends TestCase {
    private ParamBean paramBean;
    private JCRSiteNode site;
    private JCRSessionWrapper session;
    private JCRNodeWrapper node;

    @Override
    protected void setUp() throws Exception {
        JahiaSite site = TestHelper.createSite("test");

        paramBean = (ParamBean) Jahia.getThreadParamBean();

        paramBean.getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

        JahiaData jData = new JahiaData(paramBean, false);
        paramBean.setAttribute(JahiaData.JAHIA_DATA, jData);

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

    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite("test");
        node.remove();
        session.save();
    }

    public void testFullpageWrapper() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);

        resource.pushWrapper("wrapper.fullpage");

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

