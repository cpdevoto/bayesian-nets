package org.devoware.bayesian.prototype;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.devoware.bayesian.prototype.expr.ProbabilityExpression;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class Inferencer {
  
  private final Network network;

  static Inferencer create(Network network) {
    return new Inferencer(network);
  }
  
  private Inferencer(Network network) {
    this.network = network;
  }
  
  double query (String expression) {
    ProbabilityExpression expr = network.parse(expression);
    ProbabilityExpressionFilter numeratorFilter = generateNumeratorFilter(expr);
    Optional<ProbabilityExpressionFilter> denominatorFilter = generateDenominatorFilter(expr);
    BigDecimal result = compute(numeratorFilter);
    if (denominatorFilter.isPresent()) {
      BigDecimal denominator = compute(denominatorFilter.get());
      result = result.divide(denominator, new MathContext(5, RoundingMode.HALF_UP));
    }
    return result.doubleValue();
  }

  private ProbabilityExpressionFilter generateNumeratorFilter(ProbabilityExpression expr) {
    ProbabilityExpressionFilter.Builder builder = ProbabilityExpressionFilter.builder();
    for (Entry<String, Boolean> entry : expr.getHypothesesMap().entrySet()) {
      builder.withHypothesis(entry.getKey(), entry.getValue());
    }
    for (Entry<String, Boolean> entry : expr.getEvidenceMap().entrySet()) {
      builder.withHypothesis(entry.getKey(), entry.getValue());
    }
    return builder.build();
  }

  private Optional<ProbabilityExpressionFilter> generateDenominatorFilter(ProbabilityExpression expr) {
    if (!expr.hasEvidence()) {
      return Optional.empty();
    }
    ProbabilityExpressionFilter.Builder builder = ProbabilityExpressionFilter.builder();
    for (Entry<String, Boolean> entry : expr.getEvidenceMap().entrySet()) {
      builder.withHypothesis(entry.getKey(), entry.getValue());
    }
    return Optional.of(builder.build());
  }
  
  private BigDecimal compute(ProbabilityExpressionFilter filter) {
    List<List<ProbabilityExpression>> allMatches = getAllMatches(filter);
    List<List<ProbabilityExpression>> permutations = permute(allMatches);
    BigDecimal result = new BigDecimal("0.0");
    for (List<ProbabilityExpression> permutation : permutations) {
      BigDecimal intermediate = null;
      for (ProbabilityExpression expr : permutation) {
        BigDecimal probability = eval(expr);
        if (intermediate == null) {
          intermediate = probability;
        } else {
          intermediate = intermediate.multiply(probability);
        }
      }
      if (intermediate != null) {
        result = result.add(intermediate);
      }
    }
    return result;
  }
  
  private BigDecimal eval(ProbabilityExpression expr) {
    String hypothesisId = Lists.newArrayList(expr.getHypothesesMap().keySet()).get(0);
    RandomVariable var = network.getVariable(hypothesisId);
    return var.getCpt().getDecimal(expr);
  }

  private List<List<ProbabilityExpression>> getAllMatches(ProbabilityExpressionFilter filter) {
    List<List<ProbabilityExpression>> allMatches = Lists.newArrayList();
    for (RandomVariable var : getVariables()) {
      if (!var.getCpt().hasAllRequiredProbabilities()) {
        throw new IllegalStateException("Random variable " + var.getId() + " does not have all of its required probabilities set.");
      }
      List<ProbabilityExpression> matches = var.getCpt().findExpressions(filter);
      allMatches.add(matches);
    }
    return allMatches;
  }

  private Set<RandomVariable> getVariables() {
    // TODO: Is it correct to include matches from all vars, or just the ones that are reachable for the vars referenced in the filter?
    return network.getVariables();
  }
  
  private List<List<ProbabilityExpression>> permute(List<List<ProbabilityExpression>> allMatches) {
    List<List<ProbabilityExpression>> permutations = Lists.newArrayList();
    permute(allMatches, permutations, Lists.newArrayList(), 0);
    permutations = eliminatePermutationsWithDisagreements(permutations);
    return permutations;
  }


  private List<List<ProbabilityExpression>> eliminatePermutationsWithDisagreements(
      List<List<ProbabilityExpression>> permutations) {
    List<List<ProbabilityExpression>> result = Lists.newArrayList();
    for (List<ProbabilityExpression> permutation : permutations) {
      Map<String, Boolean> seen = Maps.newHashMap();
      boolean validPermutation = true;
      outer: for (ProbabilityExpression expr : permutation) {
        for (Entry<String,Boolean> entry : expr.getHypothesesMap().entrySet()) {
          if (!seen.containsKey(entry.getKey())) {
            seen.put(entry.getKey(), entry.getValue());
          } else if (seen.get(entry.getKey()) != entry.getValue()) {
            validPermutation = false;
            break outer;
          }
        }
        for (Entry<String,Boolean> entry : expr.getEvidenceMap().entrySet()) {
          if (!seen.containsKey(entry.getKey())) {
            seen.put(entry.getKey(), entry.getValue());
          } else if (seen.get(entry.getKey()) != entry.getValue()) {
            validPermutation = false;
            break outer;
          }
        }
      }
      if (validPermutation) {
        result.add(permutation);
      }
    }
    
    return result;
  }

  private void permute(List<List<ProbabilityExpression>> allMatches,
      List<List<ProbabilityExpression>> permutations, List<ProbabilityExpression> base, int idx) {
    List<ProbabilityExpression> expressions = allMatches.get(idx); 
    for (int i = 0; i < expressions.size(); i++) {
      if (idx >= allMatches.size() - 1) {
        List<ProbabilityExpression> permutation = Lists.newArrayList(base);
        permutation.add(expressions.get(i));
        permutations.add(permutation);
      } else {
        List<ProbabilityExpression> newBase = Lists.newArrayList(base);
        newBase.add(expressions.get(i));
        permute(allMatches, permutations, newBase, idx + 1);
      }
    }
  }

}
