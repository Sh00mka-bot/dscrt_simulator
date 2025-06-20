import java.util.*;

public class SimulatorPA2 {
    static final int ARRIVAL   = 0;
    static final int DEPARTURE = 1;

    public static void main(String[] args) {

        double arrivalRate     = Double.parseDouble(args[0]);
        double avgServiceTime  = Double.parseDouble(args[1]);
        int    runNum = 10_000;

        Random rand = new Random();
        LinkedList<Event> eventList = new LinkedList<>();
        Queue<Process>    readyQ   = new LinkedList<>();

        double clock       = 0.0;
        boolean cpuIdle    = true;
        int     completed  = 0, nextId = 1;
        double  sumTurn    = 0.0, busyTime = 0.0;


        double firstArrival = -Math.log(rand.nextDouble()) / arrivalRate;
        eventList.add(new Event(
                ARRIVAL,
                firstArrival,
                new Process(nextId++, firstArrival,
                        -Math.log(rand.nextDouble()) * avgServiceTime)
        ));

        double lastEventTime = 0.0;
        double areaQueue     = 0.0;

        while (completed < runNum) {

            eventList.sort((a, b) -> Double.compare(a.time, b.time));
            Event ev = eventList.removeFirst();
            clock = ev.time;


            double dt = clock - lastEventTime;
            areaQueue += readyQ.size() * dt;
            lastEventTime = clock;

            if (ev.type == ARRIVAL) {

                double nextArr = clock + (-Math.log(rand.nextDouble()) / arrivalRate);
                eventList.add(new Event(
                        ARRIVAL, nextArr,
                        new Process(nextId++, nextArr,
                                -Math.log(rand.nextDouble()) * avgServiceTime)
                ));

                if (cpuIdle) {
                    cpuIdle = false;
                    busyTime += ev.p.service;
                    eventList.add(new Event(
                            DEPARTURE, clock + ev.p.service, ev.p
                    ));
                } else {
                    readyQ.offer(ev.p);
                }

            } else {
                sumTurn += (clock - ev.p.arrival);
                completed++;

                if (readyQ.isEmpty()) {
                    cpuIdle = true;
                } else {
                    Process next = readyQ.poll();
                    busyTime += next.service;
                    eventList.add(new Event(
                            DEPARTURE, clock + next.service, next
                    ));
                }
            }
        }

        double throughput    = runNum / clock;
        double avgQueueLength= areaQueue / clock;

        System.out.printf(
                "Given: arrivalRate=%.0f, avgServiceTime=%.2f \n" +
                        "Results: AvgTurnAr=%.4f, Throughput=%.4f, CPU-util=%.4f, ReadyQueue=%.4f%n",
                arrivalRate, avgServiceTime,
                sumTurn/runNum,
                throughput,
                busyTime/clock,
                avgQueueLength
        );
    }




    static class Process {
        int    id;
        double arrival, service;
        Process(int id, double a, double s) {
            this.id = id;
            arrival = a;
            service = s;
        }
    }

    static class Event {
        int      type;
        double   time;
        Process  p;
        Event(int t, double tm, Process p) {
            type = t; time = tm; this.p = p;
        }
    }


}
