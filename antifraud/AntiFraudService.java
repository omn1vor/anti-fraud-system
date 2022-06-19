package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AntiFraudService {
    @Autowired
    SuspiciousIpRepository ipRepo;
    @Autowired
    StolenCardRepository stolenCardRepo;
    @Autowired
    TransactionRepository transactionRepo;
    @Autowired
    CardRepository cardRepo;

    public Map<String, String> checkTransaction(Transaction transaction) {
        if (Util.isInvalidIp(transaction.getIp()) || Util.isInvalidCardNumber(transaction.getNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Map<CheckResult, List<String>> results = new HashMap<>();
        results.put(CheckResult.ALLOWED, List.of("none"));

        Map<Function<Transaction, CheckResult>, String> checks = Map.of(
                this::checkSum, "amount",
                this::checkCardNumber, "card-number",
                this::checkIp, "ip",
                this::checkIpHistory, "ip-correlation",
                this::checkRegionHistory, "region-correlation"
        );

        checks.forEach((f, name) -> {
            CheckResult res = f.apply(transaction);
            if (res != CheckResult.ALLOWED) {
                List<String> reasons = results.getOrDefault(res, new ArrayList<>());
                reasons.add(name);
                results.put(res, reasons);
            }
        });

        CheckResult result = CheckResult.ALLOWED;
        if (results.containsKey(CheckResult.PROHIBITED)) {
            result = CheckResult.PROHIBITED;
        } else if (results.containsKey(CheckResult.MANUAL_PROCESSING)) {
            result = CheckResult.MANUAL_PROCESSING;
        }
        String reasons = results.get(result).stream()
                .sorted()
                .collect(Collectors.joining(", "));

        Card card = cardRepo.findById(transaction.getNumber())
                .orElse(new Card(transaction.getNumber()));
        cardRepo.save(card);
        transaction.setResult(result);
        transactionRepo.save(transaction);

        return Map.of(
                "result", result.toString(),
                "info", reasons
        );
    }

    public Transaction registerFeedback(FeedbackRequest request) {
        final double TRANSACTION_PART = 0.2;
        final double BASE_PART = 0.8;

        Transaction transaction = transactionRepo.findById(request.getTransactionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (transaction.getFeedback() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        if (transaction.getResult() == request.getFeedback()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Card card = cardRepo.findById(transaction.getNumber())
                .orElse(new Card(transaction.getNumber()));
        long maxAllowed = card.getMax_allowed();
        long maxManual = card.getMax_manual();

        transaction.setFeedback(request.getFeedback());
        transactionRepo.save(transaction);

        int steps = Math.abs(request.feedback.ordinal() - transaction.getResult().ordinal());
        int sign = request.feedback.ordinal() > transaction.getResult().ordinal() ? 1 : -1;
        double delta = transaction.getAmount() * TRANSACTION_PART;
        long newAllowed = (long) Math.ceil(maxAllowed * BASE_PART + sign * delta);
        long newManual = (long) Math.ceil(maxManual * BASE_PART + sign * delta);

        if (sign < 0) {
            if (transaction.getResult() == CheckResult.MANUAL_PROCESSING || steps > 1) {
                card.setMax_manual(newManual);
            }
            if (transaction.getFeedback() == CheckResult.MANUAL_PROCESSING || steps > 1) {
                card.setMax_allowed(newAllowed);
            }
        } else {
            if (transaction.getResult() == CheckResult.MANUAL_PROCESSING || steps > 1) {
                card.setMax_allowed(newAllowed);
            }
            if (transaction.getFeedback() == CheckResult.MANUAL_PROCESSING || steps > 1) {
                card.setMax_manual(newManual);
            }
        }
        cardRepo.save(card);
        return transaction;
    }

    public List<Transaction> getHistory() {
        return transactionRepo.findAll();
    }

    public List<Transaction> getHistory(String cardNumber) {
        if (Util.isInvalidCardNumber(cardNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        List<Transaction> history = transactionRepo.findByNumber(cardNumber);
        if (history.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return history;
    }

    public SuspiciousIp addSuspiciousIp(IpRequest ipRequest) {
        if (Util.isInvalidIp(ipRequest.getIp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (ipRepo.existsByIp(ipRequest.getIp())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        SuspiciousIp suspiciousIp = new SuspiciousIp(ipRequest.getIp());
        ipRepo.save(suspiciousIp);
        return suspiciousIp;
    }

    public Map<String, String> deleteSuspiciousIp(String ip) {
        if (Util.isInvalidIp(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        SuspiciousIp suspiciousIp = ipRepo.findByIp(ip)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ipRepo.delete(suspiciousIp);
        return Map.of(
                "status", String.format("IP %s successfully removed!", ip)
        );
    }

    public List<SuspiciousIp> getSuspiciousIps() {
        return ipRepo.findAll();
    }

    public StolenCard addStolenCard(CardRequest cardRequest) {
        if (Util.isInvalidCardNumber(cardRequest.getNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (stolenCardRepo.existsByNumber(cardRequest.getNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        StolenCard stolenCard = new StolenCard(cardRequest.getNumber());
        stolenCardRepo.save(stolenCard);
        return stolenCard;
    }

    public Map<String, String> deleteStolenCard(String number) {
        if (Util.isInvalidCardNumber(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        StolenCard stolenCard = stolenCardRepo.findAllByNumber(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        stolenCardRepo.delete(stolenCard);
        return Map.of(
                "status", String.format("Card %s successfully removed!", number)
        );
    }

    public List<StolenCard> getStolenCards() {
        return stolenCardRepo.findAll();
    }

    private List<Transaction> getLastHourTransactions(Transaction transaction) {
        LocalDateTime prevHour = transaction.getDate().minusHours(1);

        return transactionRepo.findByNumberAndDateBetween(transaction.getNumber(),
                prevHour, transaction.getDate());
    }

    private CheckResult checkIp(Transaction transaction) {
        if (ipRepo.existsByIp(transaction.getIp())) {
            return CheckResult.PROHIBITED;
        }
        return CheckResult.ALLOWED;
    }

    private CheckResult checkCardNumber(Transaction transaction) {
        if (stolenCardRepo.existsByNumber(transaction.getNumber())) {
            return CheckResult.PROHIBITED;
        }
        return CheckResult.ALLOWED;
    }

    private CheckResult checkRegionHistory(Transaction transaction) {
        List<Transaction> list = getLastHourTransactions(transaction);
        int regionsCount = list.stream()
                .map(Transaction::getRegion)
                .filter(region -> region != transaction.getRegion())
                .collect(Collectors.toSet())
                .size();
        if (regionsCount > 2) {
            return CheckResult.PROHIBITED;
        } else if (regionsCount == 2) {
            return CheckResult.MANUAL_PROCESSING;
        } else {
            return CheckResult.ALLOWED;
        }
    }

    private CheckResult checkIpHistory(Transaction transaction) {
        List<Transaction> list = getLastHourTransactions(transaction);
        int ipCount = list.stream()
                .map(Transaction::getIp)
                .filter(ip -> !ip.equals(transaction.getIp()))
                .collect(Collectors.toSet())
                .size();
        if (ipCount > 2) {
            return CheckResult.PROHIBITED;
        } else if (ipCount == 2) {
            return CheckResult.MANUAL_PROCESSING;
        } else {
            return CheckResult.ALLOWED;
        }
    }

    private CheckResult checkSum(Transaction transaction) {
        long sum = transaction.getAmount();
        Card card = cardRepo.findById(transaction.getNumber())
                .orElse(new Card(transaction.getNumber()));
        if (sum > card.getMax_manual()) {
            return CheckResult.PROHIBITED;
        } else if (sum > card.getMax_allowed()) {
            return CheckResult.MANUAL_PROCESSING;
        } else {
            return CheckResult.ALLOWED;
        }
    }
}
