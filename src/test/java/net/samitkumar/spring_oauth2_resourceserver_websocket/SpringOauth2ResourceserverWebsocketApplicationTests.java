package net.samitkumar.spring_oauth2_resourceserver_websocket;

import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserMessage;
import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserMessageRepository;
import org.junit.experimental.results.ResultMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SpringOauth2ResourceserverWebsocketApplicationTests {

	@Autowired
	UserMessageRepository userMessageRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@WithMockUser
	void dbSchemaTest() {
		assertAll(
				() -> userMessageRepository
						.saveAll(
								List.of(
										new UserMessage(null, 1L, 2L, "Hello", null, null),
										new UserMessage(null, 1L, 2L, "How are you?", null, null),
										new UserMessage(null, 2L, 1L, "I am fine", null, null)
								)
						),
				() -> userMessageRepository
						.findAll()
						.forEach(System.out::println),
				() -> userMessageRepository
						.markMessageAsRead(1L),
				() -> userMessageRepository
						.findAll()
						.forEach(System.out::println),
				() -> userMessageRepository
						.findMessagesBetweenUsers(1L, 2L)
						.forEach(System.out::println)
		);

	}

	@Test
	@WithMockUser
	void apiEndpointTest(MockMvc mockMvc) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.post("/message")
						.contentType("application/json")
						.content("""
								{
									"senderId": 1,
									"receiverId": 2,
									"content": "Hello"
								}
								""").accept("application/json"));
	}

}
