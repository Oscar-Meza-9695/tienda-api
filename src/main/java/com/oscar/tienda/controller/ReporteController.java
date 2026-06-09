package com.oscar.tienda.controller;

import com.oscar.tienda.model.Deuda;
import com.oscar.tienda.model.Pago;
import com.oscar.tienda.model.Venta;
import com.oscar.tienda.repository.DeudaRepository;
import com.oscar.tienda.repository.PagoRepository;
import com.oscar.tienda.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired VentaRepository ventaRepository;
    @Autowired DeudaRepository deudaRepository;
    @Autowired PagoRepository pagoRepository;

    @GetMapping("/resumen")
    public ResponseEntity<?> resumenGeneral(@RequestParam(defaultValue = "hoy") String periodo) {
        LocalDateTime[] rango = calcularRango(periodo);
        LocalDateTime inicio = rango[0];
        LocalDateTime fin    = rango[1];

        double ingresoVentas = ventaRepository.sumTotalByFechaVentaBetween(inicio, fin);
        long   numVentas     = ventaRepository.countByFechaVentaBetween(inicio, fin);
        double ingresosPagos = pagoRepository.sumMontoPagadoEnPeriodo(inicio, fin);

        // CORRECCIÓN: calcular deuda pendiente REAL (total - sum(pagos)) en Java
        // porque HQL no soporta subconsultas agregadas en COALESCE
        List<Deuda> todasDeudas = deudaRepository.findAll();
        double deudaPendienteReal = todasDeudas.stream()
                .mapToDouble(Deuda::getPendiente)   // @Transient: total - sum(pagos)
                .filter(p -> p > 0)                  // solo positivos (ignorar saldo a favor)
                .sum();

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("periodo",       periodo);
        resumen.put("ingresoVentas", ingresoVentas);
        resumen.put("numVentas",     numVentas);
        resumen.put("ingresosPagos", ingresosPagos);
        resumen.put("totalCaja",     ingresoVentas + ingresosPagos);
        resumen.put("deudaTotal",    deudaPendienteReal);  // ← ahora es el pendiente real

        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/ventas")
    public ResponseEntity<?> ventasDelPeriodo(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {

        LocalDateTime[] rango = resolverRango(periodo, desde, hasta);
        List<Venta> ventas = ventaRepository.findByFechaVentaBetween(rango[0], rango[1]);

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Venta v : ventas) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("idVenta",   v.getIdVenta());
            fila.put("fecha",     v.getFechaVenta().toString());
            fila.put("total",     v.getTotal());
            fila.put("productos", v.getDetalles() != null ? v.getDetalles().size() : 0);
            resultado.add(fila);
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/productos-top")
    public ResponseEntity<?> topProductos(
            @RequestParam(defaultValue = "mes") String periodo,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {

        LocalDateTime[] rango = resolverRango(periodo, desde, hasta);
        List<Object[]> rows = ventaRepository.topProductosVendidos(rango[0], rango[1]);

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("producto",         row[0]);
            fila.put("unidadesVendidas", ((Number) row[1]).longValue());
            fila.put("totalIngresos",    ((Number) row[2]).doubleValue());
            resultado.add(fila);
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/clientes-deuda")
    public ResponseEntity<?> clientesConDeuda() {
        // Misma corrección: calculamos en Java usando @Transient getPendiente()
        List<Deuda> todasDeudas = deudaRepository.findAll();

        // Agrupar por cliente
        Map<String, double[]> porCliente = new LinkedHashMap<>();
        for (Deuda d : todasDeudas) {
            if (d.getCliente() == null) continue;
            String nombre = d.getCliente().getNombre();
            porCliente.computeIfAbsent(nombre, k -> new double[]{0, 0, 0});
            double[] vals = porCliente.get(nombre);
            vals[0] += d.getTotal();        // totalDeuda
            vals[1] += d.getPagado();       // pagado
            vals[2] += d.getPendiente();    // pendiente
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : porCliente.entrySet()) {
            double pendiente = entry.getValue()[2];
            if (pendiente <= 0) continue;   // ignorar saldo a favor y deudas saldadas

            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("cliente",    entry.getKey());
            fila.put("totalDeuda", entry.getValue()[0]);
            fila.put("pagado",     entry.getValue()[1]);
            fila.put("pendiente",  pendiente);
            resultado.add(fila);
        }

        resultado.sort((a, b) -> Double.compare(
                (Double) b.get("pendiente"), (Double) a.get("pendiente")));

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/pagos")
    public ResponseEntity<?> historialPagos(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {

        LocalDateTime[] rango = resolverRango(periodo, desde, hasta);
        List<Pago> pagos = pagoRepository.findByFechaBetween(rango[0], rango[1]);

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Pago p : pagos) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("idPago",     p.getIdPago());
            fila.put("fecha",      p.getFecha().toString());
            fila.put("cliente",    p.getDeuda() != null && p.getDeuda().getCliente() != null
                    ? p.getDeuda().getCliente().getNombre() : "—");
            fila.put("monto",      p.getMonto());
            fila.put("metodoPago", p.getMetodoPago());
            resultado.add(fila);
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/metodos-pago")
    public ResponseEntity<?> metodosPago(
            @RequestParam(defaultValue = "mes") String periodo,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {

        LocalDateTime[] rango = resolverRango(periodo, desde, hasta);
        List<Object[]> rows = pagoRepository.resumenPorMetodoPago(rango[0], rango[1]);

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("metodo",   row[0]);
            fila.put("cantidad", ((Number) row[1]).longValue());
            fila.put("total",    ((Number) row[2]).doubleValue());
            resultado.add(fila);
        }
        return ResponseEntity.ok(resultado);
    }

   private LocalDateTime[] calcularRango(String periodo) {
        LocalDate hoy = LocalDate.now();
        return switch (periodo.toLowerCase()) {
            case "semana" -> new LocalDateTime[]{
                    hoy.minusDays(6).atStartOfDay(),
                    hoy.atTime(LocalTime.MAX)
            };
            case "mes" -> new LocalDateTime[]{
                    hoy.withDayOfMonth(1).atStartOfDay(),
                    hoy.atTime(LocalTime.MAX)
            };
            default -> new LocalDateTime[]{
                    hoy.atStartOfDay(),
                    hoy.atTime(LocalTime.MAX)
            };
        };
    }

    private LocalDateTime[] resolverRango(String periodo, String desde, String hasta) {
        if (desde != null && hasta != null) {
            return new LocalDateTime[]{
                    LocalDate.parse(desde).atStartOfDay(),
                    LocalDate.parse(hasta).atTime(LocalTime.MAX)
            };
        }
        return calcularRango(periodo != null ? periodo : "mes");
    }
}