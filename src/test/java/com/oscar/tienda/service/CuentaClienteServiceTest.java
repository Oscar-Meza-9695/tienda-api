package com.oscar.tienda.service;


import com.oscar.tienda.dto.PagoRequestDTO;
import com.oscar.tienda.enums.MetodoPago;
import com.oscar.tienda.exception.SaldoAFavorRequiereConfirmacionException;
import com.oscar.tienda.model.Cliente;
import com.oscar.tienda.model.Pago;
import com.oscar.tienda.repository.ClienteRepository;
import com.oscar.tienda.repository.PagoRepository;
import com.oscar.tienda.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaClienteServiceTest {

    @Mock
    ClienteRepository clienteRepository;
    @Mock
    VentaRepository ventaRepository;
    @Mock
    PagoRepository pagoRepository;
    @InjectMocks CuentaClienteService service;

    @BeforeEach
    void clienteQueDebe400(){
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Juan");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(ventaRepository.totalCreditoPorCliente(1L)).thenReturn(new BigDecimal("400.00"));
        when(pagoRepository.totalPagadoPorCliente(1L)).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void PagarDeMasSinConfirmarSeRechazaYNoGuardaNada(){
        PagoRequestDTO dto = new PagoRequestDTO(new BigDecimal("500"), MetodoPago.EFECTIVO,null);

        assertThatThrownBy(() -> service.registrarPago(1L, dto))
                .isInstanceOfSatisfying(SaldoAFavorRequiereConfirmacionException.class,
                        e -> assertThat(e.getSaldoAFavor()).isEqualByComparingTo("100"));

        verify(pagoRepository,never()).save(any());
    }

    @Test
    void pagarDeMasConConfiguracionSeGuarda(){
        when(pagoRepository.save(any(Pago.class))).thenAnswer(i -> i.getArgument(0));
        PagoRequestDTO dto = new PagoRequestDTO(new BigDecimal("500"), MetodoPago.EFECTIVO, true);

        service.registrarPago(1L, dto);

        verify(pagoRepository).save(any(Pago.class));
    }

    @Test
    void pagarExactamenteLoQueSeDebeNoPideConfirmacion() {
        when(pagoRepository.save(any(Pago.class))).thenAnswer(i -> i.getArgument(0));
        PagoRequestDTO dto = new PagoRequestDTO(new BigDecimal("400"), MetodoPago.EFECTIVO, null);

        service.registrarPago(1L, dto);

        verify(pagoRepository).save(any(Pago.class));
    }
}
