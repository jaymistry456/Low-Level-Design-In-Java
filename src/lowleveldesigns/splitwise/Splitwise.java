package lowleveldesigns.splitwise;

/*
classes
* */
/*
User
Group
Split
Expense
ExpenseSplitStrategy
Splitwise
* */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/*
User
    knows:
        userId
        name
        email
        phoneNo
    does:
        nothing (data carrier)
* */
class User {
    private String userId;
    private String name;
    private String email;
    private int phoneNo;

    public User (String userId, String name, String email, int phoneNo) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
    }

    public String getUserId () {
        return userId;
    }

    public String getName () {
        return name;
    }

    public String getEmail () {
        return email;
    }

    public int getPhoneNo () {
        return phoneNo;
    }
}

/*
Split
    knows:
        User
        amount
    does:
        nothing (data carrier)
* */
class Split {
    private User user;
    private Double amount;

    public Split(User user, Double amount) {
        this.user = user;
        this.amount = amount;
    }

    public User getUser() {
        return user;
    }

    public Double getAmount() {
        return amount;
    }
}

/*
SplitStrategy (interface)
    Knows:
    does:
        split(List<User>, amount, Map<User, Double> customValues) -> List<Split>
* */
interface SplitStrategy {
    List<Split> split(List<User> users, Double amount, Map<User, Double> customValues);
}

/*
EqualStrategy implements SplitStrategy
    Knows:
    does:
        split(List<User>, amount) -> List<Split>
* */
class EqualStrategy implements SplitStrategy {
    @Override
    public List<Split> split(List<User> users, Double amount, Map<User, Double> customValues) {
        List<Split> splits = new ArrayList<>();
        double share = amount / users.size();
        for(User user: users) {
            splits.add(new Split(user, share));
        }
        return splits;
    }
}

/*
ExactStrategy implements SplitStrategy
    Knows:
    does:
        split(List<User>, amount, Map<User, Double> exactAmounts) -> List<Split>
 * */
class ExactStrategy implements SplitStrategy {
    public List<Split> split(List<User> users, Double amount, Map<User, Double> exactAmounts) {
        List<Split> splits = new ArrayList<>();
        for(Map.Entry<User, Double> entry: exactAmounts.entrySet()) {
            splits.add(new Split(entry.getKey(), entry.getValue()));
        }
        return splits;
    }
}

/*
PercentageStrategy implements SplitStrategy
    Knows:
    does:
        split(List<User>, amount, Map<User, Double> percentages) -> List<Split>
 * */
class PercentageStrategy implements SplitStrategy {
    public List<Split> split (List<User> users, Double amount, Map<User, Double> percentages) {
        List<Split> splits = new ArrayList<>();
        for(Map.Entry<User, Double> entry: percentages.entrySet()) {
            splits.add(new Split(entry.getKey(), entry.getValue() * amount));
        }
        return splits;
    }
}

/*
Expense
    knows:
        expenseId
        User (paidBy)
        amount
        description
        timestamp
        List<Split>
        SplitStrategy
    does:
        nothing (data carrier)
* */
class Expense {
    private String expenseId;
    private User paidBy;
    private Double amount;
    private String description;
    private LocalDateTime timestamp;
    private List<Split> splits;
    private SplitStrategy strategy;

    public Expense(String expenseId,
                   User paidBy,
                   List<User> users,
                   Double amount,
                   String description,
                   SplitStrategy strategy,
                   Map<User, Double> customValues
    ) {
        this.expenseId = expenseId;
        this.paidBy = paidBy;
        this.amount = amount;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.strategy = strategy;
        this.splits = strategy.split(users, amount, customValues);
    }

    public String getExpenseId() {
        return expenseId;
    }

    public User getPaidBy() {
        return paidBy;
    }

    public Double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<Split> getSplits() {
        return splits;
    }
}

