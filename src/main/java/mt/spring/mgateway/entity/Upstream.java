package mt.spring.mgateway.entity;

import lombok.Data;
import mt.spring.mgateway.service.WeightAble;

import java.util.Map;

@Data
public class Upstream implements WeightAble {
	private String host;
	private Integer weight;
	private Map<String, String> addRequestHeaders;
	private Map<String, ReplaceInfo> replaceRequestHeaders;
	private Map<String, String> addResponseHeaders;
	private Map<String, ReplaceInfo> replaceResponseHeaders;
	
	@Data
	public static class ReplaceInfo {
		public ReplaceInfo(String replace, String as) {
			this.replace = replace;
			this.as = as;
		}
		
		public ReplaceInfo() {
		}
		
		private String replace;
		private String as;
	}
}