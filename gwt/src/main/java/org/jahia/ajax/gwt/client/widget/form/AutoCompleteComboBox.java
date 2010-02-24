package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 24, 2010
 * Time: 5:09:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class AutoCompleteComboBox extends ComboBox<GWTJahiaNode> {

    public AutoCompleteComboBox(final String nodeType,final int maxResult) {
        setDisplayField("name");
        final ListStore<GWTJahiaNode> store = new ListStore<GWTJahiaNode>(new BaseListLoader(
                new RpcProxy<List<GWTJahiaNode>>() {
                    @Override
                    protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaNode>> asyncCallback) {
                        JahiaContentManagementService.App.getInstance().search(getRawValue()+"*", maxResult, nodeType, null, null, asyncCallback);
                    }
                }));
        setStore(store);
        setTypeAhead(true);
        setTypeAheadDelay(100);
        setTriggerAction(ComboBox.TriggerAction.ALL);
        setWidth(500);
        setMinLength(3);
    }

}
