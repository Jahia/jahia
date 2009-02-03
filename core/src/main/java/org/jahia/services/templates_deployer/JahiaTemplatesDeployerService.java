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

//
//
//  JahiaTemplatesDeployerService
//
//  NK      12.01.2001
//
//

package org.jahia.services.templates_deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.sites.JahiaSite;

/**
 * This Service Deploy templates packed in a .jar file.
 * 
 * @author Khue ng
 * @version 1.0
 * @deprecated due to changes in the template deployment
 */
public abstract class JahiaTemplatesDeployerService extends JahiaService {

    /** a temporary folder **/
    protected static String m_TempFolderDiskPath = "";

    /** temp folder prefix **/
    protected static String m_TempFolderPrefix = "todelete_";

    /**
     * The Map of templates packages. The entry key is the path to an archive file ( .jar ) The value Object is a
     * JahiaTemplatesPackage object containing a list of JahiaWebAppDef ( web components informations bean )
     * 
     */
    protected static Map m_TemplatesPackage = new HashMap();

    // -------------------------------------------------------------------------
    /**
     * Deploy a template .jar file
     * 
     * @param (int) the site
     * @param (String) rootFolder, root folder for the template
     * @param (String) filePath , the full path to the template.jar file
     * @param (boolean) moveTemplate , if true, move te template package to the deployed directory when done
     */
    public abstract boolean deploy(JahiaSite site, String rootFolder,
            String filePath, boolean moveTemplate) throws JahiaException;

    // -------------------------------------------------------------------------
    /**
     * Undeploy a template
     * 
     * @param (JahiaPageDefinition) template
     */
    public abstract boolean undeploy(JahiaPageDefinition template)
            throws JahiaException;

    // -------------------------------------------------------------------------
    /**
     * Deploy a List of .jar templates files
     * 
     * @param (JahiaSite) the site
     * @param (List) files, a List of .jar templates package file
     * @return (boolean) false on exception
     */
    public abstract boolean deploy(JahiaSite site, List files);

    // -------------------------------------------------------------------------
    /**
     * Return the template root path
     * 
     * @return String the template root path
     */
    public abstract String getTemplateRootPath();

    // -------------------------------------------------------------------------
    /**
     * Return the template jsp context
     * 
     * @return String the template jsp context
     */
    public abstract String getTemplatesContext();

    // -------------------------------------------------------------------------
    /**
     * Return the new templates root path
     * 
     * @return String the new templates root path
     */
    public abstract String getNewTemplatesPath();

    // -------------------------------------------------------------------------
    /**
     * Return the shared templates folder disk path
     * 
     * @return String the shared templates folder disk path
     */
    public abstract String getSharedTemplatesPath();

    // -------------------------------------------------------------------------
    /**
     * Try to load info from a .jar template file or an unzipped directory Informations are read from the templates.xml file
     * 
     * @param (String) full path to a file or directory
     * @return a JahiaTemplatesPackage object or null
     */
    public abstract JahiaTemplatesPackage loadTemplatesInfo(String path);

    // -------------------------------------------------------------------------
    /**
     * Register templates in Jahia
     * 
     * @param JahiaSite
     *            the site
     * @param JahiaTemplatesPackage
     *            the templates package
     */
    public abstract void registerTemplates(JahiaSite site,
            JahiaTemplatesPackage pack) throws JahiaException;

    /************************************************************************
     * Templates Packages ( .jar files ) handling
     * 
     * 
     * The directory jahiafiles/new_templates is periodically scanned for new templates packages. A package is a .jar file
     * 
     * If the informations contained in the templates.xml descriptor file is scanned successfully a JahiaTemplatesPackage object that holds
     * all informations needed by Jahia to deploy them is created.
     * 
     * If the deployment mode is automatic, they are automatically deployed and registered in Jahia. If not, they are added in the Map
     * of packages waiting to be deployed manually throught the JahiaAdministration interface.
     * 
     * The Key in the Map of packages used to identify each package is the filename of the package.
     * 
     ************************************************************************/

