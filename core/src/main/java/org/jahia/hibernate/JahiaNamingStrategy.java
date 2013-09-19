package org.jahia.hibernate;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.io.IOUtils;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.cfg.NamingStrategy;
import org.jahia.settings.SettingsBean;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom naming strategy to prefix the table names, prefix any name that uses an SQL reserved word, and can
 * force names to use lowercase.
 */
public class JahiaNamingStrategy extends ImprovedNamingStrategy implements Serializable {

    /**
   	 * A convenient singleton instance
   	 */
   	public static final NamingStrategy INSTANCE = new JahiaNamingStrategy();

    private static String[] sqlReservedWords = new String[0];

    static {
        InputStream sqlReservedWordsStream = JahiaNamingStrategy.class.getClassLoader().getResourceAsStream("org/jahia/hibernate/sqlReservedWords.txt");
        if (sqlReservedWordsStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sqlReservedWordsStream));
            String newLine = null;
            List<String> reservedWordList = new ArrayList<String>();
            try {
                while ((newLine = bufferedReader.readLine()) != null) {
                    reservedWordList.add(newLine.trim().toLowerCase());
                }
                sqlReservedWords = reservedWordList.toArray(new String[reservedWordList.size()]);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } finally {
                IOUtils.closeQuietly(bufferedReader);
            }
        }
    }

    @Override
    public String classToTableName(String className) {
        String tableName = super.classToTableName(className);
        return processTableName(tableName);
    }

    @Override
    public String propertyToColumnName(String propertyName) {
        String columnName = super.propertyToColumnName(propertyName);
        return processColumnName(columnName);
    }

    @Override
    public String tableName(String tableName) {
        return processTableName(super.tableName(tableName));
    }

    @Override
    public String columnName(String columnName) {
        return processColumnName(super.columnName(columnName));
    }

    public static boolean isSqlReservedWord(String name) {
        String lowerCaseName = name.toLowerCase();
        for (String reservedWord : sqlReservedWords) {
            if (reservedWord.equals(lowerCaseName)) {
                return true;
            }
        }
        return false;
    }

    public static String processTableName(String tableName) {
        if (StringUtils.isNotEmpty(SettingsBean.getInstance().getHibernateNamingTablePrefix())) {
            return processNameCase(SettingsBean.getInstance().getHibernateNamingTablePrefix() + tableName);
        } else {
            return processNameCase(tableName);
        }
    }

    public static String processColumnName(String columnName) {
        return processNameCase(prefixSqlReservedWords(columnName));
    }

    public static String prefixSqlReservedWords(String name) {
        if (isSqlReservedWord(name) && StringUtils.isNotEmpty(SettingsBean.getInstance().getHibernateNamingReservedWordPrefix())) {
            return SettingsBean.getInstance().getHibernateNamingReservedWordPrefix() + name;
        } else {
            return name;
        }
    }

    public static String processNameCase(String name) {
        if (SettingsBean.getInstance().isHibernateNamingForceLowerCase()) {
            return name.toLowerCase();
        } else {
            return name;
        }
    }
}
