# ADR-0002: Load scanned classes without initializing them; scan directories only

## Status

Accepted

## Context

The container needs to know which classes exist in a package before it can decide which of
them become beans. The JVM has no API to list the classes of a package, so the scanner reads
`.class` files from the classpath and turns them into `Class` objects for inspection.

There are two common ways to do that:

1. Read the `.class` file bytes with a bytecode library (Spring uses ASM) and inspect
   annotations without loading the class at all.
2. Load each class with reflection.

Loading a class with plain `Class.forName(name)` also initializes it, which runs its static
initializer. Scanning must not trigger side effects in classes that may never become beans.

Classes can live either in directories (IDE, Maven builds) or inside JAR files (packaged
applications). This project always runs from directories.

## Decision

- Classes are loaded with `Class.forName(name, false, classLoader)`: loaded so they can be
  inspected, but not initialized.
- No bytecode library is used.
- Only directory classpath locations are scanned. Any other location type, such as a JAR,
  fails with a descriptive `IllegalStateException`.

## Consequences

- No external dependency, and the scanning code stays short and readable.
- Static initializers never run during scanning; a test fixture with a failing static block
  proves this.
- Every class in the scanned package is loaded into the JVM, including those that will not
  become beans. Spring's ASM approach avoids that cost; for a project of this size it is
  negligible.
- The application cannot be run from a packaged JAR. The failure is explicit rather than
  silent, so the limitation is visible as soon as it is hit.