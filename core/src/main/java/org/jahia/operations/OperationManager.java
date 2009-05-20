/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
//  Here is the check to see if the page is Forbidden for the current user!
//

package org.jahia.operations;

import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.PipelineException;
import org.jahia.settings.SettingsBean;

/**
 * This class is responsible for dispatching to an engine. In the special
 * case of the Core engine (output of CMS data), the output caching mechanism
 * is interrogated to avoid processing a request and generate HTML if it isn't
 * needed.
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Inc.</p>
 * @author Eric Vassalli
 * @author Serge Huber
 * @author Fulco Houkes
 *
 * @version 3.1
 */
public class OperationManager {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(OperationManager.class);

    /** Default constructor, creates a new <code>OperationManager</code> object instance
     */
    public OperationManager () {
        logger.debug("Starting up...");
    }

    /** Handles the operations.
     *
     * @param jParams       the parameter bean
     * @param jSettings     the Jahia settings
     *
     * @throws org.jahia.exceptions.JahiaException
     *      when a general exception occured
     *
     * @throws org.jahia.exceptions.JahiaSessionExpirationException
     *      when the user session expired
     *
     * @throws org.jahia.exceptions.JahiaForbiddenAccessException
     *      when the user has not access to the requested operation
     */
    public void handleOperations (ProcessingContext jParams,
                                  SettingsBean jSettings)
        throws JahiaException,
        JahiaSessionExpirationException,
        JahiaForbiddenAccessException {

        Pipeline processPipeline = Jahia.getProcessPipeline();
        try {
            processPipeline.invoke(jParams);
        } catch (PipelineException pe) {
            Throwable t = pe;
            if (pe.getNested() != null) {
                t = pe.getNested();
            }
            if (t instanceof JahiaException) {
                throw (JahiaException) t;
            }
            if (t instanceof JahiaRuntimeException) {
                throw (JahiaRuntimeException) t;
            }
            logger.error("Error while processing request ", pe);
            throw new JahiaException("Error while processing request",
                                     "Exception in processing pipeline",
                                     JahiaException.ENGINE_ERROR,
                                     JahiaException.CRITICAL_SEVERITY, t);
        }
    }

}
