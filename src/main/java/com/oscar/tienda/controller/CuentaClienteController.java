package com.oscar.tienda.controller;

import com.oscar.tienda.dto.*;
import com.oscar.tienda.service.CuentaClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes/{idCliente}")
@RequiredArgsConstructor
public class CuentaClienteController {

    private final CuentaClienteService cuentaClienteService;

    @PostMapping("/pagos")
    @ResponseStatus(HttpStatus.CREATED)
    public PagoResponseDTO registrarPago(@PathVariable Long idCliente,
                                         @Valid @RequestBody PagoRequestDTO dto) {
        return cuentaClienteService.registrarPago(idCliente, dto);
    }

    @GetMapping("/saldo")
    public SaldoClienteDTO saldo(@PathVariable Long idCliente) {
        return cuentaClienteService.obtenerSaldo(idCliente);
    }

    @GetMapping("/estado-cuenta")
    public EstadoCuentaDTO estadoCuenta(@PathVariable Long idCliente) {
        return cuentaClienteService.estadoCuenta(idCliente);
    }
}