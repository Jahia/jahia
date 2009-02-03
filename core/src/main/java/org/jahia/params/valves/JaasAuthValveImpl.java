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
                Collection ps = s.getPrincipals();
                for (Iterator iterator = ps.iterator(); iterator.hasNext();) {
                    Principal principal = (Principal) iterator.next();
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
