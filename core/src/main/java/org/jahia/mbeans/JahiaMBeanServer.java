/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.mbeans;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.naming.NamingException;

import mx4j.adaptor.http.HttpAdaptor;
import mx4j.adaptor.rmi.jrmp.JRMPAdaptorMBean;
import mx4j.adaptor.ssl.SSLAdaptorServerSocketFactoryMBean;
import mx4j.util.StandardMBeanProxy;

import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;
import org.jahia.settings.SettingsBean;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class JahiaMBeanServer implements NotificationListener {

    /** logging */
    static final private org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaMBeanServer.class);

    /** class unique instance */
    private static JahiaMBeanServer instance;
    /**
     * The configuration information registry for our managed beans.
     */
    private Registry registry = null;

    /**
     * The <code>MBeanServer</code> for this application.
     */
    private MBeanServer server = null;

    private SettingsBean settings = null;

    private JahiaMBeanServer () {
    }

    /** Return the unique instance of this class.
     *
     * @return  the class' unique instance.
     */
    public static synchronized JahiaMBeanServer getInstance () {

        if (instance == null) {
            instance = new JahiaMBeanServer();
        }
        return instance;
    }

    public void init(SettingsBean settings) {
        this.settings = settings;
        if (settings.isJmxActivated()) {
        createRegistry();
        createServer();
        createAdaptors();
        // createMBeans();
        // dumpServer();
    }
    }

    public void registerManagedInstance (Object managedInstance,
                                         String mbeanName, String instanceName) {
        if (!settings.isJmxActivated()) {
            return;
        }
        // Create the MBean for the top-level Server object
        String domain = server.getDefaultDomain();
        logger.debug("Trying to register MBean for instance with mbeanName " + mbeanName);
        try {
            ManagedBean managedBean = registry.findManagedBean(mbeanName);
            if (managedBean != null) {
                ModelMBean modelMBean = managedBean.createMBean(managedInstance);
                // mm.addAttributeChangeNotificationListener(this, "shutdown", cacheFactory);
                // mm.addAttributeChangeNotificationListener(this, "port", cacheFactory);

                ObjectName name = null;
                name = new ObjectName(domain + ":type=" + mbeanName + ",name=" + instanceName );
                server.registerMBean(modelMBean, name);
            } else {
                logger.warn("ManagedBean " + mbeanName +
                    " not found in descriptor file, not adding managed instance to MBean server");
            }
        } catch (MalformedObjectNameException mone) {
            logger.error(
                "Error in object name, not adding managed instance with mbeanName=" +
                mbeanName + " to MBean server", mone);
        } catch (InstanceNotFoundException infe) {
            logger.error("Instance not found for mbeanName " + mbeanName +
                         ", not adding managed instance to MBean server", infe);
        } catch (InstanceAlreadyExistsException iaee) {
            logger.error("This instance with mbeanName " + mbeanName +
                " already exists in the MBean server, not adding again", iaee);
        } catch (NotCompliantMBeanException mcmbe) {
            logger.error(
                "Trying to add a non-compliant MBean to server, ignoring mbeanName" +
                mbeanName + " registration", mcmbe);
        } catch (MBeanException mbe) {
            logger.error("MBean exception, not registering mbeanName " +
                         mbeanName + " into MBean server", mbe);
        } catch (InvalidTargetObjectTypeException itote) {
            logger.error(
                "Invalid target object type exception, not registering mbeanName " +
                mbeanName + " into MBean server", itote);
        }
    }



    /**
     * Create and configure the registry of managed objects.
     */
    private void createRegistry () {

        logger.debug("Create configuration registry ...");
        try {
            URL url = JahiaMBeanServer.class.getResource
                ("/mbeans-descriptors.xml");
            InputStream stream = url.openStream();
            registry = new Registry();
            registry.loadDescriptors(stream);
            stream.close();
        } catch (Exception t) {
            logger.error("Error while initializing managed bean registry", t);
        }

    }

    /**
     * Create the <code>MBeanServer</code> with which we will be
     * registering our <code>ModelMBean</code> implementations.
     */
    private void createServer () {

        logger.debug("Creating MBeanServer ...");
        try {
            //            System.setProperty("LEVEL_TRACE", "true");
            server = MBeanServerFactory.createMBeanServer();
        } catch (Exception t) {
            logger.error("Error while creating MBeanServer", t);
        }

    }

    /**
     * Create the MBeans that correspond to every node of our tree.
     */
    private void createAdaptors () {

        try {

            if (settings.isJmxHTTPAdaptorActivated()) {
                createHttpAdaptor();
            }
            if (settings.isJmxRMIAdaptorActivated()) {
                createRMIAdaptor();
            }

        } catch (MBeanException t) {

            Exception e = t.getTargetException();
            if (e == null)
                e = t;

            logger.error("Error creating MBeans", e);

        } catch (Exception t) {

            logger.error("Error creating MBeans", t);

        }

    }

    private void createHttpAdaptor ()
        throws IOException, InstanceAlreadyExistsException,
        MBeanRegistrationException, NotCompliantMBeanException,
        MalformedObjectNameException, InstanceNotFoundException, MBeanException,
        ReflectionException, InvalidAttributeValueException,
        AttributeNotFoundException {

        logger.debug("Creating an Http protocol adapter");
        // the following is MX4J specific
        HttpAdaptor adapter = new HttpAdaptor();
        ObjectName name = new ObjectName("Server:name=HttpAdaptor");
        server.registerMBean(adapter, name);
        adapter.setPort(settings.getJmxHTTPPort());
        adapter.setHost(settings.getJmxHTTPHostname());
        if (settings.isJmxXSLProcessorActivated()) {
            ObjectName processorName = new ObjectName("Server:name=XSLTProcessor");
            server.createMBean("mx4j.adaptor.http.XSLTProcessor", processorName, null);
            /*
            // set it to use a dir
            server.setAttribute(processorName, new Attribute("File", "/home/tibu/devel/mx4j/src/core/mx4j/adaptor/http/xsl"));
            // set it to use a compressed file
            server.setAttribute(processorName, new Attribute("File", "/home/tibu/skins.jar"));
            // set the target dir
            server.setAttribute(processorName, new Attribute("PathInJar", "/mx4j/adaptor/http/xsl"));
            // set not to use cache
            server.setAttribute(processorName, new Attribute("UseCache", Boolean.FALSE));
            // set not to use cache
            server.setAttribute(processorName, new Attribute("LocaleString", "fi"));
            // adds a mime type
            server.invoke(processorName, "addMimeType", new Object[] {".pdf", "application/pdf"}, new String[] {"java.lang.String", "java.lang.String"});
            */
        }

        if (settings.getJmxHTTPAutorizationMode() != null) {
            adapter.setAuthenticationMethod(settings.getJmxHTTPAutorizationMode());
            if ( (settings.getJmxHTTPAuthorizationUser() != null) &&
                 (settings.getJmxHTTPAuthorizationPassword() != null) ) {
                adapter.addAuthorization(settings.getJmxHTTPAuthorizationUser(),
                                         settings.getJmxHTTPAuthorizationPassword());
            }
        }
        if (settings.getJmxHTTPProcessorNameString() != null) {
            adapter.setProcessorNameString(settings.getJmxHTTPProcessorNameString());
        }
        if (settings.getJmxHTTPSocketFactoryNameString() != null) {
            adapter.setSocketFactoryNameString(settings.getJmxHTTPSocketFactoryNameString());
        }
        adapter.start();
    }

    private void createRMIAdaptor ()
        throws JMException, NamingException, RemoteException {

        logger.debug("Creating an RMI protocol adapter");
        // the following is MX4J specific

        SSLAdaptorServerSocketFactoryMBean factory = null;
        ObjectName ssl = null;
        if (settings.isJmxRMISSLServerSocketFactoryActivated()) {
            // Create the SSL ServerSocket factory
            ssl = new ObjectName("Adaptor:service=SSLServerSocketFactory");
            server.createMBean("mx4j.adaptor.ssl.SSLAdaptorServerSocketFactory",
                               ssl, null);
            factory = (SSLAdaptorServerSocketFactoryMBean) StandardMBeanProxy.
                create(
                SSLAdaptorServerSocketFactoryMBean.class, server, ssl);
            factory.setKeyStoreName(settings.getJmxRMISSLServerSocketFactoryKeyStoreName());
            factory.setKeyStorePassword(settings.getJmxRMISSLServerSocketFactoryKeyStorePassword());
            factory.setKeyManagerPassword(settings.getJmxRMISSLServerSocketFactoryKeyManagerPassword());
        }

        // Create and start the naming service
        ObjectName naming = new ObjectName("Naming:type=rmiregistry");
        server.createMBean("mx4j.tools.naming.NamingService", naming, null);
        server.invoke(naming, "start", null, null);

        // Create the JRMP adaptor
        ObjectName adaptor = new ObjectName("Adaptor:protocol=JRMP");
        server.createMBean("mx4j.adaptor.rmi.jrmp.JRMPAdaptor", adaptor, null);
        JRMPAdaptorMBean mbean = (JRMPAdaptorMBean) StandardMBeanProxy.create(
            JRMPAdaptorMBean.class, server, adaptor);

        // Set the JNDI name with which will be registered
        String jndiName = "jrmp";
        mbean.setJNDIName(jndiName);
        /*
              // Optionally, you can specify the JNDI properties,
              // instead of having in the classpath a jndi.properties file
              mbean.putJNDIProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
             mbean.putJNDIProperty(Context.PROVIDER_URL, "rmi://localhost:1099");
         */
        if (factory != null) {
            // Set the SSL ServerSocket Factory
            mbean.setSSLFactory(ssl.toString());
        }

        // Registers the JRMP adaptor in JNDI and starts it
        mbean.start();
    }

    /**
     * Handle the notification of a JMX event.
     *
     * @param notification The event that has occurred
     * @param handback The handback object for this event
     */
    public void handleNotification (Notification notification,
                                    Object handback) {
        if (!settings.isJmxActivated()) {
            return;
        }
        logger.debug("NOTIFICATION=" + notification +
                     ", HANDBACK=" + handback);

    }

}