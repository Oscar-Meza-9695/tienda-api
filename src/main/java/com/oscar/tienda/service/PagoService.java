package com.oscar.tienda.service;

import com.oscar.tienda.model.Pago;
import com.oscar.tienda.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoService {
    @Autowired
    PagoRepository pagoRepository;
    public Pago guardarPago (Pago pago){
        return pagoRepository.save(pago);
    }

    public List<Pago>buscarPorFecha(LocalDateTime inicio, LocalDateTime fin){
        List<Pago>fechaPagos = pagoRepository.findByFechaBetween(inicio, fin);
        if(fechaPagos.isEmpty()){
            throw new RuntimeException("No se encontro ningun pago en esa fecha");
        }
        return fechaPagos;
    }
}
