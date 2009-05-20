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

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.iterators.EnumerationIterator;
import org.jahia.resourcebundle.ResourceMessage;
import org.jahia.utils.xml.XmlWriter;

/**
 * @author loom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class License {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(License.class);

    private String certAlias;
    private String signatureString;
    private String componentName;
    private String licensee;
    private List limits;
    private Properties properties;

    /**
     * @param componentName
     * @param licensee
     * @param signatureString
     * @param certAlias
     * @param limits
     * @param properties
     */
    public License(
        String componentName,
        String licensee,
        String signatureString,
        String certAlias,
        List limits,
        Properties properties) {
        this.componentName = componentName;
        this.licensee = licensee;
        this.signatureString = signatureString;
        this.certAlias = certAlias;
        this.limits = limits;
        this.properties = properties;
    }

    /**
     * @return
     */
    public String getSignatureString() {
        return signatureString;
    }

    /**
     * @return
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * @return
     */
    public String getLicensee() {
        return licensee;
    }

    /**
     * @return
     */
    public List getLimits() {
        return limits;
    }

    /**
     * @return
     */
    public String getCertAlias() {
        return certAlias;
    }

    public boolean verifySignature(
        InputStream keystoreIn,
        String keystorePassword) {

        // now let's verify the signature...

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(keystoreIn, keystorePassword.toCharArray()
            /* keystore password */
            );
            java.security.cert.Certificate cert =
                ks.getCertificate(certAlias);
            PublicKey publicKey = cert.getPublicKey();

            Base64 base64 = new Base64();
            byte[] base64SigToVerify = signatureString.getBytes("ISO-8859-1");
            byte[] sigToVerify = base64.decode(base64SigToVerify);

            Signature sig = Signature.getInstance("SHA1withDSA");
            sig.initVerify(publicKey);

            byte[] buffer = toSignatureData().getBytes("ISO-8859-1");
            logger.debug("buffer length=" + buffer.length);

            sig.update(buffer, 0, buffer.length);

            return sig.verify(sigToVerify);
        } catch (Exception t) {
            logger.error("Error while verifying signature:", t);
        }
        return false;
    }

    public void updateSignature(
        InputStream keystoreIn,
        String keystorePassword,
        String privateKeyAlias,
        String privateKeyPassword,
        String certAlias) {

        this.certAlias = certAlias;

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(keystoreIn, keystorePassword.toCharArray());

            PrivateKey privateKey =
                (PrivateKey) ks.getKey(
                    privateKeyAlias,
                    privateKeyPassword.toCharArray());

            Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");

            dsa.initSign(privateKey);

            byte[] buffer = toSignatureData().getBytes("ISO-8859-1");
            logger.debug("buffer length=" + buffer.length);

            dsa.update(buffer, 0, buffer.length);
            byte[] realSig = dsa.sign();

            Base64 base64 = new Base64();

            byte[] base64Signature = base64.encode(realSig);
            signatureString = new String(base64Signature, "ISO-8859-1");

        } catch (Exception t) {
            logger.error("Error while updating signature:", t);
        }

    }

    public void toXML(XmlWriter xmlWriter) throws IOException {
        xmlWriter.writeEntity("license");
        xmlWriter.writeAttribute("component", componentName);
        xmlWriter.writeAttribute("licensee", licensee);
        xmlWriter.writeAttribute("signature", signatureString);
        xmlWriter.writeAttribute("certAlias", certAlias);
        Iterator limitIter = limits.iterator();
        while (limitIter.hasNext()) {
            Limit curLimit = (Limit) limitIter.next();
            curLimit.toXML(xmlWriter);
        }
        Iterator propertyNameEnum = new EnumerationIterator(properties.propertyNames());
        while (propertyNameEnum.hasNext()) {
            String curPropertyName = (String) propertyNameEnum.next();
            xmlWriter.writeEntity("property");
            xmlWriter.writeAttribute("name", curPropertyName);
            xmlWriter.writeAttribute("value", properties.getProperty(curPropertyName));
            xmlWriter.endEntity();
        }
        xmlWriter.endEntity();
    }

    public boolean checkLimits() {
        Iterator limitIter = limits.iterator();
        while (limitIter.hasNext()) {
            Limit curLimit = (Limit) limitIter.next();
            if (!curLimit.check()) {
                return false;
            }
        }
        return true;
    }

    public ResourceMessage[] getErrorMessages() {

        List errorMessages = new ArrayList();
        Iterator limitIter = limits.iterator();
        while (limitIter.hasNext()) {
            Limit curLimit = (Limit) limitIter.next();
            ResourceMessage curErrorMessage = curLimit.getErrorMessage();
            if (curErrorMessage != null) {
                errorMessages.add(curErrorMessage);
            }
        }
        return (ResourceMessage[]) errorMessages.toArray(new ResourceMessage[errorMessages.size()]);

    }

    public Limit getLimit(String name) {
        Iterator limitIter = limits.iterator();
        while (limitIter.hasNext()) {
            Limit curLimit = (Limit) limitIter.next();
            if (curLimit.getName().equals(name)) {
                return curLimit;
            }
        }
        return null;
    }

    protected String toSignatureData() {
        StringBuffer signDataBuf = new StringBuffer();
        signDataBuf.append(componentName);
        signDataBuf.append("\n");
        signDataBuf.append(licensee);
        signDataBuf.append("\n");
        signDataBuf.append(certAlias);
        signDataBuf.append("\n");
        Iterator limitIter = limits.iterator();
        while (limitIter.hasNext()) {
            Limit curLimit = (Limit) limitIter.next();
            signDataBuf.append(curLimit.toSignatureData());
            signDataBuf.append("\n");
        }
        Iterator propertyNameEnum = new EnumerationIterator(properties.propertyNames());
        while (propertyNameEnum.hasNext()) {
            String curPropertyName = (String) propertyNameEnum.next();
            signDataBuf.append(curPropertyName);
            signDataBuf.append("\n");
            signDataBuf.append(properties.getProperty(curPropertyName));
            signDataBuf.append("\n");
        }
        return signDataBuf.toString();
    }
  public void setLimits(List limits) {
    this.limits = limits;
  }
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

}
