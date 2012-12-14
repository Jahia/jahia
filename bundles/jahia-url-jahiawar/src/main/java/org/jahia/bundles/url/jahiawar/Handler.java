package org.jahia.bundles.url.jahiawar;

import org.jahia.bundles.url.jahiawar.internal.ConfigurationImpl;
import org.jahia.bundles.url.jahiawar.internal.Connection;
import org.ops4j.util.property.PropertiesPropertyResolver;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * {@link URLStreamHandler} implementation for "jahiawar:" protocol.
 */
public class Handler
    extends URLStreamHandler
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected URLConnection openConnection( final URL url )
        throws IOException
    {
        final ConfigurationImpl config = new ConfigurationImpl(
            new PropertiesPropertyResolver( System.getProperties() )
        );
        return new Connection( url, config );
    }

    @Override
    protected void parseURL(URL url, String s, int i, int i1) {
        super.parseURL(url, s, i, i1);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected String toExternalForm(URL url) {
        return super.toExternalForm(url);    //To change body of overridden methods use File | Settings | File Templates.
    }
}