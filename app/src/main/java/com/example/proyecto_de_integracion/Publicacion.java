package com.example.proyecto_de_integracion;

public class Publicacion {

    private String idPublicacion;
    private String titulo;
    private String descripcion;
    private String categoria;
    private double precio;
    private String imagenUrl;
    private String estadoProducto;
    private String uso;
    private String telefonoContacto;
    private String correoVendedor;
    private String uidVendedor;
    private boolean activo;

    public Publicacion() {
        // Requerido por Firebase
    }

    public Publicacion(String idPublicacion, String titulo, String descripcion,
                       String categoria, double precio, String imagenUrl,
                       String estadoProducto, String uso,
                       String telefonoContacto, String correoVendedor,
                       String uidVendedor, boolean activo) {
        this.idPublicacion = idPublicacion;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.estadoProducto = estadoProducto;
        this.uso = uso;
        this.telefonoContacto = telefonoContacto;
        this.correoVendedor = correoVendedor;
        this.uidVendedor = uidVendedor;
        this.activo = activo;
    }

    public String getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(String idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getEstadoProducto() {
        return estadoProducto;
    }

    public void setEstadoProducto(String estadoProducto) {
        this.estadoProducto = estadoProducto;
    }

    public String getUso() {
        return uso;
    }

    public void setUso(String uso) {
        this.uso = uso;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getCorreoVendedor() {
        return correoVendedor;
    }

    public void setCorreoVendedor(String correoVendedor) {
        this.correoVendedor = correoVendedor;
    }

    public String getUidVendedor() {
        return uidVendedor;
    }

    public void setUidVendedor(String uidVendedor) {
        this.uidVendedor = uidVendedor;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
