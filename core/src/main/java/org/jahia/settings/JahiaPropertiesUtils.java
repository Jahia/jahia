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
package org.jahia.settings;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class JahiaPropertiesUtils {
    public static final Charset CHARSET = Charset.forName(System.getProperty("jahia.properties.charset", "UTF-8"));
    private static final Logger logger = LoggerFactory.getLogger(JahiaPropertiesUtils.class);

    private JahiaPropertiesUtils() {
        super();
    }

    /**
     * Add the provided entry into the specified properties file.
     *
     * @param key                 the key to be added
     * @param value               the key value to be added
     * @param comment             the entry comment
     * @param afterLinePattern    the pattern of the line after which the entry should be added; <code>null</code> if the entry should be
     *                            appended to the end of the file
     * @param jahiaPropertiesFile the target properties file
     * @throws IOException in case of an I/O error
     */
    public static final void addEntry(String key, String value, String comment, String afterLinePattern, File jahiaPropertiesFile) throws IOException {
        List<String> lines = FileUtils.readLines(jahiaPropertiesFile, CHARSET);

        if (getPropertyLine(key, lines) != null || getCommentedPropertyLine(key, lines) != null) {
            return;
        }

        int insertPosition = lines.size();
        if (afterLinePattern != null) {
            final Pattern pattern = Pattern.compile(afterLinePattern);
            String afterLineMatch = (String) CollectionUtils.find(lines, line -> pattern.matcher(((String) line)).matches());
            if (afterLineMatch != null) {
                insertPosition = lines.indexOf(afterLineMatch) + 1;
            }
        }
        List<String> toBeAdded = new ArrayList<>();
        if (insertPosition == lines.size()) {
            toBeAdded.add("");
        }
        if (comment != null) {
            String[] commentLines = StringUtils.split(comment, "\n");
            for (String commentLine : commentLines) {
                if (" ".equals(commentLine)) {
                    // add an empty line
                    toBeAdded.add("");
                } else if (commentLine.length() == 0 || commentLine.charAt(0) != '#') {
                    toBeAdded.add("# " + commentLine);
                } else {
                    toBeAdded.add(commentLine);
                }
            }
        }

        toBeAdded.add(key + " = " + value);

        for (int i = toBeAdded.size() - 1; i >= 0; i--) {
            lines.add(insertPosition, toBeAdded.get(i));
        }

        FileUtils.writeLines(jahiaPropertiesFile, CHARSET.name(), lines);
    }

    /**
     * Remove content from jahia.properties, look at the RemoveOperation class java doc to see all the possibility available
     *
     * @param operations the remove operations
     * @throws IOException in case of an I/O error
     */
    public static void removeEntry(RemoveOperation[] operations) throws IOException {
        File cfg = detectJahiaPropertiesFile();
        if (cfg == null) {
            jahiaPropertiesFileNotFound();
            for (RemoveOperation operation : operations) {
                operation.logError();
            }
            return;
        }

        List<String> innerlines = FileUtils.readLines(cfg, CHARSET);

        List<RemoveOperation> failedOperations = new ArrayList<>();
        for (RemoveOperation operation : operations) {
            boolean success = false;
            // if operation is not valid continue
            if (operation == null || operation.getPattern() == null || operation.getPattern().length == 0) continue;

            if (operation.getType().equals(RemoveOperation.Type.EXACT_BLOCK)) {
                int startIndex = findInLines(innerlines, operation.getPattern()[0], 0);
                boolean blockFound = false;
                while (startIndex != -1 && !blockFound) {
                    if (startIndex + operation.getPattern().length <= innerlines.size()) {
                        boolean goToNext = false;
                        // check that next block lines correspond to the next lines after startIndex
                        for (int i = 1; i < operation.getPattern().length; i++) {
                            String subLine = operation.getPattern()[i];
                            if (!innerlines.get(startIndex + i).equals(subLine)) {
                                goToNext = true;
                                // line do not match, continue to browse lines to next matching first block line
                                break;
                            }
                        }
                        if (goToNext) {
                            startIndex = findInLines(innerlines, operation.getPattern()[0], startIndex);
                        } else {
                            blockFound = true;
                        }
                    } else {
                        startIndex = findInLines(innerlines, operation.getPattern()[0], startIndex);
                    }
                }
                if (blockFound) {
                    for (int i = (operation.getPattern().length - 1); i >= 0; i--) {
                        innerlines.remove(startIndex + i);
                    }
                    success = true;
                } else {
                    // Block not found log info
                    failedOperations.add(operation);
                }
            } else if (operation.getType().equals(RemoveOperation.Type.REGEXP_LINE)) {
                final Pattern pattern = Pattern.compile(operation.getPattern()[0]);
                int deletePosition = -1;

                String afterLineMatch = (String) CollectionUtils.find(innerlines, line -> pattern.matcher(((String) line)).matches());
                if (afterLineMatch != null) {
                    deletePosition = innerlines.indexOf(afterLineMatch) + 1;
                }

                if (deletePosition != -1) {
                    innerlines.remove(deletePosition - 1);
                    success = true;
                } else {
                    failedOperations.add(operation);
                }
            }
            if (success) {
                logger.info("Removed entry {} from {}",operation.concernedProperty, cfg);
            }
        }
        if (!failedOperations.isEmpty()) {
            for (RemoveOperation operation : failedOperations) {
                operation.logError();
            }
        }

        FileUtils.writeLines(cfg, CHARSET.name(), innerlines);
    }

    /**
     * Replaces all occurrences of the specified string in the jahia.properties file with the provided replacement.
     *
     * @param searchString the string to be replaced
     * @param replacement  the replacement
     * @throws IOException in case of an I/O error
     */
    public static void replace(String searchString, String replacement) throws IOException {
        File cfg = detectJahiaPropertiesFile();
        if (cfg == null) {
            jahiaPropertiesFileNotFound();
            return;
        }
        String content = FileUtils.readFileToString(cfg, CHARSET);
        String modifiedContent = StringUtils.replaceAllTokens(content, searchString, replacement);
        if (!modifiedContent.equals(content)) {
            logger.info("Replaced in {} occurrences of the {} with {}", cfg.getCanonicalPath(), searchString, replacement);
            FileUtils.writeStringToFile(cfg, modifiedContent, CHARSET.name());
        }
    }

    private static int findInLines(List<String> lines, String lineToFind, int startIndex) {
        for (int i = 0; i < lines.size(); i++) {
            if (i <= startIndex) {
                continue;
            }
            String line = lines.get(i);
            if (line.equals(lineToFind)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Add the provided entry into the automatically detected jahia.properties file. If the jahia.properties location could not be detected,
     * issues a warning.
     *
     * @param key              the key to be added
     * @param value            the key value to be added
     * @param comment          the entry comment
     * @param afterLinePattern the pattern of the line after which the entry should be added; <code>null</code> if the entry should be
     *                         appended to the end of the file
     * @throws IOException in case of an I/O error
     */
    public static final void addEntry(String key, String value, String comment, String afterLinePattern, String error) throws IOException {
        File cfg = detectJahiaPropertiesFile();
        if (cfg != null) {
            addEntry(key, value, comment, afterLinePattern, cfg);
            logger.info("Added entry {} into {}",key, cfg);
        } else {
            jahiaPropertiesFileNotFound();
            logger.info(error);
        }
    }

    /**
     * Set a property to its default value in case the property is missing from the java.properties file.
     * Leave the file unchanged if the property is already set to any value.
     * Un-comment the property instead of adding it in case it is already set to the default value but commented out.
     * <p>
     * Note, multiline property values are unsupported.
     *
     * @param key          Property key
     * @param defaultValue Default property value
     */
    public static final void uncommentOrAddEntryIfMissing(String key, String defaultValue) throws IOException {

        File cfg = detectJahiaPropertiesFile();
        if (cfg == null) {
            jahiaPropertiesFileNotFound();
            return;
        }

        List<String> lines = FileUtils.readLines(cfg, CHARSET);

        String configured = getPropertyLine(key, lines);

        if (configured != null) {
            // A value is configured: do not change anything.
            return;
        }

        String commented = getCommentedPropertyLine(key, lines);

        if (commented == null) {
            // No commented out value found: add default one at the end of the file.
            lines.add("");
            lines.add(key + " = " + defaultValue);
        } else {
            int index = findInLines(lines, commented, 0);
            if (commented.endsWith(defaultValue)) {
                // Commented out default value found: just un-comment it.
                lines.remove(index);
                lines.add(index, key + " = " + defaultValue);
            } else {
                // Commented out non-default value found: keep it as it is and add default one at the next line.
                lines.add(index + 1, key + " = " + defaultValue);
            }
        }

        FileUtils.writeLines(cfg, lines);

        logger.info("Added entry {} into {}", key, cfg);
    }

    public static String getPropertyLine(String key, List<String> lines) {
        final Pattern patternConfigured = Pattern.compile("^[ \\t]*" + Pattern.quote(key) + "[ \\t]*[=:][ \\t]*\\S+$", Pattern.MULTILINE);
        return (String) CollectionUtils.find(lines, line -> patternConfigured.matcher((String) line).matches());
    }

    public static String getCommentedPropertyLine(String key, List<String> lines) {
        final Pattern patternConfigured = Pattern.compile("^[ \\t]*[#!]+[ \\t]*" + Pattern.quote(key) + "[ \\t]*[=:][ \\t]*\\S+$", Pattern.MULTILINE);
        return (String) CollectionUtils.find(lines, line -> patternConfigured.matcher((String) line).matches());
    }

    public static void jahiaPropertiesFileNotFound() {
        logger.warn("The fix applier cannot detect the location of the jahia.properties file");
    }

    /**
     * Detects the location of a jahia.properties file.
     *
     * @return the detected location of a jahia.properties file or <code>null</code> if the file could not be found
     * @throws IOException in case of an I/O error
     */
    public static File detectJahiaPropertiesFile() throws IOException {
        Resource[] resources = SettingsBean.getInstance().getApplicationContext().getResources("classpath*:jahia/jahia.properties");
        Optional<Resource> resource = Arrays.stream(resources).findFirst();

        if (resource.isPresent()) {
            File propFile = resource.get().getFile();
            if (propFile.exists()) {
                return propFile.getCanonicalFile();
            }
        }

        return null;
    }

    /**
     * Remove operation to remove content from jahia.properties file
     * Operation type are used to specify the nature of the operation:
     * EXACT_BLOCK: accept multiple lines deletion and search for that exact lines in the file then remove them
     * REGEXP_LINE: accept only one line deletion and search for the first line that match the regexp, then remove it
     */
    public static class RemoveOperation {
        private String[] pattern;
        private Type type;
        private String concernedProperty;

        public RemoveOperation(String concernedProperty, Type type, String... pattern) {
            this.concernedProperty = concernedProperty;
            this.pattern = pattern;
            this.type = type;
        }

        public void logError() {
            logger.info("[WARNING] The fix applier failed to remove (comments and/or property) from jahia.properties");
            logger.info("The concerned deprecated property is: {}", concernedProperty);
            logger.info("It could be that the property no longer exists or requires manual removal. Verify by looking at the following line(s) from your jahia.properties file:");
            if (type.equals(Type.EXACT_BLOCK)) {
                logger.info("-- START --");
                for (String line : pattern) {
                    logger.info(line);
                }
                logger.info("-- END --");
            } else if (type.equals(Type.REGEXP_LINE)) {
                logger.info("Regexp: {}",pattern[0]);
            }
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public String[] getPattern() {
            return pattern;
        }

        public void setPattern(String[] pattern) {
            this.pattern = pattern;
        }

        public enum Type {
            EXACT_BLOCK, REGEXP_LINE
        }
    }
}
