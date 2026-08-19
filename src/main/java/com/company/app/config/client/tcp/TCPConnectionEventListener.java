package com.company.app.config.client.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.ip.event.IpIntegrationEvent;
import org.springframework.integration.ip.tcp.connection.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TCPConnectionEventListener implements ApplicationListener<IpIntegrationEvent> {
    private static final Logger logger = LoggerFactory.getLogger(TCPConnectionEventListener.class);
    private static final String NON_VALUE_STRING = null;

    @Override
    public void onApplicationEvent(IpIntegrationEvent event) {
        Objects.requireNonNull(event, "event must not be null");

        String eventType;

        var connectionFactoryName = NON_VALUE_STRING;
        var connectionId = NON_VALUE_STRING;
        var port = NON_VALUE_STRING;
        var hostname = NON_VALUE_STRING;

        if (event instanceof TcpConnectionCloseEvent) {
            eventType = "connectionClosed";
        } else if (event instanceof TcpConnectionOpenEvent) {
            eventType = "connectionOpened";
        } else if (event instanceof TcpConnectionExceptionEvent) {
            eventType = "connectionException";
        } else {
            eventType = "notMapped";
        }

        if (event.getSource() instanceof TcpConnection tcpConnection) {
            port = String.valueOf(tcpConnection.getPort());
            hostname = tcpConnection.getHostName();
        }

        if (event.getSource() instanceof TcpNioClientConnectionFactory tcpNioClientConnectionFactory) {
            port = String.valueOf(tcpNioClientConnectionFactory.getPort());
            hostname = tcpNioClientConnectionFactory.getHost();
        }

        if (event instanceof TcpConnectionEvent tcpConnectionEvent) {
            connectionFactoryName = tcpConnectionEvent.getConnectionFactoryName();
            connectionId = tcpConnectionEvent.getConnectionId();
        }

        String msg = "tcpEvents - eventType=%s, connectionFactoryName=%s, connectionId=%s, hostname=%s, port=%s, eventClass=%s".formatted(
            eventType,
            connectionFactoryName,
            connectionId,
            hostname,
            port,
            event.getClass().getSimpleName()
        );
        if (event.getCause() != null)
            logger.error(msg, event.getCause());
        else
            logger.info(msg);
    }
}
