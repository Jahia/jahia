/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.jahia.bin.Jahia;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.EmailObfuscatorFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.test.JahiaTestCase;
import org.junit.Test;

import junit.framework.TestCase;

import java.util.regex.Pattern;

/**
 * Unit test for the e-mail obfuscation render filter. 
 * User: toto
 * Date: Nov 26, 2009
 * Time: 12:19:43 PM
 */
public class EmailObfuscatorFilterTest extends JahiaTestCase {

    private Pattern mailPattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}");

    @Test
    public void testMailInText() throws Exception {
        parseText("My email is jahia@jahia.com, please do not spam");
    }

    @Test
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
        RenderContext renderCtx = new RenderContext(getRequest(), getResponse(), getUser());
        
        String result = chain.doFilter(renderCtx, null);

        assertFalse("Email found in result : "+result, mailPattern.matcher(result).find());
    }
}
