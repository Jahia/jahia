package org.jahia.ajax.gwt.client.widget.poller;

import org.atmosphere.gwt.client.AtmosphereGWTSerializer;
import org.atmosphere.gwt.client.SerialTypes;

@SerialTypes(value = {ProcessPollingEvent.class, TaskEvent.class})
public abstract class PollerSerializer  extends AtmosphereGWTSerializer {
}
