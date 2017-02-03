package org.devoware.bayesian.prototype.expr;

import static org.devoware.bayesian.prototype.expr.Token.Type.COMMA;
import static org.devoware.bayesian.prototype.expr.Token.Type.EOF;
import static org.devoware.bayesian.prototype.expr.Token.Type.LEFT_PAREN;
import static org.devoware.bayesian.prototype.expr.Token.Type.NOT;
import static org.devoware.bayesian.prototype.expr.Token.Type.PIPE;
import static org.devoware.bayesian.prototype.expr.Token.Type.RIGHT_PAREN;
import static org.devoware.bayesian.prototype.expr.Token.Type.WORD;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.devoware.bayesian.prototype.Network;
import org.devoware.bayesian.prototype.expr.ProbabilityExpression.Builder;
import org.devoware.bayesian.prototype.expr.Token.Type;

public class Parser {

  private final Network network;
  private LexicalAnalyzer lexer;
  private Token token;

  static Parser create() {
    return new Parser(null);
  }
  
  public static Parser create(Network network) {
    return new Parser(network);
  }
  
  private Parser(Network network) {
    this.network = network;
  }
  
  public ProbabilityExpression parse(String expression) {
    try (Reader in = new StringReader(expression)) {
      return parse(in);
    } catch (IOException e) {
      // This should never happen, but if it does, throw an assertion error
      throw new AssertionError("Unexpected exception", e);
    }
  }

  public ProbabilityExpression parse(Reader in) throws IOException {
    lexer = new LexicalAnalyzer(in);
    nextToken();
    ProbabilityExpression e = expression();
    expect(EOF);
    return e;
  }
  
  private ProbabilityExpression expression() throws IOException {
    boolean pFound = false;
    if (token.getType() == WORD) {
      String value = ((WordToken) token).value();
      if ("p".equalsIgnoreCase(value)) {
        pFound = true;
      }
    }
    if (!pFound) {
      throw new SyntaxException("Syntax error at " + token.getPosition() + ": found " + token.getType() + " when expecting P");
    }
    nextToken();
    expect(LEFT_PAREN);
    nextToken();
    Builder builder = ProbabilityExpression.builder();
    hypotheses(builder);
    if (token.getType() == PIPE) {
      nextToken();
      evidence(builder);
    }
    expect(RIGHT_PAREN);
    nextToken();
    return builder.build();
  }
  
  private void hypotheses(Builder builder) throws IOException {
    do {
      if (token.getType() == COMMA) {
        nextToken();
      }
       boolean value = true;
       if (token.getType() == NOT) {
         value = false;
         nextToken();
       }
       expect(WORD);
       String id = ((WordToken) token).value();
       if (network != null && !network.containsVar(id)) {
         throw new SyntaxException("The referenced variable " + id + " is not defined");
       }
       builder.withHypothesis(id, value);
       nextToken();
    } while (token.getType() == COMMA);
  }
  
  private void evidence(Builder builder) throws IOException {
    do {
      if (token.getType() == COMMA) {
        nextToken();
      }
       boolean value = true;
       if (token.getType() == NOT) {
         value = false;
         nextToken();
       }
       expect(WORD);
       String id = ((WordToken) token).value();
       if (!network.containsVar(id)) {
         throw new SyntaxException("The referenced node " + id + " is not defined");
       }
       builder.withEvidence(id, value);
       nextToken();
    } while (token.getType() == COMMA);
  }

  private void nextToken () throws IOException {
    token = lexer.nextToken();
  }

  private void expect(Type type) {
    if (token.getType() != type) {
      throw new SyntaxException("Syntax error at " + token.getPosition() + ": found " + token.getType() + " when expecting " + type);
    }
  }

}
