package mt.spring.mgateway.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * @Author Martin
 * @Date 2021/1/30
 */
@Configuration
public class RestTemplateConfiguration {
	@Bean(name = "healthCheckRestTemplate")
	public RestTemplate healthCheckRestTemplate() {
		SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new NoRedirectClientHttpRequestFactory();
		simpleClientHttpRequestFactory.setConnectTimeout(2000);
		simpleClientHttpRequestFactory.setReadTimeout(8000);
		RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
		restTemplate.setErrorHandler(new ResponseErrorHandler() {
			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				return false;
			}
			
			@Override
			public void handleError(ClientHttpResponse response) throws IOException {
			}
		});
		return restTemplate;
	}
	
	public static class NoRedirectClientHttpRequestFactory extends SimpleClientHttpRequestFactory {
		
		@Override
		protected void prepareConnection(@NotNull HttpURLConnection connection,
										 @NotNull String httpMethod) throws IOException {
			super.prepareConnection(connection, httpMethod);
			connection.setInstanceFollowRedirects(false);
		}
	}
}
