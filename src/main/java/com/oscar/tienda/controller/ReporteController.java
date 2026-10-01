package com.oscar.tienda.controller;

import com.oscar.tienda.dto.*;
import com.oscar.tienda.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/corte-caja")
    public CorteCajaDTO corteCaja(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate d = desde != null ? desde : LocalDate.now();
        LocalDate h = hasta != null ? hasta : d;
        return reporteService.corteCaja(d, h);
    }

    @GetMapping("/productos-top")
    public List<ProductoVendidoDTO> productosTop(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate h = hasta != null ? hasta : LocalDate.now();
        LocalDate d = desde != null ? desde : h.withDayOfMonth(1);
        return reporteService.topProductos(d, h);
    }

    @GetMapping("/cuentas-por-cobrar")
    public CuentasPorCobrarDTO cuentasPorCobrar() {
        return reporteService.cuentasPorCobrar();
    }
}