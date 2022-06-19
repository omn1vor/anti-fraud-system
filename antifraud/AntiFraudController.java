package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/antifraud")
public class AntiFraudController {

    @Autowired
    AntiFraudService antiFraudService;

    @PostMapping("/transaction")
    public Map<String, String> checkTransaction(@Valid @RequestBody Transaction transaction) {
        return antiFraudService.checkTransaction(transaction);
    }

    @PutMapping("/transaction")
    public Transaction registerFeedback(@Valid @RequestBody FeedbackRequest feedbackRequest) {
        return antiFraudService.registerFeedback(feedbackRequest);
    }

    @GetMapping("/history")
    public List<Transaction> getHistory() {
        return antiFraudService.getHistory();
    }

    @GetMapping("/history/{number}")
    public List<Transaction> getCardHistory(@PathVariable String number) {
        return antiFraudService.getHistory(number);
    }

    @PostMapping("/suspicious-ip")
    public SuspiciousIp addSuspiciousIp(@Valid @RequestBody IpRequest ipRequest) {
        return antiFraudService.addSuspiciousIp(ipRequest);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public Map<String, String> deleteSuspiciousIp(@PathVariable String ip) {
        return antiFraudService.deleteSuspiciousIp(ip);
    }

    @GetMapping("/suspicious-ip")
    public List<SuspiciousIp> getSuspiciousIps() {
        return antiFraudService.getSuspiciousIps();
    }

    @PostMapping("/stolencard")
    public StolenCard addStolenCard(@Valid @RequestBody CardRequest cardRequest) {
        return antiFraudService.addStolenCard(cardRequest);
    }

    @DeleteMapping("/stolencard/{number}")
    public Map<String, String> deleteStolenCard(@PathVariable String number) {
        return antiFraudService.deleteStolenCard(number);
    }

    @GetMapping("/stolencard")
    public List<StolenCard> getStolenCards() {
        return antiFraudService.getStolenCards();
    }
}

class IpRequest {
    @NotNull
    String ip;

    public String getIp() {
        return ip;
    }
}

class CardRequest {
    @NotNull
    String number;

    public String getNumber() {
        return number;
    }
}

class FeedbackRequest {
    @NotNull
    long transactionId;
    @NotNull
    CheckResult feedback;

    public long getTransactionId() {
        return transactionId;
    }

    public CheckResult getFeedback() {
        return feedback;
    }
}