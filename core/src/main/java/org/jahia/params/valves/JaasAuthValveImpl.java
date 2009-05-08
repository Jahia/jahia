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
 package org.jahia.params.valves;

import org.apache.log4j.Logger;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.security.license.LicenseActionChecker;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.Configuration;
import javax.security.auth.Subject;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.security.Principal;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 15 d�c. 2004
 * Time: 13:02:36
 * To change this template use File | Settings | File Templates.
 */
public class JaasAuthValveImpl implements Valve {
    
    private static final transient Logger logger = Logger
            .getLogger(JaasAuthValveImpl.class);
    
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        if (!LicenseActionChecker.isAuthorizedByLicense("org.jahia.params.valves.JaasAuthValve", 0)) {
            valveContext.invokeNext(context);
        }

        ProcessingContext processingContext = (ProcessingContext) context;
        if (!"login".equals(processingContext.getEngine())) {
            try {
                LoginContext lc = new LoginContext("jahia");
                lc.login();
                Subject s = lc.getSubject();
                Collection<Principal> ps = s.getPrincipals();
                for (Iterator<Principal> iterator = ps.iterator(); iterator.hasNext();) {
                    Principal principal = iterator.next();
                    JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(principal.getName());
                    if (user != null) {
                        processingContext.setTheUser(user);
                        return;
                    }
                }
            } catch (LoginException e) {
                logger.error(e.getMessage(), e);
            }
        }
        valveContext.invokeNext(context);
        return;
    }

    public void initialize() {
        if (Configuration.getConfiguration().getAppConfigurationEntry("jahia") == null) {
            try {
                String config = System.getProperty("java.security.auth.login.config");
                File source = new File(org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath()+"/config/jaas.config");
                if (config == null) {
                    // No jaas config defined -> set the system property to our config
                    System.setProperty("java.security.auth.login.config", source.toURL().toString());
                } else {
                    // JAAS defined but does not contains jahia definition -> append jahia definition
                    String path;
                    try {
                        URL configURL = new URL(config);
                        path = configURL.getPath();
                    } catch (MalformedURLException e) {
                        path = config;
                    }
                    File target = new File(path);
                    BufferedWriter fw = new BufferedWriter(new FileWriter(target, true));
                    BufferedReader fr = new BufferedReader(new FileReader(source));
                    fw.newLine();
                    String l ;
                    while ((l = fr.readLine()) != null) {
                        System.out.println(l);
                        fw.write(l);
                        fw.newLine();
                    }
                    fw.close();
                    fr.close();
                }
                Configuration.getConfiguration().refresh();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
