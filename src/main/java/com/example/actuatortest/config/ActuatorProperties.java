package com.example.actuatortest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Actuator endpoints.
 * 
 * This class mirrors Spring Boot's management configuration structure
 * and provides type-safe configuration for actuator settings.
 */
@ConfigurationProperties(prefix = "management")
public class ActuatorProperties {

    private Endpoints endpoints = new Endpoints();

    public Endpoints getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoints endpoints) {
        this.endpoints = endpoints;
    }

    public static class Endpoints {
        private boolean enabledByDefault = false;
        private Endpoint endpoint = new Endpoint();
        private Web web = new Web();

        public boolean isEnabledByDefault() {
            return enabledByDefault;
        }

        public void setEnabledByDefault(boolean enabledByDefault) {
            this.enabledByDefault = enabledByDefault;
        }

        public Endpoint getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        public Web getWeb() {
            return web;
        }

        public void setWeb(Web web) {
            this.web = web;
        }

        public static class Endpoint {
            private Health health = new Health();

            public Health getHealth() {
                return health;
            }

            public void setHealth(Health health) {
                this.health = health;
            }

            public static class Health {
                private boolean enabled = true;

                public boolean isEnabled() {
                    return enabled;
                }

                public void setEnabled(boolean enabled) {
                    this.enabled = enabled;
                }
            }
        }

        public static class Web {
            private String exposure = "";

            public String getExposure() {
                return exposure;
            }

            public void setExposure(String exposure) {
                this.exposure = exposure;
            }
        }
    }
}
