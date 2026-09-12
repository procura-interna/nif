package pt.procurainterna.nif;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A validated Portuguese NIF ({@code Numero de Identificacao Fiscal}).
 *
 * <p>Instances exist only for currently accepted prefixes with a correct check
 * digit. Prefer {@link #parse(String)} / {@link #validate(String)} for soft
 * failure and {@link #require(String)} when invalid input is exceptional.
 *
 * <p>Obsolete leading {@code 8} never yields an instance; use
 * {@link #entityTypeOf(String)} to classify that historical prefix.
 *
 * <p>Canonical form is nine digits. Input may include spaces, dots, and an
 * optional {@code PT} VAT prefix.
 *
 * <p>Regulatory alignment for this release is documented in {@code README.md}
 * ("Regulatory alignment"). Structure / issued prefixes / check digit only;
 * not live AT validation.
 *
 * @see <a href="https://diariodarepublica.pt/dr/detalhe/decreto-lei/14-2013-257001">Decreto-Lei n.o 14/2013</a>
 * @see <a href="https://diariodarepublica.pt/dr/detalhe/portaria/302-2016-105300290">Portaria n.o 302/2016</a>
 * @see <a href="https://info.portaldasfinancas.gov.pt/pt/destaques/Paginas/Atribuicao_Nova_Gama_de_NIF_a_Pessoas_Singulares.aspx">AT Nota Informativa (gama 3), 8 Apr 2019</a>
 */
public final class Nif implements Comparable<Nif>, Serializable {

  private static final long serialVersionUID = 1L;

  private static final int LENGTH = 9;
  private static final int BODY_LENGTH = 8;

  private static final Set<Character> SINGLE_DIGIT_PREFIXES =
      Collections.unmodifiableSet(
          new HashSet<Character>(Arrays.asList('1', '2', '3', '5', '6')));

  private static final Set<String> TWO_DIGIT_PREFIXES =
      Collections.unmodifiableSet(
          new HashSet<String>(
              Arrays.asList(
                  "45", "70", "71", "72", "74", "75", "77", "78", "79", "90", "91", "98",
                  "99")));

  private static final Map<String, NifEntityType> TWO_DIGIT_TYPES;
  private static final Map<Character, NifEntityType> SINGLE_DIGIT_TYPES;

  static {
    Map<String, NifEntityType> two = new HashMap<String, NifEntityType>();
    two.put("45", NifEntityType.INDIVIDUAL_NON_RESIDENT);
    two.put("70", NifEntityType.UNDIVIDED_INHERITANCE);
    two.put("71", NifEntityType.COLLECTIVE_NON_RESIDENT_WITHHOLDING);
    two.put("72", NifEntityType.INVESTMENT_FUND);
    two.put("74", NifEntityType.UNDIVIDED_INHERITANCE);
    two.put("75", NifEntityType.UNDIVIDED_INHERITANCE);
    two.put("77", NifEntityType.OFFICIAL_ASSIGNMENT);
    two.put("78", NifEntityType.OFFICIAL_ASSIGNMENT_VAT_REFUND);
    two.put("79", NifEntityType.EXCEPTIONAL_REGIME);
    two.put("90", NifEntityType.CONDOMINIUM_OR_IRREGULAR);
    two.put("91", NifEntityType.CONDOMINIUM_OR_IRREGULAR);
    two.put("98", NifEntityType.NON_RESIDENT_NO_PERMANENT_ESTABLISHMENT);
    two.put("99", NifEntityType.CIVIL_SOCIETY);
    TWO_DIGIT_TYPES = Collections.unmodifiableMap(two);

    Map<Character, NifEntityType> one = new HashMap<Character, NifEntityType>();
    one.put('1', NifEntityType.INDIVIDUAL);
    one.put('2', NifEntityType.INDIVIDUAL);
    one.put('3', NifEntityType.INDIVIDUAL);
    one.put('5', NifEntityType.COMPANY);
    one.put('6', NifEntityType.PUBLIC_ADMINISTRATION);
    one.put('8', NifEntityType.SOLE_TRADER_OBSOLETE);
    SINGLE_DIGIT_TYPES = Collections.unmodifiableMap(one);
  }

  private final String value;
  private final NifEntityType entityType;

  private Nif(String value, NifEntityType entityType) {
    this.value = value;
    this.entityType = entityType;
  }

  /**
   * Shared validation pipeline used by {@link #isValid}, {@link #parse},
   * {@link #validate}, and {@link #require}. Exactly one of success or failure.
   */
  public static NifValidation validate(String input) {
    DigitsResult digits = extractDigits(input);
    if (digits.failureReason != null) {
      return NifValidation.fail(digits.failureReason);
    }
    String canonical = digits.digits;
    NifEntityType type = classify(canonical);
    if (type == null) {
      return NifValidation.fail(NifFailureReason.UNKNOWN_PREFIX);
    }
    if (type == NifEntityType.SOLE_TRADER_OBSOLETE) {
      return NifValidation.fail(NifFailureReason.OBSOLETE_PREFIX);
    }
    if (!hasAcceptedPrefix(canonical)) {
      return NifValidation.fail(NifFailureReason.UNKNOWN_PREFIX);
    }
    if (checkDigit(canonical.substring(0, BODY_LENGTH)) != digitAt(canonical, BODY_LENGTH)) {
      return NifValidation.fail(NifFailureReason.BAD_CHECK_DIGIT);
    }
    return NifValidation.ok(new Nif(canonical, type));
  }

  public static Optional<Nif> parse(String input) {
    NifValidation result = validate(input);
    if (result.isValid()) {
      return Optional.of(result.nif());
    }
    return Optional.empty();
  }

  public static Nif require(String input) {
    NifValidation result = validate(input);
    if (!result.isValid()) {
      throw new NifException(input, result.failureReason());
    }
    return result.nif();
  }

  public static boolean isValid(String input) {
    return validate(input).isValid();
  }

  /**
   * Strips optional {@code PT}, spaces, and dots. Returns nine digits, or
   * {@code null} if the input cannot be reduced to exactly nine digits
   * (does not check prefix or check digit).
   */
  public static String normalize(String nif) {
    DigitsResult digits = extractDigits(nif);
    return digits.digits;
  }

  public static int checkDigit(String firstEightDigits) {
    if (firstEightDigits == null || firstEightDigits.length() != BODY_LENGTH) {
      throw new IllegalArgumentException("expected exactly 8 digits");
    }
    int sum = 0;
    for (int i = 0; i < BODY_LENGTH; i++) {
      char c = firstEightDigits.charAt(i);
      if (c < '0' || c > '9') {
        throw new IllegalArgumentException("expected exactly 8 digits");
      }
      sum += (c - '0') * (9 - i);
    }
    int remainder = sum % 11;
    if (remainder < 2) {
      return 0;
    }
    return 11 - remainder;
  }

  /**
   * Classifies by leading digit(s) without requiring a valid check digit.
   * Returns {@code null} for null/unparseable/unknown prefix. Obsolete
   * {@code 8} maps to {@link NifEntityType#SOLE_TRADER_OBSOLETE}.
   */
  public static NifEntityType entityTypeOf(String nif) {
    DigitsResult digits = extractDigits(nif);
    if (digits.digits == null) {
      return null;
    }
    return classify(digits.digits);
  }

  public String value() {
    return value;
  }

  public NifEntityType entityType() {
    return entityType;
  }

  public String format(NifFormat format) {
    if (format == null) {
      throw new IllegalArgumentException("format");
    }
    switch (format) {
      case CANONICAL:
        return value;
      case GROUPED:
        return value.substring(0, 3) + " " + value.substring(3, 6) + " " + value.substring(6);
      case VAT:
        return "PT" + value;
      default:
        throw new IllegalArgumentException("unknown format: " + format);
    }
  }

  public boolean isIndividual() {
    return entityType == NifEntityType.INDIVIDUAL;
  }

  public boolean isNonResidentIndividual() {
    return entityType == NifEntityType.INDIVIDUAL_NON_RESIDENT;
  }

  public boolean isNaturalPerson() {
    return entityType.isNaturalPerson();
  }

  /**
   * See {@link NifEntityType#isLegalPerson()} - not the same as {@link #isCompany()}.
   */
  public boolean isLegalPerson() {
    return entityType.isLegalPerson();
  }

  public boolean isCompany() {
    return entityType == NifEntityType.COMPANY;
  }

  public boolean isPublicAdministration() {
    return entityType == NifEntityType.PUBLIC_ADMINISTRATION;
  }

  public boolean isUndividedInheritance() {
    return entityType == NifEntityType.UNDIVIDED_INHERITANCE;
  }

  public boolean isCollectiveNonResidentWithholding() {
    return entityType == NifEntityType.COLLECTIVE_NON_RESIDENT_WITHHOLDING;
  }

  public boolean isInvestmentFund() {
    return entityType == NifEntityType.INVESTMENT_FUND;
  }

  public boolean isOfficialAssignment() {
    return entityType == NifEntityType.OFFICIAL_ASSIGNMENT;
  }

  public boolean isOfficialAssignmentVatRefund() {
    return entityType == NifEntityType.OFFICIAL_ASSIGNMENT_VAT_REFUND;
  }

  public boolean isExceptionalRegime() {
    return entityType == NifEntityType.EXCEPTIONAL_REGIME;
  }

  public boolean isCondominiumOrIrregular() {
    return entityType == NifEntityType.CONDOMINIUM_OR_IRREGULAR;
  }

  public boolean isNonResidentNoPermanentEstablishment() {
    return entityType == NifEntityType.NON_RESIDENT_NO_PERMANENT_ESTABLISHMENT;
  }

  public boolean isCivilSociety() {
    return entityType == NifEntityType.CIVIL_SOCIETY;
  }

  /** SAF-T consumidor final placeholder {@code 999999990}. */
  public boolean isFinalConsumer() {
    return entityType == NifEntityType.FINAL_CONSUMER;
  }

  @Override
  public int compareTo(Nif other) {
    return value.compareTo(other.value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Nif)) {
      return false;
    }
    return value.equals(((Nif) o).value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }

  private static DigitsResult extractDigits(String nif) {
    if (nif == null) {
      return DigitsResult.fail(NifFailureReason.NULL_OR_EMPTY);
    }
    String trimmed = nif.trim();
    if (trimmed.isEmpty()) {
      return DigitsResult.fail(NifFailureReason.NULL_OR_EMPTY);
    }
    if (trimmed.length() >= 2) {
      String head = trimmed.substring(0, 2).toUpperCase(Locale.ROOT);
      if ("PT".equals(head)) {
        trimmed = trimmed.substring(2).trim();
      }
    }
    StringBuilder digits = new StringBuilder(LENGTH);
    for (int i = 0; i < trimmed.length(); i++) {
      char c = trimmed.charAt(i);
      if (c == ' ' || c == '.' || c == '-') {
        continue;
      }
      if (c < '0' || c > '9') {
        return DigitsResult.fail(NifFailureReason.NON_DIGIT);
      }
      digits.append(c);
    }
    if (digits.length() == 0) {
      return DigitsResult.fail(NifFailureReason.NULL_OR_EMPTY);
    }
    if (digits.length() != LENGTH) {
      return DigitsResult.fail(NifFailureReason.INVALID_LENGTH);
    }
    return DigitsResult.ok(digits.toString());
  }

  private static final String FINAL_CONSUMER_NIF = "999999990";

  private static NifEntityType classify(String canonical) {
    if (FINAL_CONSUMER_NIF.equals(canonical)) {
      return NifEntityType.FINAL_CONSUMER;
    }
    String two = canonical.substring(0, 2);
    if (TWO_DIGIT_TYPES.containsKey(two)) {
      return TWO_DIGIT_TYPES.get(two);
    }
    return SINGLE_DIGIT_TYPES.get(canonical.charAt(0));
  }

  private static boolean hasAcceptedPrefix(String canonical) {
    String two = canonical.substring(0, 2);
    if (TWO_DIGIT_PREFIXES.contains(two)) {
      return true;
    }
    return SINGLE_DIGIT_PREFIXES.contains(canonical.charAt(0));
  }

  private static int digitAt(String s, int index) {
    return s.charAt(index) - '0';
  }

  private static final class DigitsResult {
    final String digits;
    final NifFailureReason failureReason;

    private DigitsResult(String digits, NifFailureReason failureReason) {
      this.digits = digits;
      this.failureReason = failureReason;
    }

    static DigitsResult ok(String digits) {
      return new DigitsResult(digits, null);
    }

    static DigitsResult fail(NifFailureReason reason) {
      return new DigitsResult(null, reason);
    }
  }
}
