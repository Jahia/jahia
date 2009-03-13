package org.jahia.services.analytics;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: jahia
 * Date: 27 févr. 2009
 * Time: 17:01:40
 *
 * @author Ibrahim El Ghandour
 *
 */
public class DataRowMapper implements RowMapper {
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        JAMonCounterData counterData = new JAMonCounterData();
        //counterData.setId((new Integer(resultSet.getInt("counter"))));
        counterData.setTimestamp(resultSet.getString("ts"));
        counterData.setAvgtime(new Double(resultSet.getDouble("avgtime")));
        counterData.setHits(new Double(resultSet.getDouble("hits")));
        counterData.setMaxtime(new Double(resultSet.getDouble("maxtime")));
        counterData.setName(resultSet.getString("name"));
        return counterData;
    }
}
