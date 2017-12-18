/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
