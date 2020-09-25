package io.smallrye.opentracing;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 * @author Pavol Loffay
 */
public class OpenTracingCDIExtension implements Extension {

    public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager manager) {
        SmallRyeLogging.log.registeringTracerCDIProducer();
        bbd.addAnnotatedType(manager.createAnnotatedType(TracerProducer.class));
    }
}
