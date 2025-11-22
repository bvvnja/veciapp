package com.example.proyecto_de_integracion;

public class Tarjeta {
    private String last4;
    private String vencimiento; // MM/AA
    private String titular;     // opcional, por si quieres usarlo después

    public Tarjeta() {
        // Constructor vacío necesario para Firebase
    }

    public Tarjeta(String last4, String vencimiento, String titular) {
        this.last4 = last4;
        this.vencimiento = vencimiento;
        this.titular = titular;
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
