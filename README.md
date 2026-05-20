# Brainless Library

Game AI framework and utilities, WIP!

### Btw
It started as a code experiment, maybe it will end up as such.

### Warning:
It has nothing to do with generative AI or complex neural networks.

### Example of villagier AI
```java
AI ai = AI.create(brain -> {
    brain.inputs()
        .add("hunger",  0.0)
        .add("fatigue", 0.0)
        .add("danger",  0.0)
        .add("work",    0.0);

    brain.layer("desires", layer -> {
        layer.mix("desire_eat")
            .add("hunger", 1.0).sub("danger", 0.7).sigmoid(6);
        layer.mix("desire_sleep")
            .add("fatigue", 1.0).sub("danger", 0.9).sigmoid(6);
        layer.mix("desire_flee")
            .add("danger", 1.0).sigmoid(8);
        layer.mix("desire_work")
            .add("work", 1.0).sub("hunger", 0.5).sub("fatigue", 0.4).sigmoid(5);
    });
});

JobQueue queue = ai.queue(JobQueue.Mode.HIGHEST_WINS, 0.25, 3);
queue.register("Eat",   ai.getBrain().getNode("desire_eat"),   () -> System.out.println("Villager eats"));
queue.register("Sleep", ai.getBrain().getNode("desire_sleep"), () -> System.out.println("Villager sleeps"), () -> System.out.println("Sleep interrupted"));
queue.register("Flee",  ai.getBrain().getNode("desire_flee"),  () -> System.out.println("Villager flees!"), () -> System.out.println("Flee interrupted"));
queue.register("Work",  ai.getBrain().getNode("desire_work"),  () -> System.out.println("Villager works"));

// Each game tick:
ai.update(in -> in
    .set("hunger",  0.8)
    .set("fatigue", 0.3)
    .set("danger",  0.0)
    .set("work",    0.5));
```

### Queue modes

| Mode | Behavior |
|------|----------|
| `HIGHEST_WINS` | Most urgent desire always first |
| `FIFO_TIERED` | Sorted into high/mid/low tiers, FIFO within each |
| `WEIGHTED_RANDOM` | Higher desire = higher chance, not guaranteed |

Queue pre-calculates next N jobs. If total desire delta exceeds threshold, discards and rebuilds.

### Connection ops

Built-in: `add`, `sub`, `mul`, `div`, `max`, `min`.  
Custom:
```java
layer.mix("desire_eat").op("hunger", (a, b) -> a + b * 1.5, 0.8);
```
````
