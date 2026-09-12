# Changelog

## 1.0.1-SNAPSHOT

- Rename `NifException` to `InvalidNifException` (still extends `IllegalArgumentException`, keeps `reason()`).
- Document regulatory alignment (as of 2026-09-12) in README: Decreto-Lei n.º 14/2013, Portaria n.º 302/2016, AT Nota Informativa (gama 3, 2019), and issued-prefix strictness.
- Add light Javadoc `@see` links on `Nif`, `NifEntityType`, and `FINAL_CONSUMER` pointing at those instruments and the README section.

## 1.0.0

- First tagged release: valid-only `Nif` value object, shared validation pipeline, formats, entity types including SAF-T `FINAL_CONSUMER` (`999999990`), Maven Central publish profile (`-Prelease`).
