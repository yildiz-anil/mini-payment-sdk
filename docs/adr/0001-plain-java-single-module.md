# ADR-0001: Plain Java, no Spring, single module

## Status

Accepted

## Context

The goal of this project is to demonstrate an understanding of what Spring does internally — dependency injection, AOP proxies, transaction management, auto-configuration and the request filter chain — rather than the ability to use it.

Two broad approaches were considered:

1. Build a payment SDK with Spring and explain its internals in documentation.

2. Re-implement each Spring mechanism by hand and build the payment SDK on top of that.

The project also needs a structure. A multi-module Maven build (framework / SDK / examples) would enforce boundaries at compile time, at the cost of extra build configuration for a project that grows one mechanism at a time.

## Decision

- No Spring dependency. Every Spring mechanism covered by the project is implemented by hand in plain Java.

- Non-Spring libraries are allowed where the mechanism being demonstrated sits on top of them: Hibernate as the JPA provider and an embedded servlet container.

- A single Maven module. The boundary between the framework and the payment SDK is kept at package level: `..framework` must not depend on `..payment`.

- The project grows step by step; each step adds one mechanism and is delivered as its own commit(s).

## Consequences

- The code itself shows how the mechanisms work, which is the point of the project.

- The framework is deliberately incomplete. It covers the parts needed to explain each mechanism, not Spring's full feature set; edge cases are documented rather than handled.

- A single module keeps the build simple, but the framework/payment boundary is enforced only by convention until an architecture test is added.

