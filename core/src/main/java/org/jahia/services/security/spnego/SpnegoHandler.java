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
/*
 * Copyright 2006 Taglab Limited
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License
 */
package org.jahia.services.security.spnego;

import org.apache.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.Oid;

import org.jahia.services.security.spnego.message.AbstractMessagePart;
import org.jahia.services.security.spnego.message.ApplicationConstructedObject;
import org.jahia.services.security.spnego.message.ContextFlags;
import org.jahia.services.security.spnego.message.MechTypeList;
import org.jahia.services.security.spnego.message.NegResult;
import org.jahia.services.security.spnego.message.NegTokenInit;
import org.jahia.services.security.spnego.message.NegTokenTarg;
import org.jahia.services.security.spnego.message.OctetString;

/**
 * Thin Spnego wrapping mechanism for <code>org.ietf.jgss</code> GSS-API.
 * <p>
 * Spnego is not supported by Java 5, but is in Java 6, however Spnego in itself
 * is very simple and is just a thin wrapper over GSS-API, and this class
 * provides support for Spnego in Java 5.
 * <p>
 * The implementation is a generic implementation, which means that it works
 * with more than Kerberos as the underlying GSS-API mechanism (however Kerberos
 * is the only one that Java 5 JAAS supports anyway).
 * <p>
 * This class needs two things to work:
 * <ol>
 * <li>System property <code>javax.security.auth.useSubjectCredsOnly</code>
 * set to <code>false</code>.
 * <li>System property <code>java.security.auth.login.config</code> to point
 * to a file with JAAS login configuration. The login configuration needs to
 * contain this:
 * 
 * <pre><code>
 *   com.sun.security.jgss.accept {
 *     com.sun.security.auth.module.Krb5LoginModule 
 *     required 
 *     storeKey=true 
 *     keyTab=&quot;/etc/krb5.keytab&quot;
 *     doNotPrompt=true 
 *     useKeyTab=true 
 *     realm=&quot;SALAD.TAGLAB.COM&quot; 
 *     principal=&quot;HTTP/banana.salad.taglab.com@SALAD.TAGLAB.COM&quot; 
 *     debug=true;
 *   };
 * </code></pre>
 * 
 * Where you would need to adjust <code>/etc/krb5.keytab</code> to point to a
 * keytab containing the principal pointed out by principal=. Please note that
 * the service principal must be named <code>HTTP/servername@REALM</code>.
 * Where servername MUST be the name you are typing into the URL-bar of the
 * browser AND is the reverse lookup name for the IP that corresponds to what
 * was typed in.
 * </ol>
 * <p>
 * XXX The class should be updated to use Java 6 Spnego once generally in use.
 * <p>
 * This implementation was made following: <a
 * href="http://msdn2.microsoft.com/en-us/library/ms995330.aspx">http://msdn2.microsoft.com/en-us/library/ms995330.aspx</a>
 * @author Martin Algesten
 */
public class SpnegoHandler {

  /**
   * Logger.
   */
  static final Logger LOG = Logger.getLogger("org.jahia.services.security.spnego.SpnegoHandler");

  /**
   * The parser to use.
   */
  private SpnegoParser parser = new SpnegoParser();

  /**
   * Tells if we are strict rfc4178 or not. Default is true.
   */
  private boolean rfc4178 = true;

  /**
   * Flag to only warn once.
   */
  private static boolean doWarnings = true;

  /**
   * State of the SpnegoHandler.
   */
  public enum State {
    
    /**
     * Process not started yet. 
     */
    UNINITIALIZED,
    
    /**
     * Context initialized. 
     */
    INITIALIZED,
    
    /**
     * Authentication in process. 
     */
    NEGOTIATING,
    
    /**
     * Successful.
     */
    ESTABLISHED,
    
    /**
     * Failed. 
     */
    FAILED,
    
    /**
     * @see setUnauthorized() .
     */
    UNAUTHORIZED
  }

  /**
   * The GSSContext, set in init().
   */
  private GSSContext context;

