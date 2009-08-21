package org.jahia.services.shindig;

import org.apache.shindig.social.opensocial.spi.*;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.model.MessageCollection;
import org.apache.shindig.social.opensocial.model.Message;
import org.apache.shindig.social.core.model.PersonImpl;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.common.util.ImmediateFuture;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerRoutingService;
import org.jahia.services.usermanager.JahiaGroupManagerRoutingService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.api.Constants;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.concurrent.Future;
import java.util.*;

import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Lists;

import javax.servlet.http.HttpServletResponse;
import javax.jcr.*;

/**
 * Implementation of the Shindig person service, mapping Shindig people to Jahia users.
 *
 * @author loom
 *         Date: Aug 18, 2009
 *         Time: 8:04:20 AM
 */
public class JahiaShindigService implements PersonService, ActivityService, AppDataService, MessageService {

    private static final Comparator<Person> NAME_COMPARATOR = new Comparator<Person>() {
      public int compare(Person person, Person person1) {
        String name = person.getName().getFormatted();
        String name1 = person1.getName().getFormatted();
        return name.compareTo(name1);
      }
    };
        
    private JahiaUserManagerService jahiaUserManagerService;
    private JCRStoreService jcrStoreService;

    public JahiaShindigService(JahiaUserManagerService jahiaUserManagerService, JCRStoreService jcrStoreService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
        this.jcrStoreService = jcrStoreService;
    }

    /* -- PERSON SERVICE IMPLEMENTATION -- */

