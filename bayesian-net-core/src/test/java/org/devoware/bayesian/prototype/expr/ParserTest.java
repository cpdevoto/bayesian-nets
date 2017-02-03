package org.devoware.bayesian.prototype.expr;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.devoware.bayesian.prototype.Network;
import org.junit.Before;
import org.junit.Test;

public class ParserTest {
  private Network network;

  @Before
  public void setup() {
    network = new Network();
    
    network.newVariable("C", "Cloudy");
    network.newVariable("S", "Sprinkler");
    network.newVariable("R", "Raining");
    network.newVariable("W", "Wet Grass");
  }

  @Test
  public void test_parse() {
    setup();
    
    Parser parser = Parser.create(network);
    
    ProbabilityExpression expr = parser.parse("P(C)");
    assertThat(expr.toString(), equalTo("P(C)"));
    
    expr = parser.parse("P(~C)");
    assertThat(expr.toString(), equalTo("P(~C)"));
    
    expr = parser.parse("P(C,S,R,W)");
    assertThat(expr.toString(), equalTo("P(C,S,R,W)"));
    
    expr = parser.parse("P(C,~S,R,~W)");
    assertThat(expr.toString(), equalTo("P(C,~S,R,~W)"));

    ProbabilityExpression expr2 = parser.parse("P(C,R,~S,~W)");
    assertThat(expr2, equalTo(expr));
    
    expr = parser.parse("P(C|S,R)");
    assertThat(expr.toString(), equalTo("P(C|S,R)"));
    
    expr = parser.parse("P(C|S,~R)");
    assertThat(expr.toString(), equalTo("P(C|S,~R)"));
  
  }

}
