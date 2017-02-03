package org.devoware.bayesian.prototype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.devoware.bayesian.prototype.Network;
import org.devoware.bayesian.prototype.ProbabilityExpressionFilter;
import org.devoware.bayesian.prototype.RandomVariable;
import org.devoware.bayesian.prototype.expr.ProbabilityExpression;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class ProbabilityExpressionFilterTest {

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
  }


  @Test
  public void test_filter () {
    ProbabilityExpressionFilter filter = ProbabilityExpressionFilter.builder()
        .withHypothesis("C", true)
        .withHypothesis("S", true)
        .withHypothesis("R", true)
        .withHypothesis("W", true)
        .build();
    
    Set<ProbabilityExpression> cSet = null, sSet = null, rSet = null, wSet = null;
    
    List<List<ProbabilityExpression>> allMatches = getAllMatches(filter);
    
    assertThat(allMatches.size(), equalTo(4));

    cSet = ImmutableSet.copyOf(allMatches.get(0));
    sSet = ImmutableSet.copyOf(allMatches.get(1));
    rSet = ImmutableSet.copyOf(allMatches.get(2));
    wSet = ImmutableSet.copyOf(allMatches.get(3));
    
    assertThat(cSet.size(), equalTo(1));
    assertTrue(cSet.contains(network.parse("P(C)")));
    assertThat(allMatches.get(1).size(), equalTo(1));
    assertTrue(sSet.contains(network.parse("P(S|C)")));
    assertThat(allMatches.get(2).size(), equalTo(1));
    assertTrue(rSet.contains(network.parse("P(R|C)")));
    assertThat(allMatches.get(3).size(), equalTo(1));
    assertTrue(wSet.contains(network.parse("P(W|S,R)")));
    
    filter = ProbabilityExpressionFilter.builder()
        .withHypothesis("W", true)
        .build();
    
    allMatches = getAllMatches(filter);
    
    assertThat(allMatches.size(), equalTo(4));

    cSet = ImmutableSet.copyOf(allMatches.get(0));
    sSet = ImmutableSet.copyOf(allMatches.get(1));
    rSet = ImmutableSet.copyOf(allMatches.get(2));
    wSet = ImmutableSet.copyOf(allMatches.get(3));
    
    assertThat(allMatches.get(0).size(), equalTo(2));
    assertTrue(cSet.contains(network.parse("P(C)")));
    assertTrue(cSet.contains(network.parse("P(~C)")));
    assertThat(allMatches.get(1).size(), equalTo(4));
    assertTrue(sSet.contains(network.parse("P(S|C)")));
    assertTrue(sSet.contains(network.parse("P(~S|C)")));
    assertTrue(sSet.contains(network.parse("P(S|~C)")));
    assertTrue(sSet.contains(network.parse("P(~S|~C)")));
    assertThat(allMatches.get(2).size(), equalTo(4));
    assertTrue(rSet.contains(network.parse("P(R|C)")));
    assertTrue(rSet.contains(network.parse("P(~R|C)")));
    assertTrue(rSet.contains(network.parse("P(R|~C)")));
    assertTrue(rSet.contains(network.parse("P(~R|~C)")));
    assertThat(allMatches.get(3).size(), equalTo(4));
    assertTrue(wSet.contains(network.parse("P(W|S,R)")));
    assertTrue(wSet.contains(network.parse("P(W|S,~R)")));
    assertTrue(wSet.contains(network.parse("P(W|~S,R)")));
    assertTrue(wSet.contains(network.parse("P(W|~S,~R)")));
    
  }


  private List<List<ProbabilityExpression>> getAllMatches(ProbabilityExpressionFilter filter) {
    List<List<ProbabilityExpression>> allMatches = Lists.newArrayList();
    for (RandomVariable var : network.getVariables()) {
      List<ProbabilityExpression> matches = var.getCpt().findExpressions(filter);
      allMatches.add(matches);
    }
    return allMatches;
  }
}
