//package mt.spring.mgateway.utils;
//
//import com.netflix.util.Pair;
//import com.netflix.zuul.constants.ZuulHeaders;
//import com.netflix.zuul.context.RequestContext;
//import com.netflix.zuul.util.HTTPRequestUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.http.Header;
//import org.apache.http.HttpHost;
//import org.apache.http.HttpRequest;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPatch;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.methods.HttpPut;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.InputStreamEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.message.BasicHeader;
//import org.apache.http.message.BasicHttpEntityEnclosingRequest;
//import org.apache.http.message.BasicHttpRequest;
//import org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter;
//import org.springframework.http.HttpHeaders;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.util.StringUtils;
//import org.springframework.web.util.UriTemplate;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.*;
//import java.net.URL;
//import java.util.*;
//import java.util.regex.Pattern;
//import java.util.zip.GZIPInputStream;
//
//import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTING_DEBUG_KEY;
//import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_ZUUL_DEBUG_HEADER;
//
///**
// * @Author Martin
// * @Date 2021/1/30
// */
//@Slf4j
//public class HttpClientUtils {
//	public static boolean useServlet31 = true;
//	/**
//	 * Form feed pattern.
//	 */
//	public static final Pattern FORM_FEED_PATTERN = Pattern.compile("\f");
//
//	/**
//	 * Colon pattern.
//	 */
//	public static final Pattern COLON_PATTERN = Pattern.compile(":");
//
//	private static final Pattern MULTIPLE_SLASH_PATTERN = Pattern.compile("/{2,}");
//	private static final boolean forceOriginalQueryStringEncoding = false;
//	private static ThreadLocal<byte[]> buffers;
//
//	public void forward(CloseableHttpClient httpClient, HttpServletRequest request, HttpServletResponse response) throws IOException {
//		MultiValueMap<String, String> headers = buildZuulRequestHeaders(request);
//		MultiValueMap<String, String> params = HttpClientServletUtils.getQueryParams(request);
//		String verb = request.getMethod().toUpperCase();
//		InputStream requestEntity = request.getInputStream();
////		if (getContentLength(request) < 0) {
////			context.setChunkedRequestBody();
////		}
//
//		String uri = buildZuulRequestURI(request);
//
//		try {
//			CloseableHttpResponse closeableHttpResponse = forward(httpClient, verb, uri, request, headers, params, requestEntity);
//			setResponse(closeableHttpResponse);
//			addResponseHeaders(closeableHttpResponse, response);
//			writeResponse();
//		} catch (Exception ex) {
//			throw new RuntimeException(ex);
//		}
//	}
//
//
//	private void setResponse(HttpResponse response) throws IOException {
//		setResponse(response.getStatusLine().getStatusCode(),
//				response.getEntity() == null ? null : response.getEntity().getContent(),
//				revertHeaders(response.getAllHeaders()));
//	}
//
//	public void setResponse(int status, InputStream entity,
//							MultiValueMap<String, String> headers) throws IOException {
//		RequestContext context = RequestContext.getCurrentContext();
//		context.setResponseStatusCode(status);
//		if (entity != null) {
//			context.setResponseDataStream(entity);
//		}
//
//		boolean isOriginResponseGzipped = false;
//		for (Map.Entry<String, List<String>> header : headers.entrySet()) {
//			String name = header.getKey();
//			for (String value : header.getValue()) {
//				context.addOriginResponseHeader(name, value);
//
//				if (name.equalsIgnoreCase(HttpHeaders.CONTENT_ENCODING)
//						&& HTTPRequestUtils.getInstance().isGzipped(value)) {
//					isOriginResponseGzipped = true;
//				}
//				if (name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
//					context.setOriginContentLength(value);
//				}
//				if (isIncludedHeader(name)) {
//					context.addZuulResponseHeader(name, value);
//				}
//			}
//		}
//		context.setResponseGZipped(isOriginResponseGzipped);
//	}
//
//	protected long getContentLength(HttpServletRequest request) {
//		if (useServlet31) {
//			return request.getContentLengthLong();
//		}
//		String contentLengthHeader = request.getHeader(HttpHeaders.CONTENT_LENGTH);
//		if (contentLengthHeader != null) {
//			try {
//				return Long.parseLong(contentLengthHeader);
//			} catch (NumberFormatException e) {
//			}
//		}
//		return request.getContentLength();
//	}
//
//	public MultiValueMap<String, String> buildZuulRequestHeaders(HttpServletRequest request) {
//		RequestContext context = RequestContext.getCurrentContext();
//		MultiValueMap<String, String> headers = new HttpHeaders();
//		Enumeration<String> headerNames = request.getHeaderNames();
//		if (headerNames != null) {
//			while (headerNames.hasMoreElements()) {
//				String name = headerNames.nextElement();
//				if (isIncludedHeader(name)) {
//					Enumeration<String> values = request.getHeaders(name);
//					while (values.hasMoreElements()) {
//						String value = values.nextElement();
//						headers.add(name, value);
//					}
//				}
//			}
//		}
//		Map<String, String> zuulRequestHeaders = context.getZuulRequestHeaders();
//		for (String header : zuulRequestHeaders.keySet()) {
//			if (isIncludedHeader(header)) {
//				headers.set(header, zuulRequestHeaders.get(header));
//			}
//		}
//		if (!headers.containsKey(HttpHeaders.ACCEPT_ENCODING)) {
//			headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip");
//		}
//		return headers;
//	}
//
//	public boolean isIncludedHeader(String headerName) {
//		String name = headerName.toLowerCase();
//		switch (name) {
//			case "host":
//				return true;
//			case "connection":
//			case "content-length":
//			case "server":
//			case "transfer-encoding":
//			case "x-application-context":
//				return false;
//			default:
//				return true;
//		}
//	}
//
//	public String buildZuulRequestURI(HttpServletRequest request) {
//		return request.getRequestURI();
//	}
//
//	private CloseableHttpResponse forward(CloseableHttpClient httpclient, String verb,
//										  String uri, HttpServletRequest request, MultiValueMap<String, String> headers,
//										  MultiValueMap<String, String> params, InputStream requestEntity)
//			throws Exception {
//		URL host = RequestContext.getCurrentContext().getRouteHost();
//		HttpHost httpHost = getHttpHost(host);
//		uri = StringUtils.cleanPath(MULTIPLE_SLASH_PATTERN.matcher(host.getPath() + uri).replaceAll("/"));
//		long contentLength = getContentLength(request);
//
//		ContentType contentType = null;
//
//		if (request.getContentType() != null) {
//			contentType = ContentType.parse(request.getContentType());
//		}
//
//		InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength,
//				contentType);
//
//		HttpRequest httpRequest = buildHttpRequest(verb, uri, entity, headers, params,
//				request);
//		log.debug(httpHost.getHostName() + " " + httpHost.getPort() + " "
//				+ httpHost.getSchemeName());
//		return forwardRequest(httpclient, httpHost,
//				httpRequest);
//	}
//
//	public String getQueryString(MultiValueMap<String, String> params) {
//		if (params.isEmpty()) {
//			return "";
//		}
//		StringBuilder query = new StringBuilder();
//		Map<String, Object> singles = new HashMap<>();
//		for (String param : params.keySet()) {
//			int i = 0;
//			for (String value : params.get(param)) {
//				query.append("&");
//				query.append(param);
//				if (!"".equals(value)) { // don't add =, if original is ?wsdl, output is
//					// not ?wsdl=
//					String key = param;
//					// if form feed is already part of param name double
//					// since form feed is used as the colon replacement below
//					if (key.contains("\f")) {
//						key = (FORM_FEED_PATTERN.matcher(key).replaceAll("\f\f"));
//					}
//					// colon is special to UriTemplate
//					if (key.contains(":")) {
//						key = COLON_PATTERN.matcher(key).replaceAll("\f");
//					}
//					key = key + i;
//					singles.put(key, value);
//					query.append("={");
//					query.append(key);
//					query.append("}");
//				}
//				i++;
//			}
//		}
//
//		UriTemplate template = new UriTemplate("?" + query.toString().substring(1));
//		return template.expand(singles).toString();
//	}
//
//	protected HttpRequest buildHttpRequest(String verb, String uri,
//										   InputStreamEntity entity, MultiValueMap<String, String> headers,
//										   MultiValueMap<String, String> params, HttpServletRequest request) {
//		HttpRequest httpRequest;
//		String uriWithQueryString = uri + (forceOriginalQueryStringEncoding
//				? getEncodedQueryString(request) : getQueryString(params));
//
//		switch (verb.toUpperCase()) {
//			case "POST":
//				HttpPost httpPost = new HttpPost(uriWithQueryString);
//				httpRequest = httpPost;
//				httpPost.setEntity(entity);
//				break;
//			case "PUT":
//				HttpPut httpPut = new HttpPut(uriWithQueryString);
//				httpRequest = httpPut;
//				httpPut.setEntity(entity);
//				break;
//			case "PATCH":
//				HttpPatch httpPatch = new HttpPatch(uriWithQueryString);
//				httpRequest = httpPatch;
//				httpPatch.setEntity(entity);
//				break;
//			case "DELETE":
//				BasicHttpEntityEnclosingRequest entityRequest = new BasicHttpEntityEnclosingRequest(
//						verb, uriWithQueryString);
//				httpRequest = entityRequest;
//				entityRequest.setEntity(entity);
//				break;
//			default:
//				httpRequest = new BasicHttpRequest(verb, uriWithQueryString);
//				log.debug(uriWithQueryString);
//		}
//
//		httpRequest.setHeaders(convertHeaders(headers));
//		return httpRequest;
//	}
//
//	private String getEncodedQueryString(HttpServletRequest request) {
//		String query = request.getQueryString();
//		return (query != null) ? "?" + query : "";
//	}
//
//	private MultiValueMap<String, String> revertHeaders(Header[] headers) {
//		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
//		for (Header header : headers) {
//			String name = header.getName();
//			if (!map.containsKey(name)) {
//				map.put(name, new ArrayList<String>());
//			}
//			map.get(name).add(header.getValue());
//		}
//		return map;
//	}
//
//	private Header[] convertHeaders(MultiValueMap<String, String> headers) {
//		List<Header> list = new ArrayList<>();
//		for (String name : headers.keySet()) {
//			for (String value : headers.get(name)) {
//				list.add(new BasicHeader(name, value));
//			}
//		}
//		return list.toArray(new BasicHeader[0]);
//	}
//
//	private CloseableHttpResponse forwardRequest(CloseableHttpClient httpclient,
//												 HttpHost httpHost, HttpRequest httpRequest) throws IOException {
//		return httpclient.execute(httpHost, httpRequest);
//	}
//
//	private HttpHost getHttpHost(URL host) {
//		HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(),
//				host.getProtocol());
//		return httpHost;
//	}
//
//
//	private void writeResponse() throws Exception {
//		RequestContext context = RequestContext.getCurrentContext();
//		// there is no body to send
//		if (context.getResponseBody() == null
//				&& context.getResponseDataStream() == null) {
//			return;
//		}
//		HttpServletResponse servletResponse = context.getResponse();
//		if (servletResponse.getCharacterEncoding() == null) { // only set if not set
//			servletResponse.setCharacterEncoding("UTF-8");
//		}
//
//		String servletResponseContentEncoding = getResponseContentEncoding(context);
//		OutputStream outStream = servletResponse.getOutputStream();
//		InputStream is = null;
//		try {
//			if (context.getResponseBody() != null) {
//				String body = context.getResponseBody();
//				is = new ByteArrayInputStream(
//						body.getBytes(servletResponse.getCharacterEncoding()));
//			} else {
//				is = context.getResponseDataStream();
//				if (is != null && context.getResponseGZipped()) {
//					// if origin response is gzipped, and client has not requested gzip,
//					// decompress stream before sending to client
//					// else, stream gzip directly to client
//					if (isGzipRequested(context)) {
//						servletResponseContentEncoding = "gzip";
//					} else {
//						servletResponseContentEncoding = null;
//						is = handleGzipStream(is);
//					}
//				}
//			}
//			if (servletResponseContentEncoding != null) {
//				servletResponse.setHeader(ZuulHeaders.CONTENT_ENCODING,
//						servletResponseContentEncoding);
//			}
//
//			if (is != null) {
//				writeResponse(is, outStream);
//			}
//		} finally {
//			if (is != null) {
//				try {
//					is.close();
//				} catch (Exception ex) {
//					log.warn("Error while closing upstream input stream", ex);
//				}
//			}
//
//			// cleanup ThreadLocal when we are all done
//			if (buffers != null) {
//				buffers.remove();
//			}
//
//			try {
//				Object zuulResponse = context.get("zuulResponse");
//				if (zuulResponse instanceof Closeable) {
//					((Closeable) zuulResponse).close();
//				}
//				outStream.flush();
//				// The container will close the stream for us
//			} catch (IOException ex) {
//				log.warn("Error while sending response to client: " + ex.getMessage());
//			}
//		}
//	}
//
//	protected InputStream handleGzipStream(InputStream in) throws Exception {
//		// Record bytes read during GZip initialization to allow to rewind the stream if
//		// needed
//		//
//		RecordingInputStream stream = new RecordingInputStream(in);
//		try {
//			return new GZIPInputStream(stream);
//		} catch (java.util.zip.ZipException | java.io.EOFException ex) {
//
//			if (stream.getBytesRead() == 0) {
//				// stream was empty, return the original "empty" stream
//				return in;
//			} else {
//				// reset the stream and assume an unencoded response
//				log.warn(
//						"gzip response expected but failed to read gzip headers, assuming unencoded response for request "
//								+ RequestContext.getCurrentContext().getRequest()
//								.getRequestURL().toString());
//
//				stream.reset();
//				return stream;
//			}
//		} finally {
//			stream.stopRecording();
//		}
//	}
//
//	protected boolean isGzipRequested(RequestContext context) {
//		final String requestEncoding = context.getRequest()
//				.getHeader(ZuulHeaders.ACCEPT_ENCODING);
//
//		return requestEncoding != null
//				&& HTTPRequestUtils.getInstance().isGzipped(requestEncoding);
//	}
//
//	private String getResponseContentEncoding(RequestContext context) {
//		List<Pair<String, String>> zuulResponseHeaders = context.getZuulResponseHeaders();
//		if (zuulResponseHeaders != null) {
//			for (Pair<String, String> it : zuulResponseHeaders) {
//				if (ZuulHeaders.CONTENT_ENCODING.equalsIgnoreCase(it.first())) {
//					return it.second();
//				}
//			}
//		}
//		return null;
//	}
//
//	private void writeResponse(InputStream zin, OutputStream out) throws Exception {
//		byte[] bytes = buffers.get();
//		int bytesRead = -1;
//		while ((bytesRead = zin.read(bytes)) != -1) {
//			out.write(bytes, 0, bytesRead);
//		}
//	}
//
//	private void addResponseHeaders(CloseableHttpResponse closeableHttpResponse, HttpServletResponse response) {
//		Header[] allHeaders = closeableHttpResponse.getAllHeaders();
//		for (Header allHeader : allHeaders) {
//			if (!"Content-Encoding".equalsIgnoreCase(allHeader.getName())) {
//				response.addHeader(allHeader.getName(), allHeader.getValue());
//			}
//		}
//	}
//
//	private boolean isLongSafe(long value) {
//		return value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE;
//	}
//
//	private static boolean isSetContentLength = false;
//
//	protected boolean includeContentLengthHeader(RequestContext context) {
//		// Not configured to forward the header
//		if (!isSetContentLength) {
//			return false;
//		}
//
//		// Only if Content-Length is provided
//		if (context.getOriginContentLength() == null) {
//			return false;
//		}
//
//		// If response is compressed, include header only if we are not about to
//		// decompress it
//		if (context.getResponseGZipped()) {
//			return context.isGzipRequested();
//		}
//
//		// Forward it in all other cases
//		return true;
//	}
//
//	/**
//	 * InputStream recording bytes read to allow for a reset() until recording is stopped.
//	 */
//	private static class RecordingInputStream extends InputStream {
//
//		private InputStream delegate;
//
//		private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//
//		RecordingInputStream(InputStream delegate) {
//			super();
//			this.delegate = Objects.requireNonNull(delegate);
//		}
//
//		@Override
//		public int read() throws IOException {
//			int read = delegate.read();
//
//			if (buffer != null && read != -1) {
//				buffer.write(read);
//			}
//
//			return read;
//		}
//
//		@Override
//		public int read(byte[] b, int off, int len) throws IOException {
//			int read = delegate.read(b, off, len);
//
//			if (buffer != null && read != -1) {
//				buffer.write(b, off, read);
//			}
//
//			return read;
//		}
//
//		@Override
//		public void reset() {
//			if (buffer == null) {
//				throw new IllegalStateException("Stream is not recording");
//			}
//
//			this.delegate = new SequenceInputStream(
//					new ByteArrayInputStream(buffer.toByteArray()), delegate);
//			this.buffer = new ByteArrayOutputStream();
//		}
//
//		public int getBytesRead() {
//			return (buffer == null) ? -1 : buffer.size();
//		}
//
//		public void stopRecording() {
//			this.buffer = null;
//		}
//
//		@Override
//		public void close() throws IOException {
//			this.delegate.close();
//		}
//
//	}
//}
