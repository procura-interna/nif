package pt.procurainterna.nif;

/**
 * Outcome of the shared NIF validation pipeline: either a {@link Nif} or a
 * {@link NifFailureReason}. Never both; never neither.
 */
public final class NifValidation {

  private final Nif nif;
  private final NifFailureReason failureReason;

  private NifValidation(Nif nif, NifFailureReason failureReason) {
    this.nif = nif;
    this.failureReason = failureReason;
  }

  static NifValidation ok(Nif nif) {
    return new NifValidation(nif, null);
  }

  static NifValidation fail(NifFailureReason reason) {
    return new NifValidation(null, reason);
  }

  public boolean isValid() {
    return nif != null;
  }

  /** Present when {@link #isValid()}; otherwise {@code null}. */
  public Nif nif() {
    return nif;
  }

  /** Present when invalid; otherwise {@code null}. */
  public NifFailureReason failureReason() {
    return failureReason;
  }
}
