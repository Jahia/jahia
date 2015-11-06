/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.logging;

import org.slf4j.profiler.Profiler;

import java.io.Serializable;

/**
 * This Service offer you to log information for metrics usages (not for information or debugging purposes).
 * <p/>
 * Or to profile some operations.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 24 nov. 2009
 */
public interface MetricsLoggingService extends Serializable{
    
    /**
     * Returns <code>true</code> if the logging is generally enabled; <code>false</code> otherwise.
     * @return <code>true</code> if the logging is generally enabled; <code>false</code> otherwise
     */
    boolean isEnabled();
    
    /**
     * Returns <code>true</code> if the profiler logging is generally enabled; <code>false</code> otherwise.
     * @return <code>true</code> if the profiler logging is generally enabled; <code>false</code> otherwise
     */
    boolean isProfilingEnabled();
    
    /**
     * Log some metric about a node.
     *
     * @param user           user achieving the operation
     * @param ipAddress      IP address of the user
     * @param sessionID      if available, the identifier of the session, otherwise null or an empty string is fine. Note
     *                       that if you use null it will be output verbatim in the log.
     * @param nodeIdentifier if available, the node identifier on which the event took place, otherwise null
     * @param path           the node path on which the operation has been achieved
     * @param nodeType       the type of the node
     * @param logTemplate    the name of the template log you want to use.
     * @param args           variable list of arguments depending of the template you choose
     */
    void logContentEvent(String user, String sessionID, String ipAddress, String nodeIdentifier, String path, String nodeType, String logTemplate, String... args);

    /**
     * Start a profiler and start the associated action (if the profilerName is not found it will create it)
     *
     * @param profilerName name of the profiler you want to use or create
     * @param action       the action you want to profile
     */
    void startProfiler(String profilerName, String action);

    /**
     * Stop all profiling for this profiler name
     *
     * @param profilerName the name of the profiler you want to stop
     */
    void stopProfiler(String profilerName);

    /**
     * Create a sub profiler of an existing profiler
     *
     * @param parentProfilerName the parent profiler name
     * @param nestedProfilerName the sub profiler name
     * @return the nested profiler
     */
    Profiler createNestedProfiler(String parentProfilerName, String nestedProfilerName);

    /**
     * Stop a nested profiler
     *
     * @param parentProfilerName the parent profiler name
     * @param nestedProfilerName the sub profiler name
     */
    void stopNestedProfiler(String parentProfilerName, String nestedProfilerName);

    /**
     * Start a new profiler and return it to the caller.
     *
     * @param profilerName the new profiler you want to start
     * @return the newly created Profiler
     */
    Profiler startProfiler(String profilerName);
}
