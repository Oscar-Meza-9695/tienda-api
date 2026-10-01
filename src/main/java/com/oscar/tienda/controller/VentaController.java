package com.oscar.tienda.controller;

import com.oscar.tienda.dto.VentaRequestDTO;
import com.oscar.tienda.dto.VentaResponseDTO;
import com.oscar.tienda.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaResponseDTO crear(@Valid @RequestBody VentaRequestDTO dto) {
        return ventaService.crear(dto);
    }

    @GetMapping("/{idVenta}")
    public VentaResponseDTO obtener(@PathVariable Long idVenta) {
        return ventaService.obtener(idVenta);
    }
}