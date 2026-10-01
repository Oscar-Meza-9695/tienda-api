package com.oscar.tienda.controller;

import com.oscar.tienda.dto.CambiarPasswordRequest;
import com.oscar.tienda.dto.LoginRequest;
import com.oscar.tienda.dto.LoginResponse;
import com.oscar.tienda.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dto,
                               HttpServletRequest req, HttpServletResponse res) {
        // Lanza BadCredentialsException (-> 401) si usuario o contraseña fallan
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(dto.username(), dto.password()));

        if (req.getSession(false) != null) req.changeSessionId();   // evita fijación de sesión
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        contextRepository.saveContext(ctx, req, res);
        return respuesta(auth);
    }

    @GetMapping("/me")
    public LoginResponse me(Authentication auth) {
        return respuesta(auth);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        SecurityContextHolder.clearContext();
    }

    @PostMapping("/cambiar-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@Valid @RequestBody CambiarPasswordRequest dto, Authentication auth) {
        usuarioService.cambiarPassword(auth.getName(), dto.passwordActual(), dto.newPassword());
    }

    private LoginResponse respuesta(Authentication auth) {
        String rol = auth.getAuthorities().stream().findFirst()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", "")).orElse("");
        return new LoginResponse(auth.getName(), rol);
    }
}