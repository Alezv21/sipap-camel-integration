package py.com.ucom.sipap.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferMessage {
    @JsonProperty("id_transaccion")
    private String idTransaccion;

    @JsonProperty("fecha_transaccion")
    private String fechaTransaccion;

    private Transferencia transferencia;

    public TransferMessage() {
    }

    public TransferMessage(String idTransaccion, String fechaTransaccion, Transferencia transferencia) {
        this.idTransaccion = idTransaccion;
        this.fechaTransaccion = fechaTransaccion;
        this.transferencia = transferencia;
    }

    public String getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }

    public String getFechaTransaccion() { return fechaTransaccion; }
    public void setFechaTransaccion(String fechaTransaccion) { this.fechaTransaccion = fechaTransaccion; }

    public Transferencia getTransferencia() { return transferencia; }
    public void setTransferencia(Transferencia transferencia) { this.transferencia = transferencia; }
}
