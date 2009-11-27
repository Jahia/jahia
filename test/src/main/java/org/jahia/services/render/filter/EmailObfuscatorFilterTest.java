package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import java.io.IOException;
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
            public String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws IOException, RepositoryException {
                return s;
            }
        });
        String result = chain.doFilter(null,null);

        assertFalse("Email found in result : "+result, mailPattern.matcher(result).find());
    }

}
