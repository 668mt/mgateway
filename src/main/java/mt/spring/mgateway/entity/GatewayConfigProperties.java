package mt.spring.mgateway.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author Martin
 * @Date 2021/1/30
 */
@Data
@ConfigurationProperties(prefix = "gateway.route")
@Component
public class GatewayConfigProperties {
	private List<Config> configs;
	
}
