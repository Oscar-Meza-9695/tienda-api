package com.oscar.tienda.service;

import com.oscar.tienda.dto.ClienteRequestDTO;
import com.oscar.tienda.dto.ClienteResponseDTO;
import com.oscar.tienda.exception.RecursoNoEncontradoException;
import com.oscar.tienda.exception.ReglaNegocioException;
import com.oscar.tienda.model.Cliente;
import com.oscar.tienda.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final CuentaClienteService cuentaClienteService;

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> leerActivos(){
        return clienteRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarInactivos(String nombre){
        List<Cliente> clientes = (nombre == null || nombre.isBlank())
                ? clienteRepository.findByActivoFalse()
                : clienteRepository.findByNombreContainingIgnoreCaseAndActivoFalse(nombre);
        return clientes.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> buscarPorNombre(String nombre){
        return clienteRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO obtener(Long idCliente){
        return toResponse(obtenerPorId(idCliente));
    }

    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO dto){
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.nombre());
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponseDTO actualizar(Long idCliente, ClienteRequestDTO dto){
        Cliente cliente = obtenerPorId(idCliente);
        cliente.setNombre(dto.nombre());
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void desactivar(Long idCliente) {
        Cliente cliente = obtenerPorId(idCliente);
        BigDecimal saldo = cuentaClienteService.calcularSaldo(idCliente);
        if (saldo.signum() != 0) {
            throw new ReglaNegocioException("No se puede desactivar: el saldo del cliente es $"
                    + saldo + " (positivo = debe, negativo = a favor)");
        }
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Transactional
    public ClienteResponseDTO reactivar(Long idCliente){
        Cliente cliente = obtenerPorId(idCliente);
        cliente.setActivo(true);
        return toResponse(clienteRepository.save(cliente));
    }

    private Cliente obtenerPorId(Long idCliente){
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con el id: " + idCliente
                ));
    }

    private ClienteResponseDTO toResponse(Cliente c){
        return new ClienteResponseDTO(
                c.getIdCliente(),
                c.getNombre(),
                c.getActivo(),
                c.getFechaRegistro()
        );
    }
}