package org.devoware.bayesian.prototype.expr;

public class Position {
  
  public static Position copyOf(Position position) {
    return new Position(position.getLine(), position.getCharacter());
  }

  private int line = 1;
  private int character = -1;
  
  Position(int line, int character) {
    this.line = line;
    this.character = character;
  }

  Position() {}
  
  Position advanceCharacter () {
    character += 1;
    return this;
  }

  Position advanceLine () {
    line += 1;
    character = -1;
    return this;
  }

  public int getLine() {
    return line;
  }

  public int getCharacter() {
    return Math.max(character, 0);
  }
  
  @Override
  public String toString() {
    return "line " + line + ", character " + character;
  }
    
}
