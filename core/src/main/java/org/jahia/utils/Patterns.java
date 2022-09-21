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
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils;

import java.util.regex.Pattern;

/**
 * Collections of reusable {@link Pattern} instances.
 * 
 * @author Sergiy Shyrkov
 */
public final class Patterns {

    public static final Pattern AT = Pattern.compile("@", Pattern.LITERAL);

    public static final Pattern BACKSLASH = Pattern.compile("\\", Pattern.LITERAL);

    public static final Pattern COLON = Pattern.compile(":", Pattern.LITERAL);

    public static final Pattern COMMA = Pattern.compile(",", Pattern.LITERAL);

    public static final Pattern COMMA_WHITESPACE = Pattern.compile("[\\s,]+");

    public static final Pattern DASH = Pattern.compile("-", Pattern.LITERAL);

    public static final Pattern DOLLAR = Pattern.compile("$", Pattern.LITERAL);

    public static final Pattern DOT = Pattern.compile(".", Pattern.LITERAL);

    public static final Pattern DOUBLE_QUOTE = Pattern.compile("\"", Pattern.LITERAL);

    public static final Pattern EXCLAMATION_MARK = Pattern.compile("!", Pattern.LITERAL);
    
    public static final Pattern NUMBERS = Pattern.compile("[0-9]+");

    public static final Pattern PERCENT = Pattern.compile("%", Pattern.LITERAL);

    public static final Pattern PIPE = Pattern.compile("|", Pattern.LITERAL);

    public static final Pattern PLUS = Pattern.compile("+", Pattern.LITERAL);

    public static final Pattern SEMICOLON = Pattern.compile(";", Pattern.LITERAL);

    public static final Pattern SINGLE_QUOTE = Pattern.compile("'", Pattern.LITERAL);
    
    public static final Pattern SLASH = Pattern.compile("/", Pattern.LITERAL);

    public static final Pattern SPACE = Pattern.compile(" ", Pattern.LITERAL);

    public static final Pattern STAR = Pattern.compile("*", Pattern.LITERAL);

    public static final Pattern TRIPLE_HASH = Pattern.compile("###", Pattern.LITERAL);

    public static final Pattern TRIPPLE_DOLLAR = Pattern.compile("$$$", Pattern.LITERAL);

    public static final Pattern TRIPPLE_UNDERSCORE = Pattern.compile("___", Pattern.LITERAL);

    public static final Pattern UNDERSCORE = Pattern.compile("_", Pattern.LITERAL);

    public static final Pattern WEB_INF = Pattern.compile("/WEB-INF", Pattern.LITERAL);

    private Patterns() {
        super();
    }

}
