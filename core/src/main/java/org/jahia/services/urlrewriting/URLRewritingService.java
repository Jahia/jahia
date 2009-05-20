/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.urlrewriting;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.xml.sax.SAXException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class URLRewritingService extends JahiaService {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(URLRewritingService.class);

    private static URLRewritingService singletonInstance = null;

    private Digester digester;
    private List rewriteRules = new ArrayList();

    protected URLRewritingService() {
    }

    /**
     * Return the unique service instance. If the instance does not exist,
     * a new instance is created.
     *
     * @return The unique service instance.
     */
    public synchronized static URLRewritingService getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new URLRewritingService();
        }
        return singletonInstance;
    }

    /**
     * Initializes the servlet dispatching service with parameters loaded
     * from the Jahia configuration file.
     * @throws JahiaInitializationException thrown in the case of an error
     * during this initialization, that will be treated as a critical error
     * in Jahia and probably stop execution of Jahia once and for all.
     */
    public void start()
        throws JahiaInitializationException {
        try {
            load(settingsBean.getJahiaEtcDiskPath() + File.separator + "config" +
                 File.separator + "url-rewriting.xml");
        } catch (Exception t) {
            throw new JahiaInitializationException("Error while loading URL rewriting configuration file", t);
        }
    }

    public void stop() {
    }

    public String getOriginalFromRewritten(String rewrittenURL) {
        Iterator ruleIter = rewriteRules.iterator();
        while (ruleIter.hasNext()) {
            RewriteRule curRule = (RewriteRule) ruleIter.next();
            String originalURL = curRule.getOriginalFromRewritten(rewrittenURL);
            if (originalURL != null) {
                return originalURL;
            }
        }
        return null;
    }

    public String getRewrittenFromOriginal(String originalURL) {
        Iterator ruleIter = rewriteRules.iterator();
        while (ruleIter.hasNext()) {
            RewriteRule curRule = (RewriteRule) ruleIter.next();
            String rewrittenURL = curRule.getRewrittenFromOriginal(originalURL);
            if (rewrittenURL != null) {
                return rewrittenURL;
            }
        }
        return null;

    }

    private void load(String urlRewritingConfigFileName)
        throws IOException, SAXException {
        initDigester();
        File urlRewritingConfigFile = new File(urlRewritingConfigFileName);
        this.digester.parse(urlRewritingConfigFile);
    }

    private void initDigester() {
        this.digester = new Digester();

        AddRewriteRuleRule addRewriteRuleRule =
            new AddRewriteRuleRule();
        digester.addRule(
            "rewrite-rules/rule",
            addRewriteRuleRule);
        digester.addRule(
            "rewrite-rules/rule/original-from-rewritten/property",
            addRewriteRuleRule.addOriginalFromRewrittenPropsRule);
        digester.addRule(
            "rewrite-rules/rule/rewritten-from-original/property",
            addRewriteRuleRule.addRewrittenFromOriginalPropsRule);
    }

    final class AddRewriteRuleRule extends Rule {

        private Properties originalFromRewrittenProps = new Properties();
        private Properties rewrittenFromOriginalProps = new Properties();

        AddPropertyRule addOriginalFromRewrittenPropsRule = new AddPropertyRule(originalFromRewrittenProps);
        AddPropertyRule addRewrittenFromOriginalPropsRule = new AddPropertyRule(rewrittenFromOriginalProps);

        private Properties properties = new Properties();

        public void begin(
            String namespace,
            String name,
            org.xml.sax.Attributes attributes)
            throws Exception {
            for (int i = 0; i < attributes.getLength(); i++) {
                properties.setProperty(
                    attributes.getQName(i),
                    attributes.getValue(i));
            }
        }

        public void end(String namespace, String name) throws Exception {
            String className = properties.getProperty("class");
            try {
            Class rewriteRuleClass = Class.forName(className);
            Class[] parameterTypes = new Class[2];
            parameterTypes[0] = Properties.class;
            parameterTypes[1] = Properties.class;
            Constructor rewriteRuleConstructor = rewriteRuleClass.getConstructor(parameterTypes);
            Object[] initArgs = new Object[2];
            initArgs[0] = originalFromRewrittenProps;
            initArgs[1] = rewrittenFromOriginalProps;
            RewriteRule rewriteRule = (RewriteRule) rewriteRuleConstructor.newInstance(initArgs);
            rewriteRules.add(rewriteRule);
            } catch (ClassNotFoundException cnfe) {
                logger.error("Couldn't find validator class " + className, cnfe);
            } catch (IllegalAccessException iae) {
                logger.error("Error accessing validator class " + className, iae);
            } catch (InstantiationException ie) {
                logger.error("Validation class "+className+" instantiation exception", ie);
            } catch (InvocationTargetException ite) {
                logger.error("Invocation target exception while invoking validator class " + className, ite);
            } catch (NoSuchMethodException nsme) {
                logger.error("No such method exception while creating instance of validator class "+className, nsme);
            }
            originalFromRewrittenProps = new Properties();
            rewrittenFromOriginalProps = new Properties();
            properties = new Properties();
        }


        final class AddPropertyRule extends Rule {

            Properties properties = new Properties();
            Properties targetProperties;

            public AddPropertyRule(Properties targetProperties) {
                this.targetProperties = targetProperties;
            }


            public void begin(
                String namespace,
                String name,
                org.xml.sax.Attributes attributes)
                throws Exception {
                for (int i = 0; i < attributes.getLength(); i++) {
                    properties.setProperty(
                        attributes.getQName(i),
                        attributes.getValue(i));
                }
            }
            public void end(String namespace, String name)
                throws Exception {
                targetProperties.setProperty(properties.getProperty("name"), properties.getProperty("value"));
                properties = new Properties();
            }

        }
    }

}