  /**
   * The current state of this object.
   */
  private State state = State.UNINITIALIZED;

  /**
   * Creates a new SpnegoHandler .
   * @throws GSSSpnegoException If we failed to establish the GSS-API context.
   */
  public SpnegoHandler() throws GSSSpnegoException {
    testSetup();
  }

  /**
   * Check is configuration of environment is correct.  
   */
  private void testSetup() {

    String tmp = System.getProperty("javax.security.auth.useSubjectCredsOnly");
    if (tmp == null) {
      if (doWarnings)
        LOG.warn("javax.security.auth.useSubjectCredsOnly is not set "
            + "which makes it default to true. SpnegoHandler will not work.");
      state = State.FAILED;
    }

    if ("true".equalsIgnoreCase(tmp)) {
      if (doWarnings)
        LOG.warn("javax.security.auth.useSubjectCredsOnly is set to true. "
            + "SpnegoHandler will not work.");
      state = State.FAILED;
    }

    tmp = System.getProperty("java.security.auth.login.config");

    if (tmp == null) {
      if (doWarnings)
        LOG.warn("java.security.auth.login.config is not set. "
            + "This property needs to point to a JAAS config file. "
            + "SpnegoHandler will not work.");
      state = State.FAILED;
    }

    doWarnings = false;

  }

  /**
   * Initialize GSSContext.
   * @param flags ContextFlags.
   * @throws GSSException if GSS error occurs.
   */
  private void init(ContextFlags flags) throws GSSException {

    // Oid spnegoOid = new Oid("1.3.6.1.5.5.2"); // java 6 has this.

    // testSetup() in constructor might indicate setup error.
    if (state == State.FAILED)
      return;

    GSSManager manager = GSSManager.getInstance();

    // principal is set in JAAS config, which means we can send in null
    // here to pick that up. Likewise setting the Oid to null defaults to
    // Kerberos (not Spnego).
    GSSCredential cred = manager.createCredential(null,
        GSSCredential.INDEFINITE_LIFETIME, (Oid) null,
        GSSCredential.ACCEPT_ONLY);

    context = manager.createContext(cred);

    if (flags != null && !rfc4178) {

      // rfc 4178 says we MUST ignore the flags.

      context.requestAnonymity(flags.isAnonFlag());
      context.requestConf(flags.isConfigFlag());
      context.requestCredDeleg(flags.isDelegFlag());
      context.requestInteg(flags.isIntegFlag());
      context.requestMutualAuth(flags.isMutualFlag());
      context.requestReplayDet(flags.isReplayFlag());
      context.requestSequenceDet(flags.isSequenceFlag());

    }

    state = State.INITIALIZED;

  }

