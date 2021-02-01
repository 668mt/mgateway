///*
// * Copyright 2013-2019 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package mt.spring.mgateway.utils;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Collection;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.regex.Pattern;
//
//import javax.servlet.http.HttpServletRequest;
//
//import com.netflix.zuul.context.RequestContext;
//import com.netflix.zuul.util.HTTPRequestUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
//import org.springframework.cloud.netflix.zuul.util.RequestUtils;
//import org.springframework.http.HttpHeaders;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.util.UriTemplate;
//import org.springframework.web.util.UriUtils;
//import org.springframework.web.util.WebUtils;
//
//import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.REQUEST_URI_KEY;
//
///**
// * @author Dave Syer
// * @author Marcos Barbero
// * @author Spencer Gibb
// */
//public class ProxyRequestHelper {
//
//	private static final Log log = LogFactory.getLog(ProxyRequestHelper.class);
//
//	/**
//	 * Zuul context key for a collection of ignored headers for the current request.
//	 * Pre-filters can set this up as a set of lowercase strings.
//	 */
//	public static final String IGNORED_HEADERS = "ignoredHeaders";
//
//	private Set<String> ignoredHeaders = new LinkedHashSet<>();
//
//	private Set<String> sensitiveHeaders = new LinkedHashSet<>();
//
//	private Set<String> whitelistHosts = new LinkedHashSet<>();
//
//	private boolean traceRequestBody = true;
//
//
//	private boolean addHostHeader = false;
//
//	private boolean urlDecoded = true;
//
//	@Deprecated
//	// TODO Remove in 2.1.x
//	public ProxyRequestHelper() {
//	}
//
//	public ProxyRequestHelper(ZuulProperties zuulProperties) {
//		this.ignoredHeaders.addAll(zuulProperties.getIgnoredHeaders());
//		this.traceRequestBody = zuulProperties.isTraceRequestBody();
//		this.addHostHeader = zuulProperties.isAddHostHeader();
//		this.urlDecoded = zuulProperties.isDecodeUrl();
//	}
//
//	public void setWhitelistHosts(Set<String> whitelistHosts) {
//		this.whitelistHosts.addAll(whitelistHosts);
//	}
//
//	public void setSensitiveHeaders(Set<String> sensitiveHeaders) {
//		this.sensitiveHeaders.addAll(sensitiveHeaders);
//	}
//
//	@Deprecated
//	// TODO Remove in 2.1.x
//	public void setIgnoredHeaders(Set<String> ignoredHeaders) {
//		this.ignoredHeaders.addAll(ignoredHeaders);
//	}
//
//	@Deprecated
//	// TODO Remove in 2.1.x
//	public void setTraceRequestBody(boolean traceRequestBody) {
//		this.traceRequestBody = traceRequestBody;
//	}
//
//
//
//	private String characterEncoding(HttpServletRequest request) {
//		return request.getCharacterEncoding() != null ? request.getCharacterEncoding()
//				: WebUtils.DEFAULT_CHARACTER_ENCODING;
//	}
//
//
//
//
//	public Map<String, Object> debug(String verb, String uri,
//			MultiValueMap<String, String> headers, MultiValueMap<String, String> params,
//			InputStream requestEntity) throws IOException {
//		Map<String, Object> info = new LinkedHashMap<>();
//		return info;
//	}
//
//	protected boolean shouldDebugBody(RequestContext ctx) {
//		HttpServletRequest request = ctx.getRequest();
//		if (!this.traceRequestBody || ctx.isChunkedRequestBody()
//				|| RequestUtils.isZuulServletRequest()) {
//			return false;
//		}
//		if (request == null || request.getContentType() == null) {
//			return true;
//		}
//		return !request.getContentType().toLowerCase().contains("multipart");
//	}
//
//	public void appendDebug(Map<String, Object> info, int status,
//			MultiValueMap<String, String> headers) {
//	}
//
//	/**
//	 * Get url encoded query string. Pay special attention to single parameters with no
//	 * values and parameter names with colon (:) from use of UriTemplate.
//	 * @param params Un-encoded request parameters
//	 * @return url-encoded query String built from provided parameters
//	 */
//
//
//}
