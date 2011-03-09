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

import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.oauth.OAuthEntry;
import org.apache.shindig.social.core.oauth.OAuthSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.AuthenticationMode;
import org.apache.shindig.common.crypto.Crypto;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.common.collect.MapMaker;
import com.google.common.base.Preconditions;

import java.util.concurrent.ConcurrentMap;
import java.util.UUID;
import java.util.Date;

/**
 * TODO Comment me
 *
 * @author loom
 *         Date: Aug 19, 2009
 *         Time: 2:48:41 PM
 */
public class JahiaOAuthDataStore implements OAuthDataStore {
    // This needs to be long enough that an attacker can't guess it.  If the attacker can guess this
    // value before they exceed the maximum number of attempts, they can complete a session fixation
    // attack against a user.
    private final int CALLBACK_TOKEN_LENGTH = 6;

    // We limit the number of trials before disabling the request token.
    private final int CALLBACK_TOKEN_ATTEMPTS = 5;

    private final OAuthServiceProvider SERVICE_PROVIDER;

    @Inject
    public JahiaOAuthDataStore(@Named("shindig.oauth.base-url") String baseUrl) {
      this.SERVICE_PROVIDER = new OAuthServiceProvider(baseUrl + "requestToken", baseUrl + "authorize", baseUrl + "accessToken");
    }

    // All valid OAuth tokens
    private static ConcurrentMap<String,OAuthEntry> oauthEntries = new MapMaker().makeMap();

    // Get the OAuthEntry that corresponds to the oauthToken
    public OAuthEntry getEntry(String oauthToken) {
      Preconditions.checkNotNull(oauthToken);
      return oauthEntries.get(oauthToken);
    }

    public OAuthConsumer getConsumer(String consumerKey) {
      //try {
        // TODO we should lookup deployed portlets here for consumerSecret info.
        /*
        JSONObject app = service.getDb().getJSONObject("apps").getJSONObject(Preconditions.checkNotNull(consumerKey));
        String consumerSecret = app.getString("consumerSecret");
        */
        String consumerSecret = "secret";

        if (consumerSecret == null)
            return null;

        // null below is for the callbackUrl, which we don't have in the db
        OAuthConsumer consumer = new OAuthConsumer(null, consumerKey, consumerSecret, SERVICE_PROVIDER);

        // Set some properties loosely based on the ModulePrefs of a gadget
        /*
        for (String key : ImmutableList.of("title", "summary", "description", "thumbnail", "icon")) {
          if (app.has(key))
            consumer.setProperty(key, app.getString(key));
        }
        */

        return consumer;

      /*
      } catch (JSONException e) {
         return null;
      }
      */
    }

    // Generate a valid requestToken for the given consumerKey
    public OAuthEntry generateRequestToken(String consumerKey, String oauthVersion,
        String signedCallbackUrl) {
      OAuthEntry entry = new OAuthEntry();
      entry.setAppId(consumerKey);
      entry.setConsumerKey(consumerKey);
      entry.setDomain("samplecontainer.com");
      entry.setContainer("default");

      entry.setToken(UUID.randomUUID().toString());
      entry.setTokenSecret(UUID.randomUUID().toString());

      entry.setType(OAuthEntry.Type.REQUEST);
      entry.setIssueTime(new Date());
      entry.setOauthVersion(oauthVersion);
      if (signedCallbackUrl != null) {
        entry.setCallbackUrlSigned(true);
        entry.setCallbackUrl(signedCallbackUrl);
      }

      oauthEntries.put(entry.getToken(), entry);
      return entry;
    }

    // Turns the request token into an access token
    public OAuthEntry convertToAccessToken(OAuthEntry entry) {
      Preconditions.checkNotNull(entry);
      Preconditions.checkState(entry.getType() == OAuthEntry.Type.REQUEST, "Token must be a request token");

      OAuthEntry accessEntry = new OAuthEntry(entry);

      accessEntry.setToken(UUID.randomUUID().toString());
      accessEntry.setTokenSecret(UUID.randomUUID().toString());

      accessEntry.setType(OAuthEntry.Type.ACCESS);
      accessEntry.setIssueTime(new Date());

      oauthEntries.remove(entry.getToken());
      oauthEntries.put(accessEntry.getToken(), accessEntry);

      return accessEntry;
    }

    // Authorize the request token for the given user id
    public void authorizeToken(OAuthEntry entry, String userId) {
      Preconditions.checkNotNull(entry);
      entry.setAuthorized(true);
      entry.setUserId(Preconditions.checkNotNull(userId));
      if (entry.isCallbackUrlSigned()) {
        entry.setCallbackToken(Crypto.getRandomDigits(CALLBACK_TOKEN_LENGTH));
      }
    }

    public void disableToken(OAuthEntry entry) {
      Preconditions.checkNotNull(entry);
      entry.setCallbackTokenAttempts(entry.getCallbackTokenAttempts() + 1);
      if (!entry.isCallbackUrlSigned() || entry.getCallbackTokenAttempts() >= CALLBACK_TOKEN_ATTEMPTS) {
        entry.setType(OAuthEntry.Type.DISABLED);
      }

      oauthEntries.put(entry.getToken(), entry);
    }

    public void removeToken(OAuthEntry entry) {
      Preconditions.checkNotNull(entry);

      oauthEntries.remove(entry.getToken());
    }

    // Return the proper security token for a 2 legged oauth request that has been validated
    // for the given consumerKey. App specific checks like making sure the requested user has the
    // app installed should take place in this method
    public SecurityToken getSecurityTokenForConsumerRequest(String consumerKey, String userId) {
      String domain = "samplecontainer.com";
      String container = "default";

      return new OAuthSecurityToken(userId, null, consumerKey, domain, container, null,
          AuthenticationMode.OAUTH_CONSUMER_REQUEST.name());

    }
}
