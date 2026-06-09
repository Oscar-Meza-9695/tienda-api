package com.oscar.tienda.controller;

import com.oscar.tienda.model.Deuda;
import com.oscar.tienda.model.Pago;
import com.oscar.tienda.service.DeudaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/deudas")
public class DeudaController {
    @Autowired
    DeudaService deudaService;

    // Guardar deuda
    @PostMapping
    public ResponseEntity<?> guardarDeuda(@RequestBody Deuda deuda){
        try{
            Deuda guardada = deudaService.guardarDeuda(deuda);
            return ResponseEntity.ok(guardada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // Buscar deudas por nombre del cliente
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarDeudaNombre(@RequestParam String nombre){
        try{
            List<Deuda> nombrCliente = deudaService.buscarDeudaPorCliente(nombre);
            return ResponseEntity.ok(nombrCliente);
        } catch(RuntimeException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Listar todas las deudas
    @GetMapping
    public ResponseEntity<?> listarDeudas(){
        try{
            List<Deuda> listaDeudas = deudaService.listaDeudas();
            return ResponseEntity.ok(listaDeudas);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Calcular total de la deuda
    @PutMapping("/{id}/total")
    public ResponseEntity<?> totalDeuda(@PathVariable Long id){
        try{
            Deuda deudaId = deudaService.buscarId(id);
            Double total = deudaService.calcularTotal(deudaId);
            return ResponseEntity.ok("Se calculó con éxito el total de la deuda: " + total);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Total pendiente de un cliente (suma de todas sus deudas pendientes)
    @GetMapping("/total-cliente")
    public ResponseEntity<?> obtenerTotalCliente(@RequestParam String nombre) {
        try {
            double total = deudaService.calcularTotalPendientePorCliente(nombre);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<?> obtenerDetalles(@PathVariable Long id) {
        try {
            Deuda deuda = deudaService.buscarId(id);
            StringBuilder sb = new StringBuilder();
            deuda.getDetalles().forEach(d ->
                    sb.append(String.format("• %s x%d = $%.2f%n",
                            d.getProducto().getNombre(),
                            d.getCantidad(),
                            d.getSubtotal()))
            );
            return ResponseEntity.ok(sb.length() > 0 ? sb.toString() : "Sin productos registrados");
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/pagos")
    public ResponseEntity<?> obtenerPagosPorDeuda(@PathVariable Long id) {
        try {
            Deuda deuda = deudaService.buscarId(id);
            List<Pago> pagos = deuda.getPagos();

            if (pagos == null || pagos.isEmpty()) {
                return ResponseEntity.ok("Sin pagos registrados");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            StringBuilder sb = new StringBuilder();
            pagos.forEach(p -> {
                String fechaFormateada = p.getFecha() != null
                        ? p.getFecha().format(formatter)
                        : "Fecha desconocida";
                sb.append(String.format("• %s — $%.2f (%s)%n",
                        fechaFormateada,
                        p.getMonto(),
                        p.getMetodoPago()));
            });

            return ResponseEntity.ok(sb.toString());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}