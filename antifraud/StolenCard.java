package antifraud;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity(name = "stolen_cards")
public class StolenCard {
    @Id @GeneratedValue
    private long id;
    @NotNull
    private String number;

    public StolenCard() {
    }

    public StolenCard(String number) {
        this.number = number;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
