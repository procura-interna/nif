package pt.procurainterna.nif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;

public class NifTest {

  private static final String REAL_COMPANY_EDP = "500051070";
  private static final String WIKI_COMPANY = "501442600";
  private static final String WIKI_CIVIL = "999999990";

  @Test
  public void checkDigit_knownBodies() {
    assertEquals(0, Nif.checkDigit("99999999"));
    assertEquals(0, Nif.checkDigit("74089837"));
    assertEquals(8, Nif.checkDigit("28702400"));
    assertEquals(0, Nif.checkDigit("50005107"));
    assertEquals(0, Nif.checkDigit("50144260"));
  }

  @Test
  public void isValid_knownGoodPass() {
    assertTrue(Nif.isValid(REAL_COMPANY_EDP));
    assertTrue(Nif.isValid(WIKI_COMPANY));
    assertTrue(Nif.isValid(WIKI_CIVIL));
    assertTrue(Nif.isValid(nifWithPrefix("1234567")));
  }

  @Test
  public void isValid_oneDigitFlipsFail() {
    for (String good :
        Arrays.asList(
            REAL_COMPANY_EDP,
            WIKI_COMPANY,
            nifWithPrefix("1234567"),
            nifWithPrefix("2234567"),
            nifWithPrefix("5014426"))) {
      assertTrue(good, Nif.isValid(good));
      for (int i = 0; i < good.length(); i++) {
        char original = good.charAt(i);
        char flipped = (char) ('0' + ((original - '0' + 1) % 10));
        String bad = good.substring(0, i) + flipped + good.substring(i + 1);
        assertFalse("flip index " + i + " of " + good + " -> " + bad, Nif.isValid(bad));
      }
    }
  }

  @Test
  public void entityType_fixturePerType() {
    assertEntity(NifEntityType.INDIVIDUAL, nifWithPrefix("1234567"));
    assertEntity(NifEntityType.INDIVIDUAL, nifWithPrefix("2234567"));
    assertEntity(NifEntityType.INDIVIDUAL, nifWithPrefix("3234567"));
    assertEntity(NifEntityType.INDIVIDUAL_NON_RESIDENT, nifWithPrefix("4512345"));
    assertEntity(NifEntityType.COMPANY, REAL_COMPANY_EDP);
    assertEntity(NifEntityType.COMPANY, WIKI_COMPANY);
    assertEntity(NifEntityType.PUBLIC_ADMINISTRATION, nifWithPrefix("6123456"));
    assertEntity(NifEntityType.UNDIVIDED_INHERITANCE, nifWithPrefix("7012345"));
    assertEntity(NifEntityType.UNDIVIDED_INHERITANCE, nifWithPrefix("7412345"));
    assertEntity(NifEntityType.UNDIVIDED_INHERITANCE, nifWithPrefix("7512345"));
    assertEntity(NifEntityType.COLLECTIVE_NON_RESIDENT_WITHHOLDING, nifWithPrefix("7112345"));
    assertEntity(NifEntityType.INVESTMENT_FUND, nifWithPrefix("7212345"));
    assertEntity(NifEntityType.OFFICIAL_ASSIGNMENT, nifWithPrefix("7712345"));
    assertEntity(NifEntityType.OFFICIAL_ASSIGNMENT_VAT_REFUND, nifWithPrefix("7812345"));
    assertEntity(NifEntityType.EXCEPTIONAL_REGIME, nifWithPrefix("7912345"));
    assertEntity(NifEntityType.CONDOMINIUM_OR_IRREGULAR, nifWithPrefix("9012345"));
    assertEntity(NifEntityType.CONDOMINIUM_OR_IRREGULAR, nifWithPrefix("9112345"));
    assertEntity(NifEntityType.NON_RESIDENT_NO_PERMANENT_ESTABLISHMENT, nifWithPrefix("9812345"));
    assertEntity(NifEntityType.CIVIL_SOCIETY, WIKI_CIVIL);
  }

  @Test
  public void entityType_obsoleteSoleTraderRecognizedButNotValid() {
    String obsolete = nifWithPrefix("8123456");
    assertEquals(NifEntityType.SOLE_TRADER_OBSOLETE, Nif.entityTypeOf(obsolete));
    assertFalse(Nif.isValid(obsolete));
    assertEquals(NifFailureReason.OBSOLETE_PREFIX, Nif.validate(obsolete).failureReason());
  }

  @Test
  public void validate_fixturePerFailureReason() {
    assertFailure(null, NifFailureReason.NULL_OR_EMPTY);
    assertFailure("", NifFailureReason.NULL_OR_EMPTY);
    assertFailure("   ", NifFailureReason.NULL_OR_EMPTY);
    assertFailure("PT", NifFailureReason.NULL_OR_EMPTY);

    assertFailure("12345678a", NifFailureReason.NON_DIGIT);
    assertFailure("abcdefghi", NifFailureReason.NON_DIGIT);

    assertFailure("12345678", NifFailureReason.INVALID_LENGTH);
    assertFailure("1234567890", NifFailureReason.INVALID_LENGTH);

    assertFailure(nifWithPrefix("4012345"), NifFailureReason.UNKNOWN_PREFIX);
    assertFailure(nifWithPrefix("7312345"), NifFailureReason.UNKNOWN_PREFIX);
    assertFailure(nifWithPrefix("7612345"), NifFailureReason.UNKNOWN_PREFIX);
    assertFailure(nifWithPrefix("9212345"), NifFailureReason.UNKNOWN_PREFIX);

    assertFailure(nifWithPrefix("8123456"), NifFailureReason.OBSOLETE_PREFIX);

    assertFailure("123456780", NifFailureReason.BAD_CHECK_DIGIT);
    assertFailure(REAL_COMPANY_EDP.substring(0, 8) + "1", NifFailureReason.BAD_CHECK_DIGIT);
  }

