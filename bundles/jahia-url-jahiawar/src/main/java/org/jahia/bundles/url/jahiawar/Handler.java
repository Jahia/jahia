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

}