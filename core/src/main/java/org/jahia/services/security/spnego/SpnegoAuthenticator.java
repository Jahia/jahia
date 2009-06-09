/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.jahia.services.security.spnego;

import java.security.Principal;

import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;

/**
 */
public class SpnegoAuthenticator {

  /**
   * SpnegoHandler.
   */
  private SpnegoHandler handler;
  
  /**
   * Response to the client. Can be null if server has nothing to say.
   */
  private byte[] sendBackToken;

  /**
   * @see org.ietf.jgss.GSSName .
   */
  private GSSName name;
  
  /**
   * User name.
   */
  private String username;
  
  /**
   * User principal. 
   */
  private Principal principal;

  /**
   * Logger.
   */
  private static final Logger LOG = Logger.getLogger("ws.security.SpnegoAuthenticator");
  
  /**
   * Constructs instance of SpnegoAuthenticator.
   */
  public SpnegoAuthenticator() {
    handler = new SpnegoHandler();
  }
  
  /**
   * {@inheritDoc}
   */
  public void doAuthenticate(byte[] token) throws Exception {
    sendBackToken = handler.authenticate(token);
  }

  /**
   * {@inheritDoc}
   */
  public Principal getPrincipal() {
    if (principal != null)
      return principal;
    
    String name = getUser();
    if (name != null)
      return principal = new UserPrincipal(name);
    
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getSendBackToken() {
    return sendBackToken;
  }

  /**
   * {@inheritDoc}
   */
  public String getUser() {
    if (username != null)
      return username;
    
    if (name == null) {
      try {
        name = handler.getGSSContext().getSrcName();
      } catch (GSSException e) {
        LOG.error("GSSContext is not established!", e);
      }
    }
    
    if (name != null) {
      // Name returned as user@DOMAIN
      String n = name.toString();
      int d = n.indexOf('@');
      return username = n.substring(0, d);
    }
    
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isComplete() {
    return handler.isComplete();
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isSuccess() {
    return handler.isEstablished();
  }

}

