package com.example.proyecto_de_integracion;

public class GastoMensual {

    private String mesClave;             // "2025-02"
    private String mesNombre;            // "Enero"
    private String descripcion;          // "Gastos comunes enero 2025"
    private int anio;                    // 2025
    private long timestampCreacion;      // 1763695298833
    private long totalGastosEdificio;    // 20000000

    // Constructor vacío obligatorio para Firebase
    public GastoMensual() {}

    public GastoMensual(String mesClave,
                        String mesNombre,
                        String descripcion,
                        int anio,
                        long timestampCreacion,
                        long totalGastosEdificio) {
        this.mesClave = mesClave;
        this.mesNombre = mesNombre;
        this.descripcion = descripcion;
        this.anio = anio;
        this.timestampCreacion = timestampCreacion;
        this.totalGastosEdificio = totalGastosEdificio;
    }

    // Getters y setters
    public String getMesClave() {
        return mesClave;
    }

    public void setMesClave(String mesClave) {
        this.mesClave = mesClave;
    }

    public String getMesNombre() {
        return mesNombre;
    }

    public void setMesNombre(String mesNombre) {
        this.mesNombre = mesNombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public long getTimestampCreacion() {
        return timestampCreacion;
    }

    public void setTimestampCreacion(long timestampCreacion) {
        this.timestampCreacion = timestampCreacion;
    }

    public long getTotalGastosEdificio() {
        return totalGastosEdificio;
    }

    public void setTotalGastosEdificio(long totalGastosEdificio) {
        this.totalGastosEdificio = totalGastosEdificio;
    }
}
