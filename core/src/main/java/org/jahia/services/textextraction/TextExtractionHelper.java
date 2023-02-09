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
package org.jahia.services.textextraction;

import java.io.Writer;

import javax.jcr.RepositoryException;

import org.drools.core.util.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.tools.OutWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Text extraction utility class for (re-)extracting text on existing document nodes.
 *
 * @author Benjamin Papez
 */
public final class TextExtractionHelper {

    private static final Logger logger = LoggerFactory.getLogger(TextExtractionHelper.class);

    private static final String CHECKING = "checking";
    private static final String FIXING = "fixing";
    private static final String REDOING = "redoing";

    private static boolean checkingExtractions;
    private static TextExtractionChecker extractionChecker;

    /**
     * Triggers the process of checking for missing text extractions check. If the <code>fixMissingExtraction</code> is set to <code>true</code> also
     * tries to extract the text now.
     *
     * This method ensures that only one check process runs at a time.
     *
     * @param fixMissingExtraction
     *            if set to <code>true</code> performs the text extraction now; in case of <code>false</code> only the missing extraction count is
     *            reported, but no fix is done
     * @param statusOut
     *            a writer to log current processing status into
     * @return the status object to indicate the result of the check
     * @throws RepositoryException
     *             in case of JCR errors
     */
    public static synchronized ExtractionCheckStatus checkMissingExtraction(
            final boolean fixMissingExtraction, final Writer statusOut) throws RepositoryException {
        if (checkingExtractions) {
            throw new IllegalStateException("The process fpr checking extractions is currently running."
                    + " Cannot start the second process.");
        }
        checkingExtractions = true;
        long timer = System.currentTimeMillis();
        final ExtractionCheckStatus status = new ExtractionCheckStatus();

        final OutWrapper out = new OutWrapper(logger, statusOut);

        out.echo("Start {} missing extraction ", fixMissingExtraction ? FIXING : CHECKING);

        extractionChecker = new TextExtractionChecker(status, fixMissingExtraction, out);

        try {
            JCRCallback<Object> callback = new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    extractionChecker.perform(session);
                    return null;
                }
            };
            out.echo("Missing extractions in DEFAULT workspace for: ");
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, null, callback);
            if (status.extractable == 0) {
                out.echo("none");
            }
            long extractableInDefault = status.extractable;
            out.echo("\nMissing extractions in LIVE workspace for: ");
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, callback);
            if (status.extractable == extractableInDefault) {
                out.echo("none");
            }
        } finally {
            checkingExtractions = false;
            logger.info("Done {} text extractions in {} ms. Status: {}", fixMissingExtraction ? FIXING : CHECKING,
                    (System.currentTimeMillis() - timer), status);
        }

        return status;
    }

    /**
     * Triggers the process of checking for files (by filter), where extraction is possible. If the <code>redoExtraction</code> is set to <code>true</code> also
     * tries to extract the text now.
     *
     * This method ensures that only one check process runs at a time.
     *
     * @param redoExtraction
     *            if set to <code>true</code> performs the text extraction now; in case of <code>false</code> only the extraction count is
     *            reported, but not redone
     * @param statusOut
     *            a writer to log current processing status into
     * @return the status object to indicate the result of the check
     * @throws RepositoryException
     *             in case of JCR errors
     */
    public static synchronized ExtractionCheckStatus checkExtractionByFilter(
            final boolean redoExtraction, RepositoryFileFilter filter, final Writer statusOut) throws RepositoryException {
        if (checkingExtractions) {
            throw new IllegalStateException("The process for checking extractions is currently running."
                    + " Cannot start the second process.");
        }
        checkingExtractions = true;
        long timer = System.currentTimeMillis();
        final ExtractionCheckStatus status = new ExtractionCheckStatus();

        final OutWrapper out = new OutWrapper(logger, statusOut);

        out.echo("Start {} extraction by filter", redoExtraction ? REDOING : CHECKING);

        extractionChecker = new TextExtractionChecker(status, redoExtraction, filter, out);

        try {
            JCRCallback<Object> callback = new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    extractionChecker.perform(session);
                    return null;
                }
            };
            if (StringUtils.isEmpty(filter.getWorkspace())
                    || "default".equals(filter.getWorkspace())) {
                out.echo("Extractions in DEFAULT workspace for: ");
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null,
                        Constants.EDIT_WORKSPACE, null, callback);
                if (status.extractable == 0) {
                    out.echo("none");
                }
            }
            long extractableInDefault = status.extractable;
            if (StringUtils.isEmpty(filter.getWorkspace())
                    || "live".equals(filter.getWorkspace())) {
                out.echo("\nExtractions in LIVE workspace for: ");
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null,
                        Constants.LIVE_WORKSPACE, null, callback);
                if (status.extractable == extractableInDefault) {
                    out.echo("none");
                }
            }
        } finally {
            checkingExtractions = false;
            logger.info("Done {} text extractions in {} ms. Status: {}", redoExtraction ? REDOING : CHECKING,
                    (System.currentTimeMillis() - timer), status);
        }

        return status;
    }


    /**
     * Forces stop of the extraction check process if it is currently running.
     */
    public static void forceStopExtractionCheck() {
        if (extractionChecker != null) {
            extractionChecker.stop();
        }
    }

    /**
     * Returns <code>true</code> if the process for checking extractions is currently running.
     *
     * @return <code>true</code> if the process for checking extractions is currently running; <code>false</code> otherwise
     */
    public static boolean isCheckingExtractions() {
        return checkingExtractions;
    }

    private TextExtractionHelper() {
        super();
    }
}
