package com.oscar.tienda.service;

import com.oscar.tienda.dto.*;
import com.oscar.tienda.enums.MetodoPago;
import com.oscar.tienda.enums.SituacionSaldo;
import com.oscar.tienda.enums.TipoPago;
import com.oscar.tienda.exception.ReglaNegocioException;
import com.oscar.tienda.model.Cliente;
import com.oscar.tienda.repository.ClienteRepository;
import com.oscar.tienda.repository.PagoRepository;
import com.oscar.tienda.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private final VentaRepository ventaRepository;
    private final PagoRepository pagoRepository;
    private final ClienteRepository clienteRepository;
    private final CuentaClienteService cuentaClienteService;

    public CorteCajaDTO corteCaja(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();

        Map<MetodoPago, BigDecimal> porMetodo = new EnumMap<>(MetodoPago.class);
        BigDecimal totalCaja = BigDecimal.ZERO;
        for (MetodoPago metodo : MetodoPago.values()) {
            BigDecimal monto = ventaRepository.totalContadoPorMetodo(metodo, inicio, fin)
                    .add(pagoRepository.totalPagadoPorMetodo(metodo, inicio, fin));
            porMetodo.put(metodo, monto);
            totalCaja = totalCaja.add(monto);
        }

        return new CorteCajaDTO(
                desde, hasta, porMetodo, totalCaja,
                ventaRepository.totalPorTipoPago(TipoPago.CONTADO, inicio, fin),
                pagoRepository.totalPagadoEntre(inicio, fin),
                ventaRepository.totalPorTipoPago(TipoPago.CREDITO, inicio, fin),
                ventaRepository.countByFechaVentaGreaterThanEqualAndFechaVentaLessThan(inicio, fin));
    }

    public List<ProductoVendidoDTO> topProductos(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        return ventaRepository.topProductosVendidos(desde.atStartOfDay(),
                hasta.plusDays(1).atStartOfDay());
    }

    public CuentasPorCobrarDTO cuentasPorCobrar() {
        BigDecimal porCobrar = BigDecimal.ZERO;
        BigDecimal aFavor = BigDecimal.ZERO;
        List<SaldoClienteDTO> deudores = new ArrayList<>();

        for (Cliente c : clienteRepository.findAll()) {
            BigDecimal saldo = cuentaClienteService.calcularSaldo(c.getIdCliente());
            if (saldo.signum() > 0) {
                porCobrar = porCobrar.add(saldo);
                deudores.add(new SaldoClienteDTO(c.getIdCliente(), c.getNombre(),
                        saldo, SituacionSaldo.DEBE));
            } else if (saldo.signum() < 0) {
                aFavor = aFavor.add(saldo.negate());
            }
        }
        deudores.sort(Comparator.comparing(SaldoClienteDTO::saldo).reversed());
        return new CuentasPorCobrarDTO(porCobrar, aFavor, deudores);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la inicial");
        }
    }
}