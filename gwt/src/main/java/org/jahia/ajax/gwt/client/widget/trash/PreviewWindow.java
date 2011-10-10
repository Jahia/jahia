package org.jahia.ajax.gwt.client.widget.trash;


import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * Window that displays information about deleted node and its preview
 */
public class PreviewWindow extends Window {
    private LayoutContainer htmlPreview;

    public PreviewWindow(Linker linker, GWTJahiaNode jahiaNode) {

        setLayout(new BorderLayout());
        setSize(800,600);

        LayoutContainer container = new LayoutContainer(new RowLayout());
        HTML label = new HTML("<b>"+Messages.getWithArgs("label.trashedContentPreview", "{0} on {1} said about \"{2}\"" + " : ", new Object[]{jahiaNode.get("j:deletionUser"), jahiaNode.get("j:deletionDate"), jahiaNode.getDisplayName()})+"</b>");

        container.add(label);
        TextArea textArea = new TextArea();
        textArea.setReadOnly(true);
        textArea.setEnabled(false);
        textArea.setValue(jahiaNode.<String>get("j:deletionMessage"));
        textArea.setWidth("95%");
        container.add(textArea);
        container.add(new Text(Messages.get("label.path", "path") + " : " + jahiaNode.getPath()));
        add(container, new BorderLayoutData(Style.LayoutRegion.NORTH, 100));

        htmlPreview = new LayoutContainer();
        htmlPreview.setStyleAttribute("background-color", "white");
        htmlPreview.setScrollMode(Style.Scroll.AUTO);
        add(htmlPreview, new BorderLayoutData(Style.LayoutRegion.CENTER));

        // Use gwt configuration for pages

        String configuration = jahiaNode.isNodeType("jnt:page")?"gwt":"preview";

        JahiaContentManagementService.App.getInstance().getRenderedContent(
                jahiaNode.getPath(), null, JahiaGWTParameters.getLanguage(),
                "default", configuration, null, true, linker.getConfig().getName(),
                new BaseAsyncCallback<GWTRenderResult>() {
                    public void onSuccess(GWTRenderResult gwtRenderResult) {
                        htmlPreview.removeAll();
                        htmlPreview.add(new HTML(gwtRenderResult.getResult()));
                        htmlPreview.layout();
                    }
                });
    }


}
