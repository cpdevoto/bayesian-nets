package org.devoware.bayesian.prototype;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    if (expr.hasTermDisagreemets()) {
      return 0.0;
    }
    ProbabilityExpressionFilter numeratorFilter = generateNumeratorFilter(expr);
    List<ProbabilityExpressionFilter> numeratorFilters = generateJointProbabilityFilters(numeratorFilter);
    BigDecimal result = compute(numeratorFilters);
    Optional<ProbabilityExpressionFilter> denominatorFilter = generateDenominatorFilter(expr);
    if (denominatorFilter.isPresent()) {
      List<ProbabilityExpressionFilter> denominatorFilters = generateJointProbabilityFilters(denominatorFilter.get());
      BigDecimal denominator = compute(denominatorFilters);
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

  private List<ProbabilityExpressionFilter> generateJointProbabilityFilters(
      ProbabilityExpressionFilter inputFilter) {
    List<Map<String,Boolean>> jointFilterMaps = Lists.newArrayList();
    Map<String,Boolean> base = Maps.newLinkedHashMap();
    List<String> varIds = getVariables().stream().map(RandomVariable::getId).collect(Collectors.toList());
    generateJointProbabilityFilters(inputFilter.toMap(), varIds, 0, jointFilterMaps, base);
    List<ProbabilityExpressionFilter> jointFilters = Lists.newArrayList();
    for (Map<String,Boolean> jointFilterMap : jointFilterMaps) {
      jointFilters.add(ProbabilityExpressionFilter.builder().withHypotheses(jointFilterMap).build());
    }
    return jointFilters;
  }

  private void generateJointProbabilityFilters(Map<String,Boolean> inputFilter, List<String> varIds, int idx,
      List<Map<String, Boolean>> jointFilterMaps, Map<String, Boolean> base) {
    String currentVar = varIds.get(idx);
    Boolean fixedValue = inputFilter.get(currentVar);
    if (idx >= varIds.size() - 1) {
      if (fixedValue != null) {
        Map<String,Boolean> filterMap = Maps.newLinkedHashMap(base);
        filterMap.put(currentVar, fixedValue);
        jointFilterMaps.add(filterMap);
      } else {
        Map<String,Boolean> filterMap1 = Maps.newLinkedHashMap(base);
        filterMap1.put(currentVar, true);
        jointFilterMaps.add(filterMap1);
        Map<String,Boolean> filterMap2 = Maps.newLinkedHashMap(base);
        filterMap2.put(currentVar, false);
        jointFilterMaps.add(filterMap2);
      }
    } else {
      if (fixedValue != null) {
        Map<String,Boolean> newBase = Maps.newLinkedHashMap(base);
        newBase.put(currentVar, fixedValue);
        generateJointProbabilityFilters(inputFilter, varIds, idx + 1, jointFilterMaps, newBase);
      } else {
        Map<String,Boolean> newBase1 = Maps.newLinkedHashMap(base);
        newBase1.put(currentVar, true);
        generateJointProbabilityFilters(inputFilter, varIds, idx + 1, jointFilterMaps, newBase1);
        Map<String,Boolean> newBase2 = Maps.newLinkedHashMap(base);
        newBase2.put(currentVar, false);
        generateJointProbabilityFilters(inputFilter, varIds, idx + 1, jointFilterMaps, newBase2);
      }
    }
  }

  private BigDecimal compute(List<ProbabilityExpressionFilter> filters) {
    List<List<ProbabilityExpression>> permutations = getAllMatches(filters);
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

  private List<List<ProbabilityExpression>> getAllMatches(List<ProbabilityExpressionFilter> filters) {
    List<List<ProbabilityExpression>> allMatches = Lists.newArrayList();
    boolean firstLoop = true;
    for (ProbabilityExpressionFilter filter : filters) {
      List<ProbabilityExpression> matches = Lists.newArrayList();
      for (RandomVariable var : getVariables()) {
        if (firstLoop) {
          firstLoop = false;
          if (!var.getCpt().hasAllRequiredProbabilities()) {
            throw new IllegalStateException("Random variable " + var.getId() + " does not have all of its required probabilities set.");
          }
        }
        List<ProbabilityExpression> varMatches = var.getCpt().findExpressions(filter);
        // Since we are using a joint probability expression filter, we expect exactly one match from each CPT
        if (varMatches.size() != 1) {
          throw new AssertionError("Expected only one expression from the CPT for variable " + var.getId() + " to match the specified joint probability filter " + filter);
        }
        matches.add(varMatches.get(0));
      }
      allMatches.add(matches);
    }
    return allMatches;
  }

  private Set<RandomVariable> getVariables() {
    // TODO: Is it correct to include matches from all vars, or just the ones that are reachable for the vars referenced in the filter?
    return network.getVariables();
  }

}
