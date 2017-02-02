package org.devoware.bayesian.prototype;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;

import org.devoware.bayesian.prototype.expr.ProbabilityExpression;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ProbabilityExpressionFilter {

  private Map<String, Boolean> hypotheses;

  public static Builder builder () {
    return new Builder();
  }
  
  public static Builder builder (ProbabilityExpressionFilter expr) {
    return new Builder(expr);
  }

  private ProbabilityExpressionFilter(Builder builder) {
    this.hypotheses = ImmutableMap.copyOf(builder.hypotheses);
  }
  
  public boolean getValue(String id) {
    return hypotheses.get(id);
  }
  
  public boolean filter (ProbabilityExpression expr) {
    for (Entry<String,Boolean> entry : expr.getHypothesesMap().entrySet()) {
      if (this.hypotheses.containsKey(entry.getKey()) && this.hypotheses.get(entry.getKey()) != entry.getValue()) {
        return false;
      }
    }
    for (Entry<String,Boolean> entry : expr.getEvidenceMap().entrySet()) {
      if (this.hypotheses.containsKey(entry.getKey()) && this.hypotheses.get(entry.getKey()) != entry.getValue()) {
        return false;
      }
    }
    return true;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
    ProbabilityExpressionFilter other = (ProbabilityExpressionFilter) obj;
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
    
    private Builder () {}
    
    private Builder (ProbabilityExpressionFilter expr) {
      this.hypotheses.putAll(expr.hypotheses);
    }
    
    public Builder withHypothesis(String id, boolean value) {
      requireNonNull(id, "id cannot be null");
      hypotheses.put(id, value);
      return this;
    }

    public ProbabilityExpressionFilter build () {
      checkArgument(!hypotheses.isEmpty(), "You must specify at least one hypothesis");
      return new ProbabilityExpressionFilter(this);
    }
  
  }

}