    /**
     * Returns a list of people that correspond to the passed in person ids.
     *
     * @param userIds A set of users
     * @param groupId The group
     * @param collectionOptions How to filter, sort and paginate the collection being fetched
     * @param fields The profile details to fetch. Empty set implies all
     * @param token The gadget token @return a list of people.
     */
    public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds, GroupId groupId, CollectionOptions collectionOptions, Set<String> fields, SecurityToken token) throws ProtocolException {
        try {
            List<Person> result = Lists.newArrayList();

            Set<String> idSet = getIdSet(userIds, groupId, token);

            for (String id: idSet) {
                result.add(loadPerson(id, fields));
            }

            if (GroupId.Type.self == groupId.getType() && result.isEmpty()) {
              throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Person not found");
            }

            // We can pretend that by default the people are in top friends order
            if (collectionOptions.getSortBy().equals(Person.Field.NAME.toString())) {
              Collections.sort(result, NAME_COMPARATOR);
            }

            if (collectionOptions.getSortOrder() == SortOrder.descending) {
              Collections.reverse(result);
            }

            // TODO: The samplecontainer doesn't really have the concept of HAS_APP so
            // we can't support any filters yet. We should fix this.

            int totalSize = result.size();
            int last = collectionOptions.getFirst() + collectionOptions.getMax();
            result = result.subList(collectionOptions.getFirst(), Math.min(last, totalSize));

            return ImmediateFuture.newInstance(new RestfulCollection<Person>(result, collectionOptions.getFirst(),
                totalSize));
        } catch (RepositoryException re){
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, re.getMessage(), re);
        }
    }

    /**
     * Returns a person that corresponds to the passed in person id.
     *
     * @param id The id of the person to fetch.
     * @param fields The fields to fetch.
     * @param token The gadget token
     * @return a list of people.
     */
    public Future<Person> getPerson(UserId id, Set<String> fields, SecurityToken token) throws ProtocolException {
        String userKey = id.getUserId(token);
        return ImmediateFuture.newInstance(loadPerson(userKey, fields));
    }

    private Person loadPerson(String userKey, Set<String> fields) {
        try {
            JahiaUser jahiaUser = jahiaUserManagerService.lookupUser(userKey);
            if (jahiaUser == null) {
                return null;
            }
            // we must now load all the custom properties from the JCR, if they exist
            JahiaPersonImpl jahiaPerson = loadPersonPropertiesFromJCR(jahiaUser);
            Map<String, Object> appData = getPersonAppData(userKey, fields);
            jahiaPerson.setAppData(appData);

            return jahiaPerson;
        } catch (RepositoryException re) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, re.getMessage(), re);
        }
    }

    /* -- ACTIVITY SERVICE IMPLEMENTATION -- */

    public Future<RestfulCollection<Activity>> getActivities(Set<UserId> userIds, GroupId groupId, String appId, Set<String> fields, CollectionOptions options, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<RestfulCollection<Activity>> getActivities(UserId userId, GroupId groupId, String appId, Set<String> fields, CollectionOptions options, Set<String> activityIds, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Activity> getActivity(UserId userId, GroupId groupId, String appId, Set<String> fields, String activityId, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> deleteActivities(UserId userId, GroupId groupId, String appId, Set<String> activityIds, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> createActivity(UserId userId, GroupId groupId, String appId, Set<String> fields, Activity activity, SecurityToken token) throws ProtocolException {
        return null;
    }

    /* -- APPDATA SERVICE IMPLEMENTATION -- */

    public Future<DataCollection> getPersonData(Set<UserId> userIds, GroupId groupId, String appId, Set<String> fields, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> deletePersonData(UserId userId, GroupId groupId, String appId, Set<String> fields, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> updatePersonData(UserId userId, GroupId groupId, String appId, Set<String> fields, Map<String, String> values, SecurityToken token) throws ProtocolException {
        return null;
    }

    private Map<String, Object> getPersonAppData(String id, Set<String> fields) throws RepositoryException {
        Map<String, Object> appData = null;
        JCRSessionWrapper session = jcrStoreService.getSystemSession();
        Node userNode = session.getNode("/" + Constants.CONTENT + "/users/" + id);
        Node appDataFolderNode;
        if (!userNode.hasNode("appdata")) {
            appDataFolderNode = userNode.addNode("appdata", Constants.NT_FOLDER);
            session.save();
        } else {
            appDataFolderNode = session.getNode("/" + Constants.CONTENT + "/users/" + id + "/appdata");
        }
        if (appDataFolderNode != null) {
          if (fields.contains(Person.Field.APP_DATA.toString())) {
            appData = Maps.newHashMap();
            @SuppressWarnings("unchecked")
            PropertyIterator dataPropertyIterator = appDataFolderNode.getProperties();
            while (dataPropertyIterator.hasNext()) {
              Property currentDataProperty = dataPropertyIterator.nextProperty();
              appData.put(currentDataProperty.getName(), currentDataProperty.getValue());
            }
          } else {
            String appDataPrefix = Person.Field.APP_DATA.toString() + ".";
            for (String field : fields) {
              if (field.startsWith(appDataPrefix)) {
                if (appData == null) {
                  appData = Maps.newHashMap();
                }

                String appDataField = field.substring(appDataPrefix.length());
                if (appDataFolderNode.hasProperty(appDataField)) {
                  appData.put(appDataField, appDataFolderNode.getProperty(appDataField).getValue());
                }
              }
            }
          }
        }

        return appData;
    }

    /**
     * This will load all the properties from the JCR into the Person object, using introspection to find property
     * and convert them appropriately.
     * @param jahiaUser the JahiaUser to use to create the Shindig Person.
     */
    private JahiaPersonImpl loadPersonPropertiesFromJCR(JahiaUser jahiaUser) throws RepositoryException {
        JahiaPersonImpl jahiaPersonImpl = new JahiaPersonImpl(jahiaUser);
        String name = jahiaUser.getUserKey().split("}")[1];
        JCRSessionWrapper session = jcrStoreService.getSystemSession();
        Node usersFolderNode = session.getNode("/" + Constants.CONTENT + "/users/" + name);
        JCRUser jcrUser = null;
        if (!usersFolderNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
            jcrUser = new JCRUser(usersFolderNode.getUUID(), jcrStoreService);
        } else {
            return null;
        }

        return jahiaPersonImpl;
    }

    /**
     * Get the set of user id's from a user and group
     */
    private Set<String> getIdSet(UserId user, GroupId group, SecurityToken token) throws RepositoryException {
      String userId = user.getUserId(token);

      if (group == null) {
        return ImmutableSortedSet.of(userId);
      }

      Set<String> returnVal = Sets.newLinkedHashSet();
      switch (group.getType()) {
      case all:
      case friends:
          JCRSessionWrapper session = jcrStoreService.getSystemSession();
          Node userNode = session.getNode("/" + Constants.CONTENT + "/users/" + userId);
          Node usersFolderNode;
          if (!userNode.hasNode("friends")) {
              usersFolderNode = userNode.addNode("friends", Constants.NT_FOLDER);
              session.save();
          } else {
              usersFolderNode = session.getNode("/" + Constants.CONTENT + "/users/" + userId + "/friends");
          }
          Node members = usersFolderNode.getNode("j:members");
          if (members != null) {
            NodeIterator iterator = members.getNodes();
            while (iterator.hasNext()) {
                Node member = (Node) iterator.next();
                if (member.isNodeType(Constants.JAHIANT_MEMBER)) {
                    JahiaUser jahiaUser = jahiaUserManagerService.lookupUser(member.getName());
                    if(jahiaUser!=null) {
                        returnVal.add(member.getName());
                    }
                }
            }
          }
          break;
      case groupId:
        break;
      case self:
        returnVal.add(userId);
        break;
      }
      return returnVal;
    }

    /**
     * Get the set of user id's for a set of users and a group
     */
    private Set<String> getIdSet(Set<UserId> users, GroupId group, SecurityToken token)
        throws RepositoryException {
      Set<String> ids = Sets.newLinkedHashSet();
      for (UserId user : users) {
        ids.addAll(getIdSet(user, group, token));
      }
      return ids;
    }

    /* -- APPDATA SERVICE IMPLEMENTATION -- */

    public Future<RestfulCollection<MessageCollection>> getMessageCollections(UserId userId, Set<String> fields, CollectionOptions options, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<MessageCollection> createMessageCollection(UserId userId, MessageCollection msgCollection, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> modifyMessageCollection(UserId userId, MessageCollection msgCollection, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> deleteMessageCollection(UserId userId, String msgCollId, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<RestfulCollection<Message>> getMessages(UserId userId, String msgCollId, Set<String> fields, List<String> msgIds, CollectionOptions options, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> createMessage(UserId userId, String appId, String msgCollId, Message message, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> deleteMessages(UserId userId, String msgCollId, List<String> ids, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> modifyMessage(UserId userId, String msgCollId, String messageId, Message message, SecurityToken token) throws ProtocolException {
        return null;
    }
    
}
