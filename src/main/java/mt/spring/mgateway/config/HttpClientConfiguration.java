package mt.spring.mgateway.config;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @Author Martin
 * @Date 2021/1/30
 */
@Configuration
public class HttpClientConfiguration {
	@Bean
	public HttpClientConnectionManager connectionManager(ApacheHttpClientConnectionManagerFactory connectionManagerFactory) {
		HttpClientConnectionManager connectionManager = connectionManagerFactory.newConnectionManager(
				true,
				200,
				200,
				-1, TimeUnit.MILLISECONDS,
				null);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				connectionManager.closeExpiredConnections();
			}
		}, 30000, 5000);
		return connectionManager;
	}
	
	@Bean
	public CloseableHttpClient httpClient(HttpClientConnectionManager connectionManager) {
		final RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(5000)
				.setSocketTimeout(3600000)
				.setConnectTimeout(5000)
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
		return HttpClients.custom().setDefaultRequestConfig(requestConfig)
				.setConnectionManager(connectionManager).disableRedirectHandling()
				.build();
	}
	
}
