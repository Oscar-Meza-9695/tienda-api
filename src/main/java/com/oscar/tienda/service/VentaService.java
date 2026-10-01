package com.oscar.tienda.service;

import com.oscar.tienda.dto.*;
import com.oscar.tienda.enums.TipoPago;
import com.oscar.tienda.exception.RecursoNoEncontradoException;
import com.oscar.tienda.exception.ReglaNegocioException;
import com.oscar.tienda.model.Cliente;
import com.oscar.tienda.model.DetalleVenta;
import com.oscar.tienda.model.Producto;
import com.oscar.tienda.model.Venta;
import com.oscar.tienda.repository.ClienteRepository;
import com.oscar.tienda.repository.ProductoRepository;
import com.oscar.tienda.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final CuentaClienteService cuentaClienteService;

    @Transactional
    public VentaResponseDTO crear(VentaRequestDTO dto) {
        validarMetodos(dto);
        Cliente cliente = resolverCliente(dto);

        Venta venta = new Venta();
        venta.setTipoPago(dto.tipoPago());
        venta.setMetodoPago(dto.metodoPago());
        venta.setCliente(cliente);

        for (ItemVentaDTO item : dto.items()) {
            Producto producto = productoRepository.findById(item.idProducto())
                    .filter(Producto::getActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no disponible con id: " + item.idProducto()));
            venta.agregarDetalle(new DetalleVenta(producto, item.cantidad()));
        }
        venta.calcularTotal();
        Venta guardada = ventaRepository.save(venta);

        if (dto.montoPagado() != null) {
            cuentaClienteService.registrarPago(cliente.getIdCliente(),
                    new PagoRequestDTO(dto.montoPagado(), dto.metodoPagado(),
                            dto.permitirSaldoAFavor()));
        }
        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public VentaResponseDTO obtener(Long idVenta) {
        return toResponse(ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Venta no encontrada con el id: " + idVenta)));
    }

    private void validarMetodos(VentaRequestDTO dto) {
        if (dto.tipoPago() == TipoPago.CONTADO) {
            if (dto.metodoPago() == null) {
                throw new ReglaNegocioException("Indica el método de pago de la venta de contado");
            }
            if (dto.montoPagado() != null) {
                throw new ReglaNegocioException("El monto pagado solo aplica en ventas a crédito");
            }
        } else {
            if (dto.metodoPago() != null) {
                throw new ReglaNegocioException("Una venta a crédito no lleva método de pago");
            }
            if (dto.montoPagado() != null && dto.metodoPagado() == null) {
                throw new ReglaNegocioException("Indica con qué método pagó el cliente");
            }
        }
    }

    private Cliente resolverCliente(VentaRequestDTO dto) {
        if (dto.idCliente() == null) {
            if (dto.tipoPago() == TipoPago.CREDITO) {
                throw new ReglaNegocioException("Una venta a crédito requiere un cliente");
            }
            return null;
        }
        Cliente cliente = clienteRepository.findById(dto.idCliente())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El cliente con id: " + dto.idCliente() + " no se encontró"));
        if (dto.tipoPago() == TipoPago.CREDITO && !cliente.getActivo()) {
            throw new ReglaNegocioException("El cliente no está activo para fiado");
        }
        return cliente;
    }

    private VentaResponseDTO toResponse(Venta v) {
        List<DetalleVentaResponseDTO> detalles = v.getDetalles().stream()
                .map(d -> new DetalleVentaResponseDTO(
                        d.getProducto().getIdProducto(), d.getProducto().getNombre(),
                        d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                .toList();
        Cliente c = v.getCliente();
        return new VentaResponseDTO(v.getIdVenta(), v.getFechaVenta(), v.getTipoPago(),
                v.getMetodoPago(),
                c != null ? c.getIdCliente() : null,
                c != null ? c.getNombre() : null,
                v.getTotal(), detalles);
    }
}