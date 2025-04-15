package net.samitkumar.spring_oauth2_resourceserver_websocket;

import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserMessage;
import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpringOauth2ResourceserverWebsocketApplicationTests {

	@Autowired
	UserMessageRepository userMessageRepository;
	@Autowired
	MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("Test DB schema")
	@Order(1)
	//@SneakyThrows
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
						.forEach(System.out::println),
				() -> {
					var count = userMessageRepository.countUnreadMessage(1L, 2L);
					System.out.println("##Count:## " + count);
				}
		);

	}

	@Test
	@DisplayName("Test API endpoint")
	@Order(2)
	void apiEndpointTest() throws Exception {
		assertAll(
				() -> mockMvc.perform(MockMvcRequestBuilders
								.post("/message")
								.with(SecurityMockMvcRequestPostProcessors
										.jwt()
										.jwt(jwt -> jwt.claims(claims -> {
											claims.put("sub", "Antonette");
											claims.put("authorities", List.of("ROLE_USER"));
										}))
								)
								.contentType("application/json")
								.content("""
								{
									"receiverId": 1,
									"content": "Hello from api"
								}
								""")
								.accept("application/json")
						)
						.andExpect(MockMvcResultMatchers.status().isOk())
						.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
				/*.andExpect(MockMvcResultMatchers.content().json("""
					{"id":1,"senderId":1,"receiverId":2,"content":"Hello from api","createdAt":null,"isRead":null}
				"""))*/
				,
				() -> mockMvc.perform(MockMvcRequestBuilders
								.get("/message/conversation/2")
								.with(SecurityMockMvcRequestPostProcessors
										.jwt()
										.jwt(jwt -> jwt.claims(claims -> {
											claims.put("sub", "Bret");
											claims.put("authorities", List.of("ROLE_USER"));
										}))
								)
								.accept("application/json")
						)
						.andExpect(MockMvcResultMatchers.status().isOk())
						/*.andExpect(MockMvcResultMatchers.content().json("""
							[
								{"id":2,"senderId":1,"receiverId":2,"content":"How are you?","createdAt":"2025-04-11T09:30:21.550019","isRead":false},
								{"id":3,"senderId":2,"receiverId":1,"content":"I am fine","createdAt":"2025-04-11T09:30:21.550019","isRead":false},
								{"id":1,"senderId":1,"receiverId":2,"content":"Hello","createdAt":"2025-04-11T09:30:21.550019","isRead":true},
								{"id":4,"senderId":1,"receiverId":2,"content":"Hello from api","createdAt":"2025-04-11T09:30:21.674023","isRead":false}
							]
						"""))*/
				,
				() -> mockMvc.perform(MockMvcRequestBuilders
								.get("/message/conversation/2/unread")
								.with(SecurityMockMvcRequestPostProcessors
										.jwt()
										.jwt(jwt -> jwt.claims(claims -> {
											claims.put("sub", "Bret");
											claims.put("authorities", List.of("ROLE_USER"));
										}))
								)
								.accept("application/json")
						)
						.andExpect(MockMvcResultMatchers.status().isOk())
						/*.andExpect(MockMvcResultMatchers.content().json("""
						"""))*/
		);

	}

}
