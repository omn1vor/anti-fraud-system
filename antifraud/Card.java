package antifraud;

import javax.persistence.*;

@Entity(name = "cards")
public class Card {
    @Id
    String number;
    @Column(name = "max_allowed")
    long max_allowed = 200;
    @Column(name = "max_manual")
    long max_manual = 1500;

    public Card() {
    }

    public Card(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public long getMax_allowed() {
        return max_allowed;
    }

    public void setMax_allowed(long max_allowed) {
        this.max_allowed = max_allowed;
    }

    public long getMax_manual() {
        return max_manual;
    }

    public void setMax_manual(long max_manual) {
        this.max_manual = max_manual;
    }
}
