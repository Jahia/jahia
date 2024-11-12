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
/*
 * Copyright (c) 2005, Your Corporation. All Rights Reserved.
 */

//
//  ParamBean
//  EV      03.11.2000
//  EV      23.12.2000  SettingsBean now in ParamBean
//  SH      24.01.2001  added getSession accessor
//  SH      24.01.2001  added some debugging code and some comments about comportement under Orion
//	DJ		30.01.2001  added an internal wrapper for FileUpload/HttpServletRequest getParameter.
//  SH      04.02.2001  added some comments on InputStream, Parameters, WebApps problems + javadoc
//  MJ      21.03.2001  replaced basic URL parameters with PathInfo elements
//	NK		17.04.2001	added Multisite features
//	NK		14.05.2001  Jump to requested site's home page when the actual requested page is not of this site
//						instead of page not found Exception.
//	NK		11.07.2001  Added last requested page parameter
//  JB      25.10.2001  Added setOperationMode methode
//  SH      01.02.2002  Added defaultParameterValues Map to reduce URL length
//                      when using default values for engine names, operation
//                      modes, etc...
//  FH      15.08.2003  - javadoc fixes
//                      - removed redundant class casting
//                      - removed unused private attribute
//
// Development notes : for the moment this class does not handle the problematic
// of reading an input stream multiple times. This can cause problems if for
// example Jahia needs to read the InputStream object from the request before
// forwarding it to a web application. Also, under some server implementations,
// such as the Orion server, retrieving an InputStream object before reading the
// parameters will cause an error when trying to call a getParameter method.
// The main issue of all this is that we are very dependant on the implementation
// of these methods right now. A solution that was studied involved writing
// parsers for the content of the request and managing the body internally inside
// Jahia. This is probably the most solid way to do it, but it involves a lot of
// development, especially since there should be a significant performance hit
// if not done fully.
// Basically the solution would be something like this :
// 1. Jahia retrieves the InputStream (or Reader) object
// 2. Jahia retrieves the full content and stores it either in memory or on disk
// depending on some criteria to be defined (size, type of request ?)
// 3. Jahia parses the parameters included in the request body, making sure it
// then uses only the result of that parsing internally
// 4. Jahia's dispatching service can then emulate a full web container behaviour
// without much problems, since it can intercept everything, include the request
// body (which is the part of the emulation currently missing). So the application
// could indeed retrieve an InputStream or a Reader that is passed the body only
// of that application and that comes from memory or disk storage instead of the
// socket.
//
// Advantages :
// - Allows for a FULL implementation of a request object
// - Allows Jahia and web applications to process the request multiple times
// - Improved security because we could only pass the body that concerns the
// web application.
//
// Disadvantages :
// - Serious performance hit because the worst case is : Jahia processes the
// request body, parses it, forwards it to the app, that reprocesses it again !
// The current case is : Jahia forwards it directly to the webapp, not reading
// it most of the time.
// - Loss of security because in some cases an application can receive a GET
// request that actually has the body of a POST request (this is because the
// emulation replaces the URL but not the body currently, mostly for performance
// reasons).
// - More usage of resources, since request bodies should have to be stored
// in memory and/or on disk, probably multiple times.
// The current decision was not to go forward with this implementation since it
// will involve a lot of work and that the benefits are dependant on the applications
// that must run under it. If the applications do not undergo problems in the
// meantime the current solution should be sufficient.
//
/**
 * todo Implement a system to store parameters either in the session or in
 * the URL, transparently, in order to generate short URLs
 *
 * todo Implement static (or search engine) friendly URLs, such as .html ending
 * URLs
 */

package org.jahia.params;

import org.jahia.api.Constants;


/**
 * @author Eric Vassalli
 * @author Khue NGuyen
 * @author Fulco Houkes
 * @author David Jilli
 * @author Serge Huber
 * @author Mikhael Janson
 * @author Xavier Lawrence
 * @deprecated use {@link Constants} instead
 */
@Deprecated
public class ParamBean extends ProcessingContext {

    private ParamBean() {
        super();
    }
}
