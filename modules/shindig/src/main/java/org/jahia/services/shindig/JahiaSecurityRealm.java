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

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.realm.AuthorizingRealm;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;

import java.util.Set;

/**
 * Shiro Realm for Shindig
 * TODO This code doesn't really work yet.
 *
 * @author loom
 *         Date: Aug 19, 2009
 *         Time: 2:41:48 PM
 */
public class JahiaSecurityRealm extends AuthorizingRealm  {

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
      UsernamePasswordToken upToken = (UsernamePasswordToken) token;
      String username = upToken.getUsername();

      // Null username is invalid
      if (username == null) {
          throw new AccountException("Null usernames are not allowed by this realm.");
      }
      // hack since Shiro uses no-param constructor, so we have trouble using injection.
      JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(username);
      String password = "notimplemented";

      return  new SimpleAuthenticationInfo(username, password, this.getName());
    }

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
      //null usernames are invalid
      if (principals == null) {
        throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
      }

      String username = (String) principals.fromRealm(getName()).iterator().next();


      Set<String> roleNames;

      if (username == null) {
        roleNames = ImmutableSet.of();
      } else {
        roleNames = ImmutableSet.of("foo", "goo");
      }

      return new SimpleAuthorizationInfo(roleNames);
    }
}
