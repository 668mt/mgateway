package mt.spring.mgateway.entity;

import lombok.Data;

import java.util.List;

@Data
public class Config {
	private List<String> hostnames;
	private String path;
	private Proxy proxy;
}