  /**
   * Performs the actual authentication against the GSS-API. The authentication
   * might take several roundtrips to the server (with Kerberos this doesn't
   * happen) which means that depending on the result there might be more
   * roundtrips.
   * <p>
   * The method unwraps the nested GSS-API token from the Spnego token and
   * passes that into the {@link GSSContext} that was established in the
   * constructor.
   * @param token the spnego message.
   * @return sendback tokens.
   * @throws SpnegoException if an exception is encountered whilst doing the
   *             authentication.
   */
  public byte[] authenticate(byte[] token) throws SpnegoException {

    if (!isComplete())
      state = State.NEGOTIATING;

    if (token == null)
      return null;

    NegTokenInit negTokenInit;
    boolean isKerberosMicrosoft = false;

    try {
      ApplicationConstructedObject appObj = parser.parseInitToken(token);
      if (appObj == null) {
        // failed to parse...
        state = State.FAILED;
        return null;
      }
      negTokenInit = appObj.getNegTokenInit();
      if (negTokenInit.getMechTypes() != null) {
        MechTypeList list = negTokenInit.getMechTypes();
        if (list.getMechs().size() == 0) {
          LOG.info("No mech in mech list");
          state = State.FAILED;
          return null;
        } else {
          boolean hasKerberos = false;
          for (org.jahia.services.security.spnego.message.Oid oid : list.getMechs()) {
            if (oid.isKerberos() || oid.isKerberosMicrosoft()) {
              hasKerberos = true;
              isKerberosMicrosoft = oid.isKerberosMicrosoft();
              break;
            }
          }
          if (!hasKerberos) {
            LOG.info("Mech list does not contain kerberos!");
            state = State.FAILED;
            return null;
          }
        }
      }
      if (context == null) {
        init(negTokenInit.getContextFlags());
        if (context == null)
          return null; // SpnegoHandler not setup.
      }

      OctetString mechToken = negTokenInit.getMechToken();
      token = context.acceptSecContext(token, mechToken.getSourceStart(),
          mechToken.getSourceLength());
      if (context.isEstablished()) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Accepted Spnego negotiation. Authenticated user: "
              + context.getSrcName());
        }
        // in theory SPNEGO allows for several roundtrips between client
        // and server to establish the context. However in practice with
        // Kerberos, this doesn't happen.
        state = State.ESTABLISHED;
      } else {
        throw new SpnegoException("Several roundtrips to SpnegoHandler "
            + "is not currently supported");
      }
    } catch (GSSException gsse) {
      gsse.printStackTrace();
      state = State.FAILED;
      throw new GSSSpnegoException(gsse);
    }
    if (token == null)
      return null;
    token = constructResponse(isKerberosMicrosoft, token);
    return token;
  }

  /**
   * Constructs the response byte array from the give input.
   * @param isKerberosMicrosoft is Kerberos Microsoft.
   * @param gssApiToken input tokens.
   * @return response byte array.
   */
  protected byte[] constructResponse(boolean isKerberosMicrosoft,
      byte[] gssApiToken) {

    NegTokenTarg negTokenTarg = new NegTokenTarg();

    // Kerberos has no more round trips.
    negTokenTarg.setNegResult(new NegResult(NegResult.ACCEPT_COMPLETED));

    org.jahia.services.security.spnego.message.Oid oid =
      new org.jahia.services.security.spnego.message.Oid();
    if (isKerberosMicrosoft) {
      oid.setOid(org.jahia.services.security.spnego.message.Oid
          .OID_KERBEROS_MICROSOFT);
    } else {
      oid.setOid(org.jahia.services.security.spnego.message.Oid
          .OID_KERBEROS);
    }
    negTokenTarg.setSupportedMech(oid);

    OctetString responseToken = new OctetString();
    int[] tmp = new int[gssApiToken.length];
    AbstractMessagePart.arraycopy(gssApiToken, 0, tmp, 0, gssApiToken.length);
    responseToken.setData(tmp);
    negTokenTarg.setResponseToken(responseToken);

    tmp = negTokenTarg.toDer();
    byte[] token = new byte[tmp.length];
    AbstractMessagePart.arraycopy(tmp, 0, token, 0, tmp.length);

    return token;

  }

  /**
   * Tells if negotiation is complete or if more roundtrips to authenticate() is
   * expected.
   * @return true if authentication is complete, false otherwise.
   */
  public boolean isComplete() {
    return state == State.ESTABLISHED || state == State.FAILED || state == State.UNAUTHORIZED;
  }

  /**
   * @return the current state.
   */
  public State getState() {
    return state;
  }

  /**
   * Sets the state to UNAUTHORIZED. This might be interesting to a user of the
   * object, it has no effect on the handler itself.
   */
  public void setUnauthorized() {
    this.state = State.UNAUTHORIZED;
  }

  /**
   * @return the GSSContext.
   */
  public GSSContext getGSSContext() {
    return context;
  }

  /**
   * @return true if the credentials have been established, false otherwise..
   */
  public boolean isEstablished() {
    return state == State.ESTABLISHED;
  }

  /**
   * @return true if the negotiation has failed, false otherwise .
   */
  public boolean isFailed() {
    return state == State.FAILED;
  }

}
