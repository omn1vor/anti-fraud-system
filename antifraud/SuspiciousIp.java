package antifraud;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity(name = "suspicious_ips")
public class SuspiciousIp {
    @Id @GeneratedValue
    private long id;
    @NotNull
    private String ip;

    public SuspiciousIp() {
    }

    public SuspiciousIp(String ip) {
        this.ip = ip;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
