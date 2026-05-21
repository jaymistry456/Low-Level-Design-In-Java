package lowleveldesigns.atmsystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/*
enums
* */
enum TransactionType {
    WITHDRAW,
    DEPOSIT,
    CHECK_BALANCE;
}

enum WithdrawResult {
    SUCCESS,
    INSUFFICIENT_FUNDS,
    DAILY_LIMIT_EXCEEDED;
}

/*
classes
* */
/*
Account
Transaction
Card
BankService
ATMSystemState (interface)
IdleState implements ATMSystemState
CardInsertedState implements ATMSystemState
PINVerifiedState implements ATMSystemState
TransactionCompleteState implements ATMSystemState
CashDispenser
ATMSystem
* */

/*
Account
    knows:
        accountId
        name
        email
        phoneNo
        balance
    does:
        updateBalance(double amount, TransactionType)
* */
class Account {
    private String accountId;
    private String name;
    private String email;
    private String phoneNo;
    private double balance;

    public Account(String accountId, String name, String email, String phoneNo, double balance) {
        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
        this.balance = balance;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public double getBalance() {
        return balance;
    }

    public void updateBalance(double amount, TransactionType transactionType) {
        if(transactionType == TransactionType.WITHDRAW) {
            this.balance -= amount;
        }
        else {
            this.balance += amount;
        }
    }
}

/*
Transaction
    knows:
        Account
        amount
        TransactionType
        timestamp
    does:
        nothing (data carrier)
* */
class Transaction {
    private Account account;
    private double amount;
    private TransactionType transactionType;
    private LocalDateTime timestamp;

    public Transaction(Account account, double amount, TransactionType transactionType) {
        this.account = account;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();
    }

