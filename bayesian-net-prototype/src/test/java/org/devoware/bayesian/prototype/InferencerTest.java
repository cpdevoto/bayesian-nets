package org.devoware.bayesian.prototype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Before;
import org.junit.Test;

public class InferencerTest {

  private Network network;
  
  @Before
  public void setup () {
    network = new Network();
    
    RandomVariable cloudy = network.newVariable("C", "Cloudy");
    RandomVariable sprinkler = network.newVariable("S", "Sprinkler");
    RandomVariable raining = network.newVariable("R", "Raining");
    RandomVariable wetGrass = network.newVariable("W", "Wet Grass");
    
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
  public void test_inferencer_query () {
    Inferencer inferencer = Inferencer.create(network);
    BigDecimal scaledResult = null;
    
    double result = inferencer.query("P(S|W)");
    scaledResult = new BigDecimal(result).setScale(3, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.430")));
    
    result = inferencer.query("P(R|W)");
    scaledResult = new BigDecimal(result).setScale(3, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.708")));
    
    result = inferencer.query("P(S,W)");
    scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.2781")));

    result = inferencer.query("P(R,W)");
    scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.4581")));
    
    result = inferencer.query("P(W)");
    scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.6471")));

    result = inferencer.query("P(W|R)");
    scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.9162")));
  
    result = inferencer.query("P(W|S)");
    scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.9270")));
    
    result = inferencer.query("P(W|~R)");
    scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.3780")));
    
    result = inferencer.query("P(W|~S)");
    scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.5271")));

    result = inferencer.query("P(W|S,~R)");
    scaledResult = new BigDecimal(result).setScale(1, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.9")));
  
    result = inferencer.query("P(C)");
    scaledResult = new BigDecimal(result).setScale(1, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.5")));

    result = inferencer.query("P(C|C)");
    scaledResult = new BigDecimal(result).setScale(1, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("1.0")));
  
    result = inferencer.query("P(C|~C)");
    scaledResult = new BigDecimal(result).setScale(1, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.0")));
  }
}
