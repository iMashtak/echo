package io.github.imashtak.echo.spring.example;

import io.github.imashtak.echo.spring.example.controller.Controller;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest(Controller.class)
@ComponentScan("io.github.imashtak.echo.spring")
class SpringExampleApplicationTests {

	@Test
	void contextLoads() {
	}

    @Autowired
    private WebTestClient webClient;

    @Test
    @SneakyThrows
    void testController() {
        var result = webClient.post().uri("/post?second=some")
            .exchange()
            .expectStatus().isOk()
            .expectBody().returnResult();

        var body = result.getResponseBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("ok", new String(body));
    }

}
