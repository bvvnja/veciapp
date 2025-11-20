package com.example.proyecto_de_integracion;

public class UserProfile {

    private String uid;
    private String nombre;
    private String correo;
    private String coefPropiedad;
    private String numDepto;
    private String password;
    private boolean activo;

    // Constructor vacío requerido por Firebase
    public UserProfile() {
    }

    public UserProfile(String uid,
                       String nombre,
                       String correo,
                       String coefPropiedad,
                       String numDepto,
                       String password,
                       boolean activo) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
        this.coefPropiedad = coefPropiedad;
        this.numDepto = numDepto;
        this.password = password;
        this.activo = activo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCoefPropiedad() {
        return coefPropiedad;
    }

    public void setCoefPropiedad(String coefPropiedad) {
        this.coefPropiedad = coefPropiedad;
    }

    public String getNumDepto() {
        return numDepto;
    }

    public void setNumDepto(String numDepto) {
        this.numDepto = numDepto;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
