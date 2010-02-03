package org.jahia.ajax.gwt.client.data;
import com.extjs.gxt.ui.client.data.ModelData;

import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 3, 2010
 * Time: 2:20:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GWTJahiaPrincipal extends ModelData , Serializable {

    public String getName();

    public String getKey();

    public String getProvider();

    public String getSiteName();


}
