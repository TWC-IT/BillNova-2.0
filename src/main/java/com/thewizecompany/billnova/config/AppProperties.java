package com.thewizecompany.billnova.config;


import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Security security = new Security();
    private String timezone = "UTC";

    @Getter
    @Setter
    public static class Security {
        private final Jwt jwt = new Jwt();
        private final Cors cors = new Cors();
        private final RateLimit rateLimit = new RateLimit();

        @Getter
        @Setter
        public static class Jwt {

            @NotBlank(message = "Jwt access token secret must be configured")
            private String accessTokenSecret;

            @NotBlank(message = "Jwt refresh token secret must be configured")
            private String refreshTokenSecret;

            @Positive
            private long accessTokenExpiryMs = 900_000L; //15 minutes

            @Positive
            private long refreshTokenExpiryMs = 604_800_000L; // 7 days

        }

        @Getter
        @Setter
        public static class Cors {
            private String allowedOrigins;
            public List<String> getAllowedOrigins() {
                if(allowedOrigins == null || allowedOrigins.isBlank()) {
                    return List.of();
                }
                return List.of(allowedOrigins.split(","));
            }
        }

        @Getter
        @Setter
        public static class RateLimit {
            private int requestsPerMinute = 60;
            private int maxLoginAttempts = 10;
        }


    }

    public ZoneId getZoneId() {return ZoneId.of(this.timezone);}
}
