package org.jahia.bundles.url.jahiawar.internal;

import org.jahia.bundles.url.jahiawar.ServiceConstants;
import org.ops4j.pax.url.commons.handler.ConnectionFactory;
import org.ops4j.pax.url.commons.handler.HandlerActivator;
import org.ops4j.util.property.PropertyResolver;
import org.osgi.framework.BundleContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Bundle activator for jahiawar: protocol handler.
 */
public class Activator extends HandlerActivator<Configuration> {

    public Activator() {
        super(new String[] { ServiceConstants.PROTOCOL }, ServiceConstants.PID, new ConnectionFactory<Configuration>() {

            public URLConnection createConection(final BundleContext bundleContext, final URL url,
                    final Configuration config) throws MalformedURLException {
                return new Connection(url, config);
            }

            public Configuration createConfiguration(final PropertyResolver propertyResolver) {
                return new ConfigurationImpl(propertyResolver);
            }

        });
    }
}
