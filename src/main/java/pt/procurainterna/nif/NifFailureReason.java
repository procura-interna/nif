package pt.procurainterna.nif;

/**
 * Why a candidate string failed structural / check-digit validation.
 * Produced only by the shared validation pipeline.
 */
public enum NifFailureReason {
  /** Input was {@code null}, blank, or yielded no digits after stripping. */
  NULL_OR_EMPTY,
  /** A non-digit character remained after stripping allowed separators / {@code PT}. */
  NON_DIGIT,
  /** Digit count was not exactly nine. */
  INVALID_LENGTH,
  /** Leading digits are not an issued prefix this library accepts. */
  UNKNOWN_PREFIX,
  /** Leading {@code 8} (obsolete sole trader range). */
  OBSOLETE_PREFIX,
  /** Prefix and length were acceptable but the check digit did not match. */
  BAD_CHECK_DIGIT
}