    public Account getAccount() {
        return account;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

/*
Card
    knows:
        Account
        cardNo
    does:
        nothing (data carrier)
* */
class Card {
    private Account account;
    private String cardNo;

    public Card(Account account, String cardNo) {
        this.account = account;
        this.cardNo = cardNo;
    }

    public Account getAccount() {
        return account;
    }

    public String getCardNo() {
        return cardNo;
    }
}

/*
BankService
    knows:
        Map<String, Account>   // accountId -> Account
        Map<String, String>   // cardNo -> cardPIN
        Map<String, List<Transaction>>   // accountId -> List of Transactions
    does:
        addAccount(Account)
        removeAccount(Account)
        verifyPIN(Card, String pin) -> boolean
        checkWithdrawLimit(Account) -> double   // how much has been already withdrawn today
        checkBalance(Account) -> double
        withdraw(Account, double amount) -> WithdrawResult
        deposit(Account, double amount)
* */
class BankService {
    private Map<String, Account> accountMap;  // accountId -> Account
    private Map<String, String> cardPINMap;   // cardNo -> cardPIN
    private Map<String, List<Transaction>> transactionMap;   // accountId -> List of Transations
    private final double DAILY_WITHDRAW_LIMIT = 1000;

    public BankService() {
        this.accountMap = new HashMap<>();
        this.cardPINMap = new HashMap<>();
        this.transactionMap = new HashMap<>();
    }

    public void addAccount(Account account) {
        accountMap.putIfAbsent(account.getAccountId(), account);
    }

    public void removeAccount(Account account) {
        accountMap.remove(account.getAccountId());
    }

    public boolean verifyPIN(Card card, String pin) {
        return cardPINMap.getOrDefault(card.getCardNo(), "INVALID").equals(pin);
    }

    public double checkWithdrawLimit(Account account) {
        double withdrawnToday = 0;
        List<Transaction> transactions = transactionMap.getOrDefault(account.getAccountId(), new ArrayList<>());
        for(Transaction t: transactions) {
            if(t.getTransactionType() == TransactionType.WITHDRAW && t.getTimestamp().toLocalDate().equals(LocalDate.now())) {
                withdrawnToday += t.getAmount();
            }
        }

        return withdrawnToday;
    }

    public double checkBalance(Account account) {
        Transaction newTransation = new Transaction(account, account.getBalance(), TransactionType.CHECK_BALANCE);
        transactionMap.putIfAbsent(account.getAccountId(), new ArrayList<>());
        transactionMap.get(account.getAccountId()).add(newTransation);
        return account.getBalance();
    }

    public WithdrawResult withdraw(Account account, double amount) {
        if(account.getBalance() < amount) {
            return WithdrawResult.INSUFFICIENT_FUNDS;
        }
        else if(checkWithdrawLimit(account) + amount >= DAILY_WITHDRAW_LIMIT) {
            return WithdrawResult.DAILY_LIMIT_EXCEEDED;
        }
        else {
            account.updateBalance(amount, TransactionType.WITHDRAW);
            Transaction newTransaction = new Transaction(account, amount, TransactionType.WITHDRAW);
            transactionMap.putIfAbsent(account.getAccountId(), new ArrayList<>());
            transactionMap.get(account.getAccountId()).add(newTransaction);
            return WithdrawResult.SUCCESS;
        }
    }

    public void deposit(Account account, double amount) {
        Transaction newTransaction = new Transaction(account, amount, TransactionType.DEPOSIT);
        transactionMap.putIfAbsent(account.getAccountId(), new ArrayList<>());
        transactionMap.get(account.getAccountId()).add(newTransaction);
        account.updateBalance(amount, TransactionType.DEPOSIT);
    }
}

/*
ATMSystemState (interface)
    knows:
    does:
        insertCard(Card)
        enterPIN(String pin)
        checkBalance(Account) -> double
        withdraw(Account, double) -> WithdrawResult
        deposit(Account, double)
        ejectCard()
* */
interface ATMSystemState {
    void insertCard(Card card);
    void enterPIN(String pin);
    double checkBalance();
    WithdrawResult withdraw(double amount);
    void deposit(double amount);
    void ejectCard();
}

/*
IdleState implements ATMSystemState
    knows:
        ATMSystem
    does:
        insertCard(Card)
        enterPIN(String pin)
        checkBalance() -> double
        withdraw(double) -> WithdrawResult
        deposit(double)
        ejectCard()
* */
class IdleState implements ATMSystemState {
    private ATMSystem atmSystem;

    public IdleState(ATMSystem atmSystem) {
        this.atmSystem = atmSystem;
    }

    @Override
    public void insertCard(Card card) {
        atmSystem.setCard(card);
        atmSystem.setState(new CardInsertedState(atmSystem));
    }

    @Override
    public void enterPIN(String pin) {
        System.out.println("INVALID: Please insert a card first.");
    }

    @Override
    public double checkBalance() {
        System.out.println("INVALID: Please insert a card first.");
        return 0;
    }

    @Override
    public WithdrawResult withdraw(double amount) {
        System.out.println("INVALID: Please insert a card first.");
        return null;
    }

    @Override
    public void deposit(double amount) {
        System.out.println("INVALID: Please insert a card first.");
    }

    @Override
    public void ejectCard() {
        System.out.println("INVALID: Please insert a card first.");
    }
}

/*
CardInsertedState implements ATMSystemState
    knows:
        ATMSystem
    does:
        insertCard(Card)
        enterPIN(String pin)
        checkBalance(Account) -> double
        withdraw(Account, double) -> WithdrawResult
        deposit(Account, double)
        ejectCard()
* */
class CardInsertedState implements ATMSystemState {
    private ATMSystem atmSystem;

    public CardInsertedState(ATMSystem atmSystem) {
        this.atmSystem = atmSystem;
    }

    @Override
    public void insertCard(Card card) {
        System.out.println("INVALID: Card already inserted.");
    }

    @Override
    public void enterPIN(String pin) {
        if(atmSystem.getBankService().verifyPIN(atmSystem.getInsertedCard(), pin)) {
            atmSystem.setState(new PINVerifiedState(atmSystem));
        } else {
            atmSystem.incrementFailedAttempts();
        }
    }

    @Override
    public double checkBalance() {
        System.out.println("INVALID: Please enter your PIN first");
        return 0;
    }

    @Override
    public WithdrawResult withdraw(double amount) {
        System.out.println("INVALID: Please enter your PIN first");
        return null;
    }

    @Override
    public void deposit(double amount) {
        System.out.println("INVALID: Please enter your PIN first");
    }

    @Override
    public void ejectCard() {
        atmSystem.removeCard();
        atmSystem.setState(new IdleState(atmSystem));
    }
}

/*
PINVerifiedState implements ATMSystemState
    knows:
        ATMSystem
    does:
        insertCard(Card)
        enterPIN(String pin)
        checkBalance(Account) -> double
        withdraw(Account, double) -> WithdrawResult
        deposit(Account, double)
        ejectCard()
* */
class PINVerifiedState implements ATMSystemState {
    private ATMSystem atmSystem;

    public PINVerifiedState(ATMSystem atmSystem) {
        this.atmSystem = atmSystem;
    }

    @Override
    public void insertCard(Card card) {
        System.out.println("INVALID: Card already inserted.");
    }

    @Override
    public void enterPIN(String pin) {
        System.out.println("INVALID: PIN already verified.");
    }

    @Override
    public double checkBalance() {
        Account account = atmSystem.getInsertedCard().getAccount();
        return atmSystem.getBankService().checkBalance(account);
    }

    @Override
    public WithdrawResult withdraw(double amount) {
        if(!atmSystem.getCashDispenser().canDispense(amount)) {
            return WithdrawResult.INSUFFICIENT_FUNDS;
        }
        Account account = atmSystem.getInsertedCard().getAccount();
        WithdrawResult result = atmSystem.getBankService().withdraw(account, amount);
        if(result == WithdrawResult.SUCCESS) {
            atmSystem.getCashDispenser().withdraw(amount);
        }
        return result;
    }

    @Override
    public void deposit(double amount) {
        Account account = atmSystem.getInsertedCard().getAccount();
        atmSystem.getBankService().deposit(account, amount);
    }

    @Override
    public void ejectCard() {
        atmSystem.removeCard();
        atmSystem.setState(new IdleState(atmSystem));
    }
}

/*
TransactionCompleteState implements ATMSystemState
    knows:
        ATMSystem
    does:
        insertCard(Card)
        enterPIN(String pin)
        checkBalance(Account) -> double
        withdraw(Account, double) -> WithdrawResult
        deposit(Account, double)
        ejectCard()
* */
class TransactionCompleteState implements ATMSystemState {
    private ATMSystem atmSystem;

    public TransactionCompleteState(ATMSystem atmSystem) {
        this.atmSystem = atmSystem;
    }

    @Override
    public void insertCard(Card card) {
        System.out.println("INVALID: Card already inserted.");
    }

    @Override
    public void enterPIN(String pin) {
        System.out.println("PIN already entered and verified.");
    }

    @Override
    public double checkBalance() {
        Account account = atmSystem.getInsertedCard().getAccount();
        return atmSystem.getBankService().checkBalance(account);
    }

    @Override
    public WithdrawResult withdraw(double amount) {
        Account account = atmSystem.getInsertedCard().getAccount();
        return atmSystem.getBankService().withdraw(account, amount);
    }

    @Override
    public void deposit(double amount) {
        Account account = atmSystem.getInsertedCard().getAccount();
        atmSystem.getBankService().deposit(account, amount);
    }

    @Override
    public void ejectCard() {
        atmSystem.removeCard();
        atmSystem.setState(new IdleState(atmSystem));
    }
}

/*
CashDispenser
    knows:
        Map<Integer, Integer> denominationCount   // denomination -> count of denomination in the machine
    does:
        getTotalCash() -> double
        canDispense(double amount) -> boolean (private)
        withdraw(double amount) -> Map<Integer, Integer>
        deposit(Map<Integer, Integer> cash)
* */
class CashDispenser {
    private Map<Integer, Integer> denominationCount;   // denomination -> count of denomination in the machine

    public CashDispenser(Map<Integer, Integer> denominationCount) {
        this.denominationCount = new TreeMap<>(Collections.reverseOrder());
        this.denominationCount.putAll(denominationCount);
    }

    public double getTotalCash() {
        double result = 0;
        for(Map.Entry<Integer, Integer> entry: denominationCount.entrySet()) {
            result += entry.getKey() * entry.getValue();
        }
        return result;
    }

    public boolean canDispense(double amount) {
        if(amount > getTotalCash()) {
            return false;
        }

        double remaining = amount;
        for(Map.Entry<Integer, Integer> entry: denominationCount.entrySet()) {
            int denomination = entry.getKey();
            int available = entry.getValue();
            int needed = (int) remaining / denomination;
            int used = Math.min(available, needed);
            remaining -= denomination * used;
        }
        return remaining == 0;
    }

    public Map<Integer, Integer> withdraw(double amount) {
        if(!canDispense(amount)) {
            return null;
        }

        Map<Integer, Integer> result = new HashMap<>();
        double remaining = amount;

        for(Map.Entry<Integer, Integer> entry: denominationCount.entrySet()) {
            int denomination = entry.getKey();
            int available = entry.getValue();
            int needed = (int) remaining /  denomination;
            int used = Math.min(available, needed);
            remaining -= denomination * used;
            if(used > 0) {
                denominationCount.put(denomination, available - used);
                result.put(denomination, used);
            }
            if(remaining == 0) break;
        }

        return result;
    }

    public void deposit(Map<Integer, Integer> cash) {
        for(Map.Entry<Integer, Integer> entry: cash.entrySet()) {
            denominationCount.put(entry.getKey(), denominationCount.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }
}

/*
ATMSystem
    knows:
        BankService
        CashDispenser
        ATMSystemState
        insertedCard (Card)
        failedPINAttempts
    does:
        setState(ATMSystemState)
        insertCard(Card)
        setCard(Card)
        enterPIN(String pin)
        incrementFailedPINAttempts()
        resetFailedPINAttempts()
        checkBalance() -> double
        withdraw(double amount)
        deposit(double amount)
        ejectCard()
        removeCard() -> Card
* */

public class ATMSystem {
    private BankService bankService;
    private CashDispenser cashDispenser;
    private ATMSystemState atmSystemState;
    private Card insertedCard;
    private int failedPINAttempts;
    private final int MAX_FAILED_ATTEMPTS_ALLOWED = 3;

    public ATMSystem(BankService bankService, CashDispenser cashDispenser) {
        this.bankService = bankService;
        this.cashDispenser = cashDispenser;
        this.atmSystemState = new IdleState(this);
        this.insertedCard = null;
        this.failedPINAttempts = 0;
    }

    public BankService getBankService() {
        return bankService;
    }

    public CashDispenser getCashDispenser() {
        return cashDispenser;
    }

    public Card getInsertedCard() {
        return insertedCard;
    }

    public int getFailedPINAttempts() {
        return failedPINAttempts;
    }

    public void setState(ATMSystemState atmSystemState) {
        this.atmSystemState = atmSystemState;
    }

    public void insertCard(Card card) {
        atmSystemState.insertCard(card);
    }

    public void enterPIN(String pin) {
        atmSystemState.enterPIN(pin);
    }

    public double checkBalance() {
        return atmSystemState.checkBalance();
    }

    public void withdraw(double amount) {
        atmSystemState.withdraw(amount);
    }

    public void deposit(double amount) {
        atmSystemState.deposit(amount);
    }

    public void ejectCard() {
        atmSystemState.ejectCard();
    }

    public void resetFailedPINAttempts() {
        this.failedPINAttempts = 0;
    }

    public void incrementFailedAttempts() {
        failedPINAttempts++;
        if(failedPINAttempts == MAX_FAILED_ATTEMPTS_ALLOWED) {
            resetFailedPINAttempts();
            removeCard();
        }
    }

    public void setCard(Card card) {
        this.insertedCard = card;
    }

    public void removeCard() {
        setCard(null);
    }

}


/*

Transaction has-a Account
Transaction has-a TransactionType

Card has-a Account

BankService has-a Map of Card -> Account
BankService has-a Map of Account -> List of Transactions

IdleState is-a ATMSystemState
IdleState has-a ATMSystem

CardInsertedState is-a ATMSystemState
CardInsertedState has-a ATMSystem

PINVerifiedState is-a ATMSystemState
PINVerifiedState has-a ATMSystem

TransactionCompleteState is-a ATMSystemState
TransactionCompleteState has-a ATMSystem

ATMSystem has-a BankService
ATMSystem has-a CashDispenser
ATMSystem has-a ATMSystemState

* */
