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
/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.jahia.security.license;

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
