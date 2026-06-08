package lowleveldesigns.coffeemachine;

import java.util.HashMap;
import java.util.Map;

/*
enums
* */
enum MachineStatus {
    IDLE,
    BREWING,
    OUT_OF_INGREDIENTS,
    ERROR;
}

enum CoffeeType {
    ESPRESSO,
    LATTE,
    CAPPUCCINO,
    AMERICANO;
}

enum IngredientType {
    WATER,
    MILK,
    COFFEE_BEANS,
    SUGAR;
}

/*
classes
* */
/*
Coffee (interface)
Espresso
Latte
Cappuccino
Americano
CoffeeFactory
CoffeeMachine
* */

/*
Coffee
    knows:
    does:
        getCoffeeType() -> CoffeeType
        getRequirements() -> Map<IngredientType, Integer>
        brewRecipe()
* */
interface Coffee {
    CoffeeType getCoffeeType();
    Map<IngredientType, Integer> getRequirements();
    void brewRecipe();
}

/*
Espresso
    knows:
        CoffeeType
        CoffeeMachine
        Map<IngredientType, Integer> requirements
    does:
        getRequirements() -> Map<IngredientType, Integer>
        brewRecipe()
* */
class Espresso implements Coffee {
    private CoffeeType coffeeType;
    private Map<IngredientType, Integer> requirements;

    public Espresso() {
        this.coffeeType = CoffeeType.ESPRESSO;
        // Initialize requirements
        this.requirements = new HashMap<>();
        this.requirements.put(IngredientType.COFFEE_BEANS, 5);
        this.requirements.put(IngredientType.MILK, 2);
        this.requirements.put(IngredientType.SUGAR, 3);
        this.requirements.put(IngredientType.WATER, 4);
    }

    @Override
    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

    @Override
    public Map<IngredientType, Integer> getRequirements() {
        return requirements;
    }

    @Override
    public void brewRecipe() {
        System.out.println("BREWING Espresso...");
    }
}

/*
Latte
    knows:
        CoffeeType
        CoffeeMachine
        Map<IngredientType, Integer> requirements
    does:
        getIngredientRequirements() -> Map<IngredientType, Integer>
        brewRecipe()
        makeCoffee()
* */
class Latte implements Coffee {
    private CoffeeType coffeeType;
    private Map<IngredientType, Integer> requirements;

    public Latte() {
        this.coffeeType = CoffeeType.LATTE;
        // Initialize requirements
        this.requirements = new HashMap<>();
        this.requirements.put(IngredientType.COFFEE_BEANS, 5);
        this.requirements.put(IngredientType.MILK, 2);
        this.requirements.put(IngredientType.SUGAR, 3);
        this.requirements.put(IngredientType.WATER, 4);
    }

    @Override
    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

    @Override
    public Map<IngredientType, Integer> getRequirements() {
        return requirements;
    }

    @Override
    public void brewRecipe() {
        System.out.println("BREWING Latte...");
    }
}

/*
Cappuccino
    knows:
        CoffeeType
        CoffeeMachine
        Map<IngredientType, Integer> requirements
    does:
        getIngredientRequirements() -> Map<IngredientType, Integer>
        brewRecipe()
        makeCoffee()
* */
class Cappuccino implements Coffee {
    private CoffeeType coffeeType;
    private Map<IngredientType, Integer> requirements;

    public Cappuccino() {
        this.coffeeType = CoffeeType.CAPPUCCINO;
        // Initialize requirements
        this.requirements = new HashMap<>();
        this.requirements.put(IngredientType.COFFEE_BEANS, 5);
        this.requirements.put(IngredientType.MILK, 2);
        this.requirements.put(IngredientType.SUGAR, 3);
        this.requirements.put(IngredientType.WATER, 4);
    }

    @Override
    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

    @Override
    public Map<IngredientType, Integer> getRequirements() {
        return requirements;
    }

    @Override
    public void brewRecipe() {
        System.out.println("BREWING Cappuccino...");
    }
}

/*
Americano
    knows:
        CoffeeType
        CoffeeMachine
        Map<IngredientType, Integer> requirements
    does:
        getIngredientRequirements() -> Map<IngredientType, Integer>
        brewRecipe()
        makeCoffee()
* */
class Americano implements Coffee {
    private CoffeeType coffeeType;
    private Map<IngredientType, Integer> requirements;

