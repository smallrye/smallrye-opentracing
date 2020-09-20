package io.smallrye.opentracing;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SROPT", length = 5)
public interface SmallRyeLogging {

    SmallRyeLogging log = Logger.getMessageLogger(SmallRyeLogging.class, SmallRyeLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1000, value = "Provided operation name does not match http-path or class-method. Using default class-method.")
    void operationNameNotMatch();
}
