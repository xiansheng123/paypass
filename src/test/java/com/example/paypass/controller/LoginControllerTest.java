package com.example.paypass.controller;

import com.example.paypass.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Test
    public void authenticateLogin() throws Exception {
        String content = "  {\n" +
                "           \"userName\":\"456\",\n" +
                "            \"password\":\"12134\"\n" +
                "            }";

        mvc.perform(
                post("http://localhost:8080/paypass/login")
                        .contentType(MediaType.APPLICATION_JSON).content(content)
        );
    }
}