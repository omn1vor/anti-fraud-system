package antifraud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity(name = "transactions")
class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long transactionId;
    @NotNull @Min(1)
    private Long amount;
    @NotNull
    private String ip;
    @NotNull
    private String number;
    @Enumerated(EnumType.STRING) @NotNull
    private Region region;
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    private CheckResult result;
    @Enumerated(EnumType.STRING)
    private CheckResult feedback;

    public Long getAmount() {
        return amount;
    }

    public String getIp() {
        return ip;
    }

    public String getNumber() {
        return number;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public Region getRegion() {
        return region;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public CheckResult getResult() {
        return result;
    }

    public void setResult(CheckResult result) {
        this.result = result;
    }

    @JsonIgnore
    public CheckResult getFeedback() {
        return feedback;
    }

    public void setFeedback(CheckResult feedback) {
        this.feedback = feedback;
    }

    @JsonProperty("feedback")
    public String getStringFeedback() {
        return feedback == null ? "" : feedback.name();
    }
}
