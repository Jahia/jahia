package org.jahia.services.shindig;

import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.oauth.OAuthEntry;
import org.apache.shindig.social.sample.spi.JsonDbOpensocialService;
import org.apache.shindig.social.core.oauth.OAuthSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.AuthenticationMode;
import org.apache.shindig.common.crypto.Crypto;
import org.json.JSONObject;
import org.json.JSONException;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.common.collect.MapMaker;
import com.google.common.collect.ImmutableList;
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
        // TODO we should lookup deployed mashups here for consumerSecret info.
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
      entry.appId = consumerKey;
      entry.consumerKey = consumerKey;
      entry.domain = "samplecontainer.com";
      entry.container = "default";

      entry.token = UUID.randomUUID().toString();
      entry.tokenSecret = UUID.randomUUID().toString();

      entry.type = OAuthEntry.Type.REQUEST;
      entry.issueTime = new Date();
      entry.oauthVersion = oauthVersion;
      if (signedCallbackUrl != null) {
        entry.callbackUrlSigned = true;
        entry.callbackUrl = signedCallbackUrl;
      }

      oauthEntries.put(entry.token, entry);
      return entry;
    }

    // Turns the request token into an access token
    public OAuthEntry convertToAccessToken(OAuthEntry entry) {
      Preconditions.checkNotNull(entry);
      Preconditions.checkState(entry.type == OAuthEntry.Type.REQUEST, "Token must be a request token");

      OAuthEntry accessEntry = new OAuthEntry(entry);

      accessEntry.token = UUID.randomUUID().toString();
      accessEntry.tokenSecret = UUID.randomUUID().toString();

      accessEntry.type = OAuthEntry.Type.ACCESS;
      accessEntry.issueTime = new Date();

      oauthEntries.remove(entry.token);
      oauthEntries.put(accessEntry.token, accessEntry);

      return accessEntry;
    }

    // Authorize the request token for the given user id
    public void authorizeToken(OAuthEntry entry, String userId) {
      Preconditions.checkNotNull(entry);
      entry.authorized = true;
      entry.userId = Preconditions.checkNotNull(userId);
      if (entry.callbackUrlSigned) {
        entry.callbackToken = Crypto.getRandomDigits(CALLBACK_TOKEN_LENGTH);
      }
    }

    public void disableToken(OAuthEntry entry) {
      Preconditions.checkNotNull(entry);
      ++entry.callbackTokenAttempts;
      if (!entry.callbackUrlSigned || entry.callbackTokenAttempts >= CALLBACK_TOKEN_ATTEMPTS) {
        entry.type = OAuthEntry.Type.DISABLED;
      }

      oauthEntries.put(entry.token, entry);
    }

    public void removeToken(OAuthEntry entry) {
      Preconditions.checkNotNull(entry);

      oauthEntries.remove(entry.token);
    }

    // Return the proper security token for a 2 legged oauth request that has been validated
    // for the given consumerKey. App specific checks like making sure the requested user has the
    // app installed should take place in this method
    public SecurityToken getSecurityTokenForConsumerRequest(String consumerKey, String userId) {
      String domain = "samplecontainer.com";
      String container = "default";

      return new OAuthSecurityToken(userId, null, consumerKey, domain, container,
          AuthenticationMode.OAUTH_CONSUMER_REQUEST.name());

    }
}
