package com.oscar.tienda.dto;

import java.util.List;

public record EstadoCuentaDTO(
        SaldoClienteDTO saldo,
        List<MovimientoDTO> movimientos
) {}
