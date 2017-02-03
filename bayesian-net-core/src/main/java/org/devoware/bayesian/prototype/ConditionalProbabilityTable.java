package org.devoware.bayesian.prototype;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.devoware.bayesian.prototype.expr.ProbabilityExpression;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ConditionalProbabilityTable {
  private final Network network;
  private final RandomVariable var;
  private final Set<ProbabilityExpression> expressions = Sets.newLinkedHashSet();
  private final Map<ProbabilityExpression,BigDecimal> probabilities = Maps.newLinkedHashMap();

  public ConditionalProbabilityTable(Network network, RandomVariable var) {
    this.network = requireNonNull(network, "network cannot be null");
    this.var = requireNonNull(var, "var cannot be null");
    ProbabilityExpression positiveExpr = network.parse(genKey(var.getId()));
    this.expressions.add(positiveExpr);
    ProbabilityExpression negativeExpr = network.parse(genKey("~" + var.getId()));
    this.expressions.add(negativeExpr);
  }
  
  public boolean hasAllRequiredProbabilities() {
    if (expressions.size() != probabilities.size()) {
      return false;
    }
    return true;
  }
  
  public Set<ProbabilityExpression> expressions() {
    return ImmutableSet.copyOf(expressions);
  }
  
  public List<ProbabilityExpression> findExpressions(ProbabilityExpressionFilter filter) {
    List<ProbabilityExpression> matches = Lists.newArrayList();
    for (ProbabilityExpression expr : expressions) {
      if (filter.filter(expr)) {
        matches.add(expr);
      }
    }
    return matches;
  }

  public boolean contains (ProbabilityExpression expr) {
    requireNonNull(expr, "expr cannot be null");
    return expressions.contains(expr);
  }
  
  public boolean contains (String expr) {
    return contains(network.parse(expr));
  }
  
  public Double get(ProbabilityExpression expr) {
    requireNonNull(expr, "expr cannot be null");
    BigDecimal decimal = probabilities.get(expr);
    if (decimal == null) {
      return null;
    }
    return decimal.doubleValue();
  }

  public Double get(String expr) {
    return get(network.parse(expr));
  }
  
  public BigDecimal getDecimal(ProbabilityExpression expr) {
    requireNonNull(expr, "expr cannot be null");
    return probabilities.get(expr);
  }

  public BigDecimal getDecimal(String expr) {
    return getDecimal(network.parse(expr));
  }

  public void put(ProbabilityExpression expr, double prob) {
    requireNonNull(expr, "expr cannot be null");
    checkArgument(prob >= 0.0 && prob <= 1.0, "prob must be between 0.0 and 1.0");
    if (!expressions.contains(expr)) {
      throw new IllegalArgumentException("The specified probability expression is not valid for this node");
    }
    BigDecimal decimal = new BigDecimal("" + prob);
    probabilities.put(expr, decimal);
    boolean value = expr.getHypothesis(var.getId()); 
    ProbabilityExpression negativeExpression = ProbabilityExpression.builder(expr).withHypothesis(var.getId(), !value).build();
   
    probabilities.put(negativeExpression, new BigDecimal("1.0").subtract(decimal));  
  }

  public void put(String expr, double prob) {
    put(network.parse(expr), prob);
  }

  @Override
  public String toString() {
    return "ConditionalProbabilityTable [network=" + network + ", var=" + var + ", expressions="
        + expressions + ", probabilities=" + probabilities + "]";
  }

  void handleParentAdded() {
    expressions.clear();
    probabilities.clear();
    for (String permutation : generateParentPermutations(var.getParents())) {
      ProbabilityExpression positiveExpr = network.parse(genKey(var.getId() + "|" + permutation));
      expressions.add(positiveExpr);
      ProbabilityExpression negativeExpr = network.parse(genKey("~" + var.getId() + "|" + permutation));
      expressions.add(negativeExpr);
    }
  }

  private List<String> generateParentPermutations(Set<RandomVariable> parents) {
    List<RandomVariable> remaining = ImmutableList.copyOf(parents);
    List<String> results = Lists.newArrayList();
    generateParentPermutations(remaining, results, 0, "");
    return results;
  }

  private void generateParentPermutations(List<RandomVariable> parents, List<String> results,
      int idx, String base) {
    RandomVariable current = parents.get(idx);
    String newBase = base;
    if (!base.isEmpty()) {
      newBase += ",";
    }
    String perm1 = newBase + current.getId();
    String perm2 = newBase + "~" + current.getId();
    if (idx >= parents.size() - 1) {
      results.add(perm1);
      results.add(perm2);
    } else {
      generateParentPermutations(parents, results, idx + 1, perm1);
      generateParentPermutations(parents, results, idx + 1, perm2);
    }
  }

  private String genKey(String id) {
    return "P(" + id + ")";
  }
  
 

}
