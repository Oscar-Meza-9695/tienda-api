package com.oscar.tienda.controller;

import com.oscar.tienda.model.Pago;
import com.oscar.tienda.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {
    @Autowired
    PagoService pagoService;

    @PostMapping
    public ResponseEntity<?> guardaPago(@RequestBody Pago pago) {
        try {
            Pago guardarPago = pagoService.guardarPago(pago);
            // Solo devuelve el id y monto, no la deuda completa
            return ResponseEntity.ok(
                    "{\"idPago\":" + guardarPago.getIdPago() +
                            ",\"monto\":" + guardarPago.getMonto() + "}"
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPagosPorFecha(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin) {
        try {
            List<Pago> pagos = pagoService.buscarPorFecha(inicio, fin);
            return ResponseEntity.ok(pagos); // ← solo la lista
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}