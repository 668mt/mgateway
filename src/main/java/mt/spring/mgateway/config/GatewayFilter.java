package mt.spring.mgateway.config;

import mt.spring.mgateway.entity.Config;
import mt.spring.mgateway.entity.GatewayConfigProperties;
import mt.spring.mgateway.service.UpstreamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Martin
 * @Date 2021/1/30
 */
@Component
public class GatewayFilter implements Filter {
	@Autowired
	private GatewayConfigProperties gatewayConfigProperties;
	@Autowired
	private UpstreamService upstreamService;
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		List<Config> configs = gatewayConfigProperties.getConfigs();
		if (!CollectionUtils.isEmpty(configs)) {
			List<Config> collect = configs.stream()
					.filter(config -> config.getProxy() != null && StringUtils.isNotBlank(config.getPath())).sorted((o1, o2) -> {
						String path1 = o1.getPath();
						String path2 = o2.getPath();
						int length1 = path1.split("/").length;
						int length2 = path2.split("/").length;
						if (length1 == length2) {
							length1 = path1.length();
							length2 = path2.length();
						}
						return length2 - length1;
					}).collect(Collectors.toList());
			for (Config config : collect) {
				if (match(request, config)) {
					try {
						upstreamService.forward(config, request, response);
						return;
					} catch (Exception e) {
						throw new IOException(e);
					}
				}
			}
		}
		upstreamService.writeNotFoundException(response);
	}
	
	public boolean match(HttpServletRequest request, Config config) {
		String requestURI = request.getRequestURI();
		if (StringUtils.isBlank(requestURI)) {
			requestURI = "/";
		}
		String path = config.getPath();
		if (!requestURI.startsWith(path)) {
			return false;
		}
		List<String> hostnames = config.getHostnames();
		if (!CollectionUtils.isEmpty(hostnames)) {
			String url = request.getRequestURL().toString();
			return hostnames.stream()
					.anyMatch(hostname -> url.matches("^http(s)?://" + hostname + ".+$"));
		} else {
			return true;
		}
	}
}
