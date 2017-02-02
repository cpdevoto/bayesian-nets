package org.devoware.bayesian.prototype.expr;

import static java.util.Objects.requireNonNull;

public class Token {
  public static enum Type {
    LEFT_PAREN("'('"),
    RIGHT_PAREN("')'"),
    COMMA("','"),
    PIPE("'|'"),
    NOT("'~'"),
    WORD("a word"),
    EOF("end of input string");
    
    private final String stringValue;
    
    private Type (String stringValue) {
      this.stringValue = stringValue;
    }
    
    @Override
    public String toString() {
      return stringValue;
    }
  }

  private final Type type;
  private final Position position;

  public Token(Type type, Position position) {
    this.type = requireNonNull(type, "type cannot be null");
    this.position = Position.copyOf(requireNonNull(position, "position cannot be null"));
  }
  
  public final Type getType() {
    return type;
  }
  
  public final Position getPosition() {
    return position;
  }

}
