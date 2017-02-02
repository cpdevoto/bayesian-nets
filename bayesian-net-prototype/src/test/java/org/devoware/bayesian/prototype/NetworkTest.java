package org.devoware.bayesian.prototype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Before;
import org.junit.Test;

public class NetworkTest {
  
  private Network network;
  
  private RandomVariable cloudy;
  private RandomVariable sprinkler;
  private RandomVariable raining;
  private RandomVariable wetGrass;
  
  @Before
  public void setup() {
    network = new Network();
     
     cloudy = network.newVariable("C", "Cloudy");
     sprinkler = network.newVariable("S", "Sprinkler");
     raining = network.newVariable("R", "Raining");
     wetGrass = network.newVariable("W", "Wet Grass");
     
     cloudy.addChild(sprinkler);
     cloudy.addChild(raining);
     sprinkler.addChild(wetGrass);
     raining.addChild(wetGrass);
     
     cloudy.getCpt().put("P(C)", 0.5);
     
     sprinkler.getCpt().put("P(S|C)", 0.1);
     sprinkler.getCpt().put("P(S|~C)", 0.5);
     
     raining.getCpt().put("P(R|C)", 0.8);
     raining.getCpt().put("P(R|~C)", 0.2);
     
     wetGrass.getCpt().put("P(W|S,R)", 0.99);
     wetGrass.getCpt().put("P(W|S,~R)", 0.9);
     wetGrass.getCpt().put("P(W|~S,R)", 0.9);
     wetGrass.getCpt().put("P(W|~S,~R)", 0.0);
  }


