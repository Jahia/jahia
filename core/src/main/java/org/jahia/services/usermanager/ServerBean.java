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
 package org.jahia.services.usermanager;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;

/**
 * <p>Title: Stores server configuration as described in xml router files.</p>
 * <p>Description: completely specify a server associated to a provider.</p>
 *
 * @author EP
 * @version 3.0
 */
public class ServerBean {

	// Map to store parameters for public context
	private Map publicParameters;
	// Map to store parameters for private context
	private Map privateParameters;
	
	// the priority of the server
	private int priority;
	// the max tries for reconnection
	private int maxReconn;
	// the provider associated to this server
	private String provider;

	private static final int DEFAULT_PRIORITY = 99;
	private static final int DEFAULT_RECONN = 1;
	
	/**
	 * Constructor.
	 * @param newPriority String, the priority read from xml file.
	 * @param newProvider String, the provider read from xml file.
	 * @param newMaxReconn String, the maximum reconnection tries read from xml file.
	 */
	public ServerBean (String newPriority, String newProvider, String newMaxReconn) {
		try {
			priority = Integer.parseInt (newPriority);
        	} catch (Exception e) {
            		priority = DEFAULT_PRIORITY;
        	}

		try {
			maxReconn = Integer.parseInt (newMaxReconn);
        	} catch (Exception e) {
            		maxReconn = DEFAULT_RECONN;
        	}
        	
		provider = newProvider;
		
		publicParameters = new HashMap(11);
		privateParameters = new HashMap(11);
	}
	
	/**
	 * Return this server priority.
	 * @return String.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Return this server provider.
	 * @return String.
	 */	
	public String getProvider() {
		return provider;
	}

	/**
	 * Return this server maximum reconnection tries.
	 * @return int.
	 */
	public int getMaxReconnection() {
		return maxReconn;
	}
		
	/**
	 * Set this server factory name.
	 * @param factoryName String, get from xml configuration file.
	 */
	public void setFactoryName(String factoryName) {
		if (factoryName != null && factoryName.length() > 0) {
			publicParameters.put (Context.INITIAL_CONTEXT_FACTORY, factoryName);
			privateParameters.put (Context.INITIAL_CONTEXT_FACTORY, factoryName);
		}
	}

	/**
	 * Set this server url.
	 * @param url String, get from xml configuration file.
	 */	
	public void setServerUrl(String url) {
		if (url != null && url.length() > 0) {
			publicParameters.put (Context.PROVIDER_URL, url);
			privateParameters.put (Context.PROVIDER_URL, url);
		}
	}
	
	/**
	 * Set this server factory userName.
	 * @param userName String, get from xml configuration file.
	 */
	public void setUserName(String userName) {
		if (userName != null && userName.length() > 0) 
			publicParameters.put (Context.SECURITY_PRINCIPAL, userName);
	}
	
	/**
	 * Set this server authentication mode.
	 * @param authenticationMode String, get from xml configuration file.
	 */
	public void setAuthenticationMode(String authenticationMode) {
		if (authenticationMode != null && authenticationMode.length() > 0) {
			publicParameters.put (Context.SECURITY_AUTHENTICATION, authenticationMode);
			privateParameters.put (Context.SECURITY_AUTHENTICATION, authenticationMode);
		}
	}
	
	/**
	 * Set this server password.
	 * @param password String, get from xml configuration file.
	 */
	public void setUserPassword(String password) {
		if (password != null && password.length() > 0) 
			publicParameters.put (Context.SECURITY_CREDENTIALS, password);
	}
	
	/**
	 * Set this server behavior.
	 * @param behavior String, get from xml configuration file.
	 */
	public void setReferralBehavior (String behavior) {
		if (behavior != null && behavior.length() > 0) {
			publicParameters.put (Context.REFERRAL, behavior);
			privateParameters.put (Context.REFERRAL, behavior);
		}
	}
	
	/**
	 * Return the parameters for public connection.
	 * @return Map.
	 */
	public Map getPublicConnectionParameters() {
		return publicParameters;
	}

	/**
	 * Return the parameters for public connection.
	 * @param userName String, the userName for a private connection.
	 * @param password String, the password for a private connection.
	 * @return Map.
	 */
	public Map getPrivateConnectionParameters(String userName, String password) {
		privateParameters.put (Context.SECURITY_CREDENTIALS, password);
		privateParameters.put (Context.SECURITY_PRINCIPAL, userName);
		
		return privateParameters;
	}
		
	/**
	 * Return the frinedly description of this object.
	 * @return String.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("ServerBean[");
		sb.append(maxReconn);
		sb.append("::");
		sb.append(provider);
		sb.append("::");
		sb.append(priority);
		sb.append("::");
		sb.append(publicParameters);
		sb.append("]");
		
		return sb.toString();
	}
}