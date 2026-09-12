# Changelog

## 1.0.1-SNAPSHOT

## 1.0.0

- First release: valid-only `Nif` value object, shared validation pipeline, formats, entity types including SAF-T `FINAL_CONSUMER` (`999999990`), Maven Central publish profile (`-Prelease`).
- Rename `NifException` to `InvalidNifException` (still extends `IllegalArgumentException`, keeps `reason()`).
- Document regulatory alignment (as of 2026-09-12) in README: Decreto-Lei n.º 14/2013, Portaria n.º 302/2016, AT Nota Informativa (gama 3, 2019), and issued-prefix strictness.
- Add light Javadoc `@see` links on `Nif`, `NifEntityType`, and `FINAL_CONSUMER` pointing at those instruments and the README section.
- Add brief Javadoc on all public API members; document that normalize/validate accept dashes as well as spaces and dots.
- Reject unissued prefix gaps such as `40`/`46` (stricter than DL 14/2013 art. 4.º/2 headroom).
