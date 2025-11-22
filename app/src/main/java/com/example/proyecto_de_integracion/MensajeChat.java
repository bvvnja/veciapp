package com.example.proyecto_de_integracion;

public class MensajeChat {

    private String idMensaje;
    private String idPublicacion;
    private String remitenteUid;
    private String remitenteNombre;
    private String texto;
    private long timestamp;

    public MensajeChat() {
        // Requerido por Firebase
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(String idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(String idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

    public String getRemitenteUid() {
        return remitenteUid;
    }

    public void setRemitenteUid(String remitenteUid) {
        this.remitenteUid = remitenteUid;
    }

    public String getRemitenteNombre() {
        return remitenteNombre;
    }

    public void setRemitenteNombre(String remitenteNombre) {
        this.remitenteNombre = remitenteNombre;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
