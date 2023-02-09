/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.jahia.settings.SettingsBean;

import javax.jcr.*;
import javax.jcr.query.qom.PropertyValue;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle results count
 */
public class CountHandler {

    /**
     * Class for setting information about wanted count type
     */
    public static class CountType {
        private boolean approxCount;
        private boolean skipChecks;
        private int approxCountLimit = SettingsBean.getInstance().getQueryApproxCountLimit();

        /**
         * Initializes an instance of CountType.
         */
        public CountType() {
            super();
        }
        /**
         * Is approximate or exact count required
         * @return true for approximate count and false for exact count
         */
        public boolean isApproxCount() {
            return approxCount;
        }
        /**
         * Sets whether approximate or exact count is required
         * @param approxCount true for approximate count and flase for exact count
         */
        public void setApproxCount(boolean approxCount) {
            this.approxCount = approxCount;
        }
        /**
         * Should ACL, visibility, publication checks be skipped for the count
         * @return true if all checks should be skipped and false if not
         */
        public boolean isSkipChecks() {
            return skipChecks;
        }
        /**
         * Sets whether ACL, visibility, publication checks should be skipped for the count
         * @param skipChecks true if all checks should be skipped and false if not
         */
        public void setSkipChecks(boolean skipChecks) {
            this.skipChecks = skipChecks;
        }
        /**
         * If approximate count is used, return how many results should be really iterated through to
         * then calculate the approximate number of further results
         * @return how many results should be iterated through before calculating approximate number of further results
         */
        public int getApproxCountLimit() {
            return approxCountLimit;
        }
        /**
         * Sets how many results should be iterated through before calculating approximate number of further results.
         * If not explicitly set, the queryApproxCountLimit from jahia configuration will be taken.
         * @param approxCountLimit how many results should be iterated through before calculating approximate number of further results
         */
        public void setApproxCountLimit(int approxCountLimit) {
            this.approxCountLimit = approxCountLimit;
        }
    }

    /**
     * The name of the facet function without prefix but with left parenthesis.
     */
    private static final String COUNT_FUNC_LPAR = "count(";

    /**
     * The start Name for the rep:count function: rep:count(
     */
    private static final Name REP_COUNT_LPAR = NameFactoryImpl.getInstance().create(
            Name.NS_REP_URI, COUNT_FUNC_LPAR);

    /**
     * Regexp pattern for an optional setting of the apprximate count limit
     */
    private static final Pattern APPROX_COUNT_LIMIT_PATTERN = Pattern.compile(".*approxCountLimit=(\\d*).*");

    /**
     * Checks if there is a count function in the column names and return the count type based on its parameters.
     * @param columns
     * @param session
     * @return the count type
     */
    public static CountType hasCountFunction(Map<String, PropertyValue> columns, SessionImpl session) {
        CountType countType = null;
        try {
            String repCount = session.getJCRName(REP_COUNT_LPAR);
            for (String column : columns.keySet()) {
                if (column.trim().startsWith(repCount)) {
                    countType = new CountType();
                    String countSettings = StringUtils.substringBetween(column, repCount, ")");
                    if (countSettings.contains("skipChecks=1")) {
                        countType.setSkipChecks(true);
                    }
                    if (countSettings.contains("approximate=1")) {
                        countType.setApproxCount(true);
                    }
                    Matcher matcher = APPROX_COUNT_LIMIT_PATTERN.matcher(countSettings);
                    if (matcher.matches()) {
                        countType.setApproxCount(true);
                        countType.setApproxCountLimit(Integer.parseInt(matcher.group(1)));
                    }
                    break;
                }
            }
        } catch (NamespaceException e) {}
        return countType;
    }

    /**
     * Wrap a count result in a fake query result row
     * @param count
     * @return the row
     */
    public static CountRow createCountRow(long count, boolean wasApproxLimitReached) {
        return new CountRow(count, wasApproxLimitReached);
    }
}
