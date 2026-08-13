package py.com.ucom.sipap.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MerchantAccountInformation {
    @JsonProperty("globally_unique_identifier")
    private String globallyUniqueIdentifier;

    @JsonProperty("codigo_entidad")
    private String codigoEntidad;

    @JsonProperty("numero_cuenta")
    private String numeroCuenta;

    public String getGloballyUniqueIdentifier() {
        return globallyUniqueIdentifier;
    }

    public void setGloballyUniqueIdentifier(String globallyUniqueIdentifier) {
        this.globallyUniqueIdentifier = globallyUniqueIdentifier;
    }

    public String getCodigoEntidad() {
        return codigoEntidad;
    }

    public void setCodigoEntidad(String codigoEntidad) {
        this.codigoEntidad = codigoEntidad;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }
}