    // -------------------------------------------------------------------------
    /**
     * delete a package reference in the Map of detected packages and delete it physically from disk.
     * 
     * @param (JahiaSite) the site in which the templates package belong to
     * @param (String) full path to a file or directory
     * @return true if successful
     */
    public boolean deletePackage(JahiaSite site, String path) {

        synchronized (m_TemplatesPackage) {

            File tmpFile = new File(path);
            StringBuffer filename = new StringBuffer(site.getSiteKey());
            filename.append("_");
            filename.append(tmpFile.getName());

            // System.out.println(" deletePackage key is =" + filename );

            if (tmpFile != null && tmpFile.isFile()) {
                if (tmpFile.delete()) {
                    m_TemplatesPackage.remove(filename.toString());
                    return true;
                } else {
                    return false;
                }
            } else {
                m_TemplatesPackage.remove(filename.toString());
                return true;
            }
        }
    }

    // -------------------------------------------------------------------------
    /**
     * add a new file or directory only if it is recognized as a valid template package. And only if it is not registered yet.
     * 
     * @param (JahiaSite) the site for which to register the templates
     * @param (String) full path to a file or directory
     */
    public void addNewFile(JahiaSite site, String path) {

        synchronized (m_TemplatesPackage) {

            File tmpFile = new File(path);
            StringBuffer filename = new StringBuffer(site.getSiteKey());
            filename.append("_");
            filename.append(tmpFile.getName());

            JahiaTemplatesPackage pack = null;

            if (m_TemplatesPackage.get(filename.toString()) == null) {

                pack = loadTemplatesInfo(path);
                if (pack != null) {
                    m_TemplatesPackage.remove(filename.toString());
                    m_TemplatesPackage.put(filename.toString(), pack);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    /***
     * return an Enumerations of the keys of the Map of package
     * 
     * @return (Iterator) the Iterator of all keys
     */
    public Iterator getTemplatesPackageKeys() {

        synchronized (m_TemplatesPackage) {
            return m_TemplatesPackage.keySet().iterator();
        }

    }

    // -------------------------------------------------------------------------
    /***
     * return an Enumerations of the keys of the Map of package for a gived site
     * 
     * @return (String) the site key identifier
     * @return (Iterator) the Iterator of all keys
     */
    public Iterator getTemplatesPackageKeys(String siteKey) {

        Iterator it = null;
        List result = new ArrayList();
        synchronized (m_TemplatesPackage) {

            it = m_TemplatesPackage.keySet().iterator();
            String name = null;
            String siteIdent = siteKey + "_";
            while (it.hasNext()) {
                name = (String) it.next();
                if (name.startsWith(siteIdent)) {
                    result.add(siteIdent);
                }
            }
            return result.iterator();
        }

    }

    // -------------------------------------------------------------------------
    /***
     * return an Iterator of templates package Map for a gived site
     * 
     * @return (String) the site key identifier
     * @return (Iterator) the Iterator of the packages
     */
    public Iterator getTemplatesPackages(String siteKey) {

        Iterator it = null;
        List result = new ArrayList();
        synchronized (m_TemplatesPackage) {

            it = m_TemplatesPackage.keySet().iterator();
            String name = null;
            String siteIdent = siteKey + "_";
            while (it.hasNext()) {
                name = (String) it.next();
                if (name.startsWith(siteIdent)) {
                    result.add(m_TemplatesPackage.get(name));
                }
            }
            return result.iterator();
        }
    }

    // -------------------------------------------------------------------------
    /***
     * return a web app package in the Map m_TemplatesPackage looking at the key
     * 
     * @param (String) the key
     * @return (Object) a templates package or null
     */
    public Object getTemplatesPackage(String theKey) {

        synchronized (m_TemplatesPackage) {
            return m_TemplatesPackage.get(theKey);
        }
    }

    // -------------------------------------------------------------------------
    /***
     * scan a directory for templates package. Add them to the Map of TemplatesPackage.
     * 
     * @param the
     *            full path to a directory
     * @return an Map
     */
    public void scanDirectory(String path) throws JahiaException {

        synchronized (m_TemplatesPackage) {

            JahiaTemplatesPackage pack = null;

            File dir = new File(path);
            if (dir != null && dir.isDirectory()) {

                File[] files = dir.listFiles();
                int size = files.length;

                for (int i = 0; i < size; i++) {

                    if (files[i].canWrite()) {

                        pack = loadTemplatesInfo(files[i].getAbsolutePath());
                        if (pack != null) {
                            // System.out.println("added package in Map " + files[i].getAbsolutePath() );
                            // remove old map
                            m_TemplatesPackage.remove(files[i].getName());
                            // set the new map
                            m_TemplatesPackage.put(files[i].getName(), pack);
                        }
                    }
                }
            }
        }
    }

}