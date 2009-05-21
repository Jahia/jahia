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