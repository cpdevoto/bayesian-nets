# bayesian-nets
Prototypes for the creation of Bayesian Networks.

## baysesian-net-core
A simple library showing a fully functional Bayesian network with minimal functionality.  This prototype supports the following features:
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
    
    // Execute probability queries against the model
    
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
### Understanding the math behind the queries

The purpose of this section is to demystify the math underlying the inference engine which the Bayesian network uses in order to evaluate arbitrary queries such as: **P(S|W,~Q)**.

The inference engine works by converting the specified query expression into a form that is composed entirely of terms that appear within the CPT tables. Once the expression has been converted in this way, computing the result is simply a matter of doing CPT lookups and then appying basic math.

**Some Basic Definitions First:** In an expression such as **P(S|W,~C)**, any terms which appear before the pipe character are referred to as **hypotheses**.  In the case of the expression **P(S|W,~C)**, the term **S** is a hypothesis. Any terms which appear after the pipe character are referred to as **evidence**.  In the case of the expression **P(S|W,~C)**, the terms **W** and **~C** are the **evidence**.  An expression which includes the pipe character is referred to as a conditional probability expression.

####The Algorithm

![alt text](https://github.com/cpdevoto/bayesian-nets/raw/master/inference-math.png "Inference Math")


The steps of the algorithm are as follows.

**Step 1:** Rewrite the specified expression to eliminate conditional probability. The numerator includes all terms that appear either as hypotheses or as evidence within the input expression, .  The denominator includes all terms that appear as evidence within the input expression. If there is no evidence within the input expression, then there is no denominator within the result.

**Step 2:** Convert the numerator and denominator into a sigma expression representing the sum of several joint probability expressions.  All terms from the input expression remain fixed at the values specified in the input expression.  The terms that are not in the input expression are permuted.

**Step 3:** For each of the joint probability expressions generated in the preceding step, go through the CPT for each variable in the network and find the probability expression which matches all of the terms in the joint probability expression in question.  This should produce one probability expression for each random variable that appears in the Bayesian network.  The product of all of these expressions is equivalent to the joint probability expression in question.

**Step 4:** The original expression has now been converted into a form that can solved for doing simple CPT table lookups and then applying some basic math! 

The performance of this basic algorithm becomes prohibitive for large networks. To improve the performance for exact inference, we can use the Variable Elimination algorithm, or we could switch to approximate inference using block Gibbs Sampling. The goal here was to keep things simple in order to provide a basic introduction for programmers.
