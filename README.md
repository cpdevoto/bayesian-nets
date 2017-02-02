# bayesian-nets
Prototypes for the creation of Bayesian Networks.

## baysesian-net-prototype
A basic prototype showing a fully functional Bayesian network with minimal functionality.  This prototype supports the following features:
* Construct a fixed model consisting of binary variables joined by directed edges.
* For each variable, input the values for the associated conditional probability table.
* Issue conditional probability queries against the network in order to answer questions.

### sample client code
```java
import org.devoware.bayesian.prototype.Network;
import org.devoware.bayesian.prototype.RandomVariable;

public class Main {

  public static void main(String[] args) {
    // Instantiate a network
    Network network = new Network();
    
    // Define all of the random variables
    RandomVariable cloudy = network.newVariable("C", "Cloudy");
    RandomVariable sprinkler = network.newVariable("S", "Sprinkler");
    RandomVariable raining = network.newVariable("R", "Raining");
    RandomVariable wetGrass = network.newVariable("W", "Wet Grass");
    
    // Connect the random variables into parent-child relationships
    cloudy.addChild(sprinkler);
    cloudy.addChild(raining);
    sprinkler.addChild(wetGrass);
    raining.addChild(wetGrass);
    
    // Fill in the Conditional Probability Table for each variable
    
    // Cloudy CPT
    cloudy.getCpt().put("P(C)", 0.5); // probability that it is cloudy
    
    // Sprinkler CPT
    sprinkler.getCpt().put("P(S|C)", 0.1);    // probability that the sprinkler is on when it's cloudy
    sprinkler.getCpt().put("P(S|~C)", 0.5);   // probability that the sprinkler is on when it's not cloudy
    
    // Raining CPT
    raining.getCpt().put("P(R|C)", 0.8);      // probability that it's raining when its cloudy
    raining.getCpt().put("P(R|~C)", 0.2);     // probability that it's raining when its not cloudy
    
    // Wet Grass CPT
    wetGrass.getCpt().put("P(W|S,R)", 0.99);  // probability that the grass is wet when the sprinkler is on and it's raining
    wetGrass.getCpt().put("P(W|S,~R)", 0.9);  // probability that the grass is wet when the sprinkler is on and it's not raining
    wetGrass.getCpt().put("P(W|~S,R)", 0.9);  // probability that the grass is wet when the sprinkler is off and it's raining
    wetGrass.getCpt().put("P(W|~S,~R)", 0.0); // probability that the grass is wet when the sprinkler is off and it's not raining 
    
    // Execute conditional probability queries against the model
    
    // Example 1: If the grass is currently wet and the sky is cloudy, is it more likely 
    // that it is raining or that the sprinkler is on?
    
    System.out.println("EXAMPLE 1:\n\nQ. What's the most probable immediate cause for the grass being wet when the sky is cloudy?");
    
    double probabilityRaining = network.query("P(R|W,C)");
    double probabilitySprinkler = network.query("P(S|W,C)");
    
    if (probabilityRaining > probabilitySprinkler) {
      System.out.println("A. The most probable immediate cause is that it is raining.");
    } else if (probabilityRaining < probabilitySprinkler) {
      System.out.println("A. The most probable immediate cause is that the sprinkler is on.");
    } else {
      System.out.println("A. There is an equal probability that the immediate cause is that it's raining or that the sprinkler is on.");
    }
    
    // Example 2: If the grass is currently wet and the sky isn't cloudy, is it more likely 
    // that it is raining or that the sprinkler is on?
    
    System.out.println("\nEXAMPLE 2:\n\nQ. What's the most probable immediate cause for the grass being wet when the sky is clear?");
    
    probabilityRaining = network.query("P(R|W,~C)");
    probabilitySprinkler = network.query("P(S|W,~C)");
    
    if (probabilityRaining > probabilitySprinkler) {
      System.out.println("A. The most probable immediate cause is that it is raining.");
    } else if (probabilityRaining < probabilitySprinkler) {
      System.out.println("A. The most probable immediate cause is that the sprinkler is on.");
    } else {
      System.out.println("A. There is an equal probability that the immediate cause is that it's raining or that the sprinkler is on.");
    }
    
  }

}
```



