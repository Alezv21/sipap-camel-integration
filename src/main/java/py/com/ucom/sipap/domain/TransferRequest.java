package py.com.ucom.sipap.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class TransferRequest {
    @JsonProperty("id_transaccion")
    private String idTransaccion;

    @JsonProperty("fecha_transaccion")
    private String fechaTransaccion;

    private String qr;
    private BigDecimal monto;

    public String getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }

    public String getFechaTransaccion() { return fechaTransaccion; }
    public void setFechaTransaccion(String fechaTransaccion) { this.fechaTransaccion = fechaTransaccion; }

    public String getQr() { return qr; }
    public void setQr(String qr) { this.qr = qr; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}
