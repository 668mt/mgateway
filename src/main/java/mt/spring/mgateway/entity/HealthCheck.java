package mt.spring.mgateway.entity;

import lombok.Data;

import java.util.List;

@Data
public class HealthCheck {
	private String path;
	private List<Integer> status;
}