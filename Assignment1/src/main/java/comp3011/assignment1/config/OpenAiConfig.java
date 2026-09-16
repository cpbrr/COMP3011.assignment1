package comp3011.assignment1.config;

import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAiConfig {

    private final String baseUrl;
    private final String apiKey;
    private final Duration connectTimeout;
    private final Duration readTimeout;

    public OpenAiConfig(
            @Value("${openai.api.base-url}") String baseUrl,
            // obtain api key from runtime env
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.api.connect-timeout}") Duration connectTimeout,
            @Value("${openai.api.read-timeout}") Duration readTimeout) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Bean
    RestClient openAiRestClient(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                // use the default proxy for stt api
                .proxy(ProxySelector.getDefault())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }
}
