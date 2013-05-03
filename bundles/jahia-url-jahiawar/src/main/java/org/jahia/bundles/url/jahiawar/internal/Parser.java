/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.bundles.url.jahiawar.internal;

import org.ops4j.net.URLUtils;
import org.ops4j.pax.swissbox.bnd.BndUtils;
import org.ops4j.pax.swissbox.bnd.OverwriteMode;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for jahiawar: protocol.
 */
public class Parser {

    /**
     * Syntax for the url; to be shown on exception messages.
     */
    private static final String SYNTAX = "jahiawar:wrapped-jar-uri[,wrapping-instr-uri][$wrapping-instructions]";
    /**
     * Separator between wrapped jar url and instructions.
     */
    private static final String INSTRUCTIONS_SEPARATOR = "$";
    /**
     * Separator between wrapped jar url and instructions file url.
     */
    private static final String INSTRUCTIONS_FILE_SEPARATOR = ",";
    /**
     * Regexp pattern for matching jar, wrapping file and instructions.
     */
    private static final Pattern SYNTAX_JAR_BND_INSTR =
            Pattern.compile("(.+?)" + INSTRUCTIONS_FILE_SEPARATOR + "(.+?)\\" + INSTRUCTIONS_SEPARATOR + "(.+?)");
    /**
     * Regexp pattern for matching jar and instructions.
     */
    private static final Pattern SYNTAX_JAR_INSTR =
            Pattern.compile("(.+?)\\" + INSTRUCTIONS_SEPARATOR + "(.+?)");
    /**
     * Regexp pattern for matching jar and wrapping file.
     */
    private static final Pattern SYNTAX_JAR_BND =
            Pattern.compile("(.+?)" + INSTRUCTIONS_FILE_SEPARATOR + "(.+?)");

    /**
     * Wrapped jar URL.
     */
    private final URL jarURL;
    /**
     * Wrapping instructions URL.
     */
    private final Properties wrappingProperties;
    /**
     * Manifest overwrite mode.
     */
    private final OverwriteMode overwriteMode;

    /**
     * Creates a new protocol parser.
     *
     * @param path the path part of the url (without starting wrap:)
     * @throws java.net.MalformedURLException if provided path does not comply to expected syntax or has malformed urls
     */
    public Parser(final String path)
            throws MalformedURLException {
        if (path == null || path.trim().length() == 0) {
            throw new MalformedURLException("Path cannot be null or empty. Syntax " + SYNTAX);
        }
        if (path.startsWith(INSTRUCTIONS_SEPARATOR) || path.endsWith(INSTRUCTIONS_SEPARATOR)) {
            throw new MalformedURLException(
                    "Path cannot start or end with " + INSTRUCTIONS_SEPARATOR + ". Syntax " + SYNTAX
            );
        }
        wrappingProperties = new Properties();
        Matcher matcher = SYNTAX_JAR_BND_INSTR.matcher(path);
        if (matcher.matches()) {
            // we have all the parts
            jarURL = new URL(matcher.group(1));
            parseInstructionsFile(new URL(matcher.group(2)));
            wrappingProperties.putAll(BndUtils.parseInstructions(matcher.group(3)));
        } else if ((matcher = SYNTAX_JAR_INSTR.matcher(path)).matches()) {
            // we have a wrapped jar and instructions
            jarURL = new URL(matcher.group(1));
            wrappingProperties.putAll(BndUtils.parseInstructions(matcher.group(2)));
        } else if ((matcher = SYNTAX_JAR_BND.matcher(path)).matches()) {
            // we have a wrapped jar and a wrapping instructions file
            jarURL = new URL(matcher.group(1));
            parseInstructionsFile(new URL(matcher.group(2)));
        } else {
            //we have only a wrapped jar
            jarURL = new URL(path);
        }
        OverwriteMode overwriteMode;
        try {
            overwriteMode = OverwriteMode.valueOf(
                    wrappingProperties.getProperty("overwrite", OverwriteMode.KEEP.name()).toUpperCase()
            );
        } catch (Exception e) {
            overwriteMode = OverwriteMode.KEEP;
        }
        this.overwriteMode = overwriteMode;
    }

    /**
     * Loads the properties out of an url.
     *
     * @param bndFileURL url of the file containing the instructions
     * @throws MalformedURLException if the file could not be read
     */
    private void parseInstructionsFile(final URL bndFileURL)
            throws MalformedURLException {
        // TODO use the certificate check property from the handler instead of true bellow
        try {
            InputStream is = null;
            try {
                is = URLUtils.prepareInputStream(bndFileURL, true);
                wrappingProperties.load(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (IOException e) {
            throwAsMalformedURLException("Could not retrieve the instructions from [" + bndFileURL + "]", e);
        }
    }

    /**
     * Returns the wrapped URL if present, null otherwise
     *
     * @return wrapped jar URL
     */
    public URL getWrappedJarURL() {
        return jarURL;
    }

    /**
     * Returns the wrapping instructions as Properties.
     *
     * @return wrapping instructions as Properties
     */
    public Properties getWrappingProperties() {
        return wrappingProperties;
    }

    /**
     * Returns the overwrite mode.
     *
     * @return overwrite mode
     */
    public OverwriteMode getOverwriteMode() {
        return overwriteMode;
    }

    /**
     * Creates an MalformedURLException with a message and a cause.
     *
     * @param message exception message
     * @param cause   exception cause
     * @throws MalformedURLException the created MalformedURLException
     */
    private static void throwAsMalformedURLException(final String message, final Exception cause)
            throws MalformedURLException {
        final MalformedURLException exception = new MalformedURLException(message);
        exception.initCause(cause);
        throw exception;
    }


}
