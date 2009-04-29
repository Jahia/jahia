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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.jahia.utils.xml.XmlWriter;

/**
 * @author loom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LicensePackage {
    private String formatVersion = null;
    private String productName = null;
    private String releaseVersion = null;
    private String edition = null;
    private List licenses = null;

    /**
     * @param formatVersion
     * @param productName
     * @param releaseVersion
     * @param licenses
     */
    public LicensePackage(
        String formatVersion,
        String productName,
        String releaseVersion,
        String edition,
        List licenses) {
        this.formatVersion = formatVersion;
        this.productName = productName;
        this.releaseVersion = releaseVersion;
        this.edition = edition;
        this.licenses = licenses;
    }

    /**
     * @return
     */
    public String getFormatVersion() {
        return formatVersion;
    }

    /**
     * @return
     */
    public List getLicenses() {
        return licenses;
    }

    /**
     * @return
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @return
     */
    public String getReleaseVersion() {
        return releaseVersion;
    }

    public String getEdition() {
        return edition;
    }

    public void toXML(XmlWriter xmlWriter) throws IOException {
        xmlWriter.writeEntity("license-package");
        xmlWriter.writeAttribute("format", formatVersion);
        xmlWriter.writeAttribute("product", productName);
        xmlWriter.writeAttribute("release", releaseVersion);
        if (edition != null) {
            xmlWriter.writeAttribute("edition", edition);
        }
        Iterator licenseIter = licenses.iterator();
        while (licenseIter.hasNext()) {
            License curLicense = (License) licenseIter.next();
            curLicense.toXML(xmlWriter);
        }
        xmlWriter.endEntity();
    }

    public boolean verifyAllSignatures(
        InputStream keystoreIn,
        String keystorePassword)
        throws IOException {
        BufferedInputStream bufIn = new BufferedInputStream(keystoreIn);
        bufIn.mark(100000);
        Iterator licenseIter = licenses.iterator();
        while (licenseIter.hasNext()) {
            License curLicense = (License) licenseIter.next();
            if (!curLicense.verifySignature(bufIn, keystorePassword)) {
                return false;
            }
            bufIn.reset();
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
        BufferedInputStream bufIn = new BufferedInputStream(keystoreIn);
        bufIn.mark(100000);
        Iterator licenseIter = licenses.iterator();
        while (licenseIter.hasNext()) {
            License curLicense = (License) licenseIter.next();
            curLicense.updateSignature(
                bufIn,
                keystorePassword,
                privateKeyAlias,
                privateKeyPassword,
                certAlias);
            bufIn.reset();
        }
        return true;
    }

    public License getLicense(String componentName) {
        Iterator licenseIter = licenses.iterator();
        while (licenseIter.hasNext()) {
            License curLicense = (License) licenseIter.next();
            if (curLicense.getComponentName().equals(componentName)) {
                return curLicense;
            }
        }
        return null;
    }

}
