package com.opuscapita.log4j.appender

import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent

/**
 * @author Dmitry Divin
 */
class InMemoryAppender extends AppenderSkeleton implements Closeable {
    private final List<LoggingEvent> loggingEvents = new ArrayList<LoggingEvent>()

    List<LoggingEvent> getLogEvens(long offsetTimeStamp) {
        return loggingEvents.findAll {it.timeStamp > offsetTimeStamp}
    }

    void append(LoggingEvent loggingEvent) {
        loggingEvents << loggingEvent
    }

    void clearLogEvents() {
        loggingEvents.clear()
    }

    @Override
    void close() {
        // Do nothing
    }

    boolean requiresLayout() {
        return false
    }
}