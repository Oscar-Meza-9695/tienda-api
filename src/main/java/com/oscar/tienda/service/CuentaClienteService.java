package com.oscar.tienda.service;

import com.oscar.tienda.dto.*;
import com.oscar.tienda.enums.SituacionSaldo;
import com.oscar.tienda.enums.TipoMovimiento;
import com.oscar.tienda.enums.TipoPago;
import com.oscar.tienda.exception.RecursoNoEncontradoException;
import com.oscar.tienda.exception.SaldoAFavorRequiereConfirmacionException;
import com.oscar.tienda.model.Cliente;
import com.oscar.tienda.model.Pago;
import com.oscar.tienda.repository.ClienteRepository;
import com.oscar.tienda.repository.PagoRepository;
import com.oscar.tienda.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CuentaClienteService {
    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;
    private final PagoRepository pagoRepository;

    @Transactional
    public PagoResponseDTO registrarPago(Long idCliente, PagoRequestDTO dto) {
        Cliente cliente = obtenerCliente(idCliente);

        BigDecimal proyectado = calcularSaldo(idCliente).subtract(dto.monto());
        if (proyectado.signum() < 0 && !Boolean.TRUE.equals(dto.permitirSaldoAFavor())) {
            throw new SaldoAFavorRequiereConfirmacionException(proyectado.negate());
        }

        Pago pago = new Pago();
        pago.setCliente(cliente);
        pago.setMonto(dto.monto());
        pago.setMetodoPago(dto.metodoPago());
        return toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponseDTO anularPago(Long idPago, String motivo) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Pago no encontrado con el id: " + idPago));
        pago.anular(motivo);
        return toResponse(pagoRepository.save(pago));
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaldo(Long idCliente) {
        return ventaRepository.totalCreditoPorCliente(idCliente)
                .subtract(pagoRepository.totalPagadoPorCliente(idCliente));
    }

    @Transactional(readOnly = true)
    public SaldoClienteDTO obtenerSaldo(Long idCliente) {
        return armarSaldo(obtenerCliente(idCliente));
    }

    @Transactional(readOnly = true)
    public EstadoCuentaDTO estadoCuenta(Long idCliente) {
        Cliente cliente = obtenerCliente(idCliente);

        Stream<MovimientoDTO> ventas = ventaRepository
                .findByCliente_IdClienteAndTipoPagoOrderByFechaVentaDesc(idCliente, TipoPago.CREDITO)
                .stream()
                .map(v -> new MovimientoDTO(TipoMovimiento.VENTA_CREDITO, v.getFechaVenta(),
                        v.getTotal(), "Venta #" + v.getIdVenta(), false,
                        v.getDetalles().stream()
                                .map(d -> new DetalleVentaResponseDTO(
                                        d.getProducto().getIdProducto(), d.getProducto().getNombre(),
                                        d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                                .toList()));

        Stream<MovimientoDTO> pagos = pagoRepository
                .findByCliente_IdClienteOrderByFechaPagoDesc(idCliente).stream()
                .map(p -> new MovimientoDTO(TipoMovimiento.PAGO, p.getFechaPago(),
                        p.getMonto(), "Pago en " + p.getMetodoPago(), p.getAnulada(),null));

        List<MovimientoDTO> movimientos = Stream.concat(ventas, pagos)
                .sorted(Comparator.comparing(MovimientoDTO::fecha).reversed())
                .toList();

        return new EstadoCuentaDTO(armarSaldo(cliente), movimientos);
    }

    private Cliente obtenerCliente(Long idCliente) {
        return clienteRepository.findById(idCliente).orElseThrow(
                () -> new RecursoNoEncontradoException(
                        "No se encontró al cliente con el id: " + idCliente));
    }

    private SaldoClienteDTO armarSaldo(Cliente cliente) {
        BigDecimal saldo = calcularSaldo(cliente.getIdCliente());
        SituacionSaldo situacion = saldo.signum() > 0 ? SituacionSaldo.DEBE
                : saldo.signum() < 0 ? SituacionSaldo.A_FAVOR
                : SituacionSaldo.AL_CORRIENTE;
        return new SaldoClienteDTO(cliente.getIdCliente(), cliente.getNombre(), saldo, situacion);
    }

    private PagoResponseDTO toResponse(Pago p) {
        return new PagoResponseDTO(p.getIdPago(), p.getCliente().getIdCliente(),
                p.getMonto(), p.getFechaPago(), p.getMetodoPago(), p.getAnulada());
    }
}