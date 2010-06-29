package org.jahia.services.shindig;

import com.google.common.collect.ImmutableSet;
import org.jsecurity.authc.*;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.realm.AuthorizingRealm;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;

import java.util.Set;

/**
 * JSecurity Realm for Shindig
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
      // hack since jsecurity uses no-param constructor, so we have trouble using injection.
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
