package org.devoware.bayesian.prototype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Before;
import org.junit.Test;

public class InferencerTest2 {

  private Network network;
  
  @Before
  public void setup () {
    network = new Network();
    
    RandomVariable disease = network.newVariable("D", "Disease");
    RandomVariable test = network.newVariable("T", "Test");
    
    disease.addChild(test);
    
    disease.getCpt().put("P(D)", 0.01);
    
    test.getCpt().put("P(T|D)", 0.95);
    test.getCpt().put("P(T|~D)", 0.05);
  }


  @Test
  public void test_inferencer_query () {
    Inferencer inferencer = Inferencer.create(network);
    BigDecimal scaledResult = null;
    
    double result = inferencer.query("P(D|T)");
    scaledResult = new BigDecimal(result).setScale(3, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.161")));
    
    result = inferencer.query("P(D,T)");
    scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.0095")));
    
    result = inferencer.query("P(T)");
    scaledResult = new BigDecimal(result).setScale(4, RoundingMode.HALF_UP);
    assertThat(scaledResult, equalTo(new BigDecimal("0.0590")));

  }


}
