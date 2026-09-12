package pt.procurainterna.nif;

/** Output shapes for a validated {@link Nif}. */
public enum NifFormat {
  /** Nine digits, no separators: {@code 500051070}. */
  CANONICAL,
  /** Space-grouped: {@code 500 051 070}. */
  GROUPED,
  /** EU VAT style: {@code PT500051070}. */
  VAT
}
