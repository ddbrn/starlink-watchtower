package at.db.starlink.watchtower.config;

import at.db.starlink.watchtower.DeviceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    private static final String STARLINK_ROUTER_GRPC_ADDR = "192.168.1.1";
    private static final int STARLINK_ROUTER_GRPC_PORT = 9000;

    private static final String STARLINK_DISH_GRPC_ADDR = "192.168.100.1";
    private static final int STARLINK_DISH_GRPC_PORT = 9200;

    @Bean(name = "routerChannel")
    public ManagedChannel routerChannel() {
        return ManagedChannelBuilder.forAddress(STARLINK_ROUTER_GRPC_ADDR, STARLINK_ROUTER_GRPC_PORT)
                .usePlaintext()
                .build();
    }

    @Bean(name = "dishChannel")
    public ManagedChannel dishChannel() {
        return ManagedChannelBuilder.forAddress(STARLINK_DISH_GRPC_ADDR, STARLINK_DISH_GRPC_PORT)
                .usePlaintext()
                .build();
    }

    @Bean(name = "routerStub")
    public DeviceGrpc.DeviceBlockingStub routerStub(@Qualifier("routerChannel") ManagedChannel routerChannel) {
        return DeviceGrpc.newBlockingStub(routerChannel);
    }

    @Bean(name = "dishStub")
    public DeviceGrpc.DeviceBlockingStub dishStub(@Qualifier("dishChannel") ManagedChannel dishChannel) {
        return DeviceGrpc.newBlockingStub(dishChannel);
    }
}
