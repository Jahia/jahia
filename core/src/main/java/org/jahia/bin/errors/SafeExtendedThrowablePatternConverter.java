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
package org.jahia.bin.errors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.ThrowablePatternConverter;
import org.apache.logging.log4j.util.Strings;

/**
 * Outputs the Throwable portion of the LoggingEvent as a full stack trace
 * unless this converter's option is 'short', where it just outputs the first line of the trace, or if
 * the number of lines to print is explicitly specified.
 * <p>
 * The extended stack trace will also include the location of where the class was loaded from and the
 * version of the jar if available.
 * <p>
 * If the message of the throwable contains a carriage return or line feed character, it gets encoded
 * to prevent log injection. 
 */
@Plugin(name = "SafeExtendedThrowablePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "sxEx", "sxThrowable", "sxException" })
public final class SafeExtendedThrowablePatternConverter extends ThrowablePatternConverter {
    private static final String STACKTRACE_LINE_START = "\n\tat";
    private static final String NESTED_STACKTRACE_LINE_START = "\n\t\tat";
    /**
     * Private constructor.
     * 
     * @param config
     * @param options options, may be null.
     */
    private SafeExtendedThrowablePatternConverter(final Configuration config, final String[] options) {
        super("SafeExtendedThrowable", "throwable", options, config);
    }

    /**
     * Gets an instance of the class.
     *
     * @param config The current Configuration.
     * @param options pattern options, may be null.  If first element is "short",
     *                only the first line of the throwable will be formatted.
     * @return instance of class.
     */
    public static SafeExtendedThrowablePatternConverter newInstance(final Configuration config, final String[] options) {
        return new SafeExtendedThrowablePatternConverter(config, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        final ThrowableProxy proxy = event.getThrownProxy();
        final Throwable throwable = event.getThrown();
        if ((throwable != null || proxy != null) && options.anyLines()) {
            if (proxy == null) {
                super.format(event, toAppendTo);
                return;
            }
            final String suffix = getSuffix(event);
            final String extStackTrace = proxy.getExtendedStackTraceAsString(options.getIgnorePackages(), options.getTextRenderer(), suffix);
            final int len = toAppendTo.length();
            if (len > 0 && !Character.isWhitespace(toAppendTo.charAt(len - 1))) {
                toAppendTo.append(' ');
            }
            appendExtendedStackTrace(toAppendTo, extStackTrace);
        }
    }
    
    private void appendExtendedStackTrace(final StringBuilder toAppendTo, final String extStackTrace) {
        if (!options.allLines() || !Strings.LINE_SEPARATOR.equals(options.getSeparator())) {
            toAppendTo.append(replaceLineSeparator(extStackTrace));
        } else {
            int firstMessageWithCRLFIndex = indexOfMessageWithCRLF(extStackTrace);
            if (firstMessageWithCRLFIndex != -1) {
                toAppendTo.append(replaceLineSeparatorInMessages(extStackTrace, firstMessageWithCRLFIndex));
            } else {
                toAppendTo.append(extStackTrace);
            }
        }        
    }
    
    private String replaceLineSeparator(String extStackTrace) {
        String[] array = extStackTrace.split(Strings.LINE_SEPARATOR);
        return StringUtils.join(array, options.getSeparator(), 0, options.minLines(array.length));
    }
    
    private int indexOfMessageWithCRLF(String extStackTrace) {
        int messageStartIndex = 0;
        int stackTraceStartIndex;
        do {
            stackTraceStartIndex = getNextIndex(extStackTrace.indexOf(STACKTRACE_LINE_START, messageStartIndex),
                    extStackTrace.indexOf(NESTED_STACKTRACE_LINE_START, messageStartIndex));
            if (stackTraceStartIndex != -1) {
                stackTraceStartIndex--;
                if (StringUtils.lastIndexOf(extStackTrace, '\r', stackTraceStartIndex) >= messageStartIndex
                        || StringUtils.lastIndexOf(extStackTrace, '\n', stackTraceStartIndex) >= messageStartIndex) {
                    return messageStartIndex;
                } else if (stackTraceStartIndex != -1) {
                    messageStartIndex = getNextNestedMessageIndex(extStackTrace, stackTraceStartIndex);
                }
            }
        } while (messageStartIndex != -1 && stackTraceStartIndex != -1);
        return -1;
    }
    
    private int getNextNestedMessageIndex(String extStackTrace, int stackTraceStartIndex) {
        return getNextIndex(extStackTrace.indexOf("Caused by:", stackTraceStartIndex),
                extStackTrace.indexOf("Suppressed:", stackTraceStartIndex));
    }

    private int getNextNestedMessageIndex(StringBuilder extStackTrace, int stackTraceStartIndex) {
        return getNextIndex(extStackTrace.indexOf("Caused by:", stackTraceStartIndex),
                extStackTrace.indexOf("Suppressed:", stackTraceStartIndex));
    }

    private int getNextIndex(int firstIndex, int secondIndex) {
        if (secondIndex == -1) {
            return firstIndex;
        } else if (firstIndex == -1) {
            return secondIndex;
        } else {
            return Math.min(firstIndex, secondIndex);
        }
    }
    
    private String replaceLineSeparatorInMessages(String extStackTrace, int messageWithCRLFIndex) {
        final StringBuilder sb = new StringBuilder(extStackTrace);
        do {
            int beginningOfStackTrace = getNextIndex(sb.indexOf(STACKTRACE_LINE_START, messageWithCRLFIndex),
                    sb.indexOf(NESTED_STACKTRACE_LINE_START, messageWithCRLFIndex));
            replaceCRLF(sb, messageWithCRLFIndex, beginningOfStackTrace);
            messageWithCRLFIndex = indexOfNestedExceptionMessageWithCRLF(sb, beginningOfStackTrace);
        } while (messageWithCRLFIndex != -1);
        return sb.toString();
    }
    
    private int indexOfNestedExceptionMessageWithCRLF(StringBuilder extStackTrace, int startIndex) {
        int stackTraceStartIndex;
        do {
            startIndex = getNextNestedMessageIndex(extStackTrace, startIndex);
            if (startIndex == -1) {
                return startIndex;
            }
            stackTraceStartIndex = getNextIndex(extStackTrace.indexOf(STACKTRACE_LINE_START, startIndex),
                    extStackTrace.indexOf(NESTED_STACKTRACE_LINE_START, startIndex));
            if (stackTraceStartIndex != -1) {
                stackTraceStartIndex--;
                if (StringUtils.lastIndexOf(extStackTrace, '\r', stackTraceStartIndex) >= startIndex
                        || StringUtils.lastIndexOf(extStackTrace, '\n', stackTraceStartIndex) >= startIndex) {
                    return startIndex;
                }
            }
            startIndex = stackTraceStartIndex; 
        } while (startIndex != -1 && stackTraceStartIndex != -1);
        return -1;
    }
    
    private StringBuilder replaceCRLF(StringBuilder toAppendTo, int start, int end) {
        for (int i = end - 1; i >= start; i--) { 
            final char c = toAppendTo.charAt(i);
            switch (c) {
                case '\r':
                    toAppendTo.setCharAt(i, '\\');
                    toAppendTo.insert(i + 1, 'r');
                    break;
                case '\n':
                    toAppendTo.setCharAt(i, '\\');
                    toAppendTo.insert(i + 1, 'n');
                    break;
                default:
                    break;
            }
        }
        return toAppendTo;
    }

}
