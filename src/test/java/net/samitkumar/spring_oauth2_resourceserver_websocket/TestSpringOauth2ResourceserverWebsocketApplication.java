package net.samitkumar.spring_oauth2_resourceserver_websocket;

import org.springframework.boot.SpringApplication;

public class TestSpringOauth2ResourceserverWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringOauth2ResourceserverWebsocketApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
