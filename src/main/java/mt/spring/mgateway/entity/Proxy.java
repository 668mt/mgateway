package mt.spring.mgateway.entity;

import lombok.Data;

import java.util.List;

@Data
public class Proxy {
	private String location;
	private List<Upstream> upstreams;
	private HealthCheck healthCheck;
}