  @Test
  public void test_creation () {
   
   assertFalse(cloudy.hasParents());
   assertThat(cloudy.getParents().size(), equalTo(0));
   assertTrue(cloudy.hasChildren());
   assertThat(cloudy.getChildren().size(), equalTo(2));
   assertTrue(cloudy.getChildren().contains(sprinkler));
   assertTrue(cloudy.getChildren().contains(raining));
   
   assertTrue(sprinkler.hasParents());
   assertThat(sprinkler.getParents().size(), equalTo(1));
   assertTrue(sprinkler.getParents().contains(cloudy));
   assertTrue(sprinkler.hasChildren());
   assertThat(sprinkler.getChildren().size(), equalTo(1));
   assertTrue(sprinkler.getChildren().contains(wetGrass));
   
   assertTrue(raining.hasParents());
   assertThat(raining.getParents().size(), equalTo(1));
   assertTrue(raining.getParents().contains(cloudy));
   assertTrue(raining.hasChildren());
   assertThat(raining.getChildren().size(), equalTo(1));
   assertTrue(raining.getChildren().contains(wetGrass));
   
   assertTrue(wetGrass.hasParents());
   assertThat(wetGrass.getParents().size(), equalTo(2));
   assertTrue(wetGrass.getParents().contains(sprinkler));
   assertTrue(wetGrass.getParents().contains(raining));
   assertFalse(wetGrass.hasChildren());
   assertThat(wetGrass.getChildren().size(), equalTo(0));
   
   assertThat(cloudy.getCpt().expressions().size(), equalTo(2));
   assertTrue(cloudy.getCpt().contains("P(C)"));
   assertTrue(cloudy.getCpt().contains("P(~C)"));

   assertThat(sprinkler.getCpt().expressions().size(), equalTo(4));
   assertTrue(sprinkler.getCpt().contains("P(S|C)"));
   assertTrue(sprinkler.getCpt().contains("P(S|~C)"));
   assertTrue(sprinkler.getCpt().contains("P(~S|C)"));
   assertTrue(sprinkler.getCpt().contains("P(~S|~C)"));

   assertThat(raining.getCpt().expressions().size(), equalTo(4));
   assertTrue(raining.getCpt().contains("P(R|C)"));
   assertTrue(raining.getCpt().contains("P(R|~C)"));
   assertTrue(raining.getCpt().contains("P(~R|C)"));
   assertTrue(raining.getCpt().contains("P(~R|~C)"));
  
   assertThat(wetGrass.getCpt().expressions().size(), equalTo(8));
   assertTrue(wetGrass.getCpt().contains("P(W|S,R)"));
   assertTrue(wetGrass.getCpt().contains("P(W|S,~R)"));
   assertTrue(wetGrass.getCpt().contains("P(W|~S,R)"));
   assertTrue(wetGrass.getCpt().contains("P(W|~S,~R)"));
   assertTrue(wetGrass.getCpt().contains("P(~W|S,R)"));
   assertTrue(wetGrass.getCpt().contains("P(~W|S,~R)"));
   assertTrue(wetGrass.getCpt().contains("P(~W|~S,R)"));
   assertTrue(wetGrass.getCpt().contains("P(~W|~S,~R)"));
   
   assertTrue(cloudy.getCpt().hasAllRequiredProbabilities());
   assertThat(cloudy.getCpt().get("P(C)"), equalTo(0.5));
   assertThat(cloudy.getCpt().get("P(~C)"), equalTo(0.5));
   
   assertTrue(sprinkler.getCpt().hasAllRequiredProbabilities());
   assertThat(sprinkler.getCpt().get("P(S|C)"), equalTo(0.1));
   assertThat(sprinkler.getCpt().get("P(~S|C)"), equalTo(0.9));
   assertThat(sprinkler.getCpt().get("P(S|~C)"), equalTo(0.5));
   assertThat(sprinkler.getCpt().get("P(~S|~C)"), equalTo(0.5));

   assertTrue(raining.getCpt().hasAllRequiredProbabilities());
   assertThat(raining.getCpt().get("P(R|C)"), equalTo(0.8));
   assertThat(raining.getCpt().get("P(~R|C)"), equalTo(0.2));
   assertThat(raining.getCpt().get("P(R|~C)"), equalTo(0.2));
   assertThat(raining.getCpt().get("P(~R|~C)"), equalTo(0.8));

   assertTrue(wetGrass.getCpt().hasAllRequiredProbabilities());
   assertThat(wetGrass.getCpt().get("P(W|S,R)"), equalTo(0.99));
   assertThat(wetGrass.getCpt().get("P(~W|S,R)"), equalTo(0.01));
   assertThat(wetGrass.getCpt().get("P(W|S,~R)"), equalTo(0.9));
   assertThat(wetGrass.getCpt().get("P(~W|S,~R)"), equalTo(0.1));
   assertThat(wetGrass.getCpt().get("P(W|~S,R)"), equalTo(0.9));
   assertThat(wetGrass.getCpt().get("P(~W|~S,R)"), equalTo(0.1));
   assertThat(wetGrass.getCpt().get("P(W|~S,~R)"), equalTo(0.0));
   assertThat(wetGrass.getCpt().get("P(~W|~S,~R)"), equalTo(1.0));
  }

  @Test
  public void test_query() {
    BigDecimal scaledResult = null;
     
     double result = network.query("P(S|W)");
     scaledResult = new BigDecimal(result).setScale(3, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.430")));
     
     result = network.query("P(R|W)");
     scaledResult = new BigDecimal(result).setScale(3, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.708")));
     
     result = network.query("P(S,W)");
     scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.2781")));

     result = network.query("P(R,W)");
     scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.4581")));
     
     result = network.query("P(W)");
     scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.6471")));
     
     result = network.query("P(W|R)");
     scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.9162")));
 
     result = network.query("P(W|S)");
     scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.9270")));
     
     result = network.query("P(W|~R)");
     scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.3780")));
     
     result = network.query("P(W|~S)");
     scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.5271")));

     result = network.query("P(W|S,~R)");
     scaledResult = new BigDecimal(result).setScale(1, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.9")));
 
     result = network.query("P(C)");
     scaledResult = new BigDecimal(result).setScale(1, RoundingMode.HALF_UP);
     assertThat(scaledResult, equalTo(new BigDecimal("0.5")));
  }

}
