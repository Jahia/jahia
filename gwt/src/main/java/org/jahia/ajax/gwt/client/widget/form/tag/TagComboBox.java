package org.jahia.ajax.gwt.client.widget.form.tag;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.List;

/**
 * Created by kevan on 02/07/14.
 */
public class TagComboBox extends ComboBox<GWTJahiaValueDisplayBean> {
    public TagComboBox(boolean autoComplete) {
        if(autoComplete){
            setDisplayField("display");
            final ListStore<GWTJahiaValueDisplayBean> store = new ListStore<GWTJahiaValueDisplayBean>(new BaseListLoader(
                    new RpcProxy<List<GWTJahiaValueDisplayBean>>() {
                        @Override
                        protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaValueDisplayBean>> asyncCallback) {
                            // TODO handle separator to provide better autocomplete
                            GWTJahiaNode site = JahiaGWTParameters.getSiteNode();
                            JahiaContentManagementService.App.getInstance().getTags(getRawValue(),
                                    site != null ? site.getPath() : null, 1L, 10L, 0L, true, asyncCallback);
                        }
                    }));
            setStore(store);
            setTypeAhead(true);
            setTypeAheadDelay(100);
            setTriggerAction(ComboBox.TriggerAction.ALL);
            setMinChars(2);
            setQueryDelay(100);
        }else {
            // create an empty store
            setDisplayField("display");
            final ListStore<GWTJahiaValueDisplayBean> store = new ListStore<GWTJahiaValueDisplayBean>();
            setStore(store);
        }
    }
}
