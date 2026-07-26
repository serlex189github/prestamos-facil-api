package com.prestamosfacil.domain.exception;

public class SalarioInvalidoException extends RuntimeException {

    public SalarioInvalidoException() {
        super("El salario base debe estar entre 0 y 15000000.");
    }

}
