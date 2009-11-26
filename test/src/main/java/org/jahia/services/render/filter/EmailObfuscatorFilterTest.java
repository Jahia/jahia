package org.jahia.services.render.filter;

import org.jahia.services.sites.JahiaSite;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.params.ProcessingContext;
import org.jahia.api.Constants;
import org.apache.log4j.Logger;
import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 26, 2009
 * Time: 12:19:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmailObfuscatorFilterTest extends TestCase {
    private static Logger logger = Logger.getLogger(EmailObfuscatorFilterTest.class);

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
            public String doFilter(RenderContext renderContext, Resource resource, RenderChain chain) throws IOException, RepositoryException {
                return s;
            }
        });
        String result = chain.doFilter(null,null);

        assertFalse("Email found in result : "+result, mailPattern.matcher(result).find());
    }

}
