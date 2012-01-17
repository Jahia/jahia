/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

 package org.jahia.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a product version, and can be initialized by a String.
 * This is a utility object to compare versions easily.
 *
 * Currently it recognized Strings of the form :
 *
 * major.minor.servicepack.hotfix.other1.other2.*Bbetanumber
 * major.minor.servicepack.hotfix.other1.other2.*RCbetanumber
 * major.minor.servicepack.hotfix.other1.other2.*_updatenumber
 * major.minor.servicepack.hotfix.other1.other2.*_updatenumber-bbuildernumber
 * major.minor.servicepack.hotfix.other1.other2.*_updatenumber-qualifier1-qualifier2
 *
 * "B" and "RC" can be uppercase or minor case, the comparison is case insensitive
 * for the moment.
 *
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Inc.</p>
 * @author Serge Huber
 * @version 3.0
 */

public class Version implements Comparable<Version> {

    private static Logger logger = LoggerFactory.getLogger(Version.class);

    private List<Integer> orderedVersionNumbers = new ArrayList<Integer>();
    private int betaNumber = -1;
    private int releaseCandidateNumber = -1;
    private String updateMarker;
    private List<String> qualifiers = new ArrayList<String>();

    /**
     * Constructor. See class definition for syntax of the version string
     * @param versionString the String containing the version to analyze. See
     * class description for more details.
     * @throws NumberFormatException if there was a problem parsing the string
     * containing the version.
     */
    public Version(final String versionString) throws NumberFormatException {
        if (versionString == null) {
            throw new NumberFormatException("Null string passed as version !");
        }
        String trimmedVersionString = versionString.trim();
        String versionPart = null;
        StringTokenizer tokenizer = new StringTokenizer(trimmedVersionString, "-");
        if (tokenizer.hasMoreTokens()) {
            versionPart = tokenizer.nextToken().toLowerCase();
        } else {
            versionPart = trimmedVersionString.toLowerCase();
        }
        while (tokenizer.hasMoreTokens()) {
            qualifiers.add(tokenizer.nextToken());
        }
        int betaPos = versionPart.indexOf("b");
        int rcPos = versionPart.indexOf("rc");

        String betaString = null;
        String rcString = null;
        if (betaPos != -1) {
            betaString = versionPart.substring(betaPos + 1).trim();
            versionPart = versionPart.substring(0, betaPos);
            betaNumber = Integer.parseInt(betaString);
        } else if (rcPos != -1) {
            rcString = versionPart.substring(rcPos + 2).trim();
            versionPart = versionPart.substring(0, rcPos);
            releaseCandidateNumber = Integer.parseInt(rcString);
        }

        int underscorePos = versionPart.indexOf("_");
        if (underscorePos != -1) {
            updateMarker = versionPart.substring(underscorePos + 1).trim();
            versionPart = versionPart.substring(0, underscorePos).trim();
        }

        StringTokenizer versionTokenizer = new StringTokenizer(versionPart, ".");
        while (versionTokenizer.hasMoreTokens()) {
            String curToken = versionTokenizer.nextToken().trim();
            int curVersionNumber = Integer.parseInt(curToken);
            orderedVersionNumbers.add(new Integer(curVersionNumber));
        }
    }

