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


## Maven dependency

```xml
<dependency>
  <groupId>pt.procurainterna</groupId>
  <artifactId>nif</artifactId>
  <version>1.0.0</version>
</dependency>
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
boolean ok = Nif.isValid("123456780"); // false (bad check digit)

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

Only **issued** prefix ranges are accepted (the gamas actually used by AT / RNPC), not every first digit contemplated in abstract by Decreto-Lei n.o 14/2013. In particular, bare leading `4` outside `45`, and other unissued two-digit gaps (`40`/`46`/`73`/`76`/`92`, ...), are rejected as `UNKNOWN_PREFIX`.

Obsolete leading `8` (empresario em nome individual) is classified via `entityTypeOf` but **never** returned by `parse` / `require`.

`999999990` is the SAF-T **consumidor final** placeholder (Portaria 302/2016). It parses as `FINAL_CONSUMER` / `isFinalConsumer()`, not as a sociedade civil.

Company NIFs (leading `5`) are the same digits as the NIPC from RNPC ? use `isCompany()`.

Leading `45` marks non-resident individuals for definitive withholding; AT advises those NIFs are not for general contracts/banking. That policy is out of scope for validation; we only classify the prefix.


## Regulatory alignment (v1.0.1-SNAPSHOT)

As of **2026-09-12**, this library is aligned with the following instruments for
**structure, issued prefixes, and check digit only**. It does **not** perform live
AT validation and does not claim legal compliance or that a NIF is assigned/active.

- **Decreto-Lei n.o 14/2013, de 28 de janeiro** ? NIF structure and modulo-11 check digit (art. 4.o); first-digit headroom 1-4 and special `45` (art. 4.o/2-3); NIPC equivalent to NIF for RNPC entities and AT `7...` assignments (art. 11.o). [DRE](https://diariodarepublica.pt/dr/detalhe/decreto-lei/14-2013-257001) ? [PDF](https://files.dre.pt/1s/2013/01/01900/0054200548.pdf). Wording fix: Declaracao de Retificacao n.o 7/2013, de 13 de fevereiro.
- **Portaria n.o 302/2016, de 2 de dezembro** ? SAF-T placeholder `999999990` ("Consumidor final"). [DRE](https://diariodarepublica.pt/dr/detalhe/portaria/302-2016-105300290) ? [AT PDF](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Portaria_302_2016.pdf).
- **AT Nota Informativa**, Direcao de Servicos de Registo de Contribuintes, **8 Apr 2019** ? new singular gama `3` announced ([AT page](https://info.portaldasfinancas.gov.pt/pt/destaques/Paginas/Atribuicao_Nova_Gama_de_NIF_a_Pessoas_Singulares.aspx)); issuance from **4 Jul 2019**.
- **Issued prefix table** ? implements issued gamas as commonly published / AT practice. Intentionally stricter than art. 4.o/2 theoretical headroom: unissued gaps such as `40-44` / `46-49` are rejected.

When law or AT issued ranges change, bump the library version and update this block (and CHANGELOG). Do not silently reinterpret already-published majors.

## Scope

This library checks **structure, issued prefix ranges, and check digit**. It does **not** call Autoridade Tributaria to verify that a NIF is assigned or active.

## License

[Unlicense](https://unlicense.org) (public domain). See `UNLICENSE`.

## Publishing

Prepared for Maven Central the same way as other `pt.procurainterna` libraries:

- POM metadata: license, developers, SCM, issues
- `maven-source-plugin` / `maven-javadoc-plugin` attach classifiers
- `UNLICENSE` is copied into `META-INF/` of the jar
- GPG signing and `central-publishing-maven-plugin` live in the `release` profile only

Everyday builds (no keys required):

```bash
mvn verify
```

Cut a release (stay on `*-SNAPSHOT` until then), tag, then:

```bash
mvn -Prelease clean deploy
```

Requires a `maven-central` server entry and a working GPG key in your Maven `settings.xml` / agent. Finish validation on Sonatype Central's website after upload.

