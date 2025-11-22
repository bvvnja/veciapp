package com.example.proyecto_de_integracion;

public class Tarjeta {

    // 16 dígitos sin espacios (solo para tu app)
    private String numeroCompleto;
    private String last4;
    private String vencimiento; // MM/AA
    private String titular;     // opcional

    public Tarjeta() {
        // Necesario para Firebase
    }

    public Tarjeta(String numeroCompleto, String last4, String vencimiento, String titular) {
        this.numeroCompleto = numeroCompleto;
        this.last4 = last4;
        this.vencimiento = vencimiento;
        this.titular = titular;
    }

    public String getNumeroCompleto() {
        return numeroCompleto;
    }

    public void setNumeroCompleto(String numeroCompleto) {
        this.numeroCompleto = numeroCompleto;
    }

    public String getLast4() {
        return last4;
    }

    public void setLast4(String last4) {
        this.last4 = last4;
    }

    public String getVencimiento() {
        return vencimiento;
    }

    public void setVencimiento(String vencimiento) {
        this.vencimiento = vencimiento;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }
}
