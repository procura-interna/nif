# nif

Java 8 library for Portuguese NIF (`Numero de Identificacao Fiscal`) handling:
normalize, validate, classify, and format tax IDs with **zero runtime dependencies**.

## Coordinates

- GroupId: `pt.procurainterna`
- ArtifactId: `nif`
- Java: 8+
- Build: Maven

```bash
mvn test
```

## Quick start

```java
import pt.procurainterna.nif.Nif;
import pt.procurainterna.nif.NifFormat;
import pt.procurainterna.nif.NifValidation;
import pt.procurainterna.nif.NifFailureReason;

// Soft parse
Nif.parse("500 051 070").ifPresent(nif -> {
  System.out.println(nif.isCompany());      // true
  System.out.println(nif.isLegalPerson());  // true (not the same as isCompany!)
  System.out.println(nif.format(NifFormat.VAT)); // PT500051070
});

// Strict parse
Nif company = Nif.require("PT500051070");

// Boolean check
boolean ok = Nif.isValid("123456789");

// Why it failed (same pipeline as isValid / parse / require)
NifValidation result = Nif.validate("123456780");
if (!result.isValid()) {
  NifFailureReason reason = result.failureReason(); // BAD_CHECK_DIGIT, ...
}
```

## API surface

| Method | Role |
|--------|------|
| `Nif.validate(String)` | Shared pipeline: success (`Nif`) or `NifFailureReason` |
| `Nif.parse(String)` | `Optional<Nif>` |
| `Nif.require(String)` | `Nif` or throws `NifException` |
| `Nif.isValid(String)` | boolean |
| `Nif.normalize(String)` | Digits only (optional `PT`, spaces, dots, dashes) or `null` |
| `Nif.checkDigit(String)` | Modulo-11 check digit for 8 body digits |
| `Nif.entityTypeOf(String)` | Classify prefix without requiring a valid check digit |

Once you hold a `Nif`:

- `value()`, `entityType()`, `equals` / `hashCode` / `compareTo`
- `format(NifFormat.CANONICAL | GROUPED | VAT)`
- Fine-grained predicates: `isIndividual()`, `isCompany()`, `isInvestmentFund()`, ...
- Coarse predicates: `isNaturalPerson()`, `isLegalPerson()`

**Note:** `isLegalPerson()` means *any issuable non-individual type* (public admin, inheritances, condominiums, funds, ...). For pessoa colectiva / company (leading `5`), use `isCompany()`.

## Accepted input

- Canonical 9 digits: `500051070`
- Grouped: `500 051 070`, `500.051.070`, `500-051-070`
- EU VAT style: `PT500051070` (case-insensitive `PT`)

Obsolete leading `8` (empresario em nome individual) is classified via `entityTypeOf` but **never** returned by `parse` / `require`.

## Scope

This library checks **structure, prefix ranges, and check digit**. It does **not** call Autoridade Tributaria to verify that a NIF is assigned or active.

## License

[Unlicense](https://unlicense.org) (public domain). See `UNLICENSE`.
