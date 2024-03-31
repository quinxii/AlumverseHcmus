package hcmus.alumni.apigateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class HallOfFameGatewayFilterFactory extends AbstractGatewayFilterFactory<HallOfFameGatewayFilterFactory.Config> {

    public HallOfFameGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange);
        };
    }

    public static class Config {}
}
