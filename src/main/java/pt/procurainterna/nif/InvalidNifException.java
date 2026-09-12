package pt.procurainterna.nif;

/**
 * Thrown by {@link Nif#require(String)} when the input is not a valid NIF.
 * Carries the same {@link NifFailureReason} as the shared validation pipeline.
 */
public final class InvalidNifException extends IllegalArgumentException {

  private final NifFailureReason reason;

  /**
   * @param input the rejected input (may be {@code null})
   * @param reason why validation failed
   */
  public InvalidNifException(String input, NifFailureReason reason) {
    super("invalid NIF (" + reason + "): " + input);
    this.reason = reason;
  }

  /** The failure reason from the shared validation pipeline. */
  public NifFailureReason reason() {
    return reason;
  }
}
