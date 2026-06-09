package com.oscar.tienda.service;

import com.oscar.tienda.model.Deuda;
import com.oscar.tienda.model.DetalleDeuda;
import com.oscar.tienda.repository.DeudaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeudaService {

    @Autowired
    DeudaRepository deudaRepository;

    public Deuda guardarDeuda(Deuda deuda) {
        if (deuda.getDetalles() != null) {
            for (DetalleDeuda detalle : deuda.getDetalles()) {
                detalle.setDeuda(deuda);   // ← referencia inversa obligatoria
            }
        }
        return deudaRepository.save(deuda);
    }

    public List<Deuda> buscarDeudaPorCliente(String nombre) {
        List<Deuda> deudas = deudaRepository.findByClienteNombreIgnoreCase(nombre);
        if (deudas.isEmpty()) {
            throw new RuntimeException("No se encontraron deudas para este cliente");
        }
        return deudas;
    }

    public List<Deuda> listaDeudas() {
        List<Deuda> deudas = deudaRepository.findAll();
        if (deudas.isEmpty()) {
            throw new RuntimeException("No hay deudas para mostrar");
        }
        return deudas;
    }

    public double calcularTotal(Deuda deuda) {
        Double total = deuda.getDetalles()
                .stream()
                .mapToDouble(d -> d.getCantidad() * d.getPrecioUnitario())
                .sum();
        deuda.setTotal(total);
        return deudaRepository.save(deuda).getTotal();
    }

    public Deuda buscarId(Long id) {
        return deudaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deuda con id " + id + " no encontrada"));
    }

    public double calcularTotalDeudaPorCliente(String nombre) {
        List<Deuda> deudas = deudaRepository.findByClienteNombreIgnoreCase(nombre);
        return deudas.stream().mapToDouble(Deuda::getTotal).sum();
    }

    public double calcularTotalPendientePorCliente(String nombre) {
        List<Deuda> deudas = deudaRepository.findByClienteNombreIgnoreCase(nombre);
        return deudas.stream().mapToDouble(Deuda::getPendiente).sum();
    }
}