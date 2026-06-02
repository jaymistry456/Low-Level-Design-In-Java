package lowleveldesigns.trafficlightsystem;


/*
classes
* */
/*
TrafficLightState
GreenState
YellowState
RedState
TrafficLight
Intersection
TrafficLightSystem
* */

import java.util.ArrayList;
import java.util.List;

/*
TrafficLightState
    knows:
    does:
        display()
        next() -> TrafficLightState
* */
interface TrafficLightState {
    void display();
    TrafficLightState next();
}

/*
GreenState
    knows:
    does:
        display()
        next() -> TrafficLightState
* */
class GreenState implements TrafficLightState {
    @Override
    public void display() {
        System.out.println("GREEN Light!");
    }

    @Override
    public TrafficLightState next() {
        return new YellowState();
    }
}

/*
YellowState
    knows:
    does:
        display()
        next() -> TrafficLightState
* */
class YellowState implements TrafficLightState {
    @Override
    public void display() {
        System.out.println("YELLOW Light!");
    }

    @Override
    public TrafficLightState next() {
        return new RedState();
    }
}

/*
RedState
    knows:
    does:
        display()
        next() -> TrafficLightState
* */
class RedState implements TrafficLightState {
    @Override
    public void display() {
        System.out.println("RED State!");
    }

    @Override
    public TrafficLightState next() {
        return new GreenState();
    }
}

/*
TrafficLight
    knows:
        trafficLightId
        TrafficLightState
        int greenDuration
        int yellowDuration
        int redDuration
    does:
        setState(TrafficLightState)
        display()
        getDuration(TrafficLightState) -> int
* */
class TrafficLight {
    private String trafficLightId;
    private TrafficLightState state;
    private int greenDuration;
    private int yellowDuration;
    private int redDuration;

    public TrafficLight(String trafficLightId, int greenDuration, int yellowDuration, int redDuration) {
        this.trafficLightId = trafficLightId;
        this.greenDuration = greenDuration;
        this.yellowDuration = yellowDuration;
        this.redDuration = redDuration;
    }

    public void setState(TrafficLightState state) {
        this.state = state;
    }

    public TrafficLightState getState() {
        return state;
    }

    public void display() {
        state.display();
    }

    public int getDuration(TrafficLightState state) {
        if(state instanceof GreenState) return greenDuration;
        if(state instanceof YellowState) return yellowDuration;
        return redDuration;
    }

    public String getTrafficLightId() {
        return trafficLightId;
    }
}

/*
Intersection
    knows:
        intersectionId
        List<TrafficLight>
        isRunning
        Thread cycleThread
    does:
        cycleThrough()
        emergencySignal()
        start()
        end()
* */
class Intersection {
    private String intersectionId;
    private List<TrafficLight> lights;
    private boolean isRunning;
    private final Thread cycleThread;

    public Intersection(String intersectionId, List<TrafficLight> lights) {
        this.intersectionId = intersectionId;
        this.lights = lights;
        this.isRunning = false;
        this.cycleThread = new Thread(this::cycleThrough);
    }

    private void cycleThrough() {
       while(isRunning) {
           try {
               lights.forEach(TrafficLight::display);

               int duration = lights.getFirst().getDuration(lights.getFirst().getState());
               Thread.sleep(duration * 1000L);
               lights.forEach(l -> l.setState(l.getState().next()));
           } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               break;
           }
       }
    }

    public void emergencySignal() {
        lights.forEach(l -> l.setState(new RedState()));
    }

    public void start() {
        isRunning = true;
        for(int i = 0; i < lights.size(); i++) {
            if(i % 2 == 0) {
                lights.get(i).setState(new GreenState());
            } else {
                lights.get(i).setState(new RedState());
            }
        }
        cycleThread.start();
    }

    public void end() {
        isRunning = false;
        cycleThread.interrupt();
    }

    public String getIntersectionId() {
        return intersectionId;
    }
}

/*
TrafficLightSystem
    knows:
        List<Intersection>
    does:
        addIntersection(Intersection)
        removeIntersection(Intersection)
        start(Intersection)
        end(Intersection)
* */
public class TrafficLightSystem {
    private List<Intersection> intersections;

    public TrafficLightSystem() {
        this.intersections = new ArrayList<>();
    }

    public void addIntersection(Intersection intersection) {
        intersections.add(intersection);
    }

    public void removeIntersection(Intersection intersection) {
        intersections.remove(intersection);
    }

    public void start(Intersection intersection) {
        intersection.start();
    }

    public void end(Intersection intersection) {
        intersection.end();
    }
}

/*

GreenState is-a TrafficLightState
YellowState is-a TrafficLightState
RedState is-a TrafficLightState

TrafficLight has-a TrafficLightState

Intersection has-a List of TrafficLight

TrafficLightSystem has-a List of Intersection

* */