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
package org.jahia.exceptions;

/**
 * This exception is thrown in case the consistency cross-check 'site <-> host'
 * failes.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaSiteAndPageIDMismatchException extends JahiaException
{

  /** The serialVersionUID. */
  private static final long serialVersionUID = -1554145675009555250L;

  /**
   * Initializes an instance of this class.
   * 
   * @param resolvedSiteKey the site key resolved from the request parameters
   * @param siteKeyByHostName the site key resolved by the host name
   * @param hostName the host name
   */
  public JahiaSiteAndPageIDMismatchException(String resolvedSiteKey,
    String siteKeyByHostName, String hostName)
  {
    super("[Error code: 404] Consistency cross-check failed", " resolved Jahia site key '"
      + resolvedSiteKey + "' does not match the key '" + siteKeyByHostName
      + "', resolved by the host name '" + hostName + "'.", SITE_NOT_FOUND,
      JahiaException.WARNING_SEVERITY);
  }

}
