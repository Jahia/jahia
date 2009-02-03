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