package com.oscar.tienda.exception;

import java.math.BigDecimal;

public class SaldoAFavorRequiereConfirmacionException extends RuntimeException{
    private final BigDecimal saldoAFavor;

    public SaldoAFavorRequiereConfirmacionException(BigDecimal saldoAFavor) {
        super("El pago deja $" + saldoAFavor + " a favor del cliente. Confirma si es correcto.");
        this.saldoAFavor = saldoAFavor;
    }

    public BigDecimal getSaldoAFavor() { return saldoAFavor; }
}
