package org.jahia.services.analytics;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 27 févr. 2009
 * Time: 16:32:42
 *
* @author Ibrahim El Ghandour
 * 
 */
public class IdRowMapper  implements RowMapper {
     public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        JAMonCounter jc = new JAMonCounter();
        jc.setId(new Integer(resultSet.getInt("id")));
        jc.setName(resultSet.getString("name"));
        return jc;}
    
}
