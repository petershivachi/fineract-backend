package org.apache.fineract.useradministration.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class WebClientConfigs {

    @Autowired
    ApplicationConfigs properties;


    @Bean
    CloseableHttpClient httpClient() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(properties.webConfigs.getConnectionTimeout()))
                .setSocketTimeout(Timeout.ofSeconds(properties.webConfigs.getReadTimeout()))
                .build();
        log.info("Timeouts configured : connect => {}, socket => {}", properties.webConfigs.getConnectionTimeout(), properties.webConfigs.getReadTimeout());
        BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        cm.setConnectionConfig(connectionConfig);
        return
                HttpClientBuilder.create()
                        .setConnectionManager(cm)
                        .disableCookieManagement()
                        .build();
    }

    @Bean
    ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}
