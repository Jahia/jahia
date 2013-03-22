package org.jahia.modules.serversettings.flow;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.modules.serversettings.users.management.CsvFile;
import org.jahia.modules.serversettings.users.management.SearchCriteria;
import org.jahia.modules.serversettings.users.management.UserProperties;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.*;
import org.jahia.taglibs.user.User;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.security.Principal;
import java.util.*;

/**
 * @author rincevent
 */
public class UsersFlowHandler implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(UsersFlowHandler.class);
    private static final long serialVersionUID = -7240178997123886031L;
    
    public static UserProperties populateUser(String userKey, UserProperties propertiesToPopulate) {
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userKey);
        if (propertiesToPopulate == null) {
            propertiesToPopulate = new UserProperties();
        }
        propertiesToPopulate.setFirstName(jahiaUser.getProperty("j:firstName"));
        propertiesToPopulate.setLastName(jahiaUser.getProperty("j:lastName"));
        propertiesToPopulate.setUsername(jahiaUser.getUsername());
        propertiesToPopulate.setUserKey(jahiaUser.getUserKey());
        propertiesToPopulate.setEmail(jahiaUser.getProperty("j:email"));
        propertiesToPopulate.setOrganization(jahiaUser.getProperty("j:organization"));
        propertiesToPopulate.setEmailNotificationsDisabled(Boolean.valueOf(jahiaUser
                .getProperty("emailNotificationsDisabled")));
        propertiesToPopulate.setPreferredLanguage(UserPreferencesHelper.getPreferredLocale(jahiaUser));
        propertiesToPopulate.setAccountLocked(Boolean.valueOf(jahiaUser.getProperty("j:accountLocked")));
        propertiesToPopulate.setDisplayName(PrincipalViewHelper.getDisplayName(jahiaUser,
                LocaleContextHolder.getLocale()));
        propertiesToPopulate.setLocalPath(jahiaUser.getLocalPath());
        propertiesToPopulate.setGroups(new LinkedList<JahiaGroup>(User.getUserMembership(jahiaUser.getUsername()).values()));
        return propertiesToPopulate;
    }
    
    private transient JahiaPasswordPolicyService pwdPolicyService;

    private transient JahiaUserManagerService userManagerService;

    public boolean addUser(UserProperties userProperties, MessageContext context) {
        logger.info("Adding user");
        JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().createUser(
                userProperties.getUsername(), userProperties.getPassword(), transformUserProperties(userProperties));
        if (user != null) {
            context.addMessage(new MessageBuilder().info().defaultText(Messages.get("resources.JahiaServerSettings",
                    "label.user.create.successful", LocaleContextHolder.getLocale())).build());
            return true;
        } else {
            context.addMessage(new MessageBuilder().error().defaultText(Messages.get("resources.JahiaServerSettings",
                    "label.user.create.unsuccessful", LocaleContextHolder.getLocale())).build());
            return false;
        }
    }

    private Properties buildProperties(List<String> headerElementList, List<String> lineElementList) {
        Properties result = new Properties();
        for (int i = 0; i < headerElementList.size(); i++) {
            String currentHeader = headerElementList.get(i);
            String currentValue = lineElementList.get(i);
            if (!"j:nodename".equals(currentHeader) && !"j:password".equals(currentHeader)) {
                result.setProperty(currentHeader.trim(), currentValue);
            }
        }
        return result;
    }

    public boolean bulkAddUser(CsvFile csvFile, MessageContext context) {
        logger.info("Bulk adding users");
        long timer = 0;
        boolean hasErrors = false;
        try {
            timer = System.currentTimeMillis();
            CSVReader csvReader = new CSVReader(new InputStreamReader(csvFile.getCsvFile().getInputStream(), "UTF-8"),
                    csvFile.getCsvSeparator().charAt(0));
            // the first line contains the column names;
            String[] headerElements = csvReader.readNext();
            List<String> headerElementList = Arrays.asList(headerElements);
            int userNamePos = headerElementList.indexOf("j:nodename");
            int passwordPos = headerElementList.indexOf("j:password");
            if ((userNamePos < 0) || (passwordPos < 0)) {
                context.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaServerSettings",
                        "label.users.bulk.errors.missing.mandatory", LocaleContextHolder.getLocale(),"j:nodename","j:password")).build());
                return false;
            }
            String[] lineElements = null;

            while ((lineElements = csvReader.readNext()) != null) {
                List<String> lineElementList = Arrays.asList(lineElements);
                Properties properties = buildProperties(headerElementList, lineElementList);
                String userName = lineElementList.get(userNamePos);
                String password = lineElementList.get(passwordPos);
                if (userManagerService.isUsernameSyntaxCorrect(userName)) {
                    PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnUserCreate(userName, password);
                    if (evalResult.isSuccess()) {
                        JahiaUser jahiaUser = userManagerService.createUser(userName, password, properties);
                        if (jahiaUser != null) {
                            context.addMessage(new MessageBuilder().info().defaultText(Messages.getWithArgs("resources.JahiaServerSettings",
                                    "label.users.bulk.user.creation.successful", LocaleContextHolder.getLocale(),userName)).build());
                        } else {
                            context.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaServerSettings",
                                    "label.users.bulk.errors.user.creation.failed", LocaleContextHolder.getLocale(),userName)).build());
                            hasErrors = true;
                        }
                    } else {
                        StringBuilder result = new StringBuilder("<ul>");
                        for (String msg : evalResult.getTextMessages()) {
                            result.append("<li>").append(msg).append("</li>");
                        }
                        result.append("</ul>");
                        context.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaServerSettings",
                                "label.users.bulk.errors.user.skipped.password", LocaleContextHolder.getLocale(),userName,result.toString())).build());
                        hasErrors = true;
                    }
                } else {
                    context.addMessage(new MessageBuilder().error().defaultText(Messages.getWithArgs("resources.JahiaServerSettings",
                            "label.users.bulk.errors.user.skipped", LocaleContextHolder.getLocale(),userName)).build());
                    hasErrors=true;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }


        logger.info("Batch user create took " + (System.currentTimeMillis() - timer) + " ms");
        return !hasErrors;
    }

    public List<? extends JahiaUserManagerProvider> getProvidersList() {
        return userManagerService.getProviderList();
    }

    public Set<Principal> init() {
        return PrincipalViewHelper.getSearchResult(null, null, null, null, null);
    }

    public SearchCriteria initCriteria() {
        return new SearchCriteria();
    }

    public CsvFile initCSVFile() {
        CsvFile csvFile = new CsvFile();
        csvFile.setCsvSeparator(",");
        return csvFile;
    }

    public UserProperties initUser() {
        return new UserProperties();
    }
    
    public UserProperties populateUser(String[] selectedUsers) {
        assert selectedUsers.length == 1;
        return populateUser(selectedUsers[0], null);
    }

    public boolean removeUser(UserProperties userProperties, MessageContext context) {
        JahiaUser jahiaUser = userManagerService.lookupUserByKey(userProperties.getUserKey());
        if(userManagerService.deleteUser(jahiaUser)) {
            context.addMessage(new MessageBuilder().info().defaultText(Messages.get("resources.JahiaServerSettings",
                    "label.user.remove.successful", LocaleContextHolder.getLocale())).build());
            return true;
        } else {
            context.addMessage(new MessageBuilder().error().defaultText(Messages.get("resources.JahiaServerSettings",
                    "label.user.remove.unsuccessful", LocaleContextHolder.getLocale())).build());
            return false;
        }
    }

    public Set<Principal> search(SearchCriteria searchCriteria) {
        Set<Principal> searchResult = PrincipalViewHelper.getSearchResult(searchCriteria.getSearchIn(),
                searchCriteria.getSearchString(), searchCriteria.getProperties(), searchCriteria.getStoredOn(),
                searchCriteria.getProviders());
        searchResult = PrincipalViewHelper.removeJahiaAdministrators(searchResult);
        return searchResult;
    }

    @Autowired
    public void setPwdPolicyService(JahiaPasswordPolicyService pwdPolicyService) {
        this.pwdPolicyService = pwdPolicyService;
    }

    @Autowired
    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    private boolean setUserProperty(String propertyName, String propertyValue,String source, MessageContext context, JahiaUser jahiaUser) {
        if(!jahiaUser.setProperty(propertyName, propertyValue)){
            context.addMessage(new MessageBuilder().error().source(source).defaultText(Messages.getWithArgs(
                    "resources.JahiaServerSettings", "label.user.edit.errors.property",
                    LocaleContextHolder.getLocale(),source)).build());
            return true;
        }
        return false;
    }

    private Properties transformUserProperties(UserProperties userProperties) {
        Properties properties = new Properties();
        properties.put("j:firstName", userProperties.getFirstName());
        properties.put("j:lastName", userProperties.getLastName());
        properties.put("j:email", userProperties.getEmail());
        properties.put("j:organization", userProperties.getOrganization());
        properties.put("preferredLanguage", userProperties.getPreferredLanguage().toString());
        properties.put("j:accountLocked", userProperties.getAccountLocked().toString());
        properties.put("emailNotificationsDisabled", userProperties.getEmailNotificationsDisabled().toString());
        return properties;
    }

    public boolean updateUser(UserProperties userProperties, MessageContext context) {
        logger.info("Updating user");
        JahiaUser jahiaUser = userManagerService.lookupUserByKey(
                userProperties.getUserKey());
        boolean hasErrors = false;
        if (jahiaUser != null) {
            hasErrors = setUserProperty("j:firstName", userProperties.getFirstName(),"firstName", context, jahiaUser);
            hasErrors |= setUserProperty("j:lastName", userProperties.getLastName(),"lastName", context, jahiaUser);
            hasErrors |= setUserProperty("j:email", userProperties.getEmail(),"email", context, jahiaUser);
            hasErrors |= setUserProperty("j:organization", userProperties.getOrganization(),"organization", context, jahiaUser);
            hasErrors |= setUserProperty("emailNotificationsDisabled", userProperties.getEmailNotificationsDisabled().toString(),"emailNotifications", context, jahiaUser);
            hasErrors |= setUserProperty("j:accountLocked", userProperties.getAccountLocked().toString(),"accountLocked", context, jahiaUser);
            hasErrors |= setUserProperty("preferredLanguage", userProperties.getPreferredLanguage().toString(),"preferredLanguage", context, jahiaUser);
            if (StringUtils.isNotBlank(userProperties.getPassword())) {
                if(jahiaUser.setPassword(userProperties.getPassword())) {
                    context.addMessage(new MessageBuilder().info().defaultText(Messages.get("resources.JahiaServerSettings",
                            "label.user.edit.password.changed", LocaleContextHolder.getLocale())).build());
                } else {
                    context.addMessage(new MessageBuilder().error().source("password").defaultText(Messages.get(
                            "resources.JahiaServerSettings", "label.user.edit.errors.password",
                            LocaleContextHolder.getLocale())).build());
                    hasErrors = true;
                }
            }
        }
        if(!hasErrors) {
            context.addMessage(new MessageBuilder().info().defaultText(Messages.get("resources.JahiaServerSettings",
                    "label.user.edit.successful", LocaleContextHolder.getLocale())).build());
        }
        return !hasErrors;
    }
}
