/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {

    private StringUtils() {
    }

    /**
     * Delete first occurence of a given token in a string.
     *
     * @param string the string
     * @param token  token to be deleted
     * @return the processed string
     */
    public static String deleteFirstToken(String string, String token) {
        int position = string.indexOf(token);
        if (position == -1) {
            return string;
        }
        return string.substring(0, position) + string.substring(position + token.length(), string.length());
    }

    /**
     * Replace first occurence of a given token in a string by another one.
     *
     * @param string   the string
     * @param oldToken token to be deleted
     * @param newToken token to replace the old one
     * @return the processed string
     */
    public static String replaceFirstToken(String string, String oldToken, String newToken) {
        int position = string.indexOf(oldToken);
        if (position == -1) {
            return string;
        }
        return string.substring(0, position) + newToken + string.substring(position + oldToken.length(), string.length());
    }

    /**
     * Replace last occurence of a given token in a string by another one.
     *
     * @param string   the string
     * @param oldToken token to be deleted
     * @param newToken token to replace the old one
     * @return the processed string
     */
    public static String replaceLastToken(String string, String oldToken, String newToken) {
        int position = string.lastIndexOf(oldToken);
        if (position == -1) {
            return string;
        }
        return string.substring(0, position) + newToken + string.substring(position + oldToken.length(), string.length());
    }

    /**
     * Replace all occurences of a given token in a string by another one.
     *
     * @param string   the string
     * @param oldToken token to be deleted
     * @param newToken token to replace the old one
     * @return the processed string
     */
    public static String replaceAllTokens(String string, String oldToken, String newToken) {
        int[] positions = getTokenPositions(string, oldToken);
        if (positions.length == 0) {
            return string;
        }
        StringBuilder ret = new StringBuilder();
        ret.append(string.substring(0, positions[0]));
        for (int i = 0; i < positions.length - 1; i++) {
            ret.append(newToken);
            ret.append(string.substring(positions[i] + oldToken.length(), positions[i + 1]));
        }
        ret.append(newToken);
        ret.append(string.substring(positions[positions.length - 1] + oldToken.length(), string.length()));
        return ret.toString();
    }

    /**
     * Remove all characters before a given token in a string.
     *
     * @param string    the string
     * @param token     the token
     * @param keepToken keep the token or delete it as well
     * @return the processed string
     */
    public static String cleanBeforeFirstToken(String string, String token, boolean keepToken) {
        int position = string.indexOf(token);
        if (position == -1) {
            return string;
        }
        return string.substring(keepToken ? position : position + token.length(), string.length());
    }

    /**
     * Remove all characters after a given token in a string.
     *
     * @param string    the string
     * @param token     the token
     * @param keepToken keep the token or delete it as well
     * @return the processed string
     */
    public static String cleanAfterLastToken(String string, String token, boolean keepToken) {
        int position = string.lastIndexOf(token);
        if (position == -1) {
            return string;
        }
        return string.substring(0, keepToken ? position + token.length() : position);
    }

    /**
     * Get all the positions of a given token in a string.
     *
     * @param string the string
     * @param token  the token
     * @return the positions
     */
    public static int[] getTokenPositions(String string, String token) {
        int[] result = new int[0];
        if (token.length() == 0) {
            return result;
        }
        int start = 0;
        int tempInd = string.indexOf(token, start);
        if (tempInd == -1) {
            return result;
        }
        List<Integer> positions = new ArrayList<>();
        while (tempInd != -1) {
            positions.add(tempInd);
            start = tempInd + token.length();
            tempInd = string.indexOf(token, start);
        }
        int size = positions.size();
        result = new int[size];
        for (int i = 0; i < size; i++)
            result[i] = positions.get(i);
        return result;
    }

    /**
     * Split a string into as many part as needed, according to occurences of the given token.
     *
     * @param string     the string
     * @param splitToken the token
     * @return the strings from the split
     */
    public static String[] split(String string, String splitToken) {
        String[] init = new String[]{string};
        if (splitToken.length() == 0) {
            return init;
        }

        int[] positions = getTokenPositions(string, splitToken);
        if (positions.length == 0) {
            return init;
        }

        List<String> splitted = new ArrayList<>();
        if (0 < positions[0]) {
            splitted.add(string.substring(0, positions[0]));
        }
        for (int i = 0; i < positions.length - 1; i++) {
            if (positions[i] + splitToken.length() < positions[i + 1]) {
                splitted.add(string.substring(positions[i] + splitToken.length(), positions[i + 1]));
            }
        }
        if (positions[positions.length - 1] < string.length()) {
            splitted.add(string.substring(positions[positions.length - 1] + splitToken.length()));
        }
        String[] result = new String[splitted.size()];
        for (int i = 0; i < splitted.size(); i++) {
            result[i] = splitted.get(i);
        }
        return result;
    }

    /**
     * Remove duplicates into a sorted String array.
     *
     * @param sortedArray the array to process
     * @return the processed array
     */
    public static String[] removeDuplicatesFromSortedStringArray(String[] sortedArray) {
        List<String> unique = new ArrayList<>();
        String temp = "";
        for (String aSortedArray : sortedArray) {
            if (!temp.equals(aSortedArray)) {
                temp = aSortedArray;
                unique.add(temp);
            }
        }
        String[] result = new String[unique.size()];
        for (int i = 0; i < unique.size(); i++) {
            result[i] = unique.get(i);
        }
        return result;
    }

    public static String[] sortStringArrayAndRemoveDuplicates(String[] array) {
        Arrays.sort(array);
        return removeDuplicatesFromSortedStringArray(array);
    }


    public static boolean isPartiallyContained(String path, String[] highlightedElements) {
        boolean result = false;
        for (int i = 0; !result && (i < highlightedElements.length); i++) {
            String highlighter = highlightedElements[i];
            if (highlighter.contains(".java")) {
                replaceLastToken(highlighter, ".java", "");
            }
            if (path.contains(highlighter)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Clean a given String list by removing all items containig a given token.
     *
     * @param stringList the list to clean
     * @param token      the token
     * @return the cleaned list
     */
    public static String[] removeStringsContainingToken(String[] stringList, String token) {
        List<String> cleared = new ArrayList<>();
        for (String path : stringList) {
            if (!path.contains(File.separator + token + File.separator)) {
                cleared.add(path);
            }
        }
        String[] result = new String[cleared.size()];
        for (int i = 0; i < cleared.size(); i++) {
            result[i] = cleared.get(i);
        }
        return result;
    }

}

