/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */package org.jahia.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class represents a product version, and can be initialized by a String.
 * This is a utility object to compare versions easily.
 *
 * Currently it recognized Strings of the form :
 *
 * major.minor.build.other1.other2.*Bbetanumber
 * major.minor.build.other1.other2.*RCreleasecandidatenumber
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

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(Version.class);

    private List<Integer> orderedVersionNumbers = new ArrayList<Integer>();
    private int betaNumber = -1;
    private int releaseCandidateNumber = -1;

    /**
     * Constructor. See class definition for syntax of the version string
     * @param versionString the String containing the version to analyze. See
     * class description for more details.
     * @throws NumberFormatException if there was a problem parsing the string
     * containing the version.
     */
    public Version(String versionString)
    throws NumberFormatException  {
        String workString = versionString.toLowerCase();
        int betaPos = workString.indexOf("b");
        int rcPos = workString.indexOf("rc");

        String betaString = null;
        String rcString = null;
        if (betaPos != -1) {
            betaString = workString.substring(betaPos + 1).trim();
            workString = workString.substring(0, betaPos);
            betaNumber = Integer.parseInt(betaString);
        } else if (rcPos != -1) {
            rcString = workString.substring(rcPos + 2).trim();
            workString = workString.substring(0, rcPos);
            releaseCandidateNumber = Integer.parseInt(rcString);
        }

        int underscorePos = workString.indexOf("_");
        if (underscorePos != -1) {
            workString = workString.substring(0, underscorePos).trim();
        }

        int hyphenPos = workString.indexOf("-");
        if (hyphenPos != -1) {
            workString = workString.substring(0, hyphenPos).trim();
        }

        StringTokenizer versionTokenizer = new StringTokenizer(workString, ".");
        while (versionTokenizer.hasMoreTokens()) {
            String curToken = versionTokenizer.nextToken().trim();
            int curVersionNumber = Integer.parseInt(curToken);
            orderedVersionNumbers.add(new Integer(curVersionNumber));
        }
        // JahiaConsole.println("Version.constructor",
        //                      "Version=" + this.toString());
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
            Integer curVersionNumber = orderedVersionNumbers.get(i);
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
    public int compareTo(Version rightVersion)
    throws ClassCastException {
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
                Integer versionNumber = orderedVersionNumbers.get(i);
                Integer rightVersionNumber = rightOrderedVersionNumbers.get(i);
                if (versionNumber.intValue() != rightVersionNumber.intValue()) {
                    return versionNumber.compareTo(rightVersionNumber);
                }
            }
            return -1;
        } else {
            // the right version has less number than this one.
            for (int i = 0; i < rightOrderedVersionNumbers.size() ; i++) {
                Integer versionNumber = orderedVersionNumbers.get(i);
                Integer rightVersionNumber = rightOrderedVersionNumbers.get(i);
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
            // if we got here it means the version are equal.
            return true;
        } else {
            return false;
        }
    }

}