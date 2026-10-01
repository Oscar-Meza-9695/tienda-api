package com.oscar.tienda.service;

import com.oscar.tienda.dto.CorteCajaDTO;
import com.oscar.tienda.dto.ItemVentaDTO;
import com.oscar.tienda.dto.VentaRequestDTO;
import com.oscar.tienda.enums.MetodoPago;
import com.oscar.tienda.enums.TipoPago;
import com.oscar.tienda.exception.ReglaNegocioException;
import com.oscar.tienda.model.Cliente;
import com.oscar.tienda.model.Producto;
import com.oscar.tienda.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Protege la regla: la caja solo suma dinero que realmente entró
// (ventas de contado + abonos no anulados). El fiado NO es dinero.
@SpringBootTest
class ReporteServiceIntegrationTest {

    @Autowired ReporteService reporteService;
    @Autowired VentaService ventaService;
    @Autowired CuentaClienteService cuentaService;
    @Autowired VentaRepository ventaRepository;
    @Autowired PagoRepository pagoRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired ProductoRepository productoRepository;

    Cliente cliente;
    Producto producto; // $400.00

    @BeforeEach
    void datosBase() {
        pagoRepository.deleteAll();
        ventaRepository.deleteAll();
        clienteRepository.deleteAll();
        productoRepository.deleteAll();

        cliente = new Cliente();
        cliente.setNombre("Juan");
        cliente = clienteRepository.save(cliente);

        producto = new Producto();
        producto.setNombre("Azúcar");
        producto.setPrecio(new BigDecimal("400.00"));
        producto.setCodigoBarras("123");
        producto = productoRepository.save(producto);
    }

    private List<ItemVentaDTO> unProducto() {
        return List.of(new ItemVentaDTO(producto.getIdProducto(), BigDecimal.ONE));
    }

    private void ventaContado(MetodoPago metodo) {
        ventaService.crear(new VentaRequestDTO(null, TipoPago.CONTADO, metodo,
                null, null, null, unProducto()));
    }

    private void ventaCredito(BigDecimal abono, MetodoPago metodoAbono) {
        ventaService.crear(new VentaRequestDTO(cliente.getIdCliente(), TipoPago.CREDITO, null,
                abono, metodoAbono, null, unProducto()));
    }

    private CorteCajaDTO corteHoy() {
        LocalDate hoy = LocalDate.now();
        return reporteService.corteCaja(hoy, hoy);
    }

    @Test
    void laCajaSumaContadoYAbonosPeroNoElFiado() {
        ventaContado(MetodoPago.EFECTIVO);                        // entra 400 en efectivo
        ventaCredito(new BigDecimal("150"), MetodoPago.TRANSFERENCIA); // fiado 400, entra 150 por transferencia

        CorteCajaDTO corte = corteHoy();

        assertThat(corte.totalCaja()).isEqualByComparingTo("550");
        assertThat(corte.cajaPorMetodo().get(MetodoPago.EFECTIVO)).isEqualByComparingTo("400");
        assertThat(corte.cajaPorMetodo().get(MetodoPago.TRANSFERENCIA)).isEqualByComparingTo("150");
        assertThat(corte.ventasContado()).isEqualByComparingTo("400");
        assertThat(corte.abonosRecibidos()).isEqualByComparingTo("150");
        assertThat(corte.ventasACuenta()).isEqualByComparingTo("400"); // informativo, no suma a caja
        assertThat(corte.numeroVentas()).isEqualTo(2);
    }

    @Test
    void unaVentaSoloAFiadoNoMueveLaCaja() {
        ventaCredito(null, null);

        CorteCajaDTO corte = corteHoy();

        assertThat(corte.totalCaja()).isEqualByComparingTo("0");
        assertThat(corte.ventasACuenta()).isEqualByComparingTo("400");
    }

    @Test
    void unAbonoAnuladoDejaDeSumarseALaCaja() {
        ventaCredito(new BigDecimal("150"), MetodoPago.EFECTIVO);
        Long idPago = pagoRepository.findAll().get(0).getIdPago();

        cuentaService.anularPago(idPago, "Error de captura");

        CorteCajaDTO corte = corteHoy();
        assertThat(corte.totalCaja()).isEqualByComparingTo("0");
        assertThat(corte.abonosRecibidos()).isEqualByComparingTo("0");
    }

    @Test
    void elCorteRespetaElRangoDeFechas() {
        ventaContado(MetodoPago.EFECTIVO);
        LocalDate ayer = LocalDate.now().minusDays(1);

        CorteCajaDTO corteAyer = reporteService.corteCaja(ayer, ayer);

        assertThat(corteAyer.totalCaja()).isEqualByComparingTo("0");
        assertThat(corteAyer.numeroVentas()).isZero();
    }

    @Test
    void unRangoInvertidoSeRechaza() {
        LocalDate hoy = LocalDate.now();

        assertThatThrownBy(() -> reporteService.corteCaja(hoy, hoy.minusDays(1)))
                .isInstanceOf(ReglaNegocioException.class);
    }
}