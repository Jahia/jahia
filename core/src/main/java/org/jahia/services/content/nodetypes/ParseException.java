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
package org.jahia.services.content.nodetypes;

/**
 * ParseException
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = -2210995894762804559L;

    /**
     * the line number where the error occurred
     */
    private final int lineNumber;

    /**
     * the column number where the error occurred
     */
    private final int colNumber;

    /**
     * the systemid of the source that produced the error
     */
    private final String filename;


    /**
     * Constructs a new instance of this class with <code>null</code> as its
     * detail message.
     */
    public ParseException(int lineNumber, int colNumber, String filename) {
        super();
        this.lineNumber = lineNumber;
        this.colNumber = colNumber;
        this.filename = filename;
    }

    /**
     * Constructs a new instance of this class with the specified detail
     * message.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public ParseException(String message, int lineNumber, int colNumber, String filename) {
        super(message);
        this.lineNumber = lineNumber;
        this.colNumber = colNumber;
        this.filename = filename;
    }

    /**
     * Constructs a new instance of this class with the specified detail
     * message and root cause.
     *
     * @param message   the detail message. The detail message is saved for
     *                  later retrieval by the {@link #getMessage()} method.
     * @param rootCause root failure cause
     */
    public ParseException(String message, Throwable rootCause, int lineNumber, int colNumber, String filename) {
        super(message, rootCause);
        this.lineNumber = lineNumber;
        this.colNumber = colNumber;
        this.filename = filename;
    }

    /**
     * Constructs a new instance of this class with the specified root cause.
     *
     * @param rootCause root failure cause
     */
    public ParseException(Throwable rootCause, int lineNumber, int colNumber, String filename) {
        super(rootCause);
        this.lineNumber = lineNumber;
        this.colNumber = colNumber;
        this.filename = filename;
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage() {
        StringBuilder b = new StringBuilder(super.getMessage());
        String delim = " (";
        if (filename != null && !filename.equals("")) {
            b.append(delim);
            b.append(filename);
            delim = ", ";
        }
        if (lineNumber >= 0) {
            b.append(delim);
            b.append("line ");
            b.append(lineNumber);
            delim = ", ";
        }
        if (colNumber >= 0) {
            b.append(delim);
            b.append("col ");
            b.append(colNumber);
            delim = ", ";
        }
        if (delim.equals(", ")) {
            b.append(")");
        }
        return b.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return super.toString(); // + " (" + systemId + ", line " + lineNumber +", col " + colNumber +")";
    }

}
