package com.oscar.tienda.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void crearProductoSinNombreDevuelve400ConElCampo() throws Exception {
        mvc.perform(post("/api/productos")
                        .with(user("admin")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"","precio":10.00,"codigoBarras":"777"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").exists());
    }

    @Test
    void escanearUnCodigoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/productos/codigo/no-existe").with(user("admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}