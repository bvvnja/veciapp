package com.example.proyecto_de_integracion;

public class Cobranza {
    private String uidUsuario;
    private String nombres;
    private String numeroDepto;
    private String mesClave;
    private String mesNombre;
    private int anio;
    private double monto; // Cambiar a double o long según corresponda
    private String descripcion;
    private String estadoPago;
    private long timestampCreacion;
    private long totalGastosEdificio; // Asegúrate de que este campo sea de tipo 'long'
    private double coefcopropiedad; // O usa 'Double' si es necesario


    // Agregar los setters y getters correctamente para todos los campos
    public String getUidUsuario() {
        return uidUsuario;
    }

    public void setUidUsuario(String uidUsuario) {
        this.uidUsuario = uidUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getNumeroDepto() {
        return numeroDepto;
    }

    public void setNumeroDepto(String numeroDepto) {
        this.numeroDepto = numeroDepto;
    }

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

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
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

    public double getCoefcopropiedad() {
        return coefcopropiedad;
    }

    public void setCoefcopropiedad(double coefcopropiedad) {
        this.coefcopropiedad = coefcopropiedad;
    }
}