  @Test
  public void validate_pipelineConsistentWithIsValidAndParse() {
    for (String input :
        Arrays.asList(
            null,
            "",
            "123",
            "12345678a",
            nifWithPrefix("4012345"),
            nifWithPrefix("8123456"),
            "123456780",
            REAL_COMPANY_EDP,
            "PT" + REAL_COMPANY_EDP,
            "500 051 070")) {
      NifValidation detailed = Nif.validate(input);
      assertEquals(String.valueOf(input), detailed.isValid(), Nif.isValid(input));
      Optional<Nif> parsed = Nif.parse(input);
      assertEquals(String.valueOf(input), detailed.isValid(), parsed.isPresent());
      if (detailed.isValid()) {
        assertNull(detailed.failureReason());
        assertNotNull(detailed.nif());
        assertEquals(detailed.nif(), parsed.get());
      } else {
        assertNull(detailed.nif());
        assertNotNull(detailed.failureReason());
      }
    }
  }

  @Test
  public void acceptsPtPrefixAndSeparators() {
    assertEquals(REAL_COMPANY_EDP, Nif.normalize("PT500051070"));
    assertEquals(REAL_COMPANY_EDP, Nif.normalize("pt 500.051.070"));
    assertEquals(REAL_COMPANY_EDP, Nif.normalize("PT 500-051-070"));
    assertTrue(Nif.isValid("PT500051070"));
    assertTrue(Nif.require("pt500051070").isCompany());
  }

  @Test
  public void format_roundTrips() {
    Nif nif = Nif.require(REAL_COMPANY_EDP);
    assertEquals(REAL_COMPANY_EDP, nif.format(NifFormat.CANONICAL));
    assertEquals("500 051 070", nif.format(NifFormat.GROUPED));
    assertEquals("PT500051070", nif.format(NifFormat.VAT));

    assertEquals(nif, Nif.require(nif.format(NifFormat.CANONICAL)));
    assertEquals(nif, Nif.require(nif.format(NifFormat.GROUPED)));
    assertEquals(nif, Nif.require(nif.format(NifFormat.VAT)));
  }

  @Test
  public void predicates_andCoarseCategories() {
    Nif person = Nif.require(nifWithPrefix("1234567"));
    assertTrue(person.isIndividual());
    assertTrue(person.isNaturalPerson());
    assertFalse(person.isLegalPerson());

    Nif nonResident = Nif.require(nifWithPrefix("4512345"));
    assertTrue(nonResident.isNonResidentIndividual());
    assertTrue(nonResident.isNaturalPerson());
    assertFalse(nonResident.isLegalPerson());

    Nif company = Nif.require(REAL_COMPANY_EDP);
    assertTrue(company.isCompany());
    assertTrue(company.isLegalPerson());
    assertFalse(company.isNaturalPerson());

    assertTrue(Nif.require(nifWithPrefix("7212345")).isInvestmentFund());
    assertTrue(Nif.require(nifWithPrefix("6123456")).isPublicAdministration());
    assertTrue(Nif.require(nifWithPrefix("7012345")).isUndividedInheritance());
    assertTrue(Nif.require(nifWithPrefix("7112345")).isCollectiveNonResidentWithholding());
    assertTrue(Nif.require(nifWithPrefix("7712345")).isOfficialAssignment());
    assertTrue(Nif.require(nifWithPrefix("7812345")).isOfficialAssignmentVatRefund());
    assertTrue(Nif.require(nifWithPrefix("7912345")).isExceptionalRegime());
    assertTrue(Nif.require(nifWithPrefix("9012345")).isCondominiumOrIrregular());
    assertTrue(Nif.require(nifWithPrefix("9812345")).isNonResidentNoPermanentEstablishment());
    assertTrue(Nif.require(WIKI_CIVIL).isCivilSociety());
  }

  @Test
  public void equals_hashCode_compareTo() {
    Nif bare = Nif.require(REAL_COMPANY_EDP);
    Nif spaced = Nif.require("500 051 070");
    Nif vat = Nif.require("PT500051070");
    assertEquals(bare, spaced);
    assertEquals(bare, vat);
    assertEquals(bare.hashCode(), spaced.hashCode());
    assertEquals(0, bare.compareTo(vat));
    assertTrue(bare.compareTo(Nif.require(WIKI_COMPANY)) < 0);
  }

  @Test
  public void require_throwsNifExceptionWithReason() {
    try {
      Nif.require("123456780");
      fail("expected NifException");
    } catch (NifException e) {
      assertEquals(NifFailureReason.BAD_CHECK_DIGIT, e.reason());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkDigit_rejectsNull() {
    Nif.checkDigit(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkDigit_rejectsWrongLength() {
    Nif.checkDigit("1234567");
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkDigit_rejectsNonDigits() {
    Nif.checkDigit("1234567a");
  }

  private static void assertFailure(String input, NifFailureReason expected) {
    NifValidation v = Nif.validate(input);
    assertFalse(String.valueOf(input), v.isValid());
    assertEquals(String.valueOf(input), expected, v.failureReason());
    assertFalse(Nif.isValid(input));
    assertFalse(Nif.parse(input).isPresent());
  }

  private static void assertEntity(NifEntityType expected, String raw) {
    Nif nif = Nif.require(raw);
    assertEquals(raw, expected, nif.entityType());
    assertEquals(expected, Nif.entityTypeOf(raw));
  }

  private static String nifWithPrefix(String sevenOrEight) {
    String body = sevenOrEight.length() == 7 ? sevenOrEight + "0" : sevenOrEight;
    if (body.length() != 8) {
      throw new IllegalArgumentException(sevenOrEight);
    }
    return body + Nif.checkDigit(body);
  }
}
