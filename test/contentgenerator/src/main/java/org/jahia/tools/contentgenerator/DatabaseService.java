package org.jahia.tools.contentgenerator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.ArticleBO;

public class DatabaseService {
	private static DatabaseService instance;

	private static Connection dbConnection;

	private static final Logger logger = Logger.getLogger(DatabaseService.class.getName());

	private DatabaseService() {
	}

	public static DatabaseService getInstance() {
		if (instance == null) {
			instance = new DatabaseService();
		}
		return instance;
	}

	public Connection getConnection() {
		if (dbConnection == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();

				StringBuffer sbConnection = new StringBuffer("jdbc:mysql://");
				sbConnection.append(ContentGenerator.getProperty(ContentGeneratorCst.DB_HOST_PROPERTY));
				sbConnection.append("/");
				sbConnection.append(ContentGenerator.getProperty(ContentGeneratorCst.DB_DATABASE_PROPERTY));
				sbConnection.append("?user=");
				sbConnection.append(ContentGenerator.getProperty(ContentGeneratorCst.DB_LOGIN_PROPERTY));
				sbConnection.append("&password=");
				sbConnection.append(ContentGenerator.getProperty(ContentGeneratorCst.DB_PASSWORD_PROPERTY));

				dbConnection = java.sql.DriverManager.getConnection(sbConnection.toString());

				logger.info("MySQL connection established.");
			} catch (InstantiationException e) {
				logger.error("Error during MySQL connection instantiation", e);
			} catch (IllegalAccessException e) {
				logger.error("Error during MySQL connection", e);
			} catch (ClassNotFoundException e) {
				logger.error("Error during MySQL connection instantiation", e);
			} catch (SQLException e) {
				logger.error("Error during MySQL connection instantiation", e);
			}
		}
		return dbConnection;
	}

	public void closeConnection() {
		if (null != dbConnection) {
			try {
				dbConnection.close();
			} catch (SQLException e) {
				logger.error("Error during connection close", e);
			}
		}
	}

	public List getArticles(final Integer nbRecords) {
		// diviser nbRecords / pas
		List<Integer> recordSetSizes = getRecordSetsSize(nbRecords);
		List<Integer> articlesId;
		List<ArticleBO> articlesContent = new ArrayList<ArticleBO>();
		for (Iterator<Integer> iterator = recordSetSizes.iterator(); iterator.hasNext();) {
			Integer recordSetSize = (Integer) iterator.next();
			// pour chaque + 1 : faire une requete ID
			articlesId = getArticlesIds(recordSetSize);

			// envoyer ces ID au contenu (liste)
			try {
				// ajouter resultats a la liste
				articlesContent.addAll(getArticlesContent(articlesId));
			} catch (SQLException e) {
				logger.error("Error during articles content selection", e);
			}
		}
		closeConnection();

		return articlesContent;
	}

	public List<Integer> getArticlesIds(Integer recordSetSize) {
		logger.info("Selecting " + recordSetSize + " ID's from database");
		Statement stmt = null;
		List<Integer> idList = new ArrayList<Integer>();

		try {
			stmt = this.getConnection().createStatement();

			StringBuffer sbQuery = new StringBuffer("SELECT a.id_article FROM ");
			sbQuery.append(ContentGenerator.getProperty(ContentGeneratorCst.DB_TABLE_PROPERTY) + " a ");
			sbQuery.append(" ORDER BY RAND() ");
			sbQuery.append(" LIMIT 0," + recordSetSize);

			logger.debug("SQL Query: " + sbQuery.toString());
			boolean resultStmt = stmt.execute(sbQuery.toString());
			ResultSet results = null;

			if (resultStmt) {
				results = stmt.getResultSet();
				while (results.next()) {
					idList.add(results.getInt("id_article"));
				}
			}

		} catch (SQLException e) {
			logger.error("Error while requesting articles ID", e);
		}

		return idList;
	}

	public List<ArticleBO> getArticlesContent(final List<Integer> articlesId) throws SQLException {
		logger.info("Selecting " + articlesId.size() + " record(s) from database");

		Statement stmt = null;

		stmt = this.getConnection().createStatement();
		StringBuffer sbIdCommaSeparated = new StringBuffer();
		for (Iterator<Integer> iterator = articlesId.iterator(); iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			sbIdCommaSeparated.append(id + ",");
		}
		// removes trailing comma
		String idCommaSeparated = sbIdCommaSeparated.substring(0, sbIdCommaSeparated.length() - 1);

		StringBuffer sbQuery = new StringBuffer("SELECT a.id_article, a.title,a.content FROM ");
		sbQuery.append(ContentGenerator.getProperty(ContentGeneratorCst.DB_TABLE_PROPERTY) + " a ");
		sbQuery.append(" WHERE ");
		sbQuery.append(" a.id_article IN (" + idCommaSeparated + ")");

		logger.debug("SQL Query: " + sbQuery.toString());
		boolean resultStmt = stmt.execute(sbQuery.toString());
		ResultSet results = null;
		List<ArticleBO> articlesList = null;
		if (resultStmt) {
			results = stmt.getResultSet();
			articlesList = getArticleCollectionFromResultSet(results);
		}

		return articlesList;
	}

	private List<Integer> getRecordSetsSize(Integer nbRecords) {
		List<Integer> sizes = new ArrayList<Integer>();
		int nbQueries = (nbRecords.intValue() / ContentGeneratorCst.SQL_RECORDSET_SIZE);
		int remaining = (nbRecords.intValue() % ContentGeneratorCst.SQL_RECORDSET_SIZE);
		for (int i = 1; i <= nbQueries; i++) {
			sizes.add(ContentGeneratorCst.SQL_RECORDSET_SIZE);
		}
		if (remaining > 0) {
			sizes.add(new Integer(remaining));
		}
		return sizes;
	}

	private List<ArticleBO> getArticleCollectionFromResultSet(final ResultSet articles) throws SQLException {
		List<ArticleBO> listeArticles = new ArrayList<ArticleBO>();

		Integer idArticle;
		String title;
		String content;
		ArticleBO article = null;
		while (articles.next()) {
			idArticle = articles.getInt("id_article");
			title = articles.getString("title");
			content = articles.getString("content");

			article = new ArticleBO(idArticle, title, content);
			listeArticles.add(article);
		}
		return listeArticles;
	}
}
