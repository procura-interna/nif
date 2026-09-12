package pt.procurainterna.nif;

/**
 * Thrown by {@link Nif#require(String)} when the input is not a valid NIF.
 * Carries the same {@link NifFailureReason} as the shared validation pipeline.
 */
public final class InvalidNifException extends IllegalArgumentException {

  private final NifFailureReason reason;

  public InvalidNifException(String input, NifFailureReason reason) {
    super("invalid NIF (" + reason + "): " + input);
    this.reason = reason;
  }

  public NifFailureReason reason() {
    return reason;
  }
}
