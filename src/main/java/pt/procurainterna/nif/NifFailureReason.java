package pt.procurainterna.nif;

/**
 * Why a candidate string failed structural / check-digit validation.
 * Produced only by the shared validation pipeline.
 */
public enum NifFailureReason {
  NULL_OR_EMPTY,
  NON_DIGIT,
  INVALID_LENGTH,
  UNKNOWN_PREFIX,
  OBSOLETE_PREFIX,
  BAD_CHECK_DIGIT
}
