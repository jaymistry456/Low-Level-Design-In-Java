package lowleveldesigns.elevatorsystem;

/*

enums:
ElevatorState
Direction

classes:
Elevator
ElevatorAssignmentStrategy
ElevatorSystem

* */

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/*
enums
* */
enum ElevatorStatus {
    IDLE,
    MOVING_UP,
    MOVING_DOWN,
    OUT_OF_SERVICE;
}

enum Direction {
    UP,
    DOWN;
}

/*
classes
* */
/*
Elevator
    knows:
        elevatorId
        maxCapacity
        floorNo
        TreeSet<Integer> upFloorStops
        TreeSet<Integer> downFloorStops
        ElevatorStatus
    does:
        moveUp()
        moveDown()
        openDoors()
        addFloorStop(int floorNo)
        updateStatus(ElevatorStatus)
* */
class Elevator {
    private String elevatorId;
    private int maxCapacity;
    private int currFloorNo;
    private TreeSet<Integer> upFloorStops;
    private TreeSet<Integer> downFloorStops;
    private ElevatorStatus elevatorStatus;

    public Elevator(String elevatorId, int maxCapacity) {
        this.elevatorId = elevatorId;
        this.maxCapacity = maxCapacity;
        this.currFloorNo = 0;
        this.upFloorStops = new TreeSet<>();
        this.downFloorStops = new TreeSet<>();
        this.elevatorStatus = ElevatorStatus.IDLE;
    }

    public int getCurrFloorNo() {
        return currFloorNo;
    }

    public ElevatorStatus getElevatorStatus() {
        return elevatorStatus;
    }

    public void updateStatus(ElevatorStatus elevatorStatus) {
        this.elevatorStatus = elevatorStatus;
    }

    public void moveUp() {
        this.updateStatus(ElevatorStatus.MOVING_UP);
    }

    public void moveDown() {
        this.updateStatus(ElevatorStatus.MOVING_DOWN);
    }

    public void addFloorStop(int floorNo) {
        if(floorNo < currFloorNo) {
            downFloorStops.add(floorNo);
            if(elevatorStatus == ElevatorStatus.IDLE) moveDown();
        }
        else {
            upFloorStops.add(floorNo);
            if(elevatorStatus == ElevatorStatus.IDLE) moveUp();
        }
    }
}

/*
ElevatorAssignmentStrategy
    knows:
        List<Elevator>
    does:
        findElevator(floorNo, Direction) -> Elevator
* */
class ElevatorAssignmentStrategy {
    private List<Elevator> elevators;

    public ElevatorAssignmentStrategy(List<Elevator> elevators) {
        this.elevators = elevators;
    }

    public Elevator findElevator(int floorNo, Direction direction) {
        Elevator p1 = null; // IDLE elevator OR same direction and floorNo is in the path
        Elevator p2 = null; // opposite direction and floorNo is already passed
        Elevator p3 = null; // opposite direction and floorNo is NOT yet passed

        for(Elevator elevator: elevators) {
            if(elevator.getElevatorStatus() == ElevatorStatus.OUT_OF_SERVICE) continue;

            boolean idle = elevator.getElevatorStatus() == ElevatorStatus.IDLE;
            boolean sameDirFloorInPathUp = elevator.getElevatorStatus() == ElevatorStatus.MOVING_UP && direction == Direction.UP && floorNo > elevator.getCurrFloorNo();
            boolean sameDirFloorInPathDown = elevator.getElevatorStatus() == ElevatorStatus.MOVING_DOWN && direction == Direction.DOWN && floorNo < elevator.getCurrFloorNo();
            boolean oppDirFloorPassedUp = elevator.getElevatorStatus() == ElevatorStatus.MOVING_UP && direction == Direction.DOWN && floorNo < elevator.getCurrFloorNo();
            boolean oppDirFloorPassedDown = elevator.getElevatorStatus() == ElevatorStatus.MOVING_DOWN && direction == Direction.UP && floorNo > elevator.getCurrFloorNo();
            if(idle || sameDirFloorInPathUp || sameDirFloorInPathDown) {
                p1 = elevator;
                break;
            }
            else if(oppDirFloorPassedUp || oppDirFloorPassedDown) {
                p2 = elevator;
            }
            else {
                p3 = elevator;
            }
        }

        if(p1 != null) return p1;
        if(p2 != null) return p2;
        return p3;
    }
}

/*
ElevatorSystem
    knows:
        List<Elevator>
        floors (int)
        ElevatorAssignmentStrategy
    does:
        addElevator(Elevator)
        removeElevator(Elevator)
        insideRequest(Elevator, floorNo)
        outsideRequest(floorNo, Direction)
* */
public class ElevatorSystem {
    private List<Elevator> elevators;
    private int floors;
    private ElevatorAssignmentStrategy strategy;

    public ElevatorSystem(int floors, ElevatorAssignmentStrategy strategy) {
        this.elevators = new ArrayList<>();
        this.floors = floors;
        this.strategy = strategy;
    }

    public void addElevator(Elevator elevator) {
        elevators.add(elevator);
    }

    public void removeElevator(Elevator elevator) {
        elevators.remove(elevator);
    }

    public void insideRequest(Elevator elevator, int floorNo) {
        elevator.addFloorStop(floorNo);
    }

    public void outsideRequest(int floorNo, Direction direction) {
        Elevator elevator = strategy.findElevator(floorNo, direction);
        if(elevator == null) return;
        elevator.addFloorStop(floorNo);
    }
}

/*

ElevatorAssignmentStrategy has-a List of Elevators

ElevatorSystem has-a List of Elevators
ElevatorSystem has-a ElevatorAssignmentStrategy

* */
