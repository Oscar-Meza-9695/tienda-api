package com.oscar.tienda.service;

import com.oscar.tienda.model.DetalleVenta;
import com.oscar.tienda.model.Producto;
import com.oscar.tienda.model.Venta;
import com.oscar.tienda.repository.ProductoRepository;
import com.oscar.tienda.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService {
    @Autowired
    VentaRepository ventaRepository;

    public Venta guardarVenta (Venta venta){
        venta.setFechaVenta(LocalDateTime.now());

        for (DetalleVenta detalleVenta : venta.getDetalles()){
            detalleVenta.setVenta(venta);

            detalleVenta.setSubtotal(detalleVenta.getCantidad() * detalleVenta.getPrecioUnitario());
        }
        return ventaRepository.save(venta);
    }

    public List<Venta> listarVentas(){
        return ventaRepository.findAll();
    }
}