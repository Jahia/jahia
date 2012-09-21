package org.jahia.ajax.gwt.client.widget;

import org.atmosphere.gwt.client.AtmosphereGWTSerializer;
import org.atmosphere.gwt.client.SerialTypes;

@SerialTypes(value = {PollingEvent.class})
public abstract class PollerSerializer  extends AtmosphereGWTSerializer {
}
