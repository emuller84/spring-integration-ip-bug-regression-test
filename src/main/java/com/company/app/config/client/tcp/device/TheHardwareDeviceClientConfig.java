package com.company.app.config.client.tcp.device;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.connection.FailoverClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.util.List;

@Configuration
public class TheHardwareDeviceClientConfig {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${device-service.tcp.host}")
    private String deviceHost;

    @Value("${device-service.tcp.port}")
    private Integer devicePort;

    @Value("${device-service.tcp.timeout.remoteGateway}")
    private Long tcpTimeoutRemoteGateway;

    @Value("${device-service.tcp.timeout.requestGateway}")
    private Long tcpTimeoutRequestGateway;

    @Value("${device-service.tcp.timeout.connect}")
    private int tcpTimeoutCConnect;

    @Value("${device-service.tcp.failoverRefreshSharedInterval}")
    private Long failoverRefreshSharedInterval;

    TheHardwareDeviceClientConfig(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Service
    public class ConnectionA {
        @Bean(name = "theHardwareDeviceOutboundChannelA")
        @ServiceActivator(inputChannel = "theHardwareDeviceOutboundChannel")
        public MessageHandler theHardwareDeviceOutboundChannelA() {
            return createTcpOutboundGateway();
        }
    }

    @ConditionalOnProperty(value = "device-service.enable-second-connection", havingValue = "true")
    @Service
    public class ConnectionB {
        @Bean(name = "theHardwareDeviceOutboundChannelB")
        @ServiceActivator(inputChannel = "theHardwareDeviceOutboundChannel")
        public MessageHandler theHardwareDeviceOutboundChannelB() {
            return createTcpOutboundGateway();
        }
    }

    /**
     * The message channel used by the two OutboundGateways. When a message is sent, it's going through this class.
     * It's doing the round-robin between the two gateways.
     */
    @Bean
    public MessageChannel theHardwareDeviceOutboundChannel() {
        var directChannel = new DirectChannel();
        directChannel.setFailover(false);
        return directChannel;
    }

    @MessagingGateway(name = "theHardwareDeviceTCPClientGateway", defaultRequestChannel = "theHardwareDeviceOutboundChannel")
    public interface TheHardwareDeviceTCPClientGateway {
        byte[] send(byte[] message) throws MessagingException;
    }

    @Bean
    @DependsOn("theHardwareDeviceTCPClientGateway")
    public DeviceAdapter thales10kAdapter(TheHardwareDeviceTCPClientGateway theHardwareDeviceTCPClientGateway) {
        return new DeviceAdapter() {
            @Override
            protected byte[] communicateWithDevice(byte[] msg) {
                return theHardwareDeviceTCPClientGateway.send(msg);
            }
        };
    }

    private TcpOutboundGateway createTcpOutboundGateway() {
        var tcpOutboundGateway = new TcpOutboundGateway();
        tcpOutboundGateway.setConnectionFactory(
            createFailoverClientConnectionFactory()
        );
        tcpOutboundGateway.setRemoteTimeout(this.tcpTimeoutRemoteGateway);
        tcpOutboundGateway.setRequestTimeout(this.tcpTimeoutRequestGateway);
        return tcpOutboundGateway;
    }

    private FailoverClientConnectionFactory createFailoverClientConnectionFactory() {
        var failoverClientConnectionFactory = new FailoverClientConnectionFactory(
            List.of(
                createTcpNioClientConnectionFactory()
            )
        );
        failoverClientConnectionFactory.setRefreshSharedInterval(this.failoverRefreshSharedInterval);
        failoverClientConnectionFactory.setCloseOnRefresh(true);

        return failoverClientConnectionFactory;
    }

    @Nonnull
    private TcpNioClientConnectionFactory createTcpNioClientConnectionFactory() {
        var tcpNioClientConnectionFactory = new TcpNioClientConnectionFactory(this.deviceHost, this.devicePort);
        tcpNioClientConnectionFactory.setSingleUse(false);
        tcpNioClientConnectionFactory.setUsingDirectBuffers(true);
        tcpNioClientConnectionFactory.setSoKeepAlive(true);
        var msgSerializer = new ByteArrayLengthHeaderSerializer(2);
        msgSerializer.setMaxMessageSize(34000);
        tcpNioClientConnectionFactory.setSerializer(msgSerializer);
        tcpNioClientConnectionFactory.setDeserializer(msgSerializer);
        tcpNioClientConnectionFactory.setConnectTimeout(this.tcpTimeoutCConnect/1000); // Milliseconds to seconds
        tcpNioClientConnectionFactory.setApplicationEventPublisher(this.applicationEventPublisher);
        return tcpNioClientConnectionFactory;
    }
}
