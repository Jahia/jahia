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

import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import junit.framework.TestCase;

import java.util.regex.Pattern;

/**
 * Unit test for the e-mail obfuscation render filter. 
 * User: toto
 * Date: Nov 26, 2009
 * Time: 12:19:43 PM
 */
public class EmailObfuscatorFilterTest extends TestCase {

    private Pattern mailPattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}");

    public void testMailInText() throws Exception {
        parseText("My email is jahia@jahia.com, please do not spam");
    }

    public void testMailInLink() throws Exception {
        parseText("My email is <a href=\"mailto:jahia@jahia.com\">mail</a>, please do not spam");
    }

    private void parseText(final String s) throws Exception {
        assertTrue("Email not found in original : "+s, mailPattern.matcher(s).find());

        RenderChain chain = new RenderChain();
        EmailObfuscatorFilter filter = new EmailObfuscatorFilter();
        chain.addFilter(filter);
        chain.addFilter(new AbstractFilter() {
            public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
                    throws Exception {
                return s;
            }
        });
        ParamBean ctx = (ParamBean) Jahia.getThreadParamBean();
        RenderContext renderCtx = new RenderContext(ctx.getRequest(), ctx.getResponse(), ctx.getUser());        
        
        String result = chain.doFilter(renderCtx, null);

        assertFalse("Email found in result : "+result, mailPattern.matcher(result).find());
    }

}
