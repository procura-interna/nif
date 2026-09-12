package pt.procurainterna.nif;

/**
 * Fiscal entity categories inferred from the leading digit(s) of a Portuguese NIF,
 * per Autoridade Tributaria ranges (see Decreto-Lei n.o 14/2013 and AT guidance).
 */
public enum NifEntityType {

  /** Pessoa singular (leading 1, 2, or 3). */
  INDIVIDUAL,

  /**
   * Pessoa singular nao residente com rendimentos sujeitos a retencao na fonte
   * a titulo definitivo (leading 45).
   */
  INDIVIDUAL_NON_RESIDENT,

  /** Pessoa colectiva obrigada a registo no RNPC (leading 5). */
  COMPANY,

  /** Administracao Publica Central, Regional ou Local (leading 6). */
  PUBLIC_ADMINISTRATION,

  /**
   * Heranca indivisa - autor da sucessao nao era empresario individual, ou
   * conjuge sobrevivente com rendimentos comerciais (leading 70, 74, 75).
   */
  UNDIVIDED_INHERITANCE,

  /** Nao residentes colectivos sujeitos a retencao na fonte a titulo definitivo (leading 71). */
  COLLECTIVE_NON_RESIDENT_WITHHOLDING,

  /** Fundos de investimento (leading 72). */
  INVESTMENT_FUND,

  /** Atribuicao oficiosa de NIF de sujeito passivo (leading 77). */
  OFFICIAL_ASSIGNMENT,

  /** Atribuicao oficiosa a nao residentes (VAT REFUND) (leading 78). */
  OFFICIAL_ASSIGNMENT_VAT_REFUND,

  /** Regime excepcional - Expo 98 (leading 79). */
  EXCEPTIONAL_REGIME,

  /**
   * Empresario em nome individual (leading 8). Obsolete: no longer issued and
   * never returned as a {@link Nif} instance.
   */
  SOLE_TRADER_OBSOLETE,

  /**
   * Condominios, sociedades irregulares, ou herancas indivisas cujo autor era
   * empresario individual (leading 90, 91).
   */
  CONDOMINIUM_OR_IRREGULAR,

  /** Nao residentes sem estabelecimento estavel (leading 98). */
  NON_RESIDENT_NO_PERMANENT_ESTABLISHMENT,

  /** Sociedades civis sem personalidade juridica (leading 99). */
  CIVIL_SOCIETY;

  /** Pessoa singular (resident or non-resident individual ranges). */
  public boolean isNaturalPerson() {
    return this == INDIVIDUAL || this == INDIVIDUAL_NON_RESIDENT;
  }

  /**
   * Broad complement of {@link #isNaturalPerson()}: any currently issuable type that is
   * not an individual (resident or non-resident). This is NOT synonymous with
   * {@link NifEntityType#COMPANY} / "empresa" - it also includes public administration,
   * undivided inheritances, investment funds, condominiums, official assignments, civil
   * societies, and other collective ranges. Prefer {@code COMPANY} / {@link Nif#isCompany()}
   * when you mean pessoa colectiva (leading 5).
   */
  public boolean isLegalPerson() {
    return this != INDIVIDUAL
        && this != INDIVIDUAL_NON_RESIDENT
        && this != SOLE_TRADER_OBSOLETE;
  }
}
