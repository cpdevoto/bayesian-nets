package org.devoware.bayesian.prototype.expr;

public class WordToken extends Token {

  private final String value;
  
  public WordToken(String value, Position position) {
    super(Type.WORD, position);
    this.value = value;
  }
  
  public String value () {
    return value;
  }
  
}
