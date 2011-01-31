/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.shindig;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.auth.UrlParameterAuthenticationHandler;
import org.apache.shindig.social.core.oauth.OAuthAuthenticationHandler;

import java.util.List;

/**
 * Based on AuthenticationHandlerProvider to add Jahia's authentication handler.
 *
 * @author loom
 *         Date: Jun 24, 2010
 *         Time: 11:47:09 AM
 */
public class JahiaAuthenticationHandlerProvider implements Provider<List<AuthenticationHandler>> {
  protected List<AuthenticationHandler> handlers;

  @Inject
  public JahiaAuthenticationHandlerProvider(UrlParameterAuthenticationHandler urlParam,
      OAuthAuthenticationHandler threeLeggedOAuth,
      JahiaAuthentificationHandler jahia,
      AnonymousAuthenticationHandler anonymous) {
    handlers = Lists.newArrayList(urlParam, threeLeggedOAuth, jahia, anonymous);
  }

  public List<AuthenticationHandler> get() {
    return handlers;
  }
}