    /**
     * Returns true if the version represents a beta version
     * @return true if a beta version.
     */
    public boolean isBeta() {
        if (betaNumber != -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the version represents a release candidate version.
     * @return true is release candidate
     */
    public boolean isReleaseCandidate() {
        if (releaseCandidateNumber != -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the version represents final (that is to say non-beta
     * and non-release candidate) version.
     * @return true if version is neither a beta or a release candidate version.
     */
    public boolean isFinal() {
        if ( (betaNumber == -1) &&
             (releaseCandidateNumber == -1) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Generates a String from the internal data structure. This is not
     * necessarily equals to the String passed to the constructor, especially
     * since here we return a lower case string.
     * @return a lower case String representing the version
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for (int i=0; i < orderedVersionNumbers.size(); i++) {
            Integer curVersionNumber = (Integer) orderedVersionNumbers.get(i);
            result.append(curVersionNumber.intValue());
            if (i < (orderedVersionNumbers.size() -1) ) {
                result.append(".");
            }
        }
        if (betaNumber != -1) {
            result.append("b");
            result.append(betaNumber);
        } else if (releaseCandidateNumber != -1) {
            result.append("rc");
            result.append(releaseCandidateNumber);
        }
        if (updateMarker != null) {
            result.append("_");
            result.append(updateMarker);
        }
        if (qualifiers.size() > 0) {
            for (String qualifier : qualifiers) {
                result.append("-");
                result.append(qualifier);
            }
        }
        return result.toString();
    }

    /**
     * Implements the compareTo method from the Comparable interface. This
     * allows this class to be sorted by version number.
     *
     * ComparisonImpl is done the following way :
     * 1. compares the version number until there is no more to compare
     * 2. compares the "state" (beta, release candidate, final)
     *
     * Examples :
     *    4.0, 4.0.1 returns -1
     *    4.0B1, 4.0.1B1 returns -1
     *    4.1.0, 4.0.1 returns 1
     *    4.0.0, 4.0.0 return 0
     *    4.0.1B1, 4.0.1RC2 returns -1
     *    ...
     *
     * @param o a Version object to compare to. If this is not a Version class
     * object, then a ClassCastException will be raised
     * @return -1 if this version is "smaller" than the one specified. 0 if
     * it is equal, or 1 if it bigger.
     * @throws ClassCastException if the passed parameter (o) is not a Version
     * class object.
     */
    public int compareTo(Version o)
    throws ClassCastException {
        Version rightVersion = (Version) o;
        List<Integer> rightOrderedVersionNumbers = rightVersion.getOrderedVersionNumbers();

        if (this.equals(rightVersion)) {
            return 0;
        }

        if (orderedVersionNumbers.size() == rightOrderedVersionNumbers.size()) {
            for (int i = 0; i < orderedVersionNumbers.size(); i++) {
                Integer versionNumber = orderedVersionNumbers.get(i);
                Integer rightVersionNumber = rightOrderedVersionNumbers.get(i);
                if (versionNumber.intValue() != rightVersionNumber.intValue()) {
                    return versionNumber.compareTo(rightVersionNumber);
                }
            }
            // now we must compare beta numbers, release candidate number and regular versions
            // to determine which is higher.
            if (isBeta() && rightVersion.isBeta()) {
                if (betaNumber < rightVersion.getBetaNumber()) {
                    return -1;
                } else {
                    return 1;
                }
            }

            if (isReleaseCandidate() && rightVersion.isReleaseCandidate()) {
                if (releaseCandidateNumber < rightVersion.getReleaseCandidateNumber()) {
                    return -1;
                } else {
                    return 1;
                }
            }

            if (isBeta() && rightVersion.isReleaseCandidate()) { return -1; }
            if (isBeta() && rightVersion.isFinal()) { return -1; }
            if (isReleaseCandidate() && rightVersion.isBeta()) { return 1; }
            if (isReleaseCandidate() && rightVersion.isFinal()) { return -1; }
            if (isFinal() && rightVersion.isBeta()) { return 1; }
            if (isFinal() && rightVersion.isReleaseCandidate()) { return 1; }

            logger.debug("Unable to compare two versions " +
                                 this.toString() + " and " +
                                 rightVersion.toString() +
                                 " returning equality...");
            return 0;

        } else if (orderedVersionNumbers.size() < rightOrderedVersionNumbers.size()) {
            // this version has less numbers that the right one.
            for (int i = 0; i < orderedVersionNumbers.size(); i++) {
                Integer versionNumber = (Integer) orderedVersionNumbers.get(i);
                Integer rightVersionNumber = (Integer) rightOrderedVersionNumbers.get(i);
                if (versionNumber.intValue() != rightVersionNumber.intValue()) {
                    return versionNumber.compareTo(rightVersionNumber);
                }
            }
            return -1;
        } else {
            // the right version has less number than this one.
            for (int i = 0; i < rightOrderedVersionNumbers.size() ; i++) {
                Integer versionNumber = (Integer) orderedVersionNumbers.get(i);
                Integer rightVersionNumber = (Integer) rightOrderedVersionNumbers.get(i);
                if (versionNumber.intValue() != rightVersionNumber.intValue()) {
                    return versionNumber.compareTo(rightVersionNumber);
                }
            }
            return 1;
        }
    }

    /**
     * Returns an array list of Integer objects containing the version number.
     * index 0 is the major version number, index 1 is the minor, etc... This
     * method does not return beta or release candidate versions.
     * @return an List containing Integers that represent the version
     * number. The ordered of these are significant
     */
    public List<Integer> getOrderedVersionNumbers() {
        return orderedVersionNumbers;
    }

    /**
     * Returns the beta number part of the version number
     * @return an integer representing the beta number, or -1 if this is not
     * a beta version.
     */
    public int getBetaNumber() {
        return betaNumber;
    }

    /**
     * Returns the release candidate number part of the version number
     * @return an integer representing the release candidate  number, or -1
     * if this is not a release candidate version.
     */
    public int getReleaseCandidateNumber() {
        return releaseCandidateNumber;
    }

    /**
     * Returns the update marker string, located after the underscore. This is usually a numeric value but we support
     * String values
     * @return
     */
    public String getUpdateMarker() {
        return updateMarker;
    }

    /**
     * Return the list of qualifiers if there were any specified.
     * @return
     */
    public List<String> getQualifiers() {
        return qualifiers;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            Version rightVersion = (Version) obj;
            List<Integer> rightOrderedVersionNumbers = rightVersion.getOrderedVersionNumbers();
            if (orderedVersionNumbers.size() != rightOrderedVersionNumbers.size()) {
                return false;
            }
            if (betaNumber != rightVersion.getBetaNumber()) {
                return false;
            }
            if (releaseCandidateNumber != rightVersion.getReleaseCandidateNumber()) {
                return false;
            }
            for (int i=0; i < orderedVersionNumbers.size(); i++) {
                Integer leftVersionNumber = orderedVersionNumbers.get(i);
                Integer rightVersionNumber = rightOrderedVersionNumbers.get(i);
                if (leftVersionNumber.intValue() != rightVersionNumber.intValue()) {
                    return false;
                }
            }
            if ((updateMarker != null) && (rightVersion.getUpdateMarker() != null)) {
                if (!updateMarker.equals(rightVersion.getUpdateMarker())) {
                    return false;
                }
            } else {
                if ((updateMarker == null) && (rightVersion.getUpdateMarker() == null)) {
                } else {
                    return false;
                }
            }
            if (!qualifiers.equals(rightVersion.getQualifiers())) {
                return false;
            }
            // if we got here it means the version are equal.
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the major version number, which is the first number in a X.X.X.X format or 0 if there is no first number
     * @return
     */
    public int getMajorVersion() {
        if (orderedVersionNumbers.size() > 0) {
            return orderedVersionNumbers.get(0);
        } else {
            return 0;
        }
    }

    /**
     * Returns the minor version number, which is the second number in a X.X.X.X format or 0 if there is no second number
     * @return
     */
    public int getMinorVersion() {
        if (orderedVersionNumbers.size() > 1) {
            return orderedVersionNumbers.get(1);
        } else {
            return 0;
        }
    }

    /**
     * Returns the service pack version number, which is the third number in a X.X.X.X format or 0 if there is no third number
     * @return
     */
    public int getServicePackVersion() {
        if (orderedVersionNumbers.size() > 2) {
            return orderedVersionNumbers.get(2);
        } else {
            return 0;
        }
    }

    /**
     * Returns the patch version number, which is the fourth number in a X.X.X.X format or 0 if there is no fourth number
     * @return
     */
    public int getPatchVersion() {
        if (orderedVersionNumbers.size() > 3) {
            return orderedVersionNumbers.get(3);
        } else {
            return 0;
        }
    }

}