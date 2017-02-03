package org.devoware.bayesian.prototype;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.devoware.bayesian.prototype.expr.Parser;
import org.devoware.bayesian.prototype.expr.ProbabilityExpression;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

public class Network {

  private final EventBus eventBus = new EventBus();
  private final Parser parser = Parser.create(this);
  private final Inferencer inferencer = Inferencer.create(this);
  private final Set<RandomVariable> vars = Sets.newLinkedHashSet();
  private final Map<String, RandomVariable> varsById = Maps.newLinkedHashMap();
  private final Multimap<String, RandomVariable> childToParentEdges = HashMultimap.create();
  private final Multimap<String, RandomVariable> parentToChildEdges = HashMultimap.create();
  private final Map<String,Boolean> evidence = Maps.newTreeMap();
  
  public Network () {}

  public RandomVariable newVariable(String id) {
    return newVariable(id, null);
  }

  public RandomVariable newVariable(String id, String label) {
    requireNonNull(id, "id cannot be null");
    checkArgument(!varsById.containsKey(id), "A variable with an id of '" + id + "' is already defined");
    if (label == null) {
      label = id;
    }
    varsById.put(id, null);
    RandomVariable node = new RandomVariable(this, id, label);
    vars.add(node);
    varsById.put(id, node);
    eventBus.register(node);
    return node;
  }
  
  public Set<RandomVariable> getVariables () {
    return ImmutableSet.copyOf(vars);
  }

  public RandomVariable getVariable(String id) {
    requireNonNull(id, "id cannot be null");
    return varsById.get(id);
  }
  
  public ProbabilityExpression parse (String expr) {
    requireNonNull(expr, "expr cannot be null");
    return parser.parse(expr);
  }
  
  public double query(String expression) {
    if (!allCptsPopulated()) {
      throw new IllegalStateException("You must first assign probabilities to all entries in the CPT for each random variable");
    }
    return inferencer.query(expression);
  }

  public boolean containsVar(String id) {
    return varsById.containsKey(id);
  }
  
  public void setEvidence (String varId, boolean value) {
    requireNonNull(varId, "varId cannot be null");
    checkArgument(varsById.containsKey(varId), "A variable with an id of '" + varId + "' has not been defined for this network");
    evidence.put(varId, value);
    eventBus.post(this);
  }
  
  public void clearEvidence (String varId) {
    requireNonNull(varId, "varId cannot be null");
    checkArgument(varsById.containsKey(varId), "A variable with an id of '" + varId + "' has not been defined for this network");
    evidence.remove(varId);
    eventBus.post(this);
  }
  
  public void clearEvidence () {
    evidence.clear();
    eventBus.post(this);
  }
  
  public Map<String,Boolean> getEvidence () {
    return ImmutableMap.copyOf(evidence);
  }

  public boolean hasEvidence () {
    return !evidence.isEmpty();
  }
  
  public void addNetworkListener(Object object) {
    eventBus.register(object);
  }
  
  public void removeNetworkListener(Object object) {
    eventBus.unregister(object);
  }

  void addEdge(RandomVariable parent, RandomVariable child) {
    requireNonNull(parent, "parent cannot be null");
    requireNonNull(child, "child cannot be null");
    if (parent.getId().equals(child.getId())) {
      throw NetworkCycleException.create(parent.getId(), child.getId());
    }
    checkForCycles(parent.getId(), child.getId(), child);
    parentToChildEdges.put(parent.getId(), child);
    childToParentEdges.put(child.getId(), parent);
    eventBus.post(new EdgeAddedEvent(parent, child));
  }

  Set<RandomVariable> getChildren(RandomVariable node) {
    Collection<RandomVariable> children = parentToChildEdges.get(node.getId());
    return ImmutableSet.copyOf(children);
  }

  Set<RandomVariable> getParents(RandomVariable node) {
    Collection<RandomVariable> parents = childToParentEdges.get(node.getId());
    return ImmutableSet.copyOf(parents);
  }

  private void checkForCycles(String parent, String child, RandomVariable start) {
     for (RandomVariable descendant : parentToChildEdges.get(start.getId())) {
       if (parent.equals(descendant.getId())) {
         throw NetworkCycleException.create(parent, child);
       }
       checkForCycles(parent, child, descendant);
     }
  }

  private boolean allCptsPopulated() {
    for (RandomVariable var : vars) {
      if (!var.getCpt().hasAllRequiredProbabilities()) {
        return false;
      }
    }
    return true;
  }

  
}
