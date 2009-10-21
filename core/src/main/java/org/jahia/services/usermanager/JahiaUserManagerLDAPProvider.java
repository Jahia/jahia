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
package org.jahia.services.usermanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaUserManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.utils.JahiaTools;

/**
 * An LDAP provider implementation for the management of users.
 *
 * @author Serge Huber
 * @version 2.0
 * @todo Khue suggested that we might want to use the site ID to use multiple
 * connection to different LDAP repository. This is a very interesting suggestion
 * but is not yet implemented.
 */
public class JahiaUserManagerLDAPProvider extends JahiaUserManagerProvider {
// ------------------------------ FIELDS ------------------------------

    public static final String PROVIDER_NAME = "ldap";

    // the LDAP User cache name.
    public static final String LDAP_USER_CACHE = "LDAPUsersCache";

    /** the overall provider User cache name. */
    public static final String PROVIDERS_USER_CACHE = "ProvidersUsersCache";

    /** Root user unique identification number */
    public static final int ROOT_USER_ID = 0;

    /** Guest user unique identification number */
    public static final int GUEST_USER_ID = 1;

    /** logging */
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaUserManagerLDAPProvider.class);

    private static String CONTEXT_FACTORY_PROP = "context.factory";
    private static String LDAP_URL_PROP = "url";
    private static String AUTHENTIFICATION_MODE_PROP =
        "authentification.mode";
    private static String PUBLIC_BIND_DN_PROP = "public.bind.dn";
    private static String PUBLIC_BIND_PASSWORD_PROP =
        "public.bind.password";

    private static String UID_SEARCH_ATTRIBUTE_PROP =
        "uid.search.attribute";
    private static String UID_SEARCH_NAME_PROP = "uid.search.name";
    private static String USERS_OBJECTCLASS_ATTRIBUTE =
        "search.objectclass";

    private static String LDAP_REFFERAL_PROP = "refferal";
    private static String SEARCH_COUNT_LIMIT_PROP =
        "search.countlimit";
    private static String SEARCH_WILDCARD_ATTRIBUTE_LIST =
        "search.wildcards.attributes";

    private static String LDAP_USERNAME_ATTRIBUTE =
        "username.attribute.map";
    private static String USE_CONNECTION_POOL = "users.ldap.connect.pool"; 
    
    private static JahiaUserManagerLDAPProvider instance;

    private Properties ldapProperties = null;

    private List<String> searchWildCardAttributeList = null;

    private Cache<String, Serializable> mUserCache;
    private Cache<String, JahiaUser> mProvidersUserCache;

    //(-PredragV-) private JahiaGroupManagerDBService    groupService = null;
    private CacheService cacheService = null;
    private JCRUserManagerProvider jcrUserManagerProvider;

