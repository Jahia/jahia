/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.jahia.security.license;

import static org.jahia.security.license.LicenseConstants.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.jahia.utils.xml.XmlWriter;
import org.xml.sax.SAXException;

/**
 * @author loom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LicenseManager {

    private static LicenseManager singletonInstance = null;

    private List licensePackages = new ArrayList();
    private Digester digester;

    private LicenseManager() {
        super();
    }

    public static LicenseManager getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new LicenseManager();
        }
        return singletonInstance;
    }

    public void load(String xmlLicenseFileName)
        throws IOException, SAXException {
        licensePackages.clear();
        initDigester();
        File xmlLicenseFile = new File(xmlLicenseFileName);
        this.digester.parse(xmlLicenseFile);
    }

    public void load(Reader xmlLicenseReader)
        throws IOException, SAXException {
        licensePackages.clear();
        initDigester();
        this.digester.parse(xmlLicenseReader);
    }

    public void save(String xmlLicenseFileName) throws IOException {
        FileWriter fileWriter = new FileWriter(xmlLicenseFileName);
        XmlWriter xmlWriter = new XmlWriter(fileWriter);
        xmlWriter.enablePrettyPrint(true);
        xmlWriter.writeEntity("jahia-licenses");
        Iterator licensePackageIter = licensePackages.iterator();
        while (licensePackageIter.hasNext()) {
            LicensePackage curLicensePackage =
                (LicensePackage) licensePackageIter.next();
            curLicensePackage.toXML(xmlWriter);
        }
        xmlWriter.endEntity();
        fileWriter.flush();
        fileWriter.close();
    }

    public void save(Writer xmlLicenseWriter) throws IOException {
        XmlWriter xmlWriter = new XmlWriter(xmlLicenseWriter);
        xmlWriter.enablePrettyPrint(true);
        xmlWriter.writeEntity("jahia-licenses");
        Iterator licensePackageIter = licensePackages.iterator();
        while (licensePackageIter.hasNext()) {
            LicensePackage curLicensePackage =
                (LicensePackage) licensePackageIter.next();
            curLicensePackage.toXML(xmlWriter);
        }
        xmlWriter.endEntity();
        xmlWriter.close();
    }

    public License getLicenseByComponentName(String componentName) {
        Iterator licensePackageIter = licensePackages.iterator();
        while (licensePackageIter.hasNext()) {
            LicensePackage curLicensePackage =
                (LicensePackage) licensePackageIter.next();
            List licenses = curLicensePackage.getLicenses();
            Iterator licenseIter = licenses.iterator();
            while (licenseIter.hasNext()) {
                License curLicense = (License) licenseIter.next();
                if (curLicense.getComponentName().equals(componentName)) {
                    return curLicense;
                }
            }
        }
        return null;
    }

    public LicensePackage getLicensePackage(String productName) {
        Iterator licensePackageIter = licensePackages.iterator();
        while (licensePackageIter.hasNext()) {
            LicensePackage curLicensePackage =
                (LicensePackage) licensePackageIter.next();
            if (curLicensePackage.getProductName().equals(productName)) {
                return curLicensePackage;
            }
        }
        return null;
    }

    public LicensePackage getJahiaLicensePackage() {
        return getLicensePackage(JAHIA_PRODUCT_NAME);
    }

    /**
     * Returns the Jahia product (the core feature) expiration date.
     * 
     * @return the Jahia product (the core feature) expiration date; returns -1
     *         if the limit is not specified
     */
    public long getJahiaExpirationDate() {
        long expiraionTime = -1;
        int maxUsageDays = getJahiaMaxUsageDays();
        if (maxUsageDays > 0) {
            expiraionTime = ((CommonDaysLeftValidator) getJahiaLicensePackage()
                    .getLicense(CORE_COMPONENT).getLimit(
                            MAX_USAGE_DAYS_LIMIT_NAME).getValidator())
                    .getCommonInstallDate().getTime()
                    + 1000L * 60L * 60L * 24L * maxUsageDays;
        } else {
            Limit dateLimit = getJahiaLicensePackage().getLicense(
                    CORE_COMPONENT).getLimit(DATE_LIMIT_NAME);
            if (dateLimit != null) {
                expiraionTime = ((DateValidator) dateLimit.getValidator())
                        .getDate();
            }

        }
        return expiraionTime;
    }
    
    /**
     * Returns the global limit for usage days for Jahia product (the core
     * feature).
     * 
     * @return the global limit for usage days for Jahia product (the core
     *         feature); returns -1 if the limit is not specified
     */
    public int getJahiaMaxUsageDays() {
        Limit maxUsageDays = getJahiaLicensePackage()
                .getLicense(CORE_COMPONENT).getLimit(MAX_USAGE_DAYS_LIMIT_NAME);
        return maxUsageDays != null ? Integer.valueOf(maxUsageDays
                .getValueStr()) : -1;
    }

    public boolean verifyAllSignatures(
        InputStream keystoreIn,
        String keystorePassword)
        throws IOException {
        Iterator licensePackageIter = licensePackages.iterator();
        while (licensePackageIter.hasNext()) {
            LicensePackage curLicensePackage =
                (LicensePackage) licensePackageIter.next();
            if (!curLicensePackage
                .verifyAllSignatures(keystoreIn, keystorePassword)) {
                return false;
            }
        }
        return true;
    }

    public boolean updateAllSignatures(
        InputStream keystoreIn,
        String keystorePassword,
        String privateKeyAlias,
        String privateKeyPassword,
        String certAlias)
        throws IOException {
        Iterator licensePackageIter = licensePackages.iterator();
        while (licensePackageIter.hasNext()) {
            LicensePackage curLicensePackage =
                (LicensePackage) licensePackageIter.next();
            curLicensePackage.updateAllSignatures(
                keystoreIn,
                keystorePassword,
                privateKeyAlias,
                privateKeyPassword,
                certAlias);
        }
        return true;
    }

    private void initDigester() {
        this.digester = new Digester();

        AddLicensePackageRule addLicensePackageRule =
            new AddLicensePackageRule();
        digester.addRule(
            "jahia-licenses/license-package",
            addLicensePackageRule);
        digester.addRule(
            "jahia-licenses/license-package/license",
            addLicensePackageRule.addLicenseRule);
        digester.addRule(
            "jahia-licenses/license-package/license/limit",
            addLicensePackageRule.addLicenseRule.addLimitRule);
        digester.addRule(
            "jahia-licenses/license-package/license/property",
            addLicensePackageRule.addLicenseRule.addPropertyRule);
    }

    final class AddLicensePackageRule extends Rule {
        private List licenses = new ArrayList();
        AddLicenseRule addLicenseRule = new AddLicenseRule();

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
            LicensePackage newLicensePackage =
                new LicensePackage(
                    properties.getProperty("format"),
                    properties.getProperty("product"),
                    properties.getProperty("release"),
                    properties.getProperty("edition"),
                    licenses);
            licensePackages.add(newLicensePackage);
            licenses = new ArrayList();
            properties = new Properties();
        }

        final class AddLicenseRule extends Rule {
            private List limits = new ArrayList();
            private Properties licenseProperties = new Properties();
            AddLimitRule addLimitRule = new AddLimitRule();
            AddPropertyRule addPropertyRule = new AddPropertyRule();

            private Properties properties = new Properties();
            private License currentLicense = null;

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
                currentLicense =
                    new License(
                    properties.getProperty("component"),
                    properties.getProperty("licensee"),
                    properties.getProperty("signature"),
                    properties.getProperty("certAlias"),
                    limits,
                    licenseProperties);
            }

            public void end(String namespace, String name) throws Exception {
                licenses.add(currentLicense);
                currentLicense = null;
                limits = new ArrayList();
                licenseProperties = new Properties();
                properties = new Properties();
            }

            final class AddLimitRule extends Rule {

                Properties properties = new Properties();

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
                    Limit limit =
                        new Limit(
                            properties.getProperty("class"),
                            properties.getProperty("name"),
                            properties.getProperty("value"),
                            currentLicense);
                    limits.add(limit);
                    properties = new Properties();
                }

            }

            final class AddPropertyRule extends Rule {

                Properties properties = new Properties();

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
                    licenseProperties.setProperty(properties.getProperty("name"), properties.getProperty("value"));
                    properties = new Properties();
                }

            }
        }
    }

}
