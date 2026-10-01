package com.oscar.tienda.controller;

import com.oscar.tienda.repository.UsuarioRepository;
import com.oscar.tienda.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired UsuarioService usuarioService;

    @BeforeEach
    void usuarioDePrueba() {
        usuarioRepository.deleteAll();
        usuarioService.registrarUsuario("dueno", "clave-segura-1");
    }

    private static String login(String user, String pass) {
        return "{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}";
    }

    private MockHttpSession iniciarSesion() throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(login("dueno", "clave-segura-1")))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }

    @Test
    void sinSesionLaApiDevuelve401() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void conLoginCorrectoLaSesionDaAccesoALaApi() throws Exception {
        MockHttpSession sesion = iniciarSesion();

        mvc.perform(get("/api/productos").session(sesion)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("dueno"));
    }

    @Test
    void contrasenaIncorrectaDevuelve401ConMensajeUnico() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(login("dueno", "mala")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario o contraseña incorrectos"));

        // un usuario inexistente responde exactamente igual (no revela si existe)
        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(login("fantasma", "mala")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void unPostSinTokenCsrfSeRechaza() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login("dueno", "clave-segura-1")))
                .andExpect(status().isForbidden());
    }

    @Test
    void cambiarPasswordConLaActualIncorrectaDevuelve400() throws Exception {
        MockHttpSession sesion = iniciarSesion();

        mvc.perform(post("/api/auth/cambiar-password").session(sesion).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"x\",\"newPassword\":\"otra-clave-123\"}"))
                .andExpect(status().isBadRequest());
    }
}