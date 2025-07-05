import java.io.*;
import java.util.*;

class Customer {
    int id;
    String name;
    String accountType;
    double balance;
    Queue<String> transactions;

    public Customer(int id, String name, String accountType, double balance) {
        this.id = id;
        this.name = name;
        this.accountType = accountType;
        this.balance = balance;
        this.transactions = new LinkedList<>();
    }

    public void addTransaction(String transaction) {
        if (transactions.size() >= 5) {
            transactions.poll();
        }
        transactions.add(transaction);
    }

    public void printTransactionHistory() {
        System.out.println("\nTransaction History for " + name + " (" + id + "):");
        for (String t : transactions) {
            System.out.println("- " + t);
        }
    }
}

class BankQueue {
    Queue<Customer> regularQueue = new LinkedList<>();
    PriorityQueue<Customer> vipQueue = new PriorityQueue<>(Comparator.comparingInt(c -> c.id));

    public void addCustomer(Customer customer, boolean isVIP) {
        if (isVIP) {
            vipQueue.add(customer);
        } else {
            regularQueue.add(customer);
        }
        System.out.println(customer.name + " added to " + (isVIP ? "VIP" : "Regular") + " Queue.");
    }

    public Customer serveCustomer() {
        if (!vipQueue.isEmpty()) return vipQueue.poll();
        return regularQueue.poll();
    }
}

class TransactionHistory {
    private static final String FILE_PATH = "customers.txt";

    public static void saveCustomerData(Map<Integer, Customer> customers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Customer c : customers.values()) {
                writer.write(c.id + "," + c.name + "," + c.accountType + "," + c.balance + "," + String.join(";", c.transactions));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving customer data.");
        }
    }

    public static Map<Integer, Customer> loadCustomerData() {
        Map<Integer, Customer> customers = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int id = Integer.parseInt(data[0]);
                Customer customer = new Customer(id, data[1], data[2], Double.parseDouble(data[3]));
                if (data.length > 4) {
                    for (String txn : data[4].split(";")) {
                        customer.addTransaction(txn);
                    }
                }
                customers.put(id, customer);
            }
        } catch (IOException e) {
            System.out.println("No previous customer data found.");
        }
        return customers;
    }
}

class BankingApplication {
    static Scanner scanner = new Scanner(System.in);
    static Map<Integer, Customer> customers = TransactionHistory.loadCustomerData();
    static BankQueue bankQueue = new BankQueue();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Welcome to PESU Banking System ===");
            System.out.println("1. Join Queue for Service");
            System.out.println("2. Check Transaction History");
            System.out.println("3. Perform Transaction (Deposit/Withdraw)");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> joinQueue();
                case 2 -> checkTransactionHistory();
                case 3 -> performTransaction();
                case 4 -> {
                    TransactionHistory.saveCustomerData(customers);
                    System.out.println("Data saved. Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice! Try again.");
            }
        }
    }

    static void joinQueue() {
        System.out.print("Enter Customer ID: ");
        int id = scanner.nextInt();
        if (!customers.containsKey(id)) {
            System.out.print("New Customer! Enter Name: ");
            String name = scanner.next();
            System.out.print("Enter Account Type (Savings/Current): ");
            String accType = scanner.next();
            System.out.print("Enter Initial Balance: ");
            double balance = scanner.nextDouble();
            customers.put(id, new Customer(id, name, accType, balance));
        }
        System.out.print("Are you a VIP customer? (yes/no): ");
        boolean isVIP = scanner.next().equalsIgnoreCase("yes");
        bankQueue.addCustomer(customers.get(id), isVIP);
    }

    static void checkTransactionHistory() {
        System.out.print("Enter Customer ID: ");
        int id = scanner.nextInt();
        if (customers.containsKey(id)) {
            customers.get(id).printTransactionHistory();
        } else {
            System.out.println("Customer not found.");
        }
    }

    static void performTransaction() {
        System.out.print("Enter Customer ID: ");
        int id = scanner.nextInt();
        if (!customers.containsKey(id)) {
            System.out.println("Customer not found.");
            return;
        }

        Customer customer = customers.get(id);
        System.out.println("1. Deposit Money");
        System.out.println("2. Withdraw Money");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();

        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();

        if (choice == 1) {
            customer.balance += amount;
            customer.addTransaction("Deposited: $" + amount);
            System.out.println("Deposit Successful! New Balance: $" + customer.balance);
        } else if (choice == 2) {
            if (amount > customer.balance) {
                System.out.println("Insufficient funds!");
            } else {
                customer.balance -= amount;
                customer.addTransaction("Withdrew: $" + amount);
                System.out.println("Withdrawal Successful! New Balance: $" + customer.balance);
            }
        } else {
            System.out.println("Invalid choice!");
        }
    }
}
