/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.rating.mahout;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 27 juil. 2010
 */
public class Recommender implements Processor, InitializingBean, CamelContextAware {
    private transient static Logger logger = Logger.getLogger(Recommender.class);
    private Pattern pattern = Pattern.compile(
            "([0-9\\-]+ [0-9:,]+) user ([a-zA-Z@.0-9_\\-]+) ip ([0-9.:]+) session ([a-zA-Z@0-9_\\-\\/]+) path (.*) nodetype ([a-zA-Z:]+) node updated with (.*)");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    DataSource dataSource;
    private GenericUserBasedRecommender userBasedRecommender;
    private Map<String, Long> users = new HashMap<String, Long>(1024);
    private Map<String, Long> paths = new HashMap<String, Long>(1024);
    private Map<Long, String> invertedPaths = new HashMap<Long, String>(1024);
    private ItemBasedRecommender itemBasedRecommender;
    private CamelContext camelContext;
    private String from;
    private int count = 0;
    private int refreshRecommendationsEvery;

    public void process(Exchange exchange) throws Exception {
        final String message = (String) exchange.getIn().getBody();
        final Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            String user = matcher.group(2);
            Date date = dateFormat.parse(matcher.group(1));
            String path = matcher.group(5);
            String data = matcher.group(7);
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.getJSONArray("jcr:mixinTypes").getString(0).equals("jmix:rating")) {
                logger.info(
                        "Insert into database updated node " + path + " by user " + user + " at " + SimpleDateFormat.getDateTimeInstance().format(
                                date) + " with rating " + jsonObject.getJSONArray("j:lastVote").getLong(0));
                try {
                    Connection connection = dataSource.getConnection();
                    PreparedStatement statement;
                    if (!users.containsKey(user)) {
                        statement = connection.prepareStatement("insert into taste_users (name) values (?)");
                        statement.setString(1, user);
                        statement.executeUpdate();
                        statement.close();
                        statement = connection.prepareStatement("select id from taste_users where name = ?");
                        statement.setString(1, user);
                        ResultSet resultSet = statement.executeQuery();
                        resultSet.next();
                        users.put(user, resultSet.getLong(1));
                        resultSet.close();
                        statement.close();
                    }
                    if (!paths.containsKey(path)) {
                        statement = connection.prepareStatement("insert into taste_path (path) values (?)");
                        statement.setString(1, path);
                        statement.executeUpdate();
                        statement.close();
                        statement = connection.prepareStatement("select id from taste_path where path = ?");
                        statement.setString(1, path);
                        ResultSet resultSet = statement.executeQuery();
                        resultSet.next();
                        long value = resultSet.getLong(1);
                        paths.put(path, value);
                        invertedPaths.put(value, path);
                        resultSet.close();
                        statement.close();
                    }
                    statement = connection.prepareStatement(
                            "insert into taste_preferences (user_id,item_id,preference) values (?,?,?)");
                    statement.setLong(1, users.get(user));
                    statement.setLong(2, paths.get(path));
                    statement.setFloat(3, jsonObject.getJSONArray("j:lastVote").getLong(0));
                    statement.executeUpdate();
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    logger.debug(e.getMessage(), e);
                } catch (JSONException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    count++;
                    if (count % refreshRecommendationsEvery == 0) {
                        initMahoutRecommender();
                    }
                }
            }
        }
    }

    private void initMahoutRecommender() {
        try {
            MySQLJDBCDataModel model = new MySQLJDBCDataModel(dataSource);
            UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(model), model);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, 0.2, similarity, model, 0.2);
            userBasedRecommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            ItemSimilarity itemSimilarity = new CachingItemSimilarity(new LogLikelihoodSimilarity(model), model);
            itemBasedRecommender = new GenericItemBasedRecommender(model, itemSimilarity);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }
    }

    public void afterPropertiesSet() throws Exception {
        Connection connection = dataSource.getConnection();
        PreparedStatement statement;
        statement = connection.prepareStatement("select id,name from taste_users");
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            users.put(resultSet.getString(2), resultSet.getLong(1));
        }
        statement.close();
        statement = connection.prepareStatement("select id,path from taste_path");
        resultSet = statement.executeQuery();
        while (resultSet.next()) {
            paths.put(resultSet.getString(2), resultSet.getLong(1));
            invertedPaths.put((long) resultSet.getInt(1), resultSet.getString(2));
        }
        statement.close();
        connection.close();
        initMahoutRecommender();
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Recommender.RecommendedItem> recommendForUser(JahiaUser user) {
        try {
            if (userBasedRecommender != null) {
                List<org.apache.mahout.cf.taste.recommender.RecommendedItem> list = userBasedRecommender.recommend(
                        users.get(user.getName()), 3);
                List<RecommendedItem> recommendedItems = new LinkedList<RecommendedItem>();
                for (org.apache.mahout.cf.taste.recommender.RecommendedItem item : list) {
                    recommendedItems.add(new RecommendedItem(invertedPaths.get(item.getItemID()), item.getValue()));
                }
                return recommendedItems;
            }
        } catch (TasteException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public List<Recommender.RecommendedItem> mostSimilarItems(JCRNodeWrapper node) {
        try {
            if (itemBasedRecommender != null) {
                List<org.apache.mahout.cf.taste.recommender.RecommendedItem> list = itemBasedRecommender.mostSimilarItems(
                        paths.get(node.getPath()), 3);
                List<RecommendedItem> recommendedItems = new LinkedList<RecommendedItem>();
                for (org.apache.mahout.cf.taste.recommender.RecommendedItem item : list) {
                    recommendedItems.add(new RecommendedItem(invertedPaths.get(item.getItemID()), item.getValue()));
                }
                return recommendedItems;
            }
        } catch (TasteException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public void setCamelContext(final CamelContext camelContext) {
        this.camelContext = camelContext;
        final Recommender recommender = this;
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(from).filter("groovy", "request.body.contains(\"node updated\")").to(new ProcessorEndpoint(
                            "recommender", camelContext, recommender));
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setRefreshRecommendationsEvery(int refreshRecommendationsEvery) {
        this.refreshRecommendationsEvery = refreshRecommendationsEvery;
    }

    public class RecommendedItem {
        private final String path;
        private final float value;

        public RecommendedItem(String path, float value) {
            //To change body of created methods use File | Settings | File Templates.
            this.path = path;
            this.value = value;
        }

        public String getPath() {
            return path;
        }

        public float getValue() {
            return value;
        }
    }
}