// -------------------------- STATIC METHODS --------------------------

    /**
     * Create an new instance of the User Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the User Manager Service.
     */
    public static JahiaUserManagerLDAPProvider getInstance () {
        if (instance == null) {
            try {
                instance = new JahiaUserManagerLDAPProvider();
            } catch (JahiaException ex) {
                logger.error(
                    "Could not create an instance of the JahiaUserManagerLDAPProvider class");
            }
        }
        return instance;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Default constructor
     *
     * @throws JahiaException The user manager need some Jahia services to be
     *                        able to run correctly. If one of these services are not instanciated then a
     *                        JahiaException exception is thrown.
     */
    protected JahiaUserManagerLDAPProvider ()
        throws JahiaException {

    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setJcrUserManagerProvider(JCRUserManagerProvider jcrUserManagerProvider) {
        this.jcrUserManagerProvider = jcrUserManagerProvider;
    }

    public void setLdapProperties(Properties ldapProperties) {
        this.ldapProperties = new Properties();
        this.ldapProperties.putAll(ldapProperties);
    }

    // -------------------------- OTHER METHODS --------------------------

    public void start ()
        throws JahiaInitializationException {
        mUserCache = cacheService.createCacheInstance(LDAP_USER_CACHE);
        mProvidersUserCache = cacheService.createCacheInstance(PROVIDERS_USER_CACHE);

        if (!ldapProperties.containsKey(LDAP_USERNAME_ATTRIBUTE)) {
            ldapProperties.put(LDAP_USERNAME_ATTRIBUTE, ldapProperties.get(UID_SEARCH_ATTRIBUTE_PROP));
        }

        String wildCardAttributeStr = ldapProperties.getProperty(
            JahiaUserManagerLDAPProvider.SEARCH_WILDCARD_ATTRIBUTE_LIST);
        if (wildCardAttributeStr != null) {
            this.searchWildCardAttributeList = new ArrayList<String>();
            StringTokenizer wildCardTokens = new StringTokenizer(
                wildCardAttributeStr, ", ");
            while (wildCardTokens.hasMoreTokens()) {
                String curAttrName = wildCardTokens.nextToken().trim();
                this.searchWildCardAttributeList.add(curAttrName);
            }
        }

        logger.debug("Initialized and connected to public repository");
    }

    public void stop() {
    }

    /**
     * This is the method that creates a new user in the system, with all the
     * specified attributes.
     *
     * @param name       User login name.
     * @param password   User password
     * @param properties User additional parameters. If the user has no additional
     *                   attributes, give a NULL pointer to this parameter.
     *
     * @return a JahiaUser object containing an instance of the created user,
     *         in this case a instance of JahiaLDAPUser.
     */
    public synchronized JahiaUser createUser(String name,
                                             String password,
                                             Properties properties) {
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * This method removes a user from the system. All the user's attributes are
     * remove, and also all the related objects belonging to the user. On success,
     * true is returned and the user parameter is not longer valid. Return false
     * on any failure.
     *
     * @param user reference on the user to be deleted.
     *
     * @return Return true on success, or false on any failure.
     */
    public synchronized boolean deleteUser (JahiaUser user) {
        return false;
            /** @todo not yet supported since the LDAP is read-only. */
    }

    /**
     * Return the amount of users in the database.
     *
     * @return The amount of users.
     *
     * @throws JahiaException in case there's a problem retrieving the number
     *                        of users from the storage
     */
    public synchronized int getNbUsers () {
        return -1;
    }

    public String getUrl () {
        return ldapProperties.getProperty(LDAP_URL_PROP);
    }

    /**
     * This method return all users' keys in the system.
     *
     * @return Return a List of strings holding the user identification key .
     */
    public List<String> getUserList () {
        List<String> result = new ArrayList<String>();


        DirContext ctx = null;
        try {
            ctx = getPublicContext();
            List<SearchResult> answer = getUsers(ctx, new Properties(), ldapProperties.getProperty (UID_SEARCH_NAME_PROP), SearchControls.SUBTREE_SCOPE);
            for (SearchResult sr : answer) {
                JahiaUser curUser = ldapToJahiaUser(sr);
                if (curUser != null) {
                    result.add(curUser.getUserKey());
                }
            }
        } catch (SizeLimitExceededException slee) {
            // we just return the list as it is
            logger.debug(
                "Search generated more than configured maximum search limit, limiting to " +
                this.ldapProperties.getProperty(SEARCH_COUNT_LIMIT_PROP) +
                " first results...");
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
            result = new ArrayList<String>();
        } finally {
            invalidateCtx(ctx);
        }

        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * This method returns the list of all the user names registed into the system.
     *
     * @return Return a List of strings holding the user identification names.
     */
    public List<String> getUsernameList() {
        List<String> result = new ArrayList<String>();

        DirContext ctx = null;
        try {
            ctx = getPublicContext();
            List<SearchResult> answer = getUsers(ctx, new Properties(), ldapProperties.getProperty (UID_SEARCH_NAME_PROP), SearchControls.SUBTREE_SCOPE);
            for (SearchResult sr : answer) {
                JahiaUser curUser = ldapToJahiaUser(sr);
                if (curUser != null) {
                    result.add (curUser.getUsername ());
                }
            }
        } catch (SizeLimitExceededException slee) {
            // we just return the list as it is
            logger.debug ("Search generated more than configured maximum search limit, limiting to " +
                    this.ldapProperties.getProperty (SEARCH_COUNT_LIMIT_PROP) +
                    " first results...");
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
            result = new ArrayList<String>();
        } finally {
            invalidateCtx(ctx);
        }

        return result;
    }

    /**
     * Retrieves users from the LDAP public repository.
     *
     * @param ctx     the current context in which to search for the user
     * @param filters a set of name=value string that contain RFC 2254 format
     *                filters in the value, or null if we want to look in the full repository
     *
     * @param searchBase
     * @param scope
     * @return NamingEnumeration a naming Iterator of SearchResult objects
     *         that contains the LDAP user entries that correspond to the filter
     *
     * @throws NamingException
     */
    private List<SearchResult> getUsers (DirContext ctx, Properties filters, String searchBase, int scope)
        throws NamingException {
        if (ctx == null) {
            throw new NamingException("Context is null !");
        }

        StringBuffer filterString = new StringBuffer();

        if (filters == null) {
            filters = new Properties();
        }

        if (filters.containsKey("ldap.url")) {
            String url = filters.getProperty("ldap.url");
            try {
                StringTokenizer st = new StringTokenizer(url.substring(8), "?");
                String thisBase = st.nextToken();
                String thisScope = st.nextToken();
                String thisFilter = st.nextToken();
                int intScope;
                if ("one".equalsIgnoreCase(thisScope)) {
                    intScope = SearchControls.ONELEVEL_SCOPE;
                } else if ("base".equalsIgnoreCase(thisScope)) {
                    intScope = SearchControls.OBJECT_SCOPE;
                } else {
                    intScope = SearchControls.SUBTREE_SCOPE;
                }
                if (filters.containsKey("user.key")) {
                    thisFilter = "(&("+ldapProperties.getProperty (UID_SEARCH_ATTRIBUTE_PROP)+"="+filters.get("user.key")+")("+thisFilter+"))";
                }

                return getUsers(ctx, thisFilter, thisBase, intScope);
            } catch (Exception e) {
                logger.error("Cannot get users for url : "+url);
                throw new PartialResultException("Cannot get users for url : "+url);
            }
        } else {
            filterString.append("(&(objectClass=" + ldapProperties.getProperty(
                            USERS_OBJECTCLASS_ATTRIBUTE, "*") + ")");

            // let's translate Jahia properties to LDAP properties
            mapJahiaPropertiesToLDAP(filters);


            if (filters.size() > 1) {
                filterString.append("(|");
            }

            Iterator<?> filterKeys = filters.keySet().iterator();
            while (filterKeys.hasNext()) {
                String filterName = (String) filterKeys.next();
                String filterValue = filters.getProperty(filterName);
                // we do all the RFC 2254 replacement *except* the "*" character
                // since this is actually something we want to use.
                filterValue = JahiaTools.replacePattern(filterValue, "\\",
                        "\\5c");
                filterValue = JahiaTools.replacePattern(filterValue, "(",
                        "\\28");
                filterValue = JahiaTools.replacePattern(filterValue, ")",
                        "\\29");

                if ("*".equals(filterName)) {
                    // we must match the value for all the attributes
                    // declared in the property file.
                    if (this.searchWildCardAttributeList != null) {
                        if (this.searchWildCardAttributeList.size() > 1) {
                            filterString.append("(|");
                        }
                        Iterator<String> attributeEnum = this.
                                searchWildCardAttributeList.iterator();
                        while (attributeEnum.hasNext()) {
                            String curAttributeName = attributeEnum.next();
                            filterString.append("(");
                            filterString.append(curAttributeName);
                            filterString.append("=");
                            filterString.append(filterValue);
                            filterString.append(")");
                        }
                        if (this.searchWildCardAttributeList.size() > 1) {
                            filterString.append(")");
                        }
                    }
                } else {
                    filterString.append("(");
                    filterString.append(filterName);
                    filterString.append("=");
                    filterString.append(filterValue);
                    filterString.append(")");
                }
            }

            if (filters.size() > 1) {
                filterString.append(")");
            }

            filterString.append(")");

            return getUsers(ctx, filterString.toString(), searchBase, scope);
        }
    }


    /**
     * Maps Jahia user to LDAP properties using the definition
     * mapping in the user LDAP configuration properties file. This modifies
     * the userProps
     *
     * @param userProps
     */
    private void mapJahiaPropertiesToLDAP (Properties userProps) {
        for (Iterator<?> iterator = ldapProperties.keySet().iterator();
             iterator.hasNext(); ) {
            String key = (String)iterator.next();
            if (key.endsWith(".attribute.map")) {
                String jahiaProperty = key.substring(0, key.length() - 14);
                String curProperty = ldapProperties.getProperty(key);
                if (userProps.getProperty(jahiaProperty) != null) {
                    userProps.setProperty(curProperty,
                                          (String) userProps.remove(jahiaProperty));
                }
            }
        }
    }

    /**
     * Returns the internal public context variable. The point of this is to
     * keep this connection open as long as possible, in order to reuser the
     * connection.
     *
     * @param forceRefresh
     *
     * @return DirContext the current public context.
     *
     * @throws NamingException
     */
    public DirContext getPublicContext() throws NamingException {
        DirContext publicCtx = null;
        try {
            publicCtx = connectToPublicDir();
        } catch (NamingException ne) {
            logger.warn("JNDI warning", ne);
        }
        return publicCtx;
    }


    private DirContext connectToPublicDir ()
            throws NamingException {
        // Identify service provider to use
        logger.debug ("Attempting connection to LDAP repository on " +
                ldapProperties.getProperty (LDAP_URL_PROP) + "...");

        Hashtable<String, String> publicEnv = new Hashtable<String, String> (11);
        publicEnv.put (Context.INITIAL_CONTEXT_FACTORY,
                ldapProperties.getProperty (CONTEXT_FACTORY_PROP));
        publicEnv.put (Context.PROVIDER_URL,
                ldapProperties.getProperty (LDAP_URL_PROP));
        publicEnv.put (Context.SECURITY_AUTHENTICATION,
                ldapProperties.getProperty (AUTHENTIFICATION_MODE_PROP));
        publicEnv.put (Context.SECURITY_PRINCIPAL,
                ldapProperties.getProperty (PUBLIC_BIND_DN_PROP));
        publicEnv.put (Context.REFERRAL,
                       ldapProperties.getProperty (LDAP_REFFERAL_PROP, "ignore"));
        // Enable connection pooling
        publicEnv.put("com.sun.jndi.ldap.connect.pool", ldapProperties
                .getProperty(USE_CONNECTION_POOL, "true"));        
        if (ldapProperties.getProperty (PUBLIC_BIND_PASSWORD_PROP) != null) {
            logger.debug ("Using authentification mode to connect to public dir...");
            publicEnv.put (Context.SECURITY_CREDENTIALS,
                    ldapProperties.getProperty (PUBLIC_BIND_PASSWORD_PROP));
        }

        // Create the initial directory context
        return new InitialDirContext (publicEnv);
    }

    /**
     * Translates LDAP attributes to a JahiaUser properties set. Multi-valued
     * attribute values are converted to Strings containing LINEFEED (\n)
     * characters. This way it is quite simple to use String Tokenizers to
     * extract multiple values. Note that if a value ALREADY contains a line
     * feed characters this will cause unexpected behavior.
     *
     * @param sr      result of a search on a LDAP directory context
     * @return JahiaLDAPUser a user initialized with the properties loaded
     *         from the LDAP database, or null if no userKey could be determined for
     *         the user.
     */
    private JahiaLDAPUser ldapToJahiaUser (SearchResult sr) {
        Attributes attrs = sr.getAttributes();
        String dn = sr.getName() + "," + ldapProperties.getProperty (UID_SEARCH_NAME_PROP);
        return ldapToJahiaUser(attrs, dn);
    }

    private List<SearchResult> getUsers (DirContext ctx, String filterString, String searchBase, int scope)
            throws NamingException {
        // Search for objects that have those matching attributes
        SearchControls searchCtl = new SearchControls ();
        searchCtl.setSearchScope (scope);
        List<SearchResult> answerList = new ArrayList<SearchResult>();
        int countLimit = Integer.parseInt (
                ldapProperties.getProperty (
                        JahiaUserManagerLDAPProvider.SEARCH_COUNT_LIMIT_PROP));
        searchCtl.setCountLimit (countLimit);
        logger.debug ("Using filter string [" + filterString.toString () + "]...");
        try {
            NamingEnumeration<SearchResult> enumeration = ctx.search (
                    searchBase,
                    filterString.toString (),
                    searchCtl);
            while (enumeration.hasMore()) {
                answerList.add(enumeration.next());
            } 
        } catch (javax.naming.NoInitialContextException nice) {
            logger.warn("Reconnection required", nice);
        } catch (javax.naming.CannotProceedException cpe) {
            logger.warn("Reconnection required", cpe);
        } catch (javax.naming.ServiceUnavailableException sue) {
            logger.warn("Reconnection required", sue);
        } catch (javax.naming.TimeLimitExceededException tlee) {
            logger.warn("Reconnection required", tlee);
        } catch (javax.naming.CommunicationException ce) {
            logger.warn("Reconnection required", ce);
        } catch (SizeLimitExceededException e) {
            logger.warn(
                    "User search generated more than configured maximum search limit, limiting to " +
                    this.ldapProperties.getProperty(SEARCH_COUNT_LIMIT_PROP) +
                    " first results...");
        }
        return answerList;
    }

    /**
     * Performs a login of the specified user.
     *
     * @param userKey      the user identifier defined in this service properties
     * @param userPassword the password of the user
     *
     * @return String a string that contains the common name of this user
     *         whithin the repository.
     */
    public synchronized boolean login (String userKey, String userPassword) {
        String dn = null;
        String userFinalKey = userKey;

        if ("".equals (userPassword)) {
            logger.debug ("Empty passwords are not authorized for LDAP login ! Failing user " +
                    userKey + " login request.");
            return false;
        }

        userFinalKey = removeKeyPrefix(userKey);
        DirContext privateCtx = null;

        try {
            dn = ((JahiaLDAPUser) lookupUserByKey(userFinalKey)).getDN();

            privateCtx = connectToPrivateDir (dn, userPassword);

            if (privateCtx == null) {
                dn = null;
            }
        } catch (javax.naming.CommunicationException ce) {
            logger.warn ("CommunicationException", ce);
            logger.debug ("Invalidading connection to public LDAP context...");
            dn = null;
        } catch (NamingException ne) {
            logger.debug("Login refused, server message : " + ne.getMessage());
            dn = null;
        } finally {
            invalidateCtx (privateCtx);            
        }
        return (dn != null);
    }

    private DirContext connectToPrivateDir (String dn, String personPassword)
            throws NamingException {

        // Identify service provider to use
        Hashtable<String, String> privateEnv = new Hashtable<String, String> (11);
        privateEnv.put (Context.INITIAL_CONTEXT_FACTORY,
                ldapProperties.getProperty (CONTEXT_FACTORY_PROP));
        privateEnv.put (Context.PROVIDER_URL,
                ldapProperties.getProperty (LDAP_URL_PROP));
        privateEnv.put (Context.SECURITY_AUTHENTICATION,
                ldapProperties.getProperty (AUTHENTIFICATION_MODE_PROP));
        privateEnv.put (Context.SECURITY_PRINCIPAL, dn );
        privateEnv.put (Context.SECURITY_CREDENTIALS,
                personPassword);

        // Create the initial directory context
        return new InitialDirContext (privateEnv);
    }

    private void invalidateCtx (DirContext ctx) {
        if (ctx == null) {
            logger.debug ("Context passed is null, ignoring it...");
            return;
        }
        try {
            ctx.close ();
            ctx = null;
        } catch (Exception e) {
            logger.warn (e);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param userKey User's identification name.
     *
     * @return a reference on a new created jahiaUser object.
     */
    public JahiaUser lookupUserByKey(String userKey, String searchAttributeName) {
        if (!LicenseActionChecker.isAuthorizedByLicense("org.jahia.services.usermanager.JahiaUserManagerLDAPProvider", 0)) {
            return null;
        }
        if (!userKey.startsWith("{"+getKey()+"}")) {
            return null;
        }

        /* 2004-16-06 : update by EP
        new cache to use : cross providers only based upon names... */
        JahiaUser user = mProvidersUserCache.get ("k"+userKey);

        // 2004-23-07 : use wrappers
        if (user == null) {
            // then look into the local cache
            JahiaUserWrapper juw = (JahiaUserWrapper) mUserCache.get ("k"+userKey);

            if (juw == null) {
                //logger.debug(" user with key=" + userKey + " is not found in cache");
                user = lookupUserInLDAP (removeKeyPrefix(userKey), searchAttributeName);

                if (user != null) {
                    /* 2004-16-06 : update by EP
                    new cache to populate : cross providers only based upon names... */
                    mProvidersUserCache.put("k"+userKey, user);

                    // name storage for speed
                    mUserCache.put("n"+user.getUsername(), new JahiaUserWrapper(user));
                    mProvidersUserCache.put("n"+user.getUsername(), user);
                }
                // use wrappers in local cache
                   mUserCache.put ("k"+userKey, new JahiaUserWrapper(user));
            } else {
                user = juw.getUser();
            }
        }
           return user;
    }

    /**
     * This method is the rigth way to rtrieve a user from the information stored in the member attribute in groups.
     * For example, if the member attribute of a group contains the distinguishedName of the memebers,
     * you have to use lookupUserInLDAP (userDn, "distinguishedName").
     * If your properties file are correctly defined, you could use the value of the
     * JahiaGroupManagerLDAPProvider.SEARCH_USER_ATTRIBUTE_NAME for searchAttributeName.
     *
     * This method is only called by lookupUser (String userKey, String searchAttributeName)
     * which was only called by JahiaGroupManagerLDAPProvider.getGroupMembers()
     */
    private JahiaLDAPUser lookupUserInLDAP (String userKey, String searchAttributeName) {
        JahiaLDAPUser user = null;

        DirContext ctx = null;
        try {
            ctx = getPublicContext();
            SearchResult sr = getPublicUser (ctx, searchAttributeName, userKey);
            if (sr == null) {
                return null;
            }
            user = ldapToJahiaUser (sr);
        } catch (SizeLimitExceededException slee) {
            logger.debug ("Search generated more than configured maximum search limit, limiting to " +
                    this.ldapProperties.getProperty (SEARCH_COUNT_LIMIT_PROP) +
                    " first results...");
            user = null;
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
            user = null;
        } finally {
            invalidateCtx(ctx);
        }
        return user;
    }

    public JahiaLDAPUser lookupUserFromDN(String dn) {
        logger.debug ("Lookup user from dn " + dn);
        JahiaLDAPUser user = null;
        if (mUserCache.containsKey("d"+dn)) {
            JahiaLDAPUser result = (JahiaLDAPUser) mUserCache.get("d"+dn);
            if (result != null) {
                return result;
            }
        }
        DirContext ctx = null;
        try {
            ctx = getPublicContext();
            Attributes attributes = getUser (ctx, dn);
            user = ldapToJahiaUser (attributes, dn);
            if (user != null) {
                mUserCache.put("d"+dn, user);
                mUserCache.put("k"+user.getUserKey(), new JahiaUserWrapper(user));
                mUserCache.put("n"+user.getUsername(), new JahiaUserWrapper(user));
                mProvidersUserCache.put("n"+user.getUsername(), user);
            }
        } catch (NameNotFoundException nnfe) {
            user = null;
            mUserCache.put("d"+dn, null);
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
            user = null;
        } finally {
            invalidateCtx(ctx);
        }
        return user;
    }

    private Attributes getUser (DirContext ctx, String dn)
            throws NamingException {
        Attributes attributes = null;
        try {
            if (dn != null && dn.indexOf('/') != -1) {
                dn = JahiaTools.replacePattern(dn, "/", "\\/");
            }
            attributes = ctx.getAttributes(dn);
        } catch (javax.naming.NoInitialContextException nice) {
            logger.debug("Reconnection required", nice);
        } catch (javax.naming.CannotProceedException cpe) {
            logger.debug("Reconnection required", cpe);
        } catch (javax.naming.ServiceUnavailableException sue) {
            logger.debug("Reconnection required", sue);
        } catch (javax.naming.TimeLimitExceededException tlee) {
            logger.debug("Reconnection required", tlee);
        } catch (javax.naming.CommunicationException ce) {
            logger.debug("Reconnection required", ce);
        }
        return attributes;
    }

    private JahiaLDAPUser ldapToJahiaUser(Attributes attrs, String dn) {
        JahiaLDAPUser user = null;
        UserProperties userProps = new UserProperties ();
        String usingUserKey = null;

        Enumeration<?> attrsEnum = attrs.getAll();
        while (attrsEnum.hasMoreElements()) {
            Attribute curAttr = (Attribute) attrsEnum.nextElement();
            String attrName = curAttr.getID();
            StringBuffer attrValueBuf = new StringBuffer();
            try {
                Enumeration<?> curAttrValueEnum = curAttr.getAll();
                while (curAttrValueEnum.hasMoreElements()) {
                    Object curAttrValueObj = curAttrValueEnum.nextElement();
                    if ( (curAttrValueObj instanceof String)) {
                        attrValueBuf.append( (String) curAttrValueObj);
                    } else {
                        logger.debug("Converting attribute <" + attrName +
                                     "> from class " +
                                     curAttrValueObj.getClass().toString() +
                                     " to String...");
                        /** @todo FIXME : for the moment we convert everything to String */
                        attrValueBuf.append(curAttrValueObj);
                    }
                    attrValueBuf.append('\n');
                }
            } catch (NamingException ne) {
                logger.warn ("JNDI warning",ne);
                attrValueBuf = new StringBuffer ();
            }
            String attrValue = attrValueBuf.toString();
            if (attrValue.endsWith("\n")) {
                attrValue = attrValue.substring(0, attrValue.length() - 1);
            }
            if ( (attrName != null) && (attrValue != null)) {
                if (usingUserKey == null) {
                    if (attrName.equalsIgnoreCase(
                        ldapProperties.getProperty(UID_SEARCH_ATTRIBUTE_PROP))) {
                        int multiValueMarkerPos = attrValue.indexOf('\n');
                        if (multiValueMarkerPos != -1) {
                            // we have detected a multi-valued UID_SEARCH_ATTRIBUTE_PROP, we will take only
                            // the first value for the user key.
                            usingUserKey = attrValue.substring(0, multiValueMarkerPos);   
                        } else {
                            usingUserKey = attrValue;
                        }
                    }
                }
                // mark user property as read-only as it is coming from LDAP
                UserProperty curUserProperty = new UserProperty(attrName, attrValue, true);
                userProps.setUserProperty(attrName, curUserProperty);
            }
        }
        if (usingUserKey != null) {
            mapLDAPToJahiaProperties(userProps);
            // FIXME : Quick hack for merging Jahia DB user properties with LDAP user
//            mapDBToJahiaProperties (userProps, JahiaLDAPUser.USERKEY_LDAP_PREFIX + usingUserKey);
            /* EP : changes the code to handle the name of the user as defined in properties file.
            The name of the user is the value of the LDAP_USERNAME_ATTRIBUTE properties */
            String name = usingUserKey;

            if (ldapProperties.getProperty(LDAP_USERNAME_ATTRIBUTE) != null
                    && ldapProperties.getProperty(LDAP_USERNAME_ATTRIBUTE).length() > 0) {
                name = userProps
                        .getProperty
                        (ldapProperties
                        .getProperty(
                        LDAP_USERNAME_ATTRIBUTE));
            }

               user = new JahiaLDAPUser (0,
                        name,
                        "",
                        usingUserKey,
                        0,
                        userProps,
                        dn, this);
        } else {
            logger.debug ("Ignoring entry " + dn +
                    " because it has no valid " +
                    ldapProperties.getProperty (UID_SEARCH_ATTRIBUTE_PROP) +
                    " attribute to be mapped onto user key...");
        }

        return user;
    }

    /**
     * Map LDAP properties to Jahia user properties, such as first name,
     * last name, etc...
     * This method modifies the userProps object passed on parameters to add
     * the new properties.
     *
     * @param userProps User properties to check for mappings. Basically what
     *                  we do is copy LDAP properties to standard Jahia properties. This is
     *                  defined in the user ldap properties file. Warning this object is modified
     *                  by this method !
     *
     * @todo FIXME : if properties exist in LDAP that have the same name as
     * Jahia properties these will be erased. We should probably look into
     * making the properties names more unique such as org.jahia.propertyname
     */
    private void mapLDAPToJahiaProperties (UserProperties userProps) {
        // copy attribute to standard Jahia properties if they exist both in
        // the mapping and in the repository
        for (Iterator<?> iterator = ldapProperties.keySet().iterator();
             iterator.hasNext(); ) {
            String key = (String)iterator.next();
            if (key.endsWith(".attribute.map")) {
                String jahiaProperty = key.substring(0, key.length() - 14);
                String curProperty = ldapProperties.getProperty(key);
                if (userProps.getUserProperty(curProperty) != null) {
                    UserProperty sourceProp = userProps.getUserProperty(curProperty);
                    UserProperty targetProp = new UserProperty(jahiaProperty, sourceProp.getValue(), sourceProp.isReadOnly());
                    userProps.setUserProperty(jahiaProperty,
                                              targetProp);
                } else {
                    // for properties that don't have a value in LDAP, we still
                    // create a read-only Jahia property, in case it is added
                    // later in LDAP. We don't want to authorize edition of an
                    // LDAP-mapped property.
                    UserProperty targetProp = new UserProperty(jahiaProperty, "", true);
                    userProps.setUserProperty(jahiaProperty,
                                              targetProp);
                }
            }
        }
    }

    /**
     * Retrieves properties from internal jahia DB
     *
     * @param userProps    the user properties to set
     * @param usingUserKey the user whose the properties has to be extracted.
     */
    public void mapDBToJahiaProperties (UserProperties userProps,
                                         String usingUserKey) {
        JahiaUser jahiaUser = jcrUserManagerProvider.lookupExternalUser(usingUserKey);
        if(jahiaUser!=null) {
            userProps.putAll(jahiaUser.getUserProperties());
        }
    }

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     *
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criterias, or an empty one if an error has occured. Note this will
     *         only return the configured limit of users at maxium. Check out the
     *         users.ldap.properties file to change the limit.
     */
    public Set<JahiaUser> searchUsers (int siteID, Properties searchCriterias) {
        Set<JahiaUser> result = new HashSet<JahiaUser>();
        // first let's lookup the user by the properties in Jahia's DB
        Set<String> userKeys = searchLDAPUsersByDBProperties(searchCriterias);
        // now that we have the keys, let's load all the users.
        Iterator<String> userKeyEnum = userKeys.iterator();
        while (userKeyEnum.hasNext()) {
            String curUserKey = userKeyEnum.next();
            JahiaUser user = lookupUserByKey(curUserKey);
            result.add(user);
        }

        // now let's lookup in LDAP properties.
        DirContext ctx = null;
        try {
            ctx = getPublicContext();
            List<SearchResult> ldapUsers = getUsers(ctx,
                searchCriterias, ldapProperties.getProperty (UID_SEARCH_NAME_PROP), SearchControls.SUBTREE_SCOPE);
            for (SearchResult sr : ldapUsers) {
                JahiaLDAPUser user = ldapToJahiaUser(sr);
                if (user != null) {
                    result.add(user);
                }
            }
        } catch (PartialResultException pre) {
            logger.warn (pre);
        } catch (SizeLimitExceededException slee) {
            // logger.error(slee);
            // we just return the list as it is
            logger.debug(
                "Search generated more than configured maximum search limit, limiting to " +
                this.ldapProperties.getProperty(SEARCH_COUNT_LIMIT_PROP) +
                " first results...");
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
            result = new HashSet<JahiaUser>();
        } finally {
            invalidateCtx(ctx);
        }
        return result;
    }

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     *
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criterias
     *
     * @todo this code could be cleaner if username was a real user property
     * but as it isn't we have to do a lot of custom handling.
     */
    private synchronized Set<String> searchLDAPUsersByDBProperties(Properties searchCriterias) {
        Set<String> userKeys = new HashSet<String>();

        if (searchCriterias == null) {
            searchCriterias = new Properties();
            searchCriterias.setProperty("*", "*");
        }

        JahiaUserManager groupManager = (JahiaUserManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaUserManager.class.getName());
        Iterator<?> criteriaNames = searchCriterias.keySet().iterator();
        List<String> criteriaValueList = new ArrayList<String>(searchCriterias.size());
        List<String> criteriaNameList = new ArrayList<String>(searchCriterias.size());
        while (criteriaNames.hasNext()) {
            String curCriteriaName = (String) criteriaNames.
                    next();
            String curCriteriaValue = makeLIKEString(
                    searchCriterias.getProperty(curCriteriaName));
            criteriaValueList.add(curCriteriaValue);
            criteriaNameList.add(curCriteriaName);
        }
        groupManager.searchUserName(criteriaNameList, criteriaValueList, userKeys, PROVIDER_NAME);

        return userKeys;
    }

    /**
     * Transforms a search with "*" characters into a valid LIKE statement
     * with "%" characters. Also escapes the string to remove all "'" and
     * other chars that might disturb the request construct.
     *
     * @param input the original String
     *
     * @return String a resulting string that has
     */
    private String makeLIKEString (String input) {
        String result = JahiaTools.replacePattern(input, "*", "%");
        result = JahiaTools.replacePattern(result, "'", "\\'");
        result = JahiaTools.replacePattern(result, "\"", "\\\"");
        result = JahiaTools.replacePattern(result, "_", "\\_");
        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param userKey User's identification name.
     *
     * @return a reference on a new created jahiaUser object.
     */
    public JahiaUser lookupUserByKey(String userKey) {
        if (!LicenseActionChecker.isAuthorizedByLicense("org.jahia.services.usermanager.JahiaUserManagerLDAPProvider", 0)) {
            return null;
        }

        /* 2004-16-06 : update by EP
        new cache to use : cross providers only based upon names... */
        JahiaUser user = mProvidersUserCache.get ("k"+userKey);

        // 2004-23-07 : use wrappers
        if (user == null) {
            // then look into the local cache
            JahiaUserWrapper juw = (JahiaUserWrapper) mUserCache.get ("k"+userKey);

            if (juw == null) {
                //logger.debug(" user with key=" + userKey + " is not found in cache");
                user = lookupUserInLDAP (removeKeyPrefix(userKey));

                if (user != null) {
                    /* 2004-16-06 : update by EP
                    new cache to populate : cross providers only based upon names... */
                    mProvidersUserCache.put("k"+userKey, user);

                    // name storage for speed
                    mUserCache.put("n"+user.getUsername(), new JahiaUserWrapper(user));
                    mProvidersUserCache.put("n"+user.getUsername(), user);
                }
                // use wrappers in local cache
                   mUserCache.put ("k"+userKey, new JahiaUserWrapper(user));
            } else {
                user = juw.getUser();
            }
        }

        return user;
    }

    private JahiaLDAPUser lookupUserInLDAP (String userKey) {
        JahiaLDAPUser user = null;

        DirContext ctx = null;
        try {
            ctx = getPublicContext();
            SearchResult sr = getPublicUser (ctx, ldapProperties.getProperty (UID_SEARCH_ATTRIBUTE_PROP), userKey);
            if (sr == null) {
                return null;
            }
            user = ldapToJahiaUser (sr);
        } catch (SizeLimitExceededException slee) {
            logger.debug ("Search generated more than configured maximum search limit, limiting to " +
                    this.ldapProperties.getProperty (SEARCH_COUNT_LIMIT_PROP) +
                    " first results...");
            user = null;
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
            user = null;
        } finally {
            invalidateCtx(ctx);
        }
        return user;
    }

    /**
     * Retrieves a user from the LDAP public repository.
     *
     * @param ctx the current context in which to search for the user
     * @param prop
     * @param val the unique identifier for the user
     *
     * @return a SearchResult object, which is the *first* result matching the
     *         uid
     *
     * @throws NamingException
     */
    private SearchResult getPublicUser (DirContext ctx, String prop, String val)
        throws NamingException {
        Properties filters = new Properties();

        filters.setProperty(prop, val);
        List<SearchResult> answer = getUsers(ctx, filters, ldapProperties.getProperty (UID_SEARCH_NAME_PROP), SearchControls.SUBTREE_SCOPE);
        SearchResult sr = null;
        if (!answer.isEmpty()) {
            // we only take the first value if there are multiple answers, which
            // should normally NOT happend if the uid is unique !!
            sr = answer.get(0);
            if (answer.size() > 1) {                // there is at least a second result.
                logger.debug(
                    "Warning : multiple users with same UID in LDAP repository.");
            }
        }
        return sr;
    }

    private String removeKeyPrefix (String userKey) {
        if (userKey.startsWith("{"+getKey()+"}")) {
            return userKey.substring(getKey().length()+2);
        } else {
            return userKey;
        }
    }

    public void updateCache(JahiaUser jahiaUser) {
        mUserCache.put("k"+jahiaUser.getUserKey(), new JahiaUserWrapper(jahiaUser));
        mProvidersUserCache.put("k"+jahiaUser.getUserKey(), jahiaUser);
        mUserCache.put("n"+jahiaUser.getUsername(), new JahiaUserWrapper(jahiaUser));
        mProvidersUserCache.put("n"+jahiaUser.getUsername(), jahiaUser);
    }

    //--------------------------------------------------------------------------
    /**
     * This function checks into the system if the name has already been
     * assigned to another user.
     *
     * @param name   User login name.
     *
     * @return Return true if the specified name has not been assigned yet,
     *         return false on any failure.
     */
    public boolean userExists(String name) {
        // try to avoid a NullPointerException
        if (name == null) {
            return false;
        }

        // name should not be empty.
        if (name.length() == 0) {
            return false;
        }

        return (lookupUser(name) != null);
    }

    // @author  NK
    /**
     * Load all the user data and attributes. On success a reference on the user
     * is returned, otherwise NULL is returned.
     *
     * @param name   User's identification name.
     *
     * @return Return a reference on a new created jahiaUser object.
     */
    public JahiaUser lookupUser(String name) {
        if (!LicenseActionChecker.isAuthorizedByLicense("org.jahia.services.usermanager.JahiaUserManagerLDAPProvider", 0)) {
            return null;
        }

        JahiaUser user = lookupUserByKey("{"+getKey()+"}"+name);
        if (user != null) {
            // user.setSiteID (siteID);
            mProvidersUserCache.put("n"+user.getUsername(), user);
        }

        return user;
    }
}
