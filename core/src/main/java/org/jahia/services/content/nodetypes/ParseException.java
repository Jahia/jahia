/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