/*
Group
    knows:
        List<User>
        List<Expense>
        Map<String, Map<String, Double>>   // userId -> Map of userId -> amount they owe
        lock
    does:
        enterGroup(User)
        exitGroup(User)
        addExpense(Expense)
        removeExpense(Expense)
        settle(User, User, amount)
* */
class Group {
    private List<User> users;
    private List<Expense> expenses;
    private Map<String, Map<String, Double>> balances;
    private final ReentrantLock lock = new ReentrantLock();

    public Group() {
        this.users = new ArrayList<>();
        this.expenses = new ArrayList<>();
        this.balances = new HashMap<>();
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public Map<String, Map<String, Double>> getBalances() {
        return balances;
    }

    public void enterGroup(User user) {
        users.add(user);
    }

    public void exitGroup(User user) {
        users.remove(user);
    }

    private void updateBalance(String creditorId, String debtorId, double amount) {
        balances.putIfAbsent(creditorId, new HashMap<>());
        balances.putIfAbsent(debtorId, new HashMap<>());

        double currentCredit = balances.get(creditorId).getOrDefault(debtorId, 0.0);

        if(currentCredit > 0) {
            double net = currentCredit - amount;
            if(net > 0) {
                balances.get(creditorId).put(debtorId, net);
            }
            else if(net < 0) {
                balances.get(creditorId).remove(debtorId);
                balances.get(debtorId).put(creditorId, Math.abs(net));
            }
            else {
                balances.get(creditorId).remove(debtorId);
            }
        }
        else {
            balances.get(debtorId).put(creditorId, balances.get(debtorId).getOrDefault(creditorId, 0.0) + amount);
        }
    }

    public void addExpense(Expense expense) {
        lock.lock();
        try {
            expenses.add(expense);

            for(Split split: expense.getSplits()) {
                if(!split.getUser().getUserId().equals(expense.getPaidBy().getUserId())) {
                    updateBalance(expense.getPaidBy().getUserId(), split.getUser().getUserId(), split.getAmount());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeExpense(Expense expense) {
        lock.lock();
        try {
            expenses.remove(expense);

            for(Split split: expense.getSplits()) {
                if(!split.getUser().getUserId().equals(expense.getPaidBy().getUserId())) {
                    updateBalance(split.getUser().getUserId(), expense.getPaidBy().getUserId(), split.getAmount());
                }
            }
        } finally {
            lock.unlock();
        }


    }

    public void settle(User settler, User settlee, Double amount) {
        lock.lock();
        try {
            updateBalance(settlee.getUserId(), settler.getUserId(), amount);
        } finally {
            lock.unlock();
        }
    }
}

/*
Splitwise
    knows:
        List<User>
        List<Group>
    does:
        addUser(User)
        removeUser(User)
        addGroup(User)
        removeGroup(User)
        joinGroup(User, Group)
        exitGroup(User, Group)
        addExpense(Group, Expense)
        removeExpense(Group, Expense)
        settleBalance(User, User, Group, amount)
* */
public class Splitwise {
    private List<User> users;
    private List<Group> groups;

    public Splitwise() {
        this.users = new ArrayList<>();
        this.groups = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public void removeGroup(Group group) {
        groups.remove(group);
    }

    public void joinGroup(User user, Group group) {
        group.enterGroup(user);
    }

    public void exitGroup(User user, Group group) {
        group.exitGroup(user);
    }

    public void addExpense(Group group, Expense expense) {
        group.addExpense(expense);
    }

    public void removeExpense(Group group, Expense expense) {
        group.removeExpense(expense);
    }

    public void settleBalance(User settler, User settlee, Group group, Double amount) {
        group.settle(settler, settlee, amount);
    }
}

/*

Group has-a List of Users
Group has-a List of Expenses

EqualStrategy is-a SplitStrategy
ExactStrategy is-a SplitStrategy
PercentageStrategy is-a SplitStrategy

Split has-a User

Expense has-a User (paidBy)
Expense has-a List of Splits
Expense has-a SplitStrategy

Splitwise has-a List of Users
Splitwise has-a List of Groups

* */
