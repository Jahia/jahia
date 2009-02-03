/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
