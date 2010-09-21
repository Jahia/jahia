package org.jahia.ajax.gwt.client.widget.job;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Sep 21, 2010
 * Time: 12:26:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobListWindow extends Window {

    private final Linker linker;

    public JobListWindow(Linker linker) {
        super();
        this.linker = linker;
        setLayout(new FitLayout());
        init();
    }

    public static void showJobListWindow(Linker linker) {
        if (linker.getMainNode() != null) {
            new JobListWindow(linker).show();
        }
    }

    private void init() {
        setHeading(Messages.get("label.joblist", "Background Job List"));
        setLayout(new FitLayout());
        setSize(800, 600);

        add(new JobListPanel(this, linker));
    }
}
