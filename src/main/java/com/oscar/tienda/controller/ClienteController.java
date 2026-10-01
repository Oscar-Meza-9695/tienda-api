package com.oscar.tienda.controller;

import com.oscar.tienda.dto.ClienteRequestDTO;
import com.oscar.tienda.dto.ClienteResponseDTO;
import com.oscar.tienda.dto.ProductoResponseDTO;
import com.oscar.tienda.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public List<ClienteResponseDTO> listar() {
        return clienteService.leerActivos();
    }

    @GetMapping("/inactivos")
    public List<ClienteResponseDTO> inactivos(@RequestParam(required = false) String nombre){
        return clienteService.listarInactivos(nombre);
    }

    @GetMapping("/buscar")
    public List<ClienteResponseDTO> buscar(@RequestParam String nombre) {
        return clienteService.buscarPorNombre(nombre);
    }

    @GetMapping("/{idCliente}")
    public ClienteResponseDTO obtener(@PathVariable Long idCliente) {
        return clienteService.obtener(idCliente);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponseDTO crear(@Valid @RequestBody ClienteRequestDTO dto) {
        return clienteService.crear(dto);
    }

    @PutMapping("/{idCliente}")
    public ClienteResponseDTO actualizar(@PathVariable Long idCliente,
                                         @Valid @RequestBody ClienteRequestDTO dto) {
        return clienteService.actualizar(idCliente, dto);
    }

    @DeleteMapping("/{idCliente}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long idCliente) {
        clienteService.desactivar(idCliente);
    }

    @PatchMapping("/{idCliente}/reactivar")
    public ClienteResponseDTO reactivar(@PathVariable Long idCliente) {
        return clienteService.reactivar(idCliente);
    }
}