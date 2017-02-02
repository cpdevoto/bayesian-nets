package org.devoware.bayesian.prototype.expr;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ProbabilityExpression {
  private static final Parser parser = Parser.create();

  
  private Map<String, Boolean> hypotheses;
  private Map<String, Boolean> evidence;

  public static ProbabilityExpression create (String expr) {
    requireNonNull(expr, "expr cannot be null");
    return parser.parse(expr);
  }

  public static Builder builder () {
    return new Builder();
  }
  
  public static Builder builder (ProbabilityExpression expr) {
    return new Builder(expr);
  }

  private ProbabilityExpression(Builder builder) {
    this.hypotheses = ImmutableMap.copyOf(builder.hypotheses);
    this.evidence = ImmutableMap.copyOf(builder.evidence);
  }

  public Map<String, Boolean> getHypothesesMap() {
    return ImmutableMap.copyOf(hypotheses);
  }
  
  public boolean getHypothesis(String id) {
    return hypotheses.get(id);
  }
  
  public boolean hasEvidence() {
    return !evidence.isEmpty();
  }
  
  public Map<String, Boolean> getEvidenceMap() {
    return ImmutableMap.copyOf(evidence);
  }

  public boolean getEvidence(String id) {
    return evidence.get(id);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((evidence == null) ? 0 : evidence.hashCode());
    result = prime * result + ((hypotheses == null) ? 0 : hypotheses.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProbabilityExpression other = (ProbabilityExpression) obj;
    if (evidence == null) {
      if (other.evidence != null)
        return false;
    } else if (!evidence.equals(other.evidence))
      return false;
    if (hypotheses == null) {
      if (other.hypotheses != null)
        return false;
    } else if (!hypotheses.equals(other.hypotheses))
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder("P(");
    toString(buf, hypotheses);
    if (!evidence.isEmpty()) {
      buf.append("|");
      toString(buf, evidence);
    }
    buf.append(")");
    return buf.toString();
  }
  
  private void toString(StringBuilder buf, Map<String,Boolean> map) {
    boolean firstLoop = true;
    for (Entry<String, Boolean> entry : map.entrySet()) {
      if (firstLoop) {
        firstLoop = false;
      } else {
        buf.append(",");
      }
      if (!entry.getValue()) {
        buf.append("~");
      }
      buf.append(entry.getKey());
    }
  }
  
  public static class Builder {
    
    private final Map<String, Boolean> hypotheses = Maps.newLinkedHashMap();
    private final Map<String, Boolean> evidence = Maps.newLinkedHashMap();
    
    private Builder () {}
    
    private Builder (ProbabilityExpression expr) {
      this.hypotheses.putAll(expr.hypotheses);
      this.evidence.putAll(expr.evidence);
    }
    
    public Builder withHypothesis(String id, boolean value) {
      requireNonNull(id, "id cannot be null");
      checkArgument(!evidence.containsKey(id), "You cannot reference the same variable as a hypothesis and as evidence");
      hypotheses.put(id, value);
      return this;
    }

    public Builder withEvidence(String id, boolean value) {
      requireNonNull(id, "id cannot be null");
      checkArgument(!hypotheses.containsKey(id), "You cannot reference the same variable as a hypothesis and as evidence");
      evidence.put(id, value);
      return this;
    }
    
    public ProbabilityExpression build () {
      checkArgument(!hypotheses.isEmpty(), "You must specify at least one hypothesis");
      return new ProbabilityExpression(this);
    }
  
  }

}
