package org.devoware.bayesian.prototype;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import com.google.common.eventbus.Subscribe;

public class RandomVariable {

  private final Network network;
  private final String id;
  private final String label;
  private final ConditionalProbabilityTable cpt;

  RandomVariable(Network network, String id, String label) {
    requireNonNull(id, "id cannot be null");
    requireNonNull(label, "label cannot be null");
    this.network = network;
    this.id = id;
    this.label = label;
    this.cpt = new ConditionalProbabilityTable(network, this);
  }
  
  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public void addChild(RandomVariable node) {
    requireNonNull(node, "node cannot be null");
    network.addEdge(this, node);
  }

  public boolean hasChildren() {
    return !getChildren().isEmpty();
  }
  
  public boolean hasParents() {
    return !getParents().isEmpty();
  }
  
  public Set<RandomVariable> getChildren() {
    return network.getChildren(this);
  }
  
  public Set<RandomVariable> getParents() {
    return network.getParents(this);
  }
  
  public ConditionalProbabilityTable getCpt() {
    return cpt;
  }
  
  @Override
  public String toString() {
    return id;
  }
  
  @Subscribe
  void handleEdgeAdded(EdgeAddedEvent event) {
    if (this.equals(event.getChild())) {
      handleParentAdded(event);
    }
  }

  private void handleParentAdded(EdgeAddedEvent event) {
    cpt.handleParentAdded();
  }
}
