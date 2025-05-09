package net.samitkumar.spring_oauth2_resourceserver_websocket;

import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.List;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                .withInitScript("db/schema.sql");
    }

    @Bean
    @ServiceConnection(name = "redis")
    RedisContainer redisContainer() {
        return new RedisContainer("redis:latest")
                .withExposedPorts(6379);
    }

    @Bean
    @ServiceConnection(name = "rabbitmq")
    RabbitMQContainer rabbitMQContainer() {
        return new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.7.25-management-alpine"))
                .withEnv("RABBITMQ_DEFAULT_USER", "guest")
                .withEnv("RABBITMQ_DEFAULT_PASS", "guest")
                //rabbitmq_federation_management,rabbitmq_management,rabbitmq_mqtt,rabbitmq_stomp,rabbitmq_web_stomp
                .withPluginsEnabled("rabbitmq_federation_management")
                .withPluginsEnabled("rabbitmq_management")
                .withPluginsEnabled("rabbitmq_mqtt")
                .withPluginsEnabled("rabbitmq_stomp")
                .withPluginsEnabled("rabbitmq_web_stomp")
                /*- "5672:5672"
                - "15672:15672" #Admin console access
                - "15674:15674" #rabbit_web_stomp
                - "61613:61613" # STOMP
                - "1883:1883" #MQTT*/
                .withExposedPorts(5672, 15672, 15674, 61613, 1883);
                /*.setPortBindings(List.of(
                        "5672:5672",
                        "15672:15672",
                        "15674:15674",
                        "61613:61613",
                        "1883:1883"));*/


    }

    // How to get the RabbitMq connection string and inject it into the application.properties file
    @DynamicPropertySource
    static void rabbitMQProperties(DynamicPropertyRegistry registry, RabbitMQContainer rabbitMQContainer) {
        //RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.7.25-management-alpine"));
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        //registry.add("spring.rabbitmq.port", rabbitMQContainer::getHttpPort);
        registry.add("spring.rabbitmq.relay.host", rabbitMQContainer::getHost);
        //registry.add("spring.rabbitmq.relay.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.relay.username", () -> "guest");
        registry.add("spring.rabbitmq.relay.password", () -> "guest");
    }

}
