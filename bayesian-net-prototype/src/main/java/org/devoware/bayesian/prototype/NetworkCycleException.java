package org.devoware.bayesian.prototype;

import static java.util.Objects.requireNonNull;

public class NetworkCycleException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  
  public static NetworkCycleException create(String parent, String child) {
    requireNonNull(parent, "parent cannot be null");
    requireNonNull(child, "child cannot be null");
    return new NetworkCycleException("The creation of an edge from node '" + parent
        + "' to node '" + child + "' introduces a cycle");
    
  }

  public NetworkCycleException() {}

  public NetworkCycleException(String message) {
    super(message);
  }

  public NetworkCycleException(Throwable cause) {
    super(cause);
  }

  public NetworkCycleException(String message, Throwable cause) {
    super(message, cause);
  }

  public NetworkCycleException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
