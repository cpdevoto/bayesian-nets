package org.devoware.bayesian.prototype.expr;

import static java.util.Objects.requireNonNull;
import static org.devoware.bayesian.prototype.expr.Token.Type.COMMA;
import static org.devoware.bayesian.prototype.expr.Token.Type.EOF;
import static org.devoware.bayesian.prototype.expr.Token.Type.LEFT_PAREN;
import static org.devoware.bayesian.prototype.expr.Token.Type.NOT;
import static org.devoware.bayesian.prototype.expr.Token.Type.PIPE;
import static org.devoware.bayesian.prototype.expr.Token.Type.RIGHT_PAREN;

import java.io.IOException;
import java.io.Reader;

import org.devoware.bayesian.prototype.expr.Token.Type;

class LexicalAnalyzer {

  private final Reader in;
  private int peek = ' ';
  private Position position;

  
  public LexicalAnalyzer(Reader in) {
    this.in = requireNonNull(in, "in cannot be null");
    this.position = new Position();
  }


  public Token nextToken() throws IOException {
    // skip whitespace
    for (;; readChar()) {
      if (peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r') {
        if (peek == '\n' || peek == '\r') {
          position.advanceLine();
        }
        continue;
      } else {
        break;
      }
    }
    
    // Identify tokens representing operators
    switch (peek) {
      case '(':
        return getToken(LEFT_PAREN, position);
      case ')':
        return getToken(RIGHT_PAREN, position);
      case ',':
        return getToken(COMMA, position);
      case '|':
        return getToken(PIPE, position);
      case '~':
        return getToken(NOT, position);
      case -1:
        return getToken(EOF, position);
    }

    // Identify tokens representing words
    if (Character.isLetter(peek)) {
      Position wordStart = Position.copyOf(position);
      StringBuilder buf = new StringBuilder();
      
      do {
        buf.append((char) peek);
        readChar();
      } while (Character.isLetterOrDigit(peek));
      
      return getStringToken(buf.toString(), wordStart);
    }

    if (peek == -1) {
      throw new LexicalAnalysisException("Unexpected end of string");
    }
    throw new LexicalAnalysisException("Unexpected character '" + (char) peek + "' at " + position);
  }
  
  public Position getPosition() {
    return position;
  }

  private void readChar () throws IOException {
    peek = in.read();
    position.advanceCharacter();
  }

  private Token getToken(Type type, Position position) {
    return getToken(type, position, true);
  }
  
  private Token getToken(Type type, Position position, boolean resetPeek) {
    if (resetPeek) {
      peek = ' ';
    }
    return new Token(type, position);
  }

  private Token getStringToken(String value, Position position) {
    return new WordToken(value, position);
  }
}
