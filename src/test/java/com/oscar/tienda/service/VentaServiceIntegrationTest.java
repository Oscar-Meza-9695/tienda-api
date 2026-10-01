package com.oscar.tienda.service;

import com.oscar.tienda.dto.ItemVentaDTO;
import com.oscar.tienda.dto.VentaRequestDTO;
import com.oscar.tienda.enums.MetodoPago;
import com.oscar.tienda.enums.TipoPago;
import com.oscar.tienda.exception.ReglaNegocioException;
import com.oscar.tienda.exception.SaldoAFavorRequiereConfirmacionException;
import com.oscar.tienda.model.Cliente;
import com.oscar.tienda.model.Producto;
import com.oscar.tienda.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Sin @Transactional en la clase: necesitamos que el service haga commit o rollback de verdad.
@SpringBootTest
class VentaServiceIntegrationTest {

    @Autowired VentaService ventaService;
    @Autowired CuentaClienteService cuentaService;
    @Autowired VentaRepository ventaRepository;
    @Autowired PagoRepository pagoRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired ProductoRepository productoRepository;

    Cliente cliente;
    Producto producto;

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

    private VentaRequestDTO credito(BigDecimal montoPagado, Boolean permitir) {
        return new VentaRequestDTO(cliente.getIdCliente(), TipoPago.CREDITO, null,
                montoPagado, montoPagado != null ? MetodoPago.EFECTIVO : null, permitir,
                List.of(new ItemVentaDTO(producto.getIdProducto(), BigDecimal.ONE)));
    }

    @Test
    void ventaACreditoSinPagoAumentaLaDeuda() {
        ventaService.crear(credito(null, null));

        assertThat(cuentaService.calcularSaldo(cliente.getIdCliente())).isEqualByComparingTo("400");
    }

    @Test
    void pagoParcialConLaVentaReduceLaDeuda() {
        ventaService.crear(credito(new BigDecimal("150"), null));

        assertThat(cuentaService.calcularSaldo(cliente.getIdCliente())).isEqualByComparingTo("250");
    }

    @Test
    void pagarDeMasSinConfirmarRevierteTambienLaVenta() {
        assertThatThrownBy(() -> ventaService.crear(credito(new BigDecimal("500"), null)))
                .isInstanceOf(SaldoAFavorRequiereConfirmacionException.class);

        assertThat(ventaRepository.count()).isZero();
        assertThat(pagoRepository.count()).isZero();
    }

    @Test
    void pagarDeMasConfirmandoDejaSaldoAFavor() {
        ventaService.crear(credito(new BigDecimal("500"), true));

        assertThat(cuentaService.calcularSaldo(cliente.getIdCliente())).isEqualByComparingTo("-100");
    }

    @Test
    void ventaDeContadoSinMetodoSeRechaza() {
        VentaRequestDTO dto = new VentaRequestDTO(null, TipoPago.CONTADO, null, null, null, null,
                List.of(new ItemVentaDTO(producto.getIdProducto(), BigDecimal.ONE)));

        assertThatThrownBy(() -> ventaService.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void anularUnPagoRestauraLaDeuda() {
        ventaService.crear(credito(new BigDecimal("150"), null));
        Long idPago = pagoRepository.findAll().get(0).getIdPago();

        cuentaService.anularPago(idPago, "Error de captura");

        assertThat(cuentaService.calcularSaldo(cliente.getIdCliente())).isEqualByComparingTo("400");
        assertThatThrownBy(() -> cuentaService.anularPago(idPago, "otra vez"))
                .isInstanceOf(ReglaNegocioException.class);
    }
}