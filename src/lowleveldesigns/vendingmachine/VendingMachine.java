    package lowleveldesigns.vendingmachine;

    /*
    classes
    * */
    /*
    Product
    Slot
    VendingMachineState (interface)
    IdleState implements VendingMachineState
    ProductSelectedState implements VendingMachineState
    MoneyInsertedState implements VendingMachineState
    DispensingState implements VendingMachineState
    VendingMachine
    * */

    import java.util.HashMap;
    import java.util.Map;

    /*
    Product
        knows:
            productId
            productName
            price
        does:
            nothing (data carrier)
    * */
    class Product {
        private String productId;
        private String productName;
        private double price;

        public Product(String productId, String productName, double price) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }
    }

    /*
    DispenseResult (record class containing Product and change)
    * */
    record DispenseResult(Product product, double change) {}

    /*
    Slot
        knows:
            Product
            quantity
        does:
            dispense() -> Product
            isEmpty() -> boolean
    * */
    class Slot {
        private Product product;
        private int quantity;

        public Slot(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public Product dispense() {
            quantity--;
            return product;
        }

        public boolean isEmpty() {
            return quantity == 0;
        }
    }

    /*
    VendingMachineState
        knows:
        does:
            selectProduct(Product)
            insertMoney(double amount)
            dispense() -> DispenseResult
            cancel() -> amount
    * */
    interface VendingMachineState {
        void selectProduct(Product product);
        void insertMoney(double amount);
        DispenseResult dispense();
        double cancel();
    }

    /*
    selectProduct() -> transitions to ProductSelectedState
    insertMoney() -> INVALID
    dispense() -> INVALID
    cancel() -> INVALID

    IdleState implements VendingMachineState
        knows:
            VendingMachine (reference for state transitions)
        does:
            selectProduct(Product)
            insertMoney(double amount)
            dispense() -> DispenseResult
            cancel() -> amount
    * */
    class IdleState implements VendingMachineState {
        private VendingMachine vendingMachine;

        public IdleState(VendingMachine vendingMachine) {
            this.vendingMachine = vendingMachine;
        }

        @Override
        public void selectProduct(Product product) {
            vendingMachine.setSelectedProduct(product);
            vendingMachine.setState(new ProductSelectedState(vendingMachine));
        }

        @Override
        public void insertMoney(double amount) {
            System.out.println("INVALID: Please select a Product first.");
        }

        @Override
        public DispenseResult dispense() {
            System.out.println("INVALID: Please select a Product first.");
            return null;
        }

        @Override
        public double cancel() {
            System.out.println("INVALID: Please select a Product first.");
            return 0.0;
        }
    }

    /*
    selectProduct() -> INVALID
    insertMoney() -> transitions to MoneyInsertedState
    dispense() -> INVALID
    cancel() -> VALID and deselects the selectedProduct and transitions to IdleState

    ProductSelectedState implements VendingMachineState
        knows:
            VendingMachine (reference for state transitions)
        does:
            selectProduct(Product)
            insertMoney(double amount)
            dispense() -> DispenseResult
            cancel() -> amount
    * */
    class ProductSelectedState implements VendingMachineState {
        private VendingMachine vendingMachine;

        public ProductSelectedState(VendingMachine vendingMachine) {
            this.vendingMachine = vendingMachine;
        }

        @Override
        public void selectProduct(Product product) {
            System.out.println("INVALID: Product already selected. Please insert money or cancel.");
        }

        @Override
        public void insertMoney(double amount) {
            vendingMachine.addInsertedAmount(amount);
            vendingMachine.setState(new MoneyInsertedState(vendingMachine));
        }

        @Override
        public DispenseResult dispense() {
            System.out.println("INVALID: Please insert money first.");
            return null;
        }

        @Override
        public double cancel() {
            double amount = vendingMachine.returnMoney();
            vendingMachine.setState(new IdleState(vendingMachine));
            return amount;
        }
    }

    /*
    selectProduct() -> INVALID
    insertMoney() -> VALID (user may need to add more money to reach the Product price)
    dispense() -> DispenseResult and transitions to IdleState
    cancel() -> VALID and deselects the selectedProduct, returns back the money and transitions to IdleState

    MoneyInsertedState implements VendingMachineState
        knows:
            VendingMachine (reference for state transitions)
        does:
            selectProduct(Product)
            insertMoney(double amount)
            dispense() -> DispenseResult
            cancel() -> amount
    * */
    class MoneyInsertedState implements VendingMachineState {
        private VendingMachine vendingMachine;

        public MoneyInsertedState(VendingMachine vendingMachine) {
            this.vendingMachine = vendingMachine;
        }

        @Override
        public void selectProduct(Product product) {
            System.out.println("INVALID: Product already selected and money already inserted. Please either dispense or cancel");
        }

        @Override
        public void insertMoney(double amount) {
            double moneyInserted = vendingMachine.getInsertedAmount();
            Product selectedProduct = vendingMachine.getSelectedProduct();
            if(moneyInserted < selectedProduct.getPrice()) {
                vendingMachine.addInsertedAmount(amount);
            }
            else {
                System.out.println("INVALID: Enough money inserted already. Please either dispense or cancel.");
            }
        }

        @Override
        public DispenseResult dispense() {
            double moneyInserted = vendingMachine.getInsertedAmount();
            Product selectedProduct = vendingMachine.getSelectedProduct();
            double diff = moneyInserted - selectedProduct.getPrice();
            if(diff >= 0) {
                vendingMachine.setState(new DispensingState(vendingMachine));
                vendingMachine.dispenseFromSlot(selectedProduct);
                vendingMachine.setSelectedProduct(null);
                vendingMachine.resetAmount();
                vendingMachine.setState(new IdleState(vendingMachine));
                return new DispenseResult(selectedProduct, diff);
            }
            else {
                System.out.println("INVALID: Please insert atleast" + Math.abs(diff) + " to reach the Product price.");
                return null;
            }
        }

        @Override
        public double cancel() {
            double amountToReturn = vendingMachine.returnMoney();
            vendingMachine.setState(new IdleState(vendingMachine));
            return amountToReturn;
        }
    }

    /*
    selectProduct() -> INVALID
    insertMoney() -> INVALID
    dispense() -> INVALID
    cancel() -> INVALID

    DispensingState implements VendingMachineState
        knows:
            VendingMachine (reference for state transitions)
        does:
            selectProduct(Product)
            insertMoney(double amount)
            dispense() -> DispenseResult
            cancel() -> amount
    * */
    class DispensingState implements VendingMachineState {
        private VendingMachine vendingMachine;

        public DispensingState(VendingMachine vendingMachine) {
            this.vendingMachine = vendingMachine;
        }

        @Override
        public void selectProduct(Product product) {
            System.out.println("INVALID: Machine is in dispensing state. Please wait...");
        }

        @Override
        public void insertMoney(double amount) {
            System.out.println("INVALID: Machine is in dispensing state. Please wait...");
        }

        @Override
        public DispenseResult dispense() {
            System.out.println("INVALID: Machine is in dispensing state. Please wait...");
            return null;
        }

        @Override
        public double cancel() {
            System.out.println("INVALID: Machine is in dispensing state. Please wait...");
            return 0.0;
        }
    }

    /*
    VendingMachine
        knows:
            VendingMachineState
            Map<String, Slot>   // productId -> Slot
            selectedProduct (Product)
            insertedAmount
        does:
            addProductToSlot(Product, Slot)
            removeProductFromSlot(Product)
            selectProduct(Product)
            insertMoney(double amount)
            dispense() -> DispenseResult
            cancel() -> amount
            setState(VendingMachineState)
            addInsertedAmount(double amount)
    * */
    public class VendingMachine {
        private VendingMachineState vendingMachineState;
        private Map<String, Slot> map;
        private Product selectedProduct;
        private double insertedAmount;

        public VendingMachine() {
            this.map = new HashMap<>();
            this.selectedProduct = null;
            this.insertedAmount = 0.0;
            this.vendingMachineState = new IdleState(this);
        }

        public void addProductToSlot(Product product, Slot slot) {
            map.putIfAbsent(product.getProductId(), slot);
        }

        public void removeProductFromSlot(Product product) {
            map.remove(product.getProductId());
        }

        public void selectProduct(Product product) {
            vendingMachineState.selectProduct(product);
        }

        public void setSelectedProduct(Product product) {
            this.selectedProduct = product;
        }

        public Product getSelectedProduct() {
            return selectedProduct;
        }

        public void insertMoney(double amount) {
            vendingMachineState.insertMoney(amount);
        }

        public void addInsertedAmount(double amount) {
            this.insertedAmount += amount;
        }

        public double returnMoney() {
            double amount = insertedAmount;
            insertedAmount = 0.0;
            return amount;
        }

        public void resetAmount() {
            insertedAmount = 0.0;
        }

        public double getInsertedAmount() {
            return this.insertedAmount;
        }

        public DispenseResult dispense() {
            return vendingMachineState.dispense();
        }

        public void dispenseFromSlot(Product product) {
            Slot slot = map.get(product.getProductId());
            slot.dispense();
        }

        public double cancel() {
            return vendingMachineState.cancel();
        }

        public void setState(VendingMachineState vendingMachineState) {
            this.vendingMachineState = vendingMachineState;
        }
    }

    /*

    DispenseResult has-a Product

    Slot has-a Product

    IdleState is-a VendingMachineState
    IdleState has-a VendingMachine

    ProductSelectedState is-a VendingMachineState
    ProductSelectedState has-a VendingMachine

    MoneyInsertedState is-a VendingMachineState
    MoneyInsertedState has-a VendingMachine

    DispensingState is-a VendingMachineState
    DispensingState has-a VendingMachine

    VendingMachine has-a VendingMachineState
    VendingMachine has-a Map of Product -> Slot
    VendingMachine has-a Product (selectedProduct)

    * */
