package com.oscar.tienda.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class VentaTest {

    private Producto producto(String precio){
        Producto p = new Producto();
        p.setPrecio(new BigDecimal(precio));
        return p;
    }
    @Test
    void subtotalConCantidadDecimalSeRedondeaACentavos(){
        DetalleVenta d = new DetalleVenta(
                producto("32.50"), new BigDecimal("0.750")
        );

        assertThat(d.getSubtotal()).isEqualByComparingTo("24.38");
    }

    @Test
    void elDetalleCongelaElPrecioDelProducto(){
        Producto p = producto("30.00");
        DetalleVenta d = new DetalleVenta(p, new BigDecimal("2"));

        p.setPrecio(new BigDecimal("99.00"));

        assertThat(d.getPrecioUnitario()).isEqualByComparingTo("30.00");
        assertThat(d.getSubtotal()).isEqualByComparingTo("60.00");
    }

    @Test
    void elTotalEsLaSumaDeLosSubtotales(){
        Venta venta = new Venta();
        venta.agregarDetalle(new DetalleVenta(producto("45.00"), new BigDecimal("2")));
        venta.agregarDetalle(new DetalleVenta(producto("30.00"), new BigDecimal("1")));

        venta.calcularTotal();

        assertThat(venta.getTotal()).isEqualByComparingTo("120.00");
    }

    @Test
    void agregarDetalleAsignaLaVentaAlDetalle(){
        Venta venta = new Venta();
        DetalleVenta d = new DetalleVenta(producto("10.00"), BigDecimal.ONE);

        venta.agregarDetalle(d);
        assertThat(d.getVenta()).isSameAs(venta);
    }
}
