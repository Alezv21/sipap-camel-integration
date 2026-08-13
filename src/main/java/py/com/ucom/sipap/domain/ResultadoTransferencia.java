package py.com.ucom.sipap.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultadoTransferencia {
    @JsonProperty("id_transaccion")
    private String idTransaccion;
    private String estado;
    private String mensaje;

    public ResultadoTransferencia() {
    }

    public ResultadoTransferencia(String idTransaccion, String estado, String mensaje) {
        this.idTransaccion = idTransaccion;
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public String getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
