package org.devoware.bayesian.prototype;

import static java.util.Objects.requireNonNull;

public class EdgeAddedEvent {
  
  private final RandomVariable parent;
  private final RandomVariable child;

  public EdgeAddedEvent(RandomVariable parent, RandomVariable child) {
    this.parent = requireNonNull(parent, "parent cannot be null");
    this.child = requireNonNull(child, "child cannot be null");
  }

  public RandomVariable getParent() {
    return parent;
  }

  public RandomVariable getChild() {
    return child;
  }

}