    public Americano() {
        this.coffeeType = CoffeeType.AMERICANO;
        // Initialize requirements
        this.requirements = new HashMap<>();
        this.requirements.put(IngredientType.COFFEE_BEANS, 5);
        this.requirements.put(IngredientType.MILK, 2);
        this.requirements.put(IngredientType.SUGAR, 3);
        this.requirements.put(IngredientType.WATER, 4);
    }

    @Override
    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

    @Override
    public Map<IngredientType, Integer> getRequirements() {
        return requirements;
    }

    @Override
    public void brewRecipe() {
        System.out.println("BREWING Americano...");
    }
}

/*
CoffeeFactory
    knows:
    does:
        getCoffee(CoffeeType) -> Coffee
* */
class CoffeeFactory {
    public static Coffee getCoffee(CoffeeType coffeeType) {
        return switch(coffeeType) {
            case ESPRESSO -> new Espresso();
            case LATTE -> new Latte();
            case CAPPUCCINO -> new Cappuccino();
            case AMERICANO -> new Americano();
        };
    }
}

/*
CoffeeMachine
    knows:
        MachineStatus
        CoffeeFactory
        Map<IngredientType, Integer> ingredients
    does:
        addIngredient(IngredientType, quantity)
        removeIngredients(Coffee)
        areIngredientsAvailable(Coffee) -> boolean
        makeCoffee(Coffee)
        updateStatus(MachineStatus)
* */
public class CoffeeMachine {
    private MachineStatus status;
    private CoffeeFactory coffeeFactory;
    private Map<IngredientType, Integer> ingredients;

    public CoffeeMachine(CoffeeFactory coffeeFactory) {
        this.status = MachineStatus.IDLE;
        this.coffeeFactory = coffeeFactory;
        this.ingredients = new HashMap<>();
    }

    public void addIngredient(IngredientType ingredientType, int quantity) {
        ingredients.put(
                ingredientType,
                ingredients.getOrDefault(ingredientType, 0) + quantity
        );
    }

    public void removeIngredients(Coffee coffee) {
        for(Map.Entry<IngredientType, Integer> requirements: coffee.getRequirements().entrySet()) {
            IngredientType ingredientType = requirements.getKey();
            int quantity = requirements.getValue();
            ingredients.put(
                    ingredientType,
                    ingredients.get(ingredientType) - quantity
            );
        }
    }

    public boolean areIngredientsAvailable(Coffee coffee) {
        Map<IngredientType, Integer> requirements = coffee.getRequirements();
        for(Map.Entry<IngredientType, Integer> req: requirements.entrySet()) {
            if(ingredients.getOrDefault(req.getKey(), 0) < req.getValue()) {
                return false;
            }
        }
        return true;
    }

    public void makeCoffee(CoffeeType coffeeType) {
        Coffee coffee = CoffeeFactory.getCoffee(coffeeType);
        if(!areIngredientsAvailable(coffee)) {
            updateStatus(MachineStatus.OUT_OF_INGREDIENTS);
            System.out.println("Ingredients NOT available");
            return;
        }
        updateStatus(MachineStatus.BREWING);
        removeIngredients(coffee);
        coffee.brewRecipe();
        updateStatus(MachineStatus.IDLE);
    }

    public void updateStatus(MachineStatus status) {
        this.status = status;
    }
}

/*

Espresso has-a CoffeeMachine
Espresso is-a Coffee
Espresso has-a CoffeeType
Espresso has-a Map of IngredientType -> quantity required

Latte has-a CoffeeMachine
Latte is-a Coffee
Latte has-a CoffeeType
Latte has-a Map of IngredientType -> quantity required

Cappuccino has-a CoffeeMachine
Cappuccino is-a Coffee
Cappuccino has-a CoffeeType
Cappuccino has-a Map of IngredientType -> quantity required

Americano has-a CoffeeMachine
Americano is-a Coffee
Americano has-a CoffeeType
Americano has-a Map of IngredientType -> quantity required

CoffeeMachine has-a CoffeeMachineState
CoffeeMachine has-a CoffeeFactory
CoffeeMachine has-a Map of IngredientType -> quantity available

* */