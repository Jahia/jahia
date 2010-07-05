package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 2, 2010
 * Time: 7:36:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class EngineLoader {
    public static final int CREATE = 1;
    public static final int EDIT = 2;

    public static void showEditEngine(final Linker linker, final GWTJahiaNode node) {
        showEngine(EDIT, linker, node, null,null,null,false);
    }

    public static void showCreateEngine(final Linker linker, final GWTJahiaNode node, final GWTJahiaNodeType type, final Map<String, GWTJahiaNodeProperty> props,
                                        final String targetName, final boolean createInParentAndMoveBefore) {
        showEngine(CREATE, linker, node, type, props, targetName, createInParentAndMoveBefore);
    }

    private static void showEngine(final int t, final Linker linker, final GWTJahiaNode node, final GWTJahiaNodeType type, final Map<String, GWTJahiaNodeProperty> props, final String targetName, final boolean createInParentAndMoveBefore) {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable reason) {

            }

            public void onSuccess() {
                if (t == CREATE) {
                    new CreateContentEngine(linker, node, type, props, targetName, createInParentAndMoveBefore).show();
                } else if (t == EDIT) {
                    new EditContentEngine(node, linker).show();
                }
            }
        });


    }
}
