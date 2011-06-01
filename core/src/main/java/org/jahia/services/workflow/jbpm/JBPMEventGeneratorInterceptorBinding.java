package org.jahia.services.workflow.jbpm;

import org.jbpm.pvm.internal.wire.binding.WireInterceptorBinding;
import org.jbpm.pvm.internal.wire.descriptor.ProvidedObjectDescriptor;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/**
 * JBPM Wire binding for event generator interceptor
 */
public class JBPMEventGeneratorInterceptorBinding extends WireInterceptorBinding {

  public JBPMEventGeneratorInterceptorBinding() {
    super("jbpmeventgenerator-interceptor");
  }

  public Object parse(Element element, Parse parse, Parser parser) {
    return new ProvidedObjectDescriptor(new JBPMEventGeneratorInterceptor());
  }

}
