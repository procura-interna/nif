package pt.procurainterna.nif;

/**
 * Fiscal entity categories inferred from the leading digit(s) of a Portuguese NIF,
 * per Autoridade Tributária ranges (see Decreto-Lei n.º 14/2013 and AT guidance).
 */
public enum NifEntityType {

  /** Pessoa singular (leading 1, 2, or 3). */
  INDIVIDUAL,

  /**
   * Pessoa singular não residente com rendimentos sujeitos a retenção na fonte
   * a título definitivo (leading 45).
   */
  INDIVIDUAL_NON_RESIDENT,

  /** Pessoa colectiva obrigada a registo no RNPC (leading 5). */
  COMPANY,

  /** Administração Pública Central, Regional ou Local (leading 6). */
  PUBLIC_ADMINISTRATION,

  /**
   * Herança indivisa — autor da sucessão não era empresário individual, ou
   * cônjuge sobrevivente com rendimentos comerciais (leading 70, 74, 75).
   */
  UNDIVIDED_INHERITANCE,

  /** Não residentes colectivos sujeitos a retenção na fonte a título definitivo (leading 71). */
  COLLECTIVE_NON_RESIDENT_WITHHOLDING,

  /** Fundos de investimento (leading 72). */
  INVESTMENT_FUND,

  /** Atribuição oficiosa de NIF de sujeito passivo (leading 77). */
  OFFICIAL_ASSIGNMENT,

  /** Atribuição oficiosa a não residentes (VAT REFUND) (leading 78). */
  OFFICIAL_ASSIGNMENT_VAT_REFUND,

  /** Regime excepcional — Expo 98 (leading 79). */
  EXCEPTIONAL_REGIME,

  /**
   * Empresário em nome individual (leading 8). Obsolete: no longer issued and
   * never returned as a {@link Nif} instance.
   */
  SOLE_TRADER_OBSOLETE,

  /**
   * Condomínios, sociedades irregulares, ou heranças indivisas cujo autor era
   * empresário individual (leading 90, 91).
   */
  CONDOMINIUM_OR_IRREGULAR,

  /** Não residentes sem estabelecimento estável (leading 98). */
  NON_RESIDENT_NO_PERMANENT_ESTABLISHMENT,

  /** Sociedades civis sem personalidade jurídica (leading 99). */
  CIVIL_SOCIETY;

  /** Pessoa singular (resident or non-resident individual ranges). */
  public boolean isNaturalPerson() {
    return this == INDIVIDUAL || this == INDIVIDUAL_NON_RESIDENT;
  }

  /** Everything issuable that is not a natural person. */
  public boolean isLegalPerson() {
    return this != INDIVIDUAL
        && this != INDIVIDUAL_NON_RESIDENT
        && this != SOLE_TRADER_OBSOLETE;
  }
}
