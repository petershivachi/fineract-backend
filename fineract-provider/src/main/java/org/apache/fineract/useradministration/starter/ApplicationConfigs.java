package org.apache.fineract.useradministration.starter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationConfigs {

    @NestedConfigurationProperty
    CloudinaryApiConfigs cloudinaryApiConfigs;

    @NestedConfigurationProperty
    WebConfigs webConfigs;
    @Getter
    @Setter
    public static class CloudinaryApiConfigs {
        public String cloudName;
        public String apiKey;
        public String secret;
        public String apiEnvVariable;
        public String eager;

        public String uploadUrl;
        public String deleteUrl;
    }

    @Getter
    @Setter
    public static class WebConfigs{
        private int connectionTimeout;
        private int readTimeout;

    }
}
