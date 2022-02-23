/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.hibernate;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.boot.model.naming.*;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A custom naming strategy to prefix the table names, prefix any name that uses an SQL reserved word, and forces names to use lowercase.
 * Also limits the table and column names to 30 characters using the
 * <a href="http://code.google.com/p/hibernate-naming-strategy-for-oracle/">hibernate-naming-strategy-for-oracle</a> project as an example.
 *
 * @author Serge Huber
 * @author Sergiy Shyrkov
 */
public class JahiaNamingStrategy extends PhysicalNamingStrategyStandardImpl {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaNamingStrategy.class);

    private static final int MAX_LENGTH = 30;

    private static final long serialVersionUID = 2436201913019906777L;
    private static final String VOWELS = "aeiou";
    private static String[] sqlReservedWords = new String[0];

    static {
        InputStream sqlReservedWordsStream = JahiaNamingStrategy.class.getClassLoader().getResourceAsStream(
                "org/jahia/hibernate/sqlReservedWords.txt");
        if (sqlReservedWordsStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sqlReservedWordsStream));
            String newLine = null;
            List<String> reservedWordList = new ArrayList<String>();
            try {
                while ((newLine = bufferedReader.readLine()) != null) {
                    reservedWordList.add(newLine.trim().toLowerCase());
                }
                sqlReservedWords = reservedWordList.toArray(new String[reservedWordList.size()]);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(bufferedReader);
            }
        }
    }

    protected static String addUnderscores(String name) {
        StringBuilder buf = new StringBuilder(name.replace('.', '_'));
        for (int i = 1; i < buf.length() - 1; i++) {
            if (
                    Character.isLowerCase(buf.charAt(i - 1)) &&
                            Character.isUpperCase(buf.charAt(i)) &&
                            Character.isLowerCase(buf.charAt(i + 1))
            ) {
                buf.insert(i++, '_');
            }
        }
        return buf.toString().toLowerCase(Locale.ROOT);
    }

    private static String ensureLength(String name) {
        if (name.length() <= MAX_LENGTH)
            return name;

        String[] tokens = StringUtils.split(name, '_');

        shorten(name, tokens);

        return StringUtils.join(tokens, '_');
    }

    private static int getIndexOfLongest(String[] tokens) {
        int maxLength = 0;
        int index = -1;
        for (int i = 0; i < tokens.length; i++) {
            String string = tokens[i];
            if (maxLength < string.length()) {
                maxLength = string.length();
                index = i;
            }
        }
        return index;
    }

    public static boolean isSqlReservedWord(String name) {
        String lowerCaseName = name.toLowerCase();
        for (String reservedWord : sqlReservedWords) {
            if (reservedWord.equals(lowerCaseName)) {
                return true;
            }
        }
        return false;
    }

    public static String prefixSqlReservedWords(String name) {
        if (isSqlReservedWord(name)) {
            return "r_" + name;
        } else {
            return name;
        }
    }

    public static String processColumnName(String columnName) {
        return ensureLength(processNameCase(prefixSqlReservedWords(addUnderscores(columnName))));
    }

    public static String processNameCase(String name) {
        return name.toLowerCase();
    }

    public static String processTableName(String tableName) {
        return ensureLength(processNameCase("jbpm_" + addUnderscores(tableName)));
    }

    private static void shorten(String someName, String[] tokens) {
        int currentLength = someName.length();
        while (currentLength > MAX_LENGTH) {
            int tokenIndex = getIndexOfLongest(tokens);
            String oldToken = tokens[tokenIndex];
            tokens[tokenIndex] = substringAfterLastVowel(oldToken);
            currentLength -= oldToken.length() - tokens[tokenIndex].length();
        }
    }

    private static String substringAfterLastVowel(String token) {
        boolean vowelFound = false;
        for (int i = token.length() - 1; i >= 0; i--) {
            char c = token.charAt(i);
            boolean isVowel = VOWELS.indexOf(c) != -1;
            if (isVowel) {
                vowelFound = true;
            } else if (vowelFound) {
                return token.substring(0, i + 1);
            }
        }
        return StringUtils.EMPTY;
    }


    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return Identifier.toIdentifier(processTableName(super.toPhysicalTableName(name, context).getText()));
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return Identifier.toIdentifier(processColumnName(super.toPhysicalSequenceName(name, context).getText()));
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return Identifier.toIdentifier(processColumnName(super.toPhysicalColumnName(name, context).getText()));
    }
}
