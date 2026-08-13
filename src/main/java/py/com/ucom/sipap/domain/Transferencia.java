package py.com.ucom.sipap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transferencia {
    @JsonProperty("payload_format_indicator")
    private String payloadFormatIndicator;

    @JsonProperty("point_of_initiation_method")
    private String pointOfInitiationMethod;

    @JsonProperty("merchant_account_information")
    private MerchantAccountInformation merchantAccountInformation;

    @JsonProperty("merchant_category_code")
    private String merchantCategoryCode;

    @JsonProperty("transaction_currency")
    private String transactionCurrency;

    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("merchant_name")
    private String merchantName;

    @JsonProperty("merchant_city")
    private String merchantCity;

    private String crc;

    public String getPayloadFormatIndicator() { return payloadFormatIndicator; }
    public void setPayloadFormatIndicator(String value) { this.payloadFormatIndicator = value; }

    public String getPointOfInitiationMethod() { return pointOfInitiationMethod; }
    public void setPointOfInitiationMethod(String value) { this.pointOfInitiationMethod = value; }

    public MerchantAccountInformation getMerchantAccountInformation() { return merchantAccountInformation; }
    public void setMerchantAccountInformation(MerchantAccountInformation value) { this.merchantAccountInformation = value; }

    public String getMerchantCategoryCode() { return merchantCategoryCode; }
    public void setMerchantCategoryCode(String value) { this.merchantCategoryCode = value; }

    public String getTransactionCurrency() { return transactionCurrency; }
    public void setTransactionCurrency(String value) { this.transactionCurrency = value; }

    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(BigDecimal value) { this.transactionAmount = value; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String value) { this.countryCode = value; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String value) { this.merchantName = value; }

    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String value) { this.merchantCity = value; }

    public String getCrc() { return crc; }
    public void setCrc(String crc) { this.crc = crc; }
}
