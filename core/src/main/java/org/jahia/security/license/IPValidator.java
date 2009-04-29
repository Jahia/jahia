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
 package org.jahia.security.license;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.jahia.resourcebundle.ResourceMessage;
import java.math.BigInteger;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 * @todo not finished yet.
 */

public class IPValidator extends AbstractValidator {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(IPValidator.class);

    public IPValidator(String name, String value, License license) {
        super(name, value, license);
    }

    public boolean assertEquals(String value) {

        InetAddress inetA = null;
        try {
            inetA = InetAddress.getLocalHost();
            if ( inetA == null ){
                logger.error("The inet address could not be found");
                errorMessage = new ResourceMessage("org.jahia.security.license.IPValidator.errorRetrievingLocalhostIP.label");
                return false;
            }
        } catch ( UnknownHostException uhe ){
            logger.error ("The inet address could not be found", uhe);
            errorMessage = new ResourceMessage("org.jahia.security.license.IPValidator.errorRetrievingLocalhostIP.label");
            return false;
        }

        logger.debug( "inet address hostname=" + inetA.getHostName() + ", hostAddress=" + inetA.getHostAddress());

        InetAddress authorizedIP = null;
        try {
            authorizedIP = InetAddress.getByName(value);
        } catch (UnknownHostException uhe) {
            logger.debug ("The inet address format is invalid", uhe);
            errorMessage = new ResourceMessage("org.jahia.security.license.IPValidator.invalidIPFormat.label", value);
            return false;
        }

        if ( !isIPAllowed(inetA, authorizedIP) ) {
            logger.error ("Invalid license ID <" + value + "> for host <" + inetA.getHostAddress() + ">");
            errorMessage = new ResourceMessage("org.jahia.security.license.IPValidator.invalidIPAddress.label", inetA.getHostAddress(), value);
            return false;
        }
        return true;
    }

    public boolean assertInRange(String fromValue, String toValue) {

        InetAddress inetA = null;
        try {
            inetA = InetAddress.getLocalHost();
            if ( inetA == null ){
                logger.error("The inet address could not be found");
                errorMessage = new ResourceMessage("org.jahia.security.license.IPValidator.errorRetrievingLocalhostIP.label");
                return false;
            }
        } catch ( UnknownHostException uhe ){
            logger.error ("The inet address could not be found", uhe);
            errorMessage = new ResourceMessage("org.jahia.security.license.IPValidator.errorRetrievingLocalhostIP.label");
            return false;
        }

        InetAddress fromIP = null;
        try {
            fromIP = InetAddress.getByName(fromValue);
        } catch (UnknownHostException uhe) {
            logger.error ("The inet address format is invalid", uhe);
            errorMessage = new ResourceMessage("org.jahia.security.license.IPValidator.invalidIPFormat.label", fromValue);
            return false;
        }
        InetAddress toIP = null;
        try {
            toIP = InetAddress.getByName(toValue);
        } catch (UnknownHostException uhe) {
            logger.error ("The inet address format is invalid", uhe);
            errorMessage = new ResourceMessage("org.jahia.security.license.IPValidator.invalidIPFormat.label", toValue);
            return false;
        }

        if (!isIPAllowed(inetA, fromIP, toIP)) {
            logger.error ("Host <" + inetA.getHostAddress() + "> is not in IP range " + fromValue + " to " + toValue);
            errorMessage = new ResourceMessage("org.jahia.security.license.IPValidator.invalidIPAddressForRange.label", inetA.getHostAddress(), fromValue, toValue);
            return false;
        }

        return true;
    }

    private boolean isIPAllowed(InetAddress testIP, InetAddress authorizationIP) {
        byte[] testAddress = testIP.getAddress();
        byte[] authorizationAddress = authorizationIP.getAddress();

        for (int i=0; i < authorizationAddress.length; i++) {
            if (authorizationAddress[i] != testAddress[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isIPAllowed(InetAddress testIP, InetAddress fromIP, InetAddress toIP) {
        byte[] testAddress = testIP.getAddress();
        byte[] fromAddress = fromIP.getAddress();
        byte[] toAddress = toIP.getAddress();

        BigInteger testInt = new BigInteger(testAddress);
        BigInteger fromInt = new BigInteger(fromAddress);
        BigInteger toInt = new BigInteger(toAddress);

        if ((testInt.compareTo(fromInt) >= 0) && (testInt.compareTo(toInt) <= 0)) {
            return true;
        }
        return false;
    }

}