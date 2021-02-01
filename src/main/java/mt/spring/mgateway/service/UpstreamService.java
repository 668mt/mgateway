package mt.spring.mgateway.service;

import lombok.extern.slf4j.Slf4j;
import mt.spring.mgateway.entity.*;
import mt.spring.mgateway.utils.HttpClientServletUtils;
import mt.spring.mgateway.utils.RegexUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author Martin
 * @Date 2021/1/30
 */
@Service
@Slf4j
public class UpstreamService {
	
	@Autowired
	private GatewayConfigProperties gatewayConfigProperties;
	@Autowired
	private CloseableHttpClient closeableHttpClient;
	@Autowired
	@Qualifier("healthCheckRestTemplate")
	private RestTemplate restTemplate;
	private final Map<String, List<Upstream>> aliveUpstreams = new ConcurrentHashMap<>();
	
	static {
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
	}
	
	@Scheduled(fixedDelay = 10_000)
	public void healthCheck() {
		List<Config> configs = gatewayConfigProperties.getConfigs();
		if (configs == null) {
			return;
		}
		configs.stream()
				.filter(config -> config.getProxy() != null && StringUtils.isNotBlank(config.getPath()))
				.forEach(config -> {
					String key = getKey(config);
					Proxy proxy = config.getProxy();
					List<Upstream> aliveUpstreams = proxy.getUpstreams().stream()
							.filter(upstream -> isHealthUp(upstream, proxy.getHealthCheck()))
							.collect(Collectors.toList());
					this.aliveUpstreams.put(key, aliveUpstreams);
				});
	}
	
	private String getKey(Config config) {
		StringBuilder key = new StringBuilder(config.getPath());
		List<String> hostnames = config.getHostnames();
		if (hostnames != null) {
			for (String hostname : hostnames) {
				key.append(":").append(hostname);
			}
		}
		return key.toString();
	}
	
	private boolean isHealthUp(Upstream upstream, HealthCheck healthCheck) {
		if (healthCheck == null) {
			return true;
		}
		String host = upstream.getHost();
		Map<String, String> requestHeaders = replaceVariables(upstream.getAddRequestHeaders(), upstream.getReplaceRequestHeaders(), null);
		String path = healthCheck.getPath();
		String healthUrl = host + path;
		try {
			HttpHeaders httpHeaders = new HttpHeaders();
			if (requestHeaders != null) {
				for (Map.Entry<String, String> stringStringEntry : requestHeaders.entrySet()) {
					httpHeaders.add(stringStringEntry.getKey(), stringStringEntry.getValue());
				}
			}
			ResponseEntity<String> exchange = restTemplate.exchange(healthUrl, HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
			int statusCodeValue = exchange.getStatusCodeValue();
			List<Integer> status = healthCheck.getStatus();
			for (Integer code : status) {
				if (code.equals(statusCodeValue)) {
					return true;
				}
			}
		} catch (Exception ignored) {
		}
		return false;
	}
	
	public void forward(Config config, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String key = getKey(config);
		List<Upstream> upstreams = aliveUpstreams.get(key);
		if (CollectionUtils.isEmpty(upstreams)) {
			writeBadGatewayException(response);
			return;
		}
		String path = config.getPath();
		String requestURI = request.getRequestURI();
		String location = config.getProxy().getLocation();
		if (location == null) {
			location = "";
		} else {
			if (!location.startsWith("/")) {
				location = "/" + location;
			}
			if (location.endsWith("/")) {
				location = location.substring(0, location.length() - 1);
			}
		}
		Upstream upstream = chooseUpstream(upstreams);
		String host = upstream.getHost();
		if (host.endsWith("/")) {
			host = host.substring(0, host.length() - 1);
		}
		requestURI = requestURI.substring(path.length());
		if (!requestURI.startsWith("/")) {
			requestURI = "/" + requestURI;
		}
		String targetUrl = host + location + requestURI;
		Map<String, String> requestHeaders = replaceVariables(upstream.getAddRequestHeaders(), upstream.getReplaceRequestHeaders(), name -> {
			String[] names = name.split("\\.");
			Assert.state(names.length >= 1, "name的格式不正确");
			return request.getHeader(names[0]);
		});
		HttpClientServletUtils.forward(closeableHttpClient, targetUrl, request, response, response.getOutputStream(), requestHeaders, null, (closeableHttpResponse, response1) -> {
			Map<String, String> responseHeaders = replaceVariables(upstream.getAddResponseHeaders(), upstream.getReplaceResponseHeaders(), name -> {
				String[] names = name.split("\\.");
				if (names.length == 1) {
					return response1.getHeader(names[0]);
				} else if (names.length == 2) {
					if ("request".equals(names[0])) {
						return request.getHeader(names[1]);
					} else {
						return response.getHeader(names[1]);
					}
				} else {
					throw new IllegalStateException("name的格式不正确");
				}
			});
			if (responseHeaders == null) {
				return;
			}
			for (Map.Entry<String, String> stringStringEntry : responseHeaders.entrySet()) {
				response1.setHeader(stringStringEntry.getKey(), stringStringEntry.getValue());
			}
		});
	}
	
	interface GetHeader {
		String get(String name);
	}
	
	private String replace(String value, GetHeader getHeader) {
		List<String[]> list = RegexUtils.findList(value, "\\{(.+?)\\}", new Integer[]{0, 1});
		if (list != null) {
			for (String[] group : list) {
				String header = getHeader != null ? getHeader.get(group[1]) : null;
				if (header == null) {
					header = "null";
				}
				value = value.replace(group[0], header);
			}
		}
		return value;
	}
	
	public Map<String, String> replaceVariables(Map<String, String> addHeaders, Map<String, Upstream.ReplaceInfo> replaceHeaders, @Nullable GetHeader getHeader) {
		Map<String, String> newHeaders = new HashMap<>();
		if (addHeaders != null) {
			for (Map.Entry<String, String> stringStringEntry : addHeaders.entrySet()) {
				String value = stringStringEntry.getValue();
				if (value == null) {
					continue;
				}
				value = replace(value, getHeader);
				newHeaders.put(stringStringEntry.getKey(), value);
			}
		}
		if (replaceHeaders != null) {
			for (Map.Entry<String, Upstream.ReplaceInfo> stringReplaceInfoEntry : replaceHeaders.entrySet()) {
				String name = stringReplaceInfoEntry.getKey();
				Upstream.ReplaceInfo info = stringReplaceInfoEntry.getValue();
				Assert.notNull(info.getReplace(), "replace 不能为空");
				Assert.notNull(info.getAs(), "as 不能为空");
				String value = getHeader != null ? getHeader.get(name) : null;
				if (value == null) {
					continue;
				}
				value = value.replace(replace(info.getReplace(), getHeader), replace(info.getAs(), getHeader));
				newHeaders.put(name, value);
			}
		}
		return newHeaders;
	}
	
	public Upstream chooseUpstream(List<Upstream> upstreams) {
		WeightAlgorithm<Upstream> weightAbleWeightAlgorithm = new WeightAlgorithm<>(upstreams);
		return weightAbleWeightAlgorithm.weightRandom();
	}
	
	public void writeNotFoundException(HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
		try {
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write("找不到资源");
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void writeBadGatewayException(HttpServletResponse response) {
		response.setStatus(HttpStatus.BAD_GATEWAY.value());
		try {
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write("网关异常");
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}
