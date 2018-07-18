package io.smallrye.opentracing;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import org.jboss.logging.Logger;

/**
 * @author Pavol Loffay
 */
public class OpenTracingCDIExtension implements Extension {

  private static final Logger logger = Logger.getLogger(OpenTracingCDIExtension.class);

  public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager manager) {
    logger.info("Registering Tracer CDI producer");
    bbd.addAnnotatedType(manager.createAnnotatedType(TracerProducer.class));
    bbd.addAnnotatedType(manager.createAnnotatedType(SmallRyeTracingCDIInterceptor.class));
  }
}
