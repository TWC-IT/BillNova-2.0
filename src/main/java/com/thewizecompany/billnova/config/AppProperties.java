package com.thewizecompany.billnova.config;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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

    @Valid
    private final Security security = new Security();
    private String timezone = "UTC";
    @NotBlank(message = "Admin Email must be configured")
    private String resetEmail;
    @NotBlank(message = "Password Reset URL must be configured")
    private String passwordResetUrl;

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
            @Size(min = 32, message = "jwt access token secret must be at least 32 characters")
            private String accessTokenSecret;

            @NotBlank(message = "Jwt refresh token secret must be configured")
            @Size(min = 32, message = "jwt refresh token secret must be at least 32 characters")
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
