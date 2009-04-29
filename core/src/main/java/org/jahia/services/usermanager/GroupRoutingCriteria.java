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
 package org.jahia.services.usermanager;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * <p>Title: a routing criteria is a set of pattern matching rules that
 * define a criteria to route method calls to a specific object</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Inc.</p>
 *
 * @author Viceic Predrag Predrag.Viceic@ci.unil.ch>
 * @version 1.0
 */


public class GroupRoutingCriteria {
    private String name;
    private String description;
    private Properties conditions;
    private String destination;

    public GroupRoutingCriteria (String name,
                                 String description,
                                 Properties conditions,
                                 String destination) {
        this.name = name;
        this.description = description;
        this.conditions = conditions;
        this.destination = destination;
    }

    public String getName () {
        return name;
    }

    public String getDescription () {
        return description;
    }

    public Properties getConditions () {
        return conditions;
    }

    public String getDestination () {
        return destination;
    }


    public boolean matchesValues (Properties values) {

        // let's first test all the stupid stuff...
        if (conditions == null) {
            return false;
        }
        if (conditions.size () == 0) {
            return false;
        }
        if (values == null) {
            return false;
        }
        if (values.size () == 0) {
            return false;
        }

        // now let's do some real work guys...
        Iterator valueKeys = values.keySet().iterator();
        while (valueKeys.hasNext ()) {
            Object curKeyObj = valueKeys.next ();
            if (curKeyObj instanceof String) {
                String curKey = (String) curKeyObj;
                String curValue = values.getProperty (curKey);
                String curConditionPattern = conditions.getProperty (curKey);
                if (curConditionPattern != null) {
                    // we found a matching condition for this property key
                    if (!starMatching (curConditionPattern, curValue)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Case sensitive star pattern matching. eg t*t matches "test", but not
     * "true"
     *
     * @param starPattern
     * @param inputToTest
     *
     * @return true if the inputToTest string matches the starPattern pattern
     */
    private static boolean starMatching (String starPattern, String inputToTest) {
        // we try to make every effort to determine a match as quickly as possible,
        // but we must at least parse the pattern :(
        StringTokenizer patternTokens = new StringTokenizer (starPattern, "*", false);
        List patternMatchers = new ArrayList();

        while (patternTokens.hasMoreTokens ()) {
            String curToken = patternTokens.nextToken ();
            patternMatchers.add (curToken);
        }

        if (patternMatchers.size () == 0) {
            return false;
        }

        if (!starPattern.startsWith ("*")) {
            if (!inputToTest.startsWith ((String) patternMatchers.get(0))) {
                // beginning doesn't match...
                return false;
            }
        }
        if (!starPattern.endsWith ("*")) {
            if (!inputToTest.endsWith (
                    (String) patternMatchers.get(patternMatchers.size () - 1))) {
                // beginning doesn't match...
                return false;
            }
        }

        Iterator patternMatchersEnum = patternMatchers.iterator();
        int offsetInInput = 0;
        int matchPos = 0;
        String curMatcher = null;
        while (patternMatchersEnum.hasNext ()) {
            curMatcher = (String) patternMatchersEnum.next ();
            matchPos = inputToTest.indexOf (curMatcher, offsetInInput);
            if (matchPos == -1) {
                return false;
            }
            offsetInInput = matchPos + curMatcher.length ();
            if (offsetInInput >= inputToTest.length ()) {
                // we still have pattern to match but we got to the end of
                // the string too early.
                return false;
            }
        }
        // if we got here it means we've matched all the pattern matchers in
        // the input string.
        return true;
    }

}
