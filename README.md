# Mini Payment SDK

Spring's core mechanisms, re-implemented by hand in plain Java — and a small payment SDK that runs on top of them.

Most Java developers use Spring every day; fewer can explain what happens between `@Component` and a fully wired object graph, why `@Transactional` silently does nothing on a self-invocation, or how Spring Boot decides which beans to create. This project answers those questions with working code instead of diagrams: each step replaces one piece of Spring "magic" with an explicit, tested implementation.

> **No Spring on the classpath.** Every mechanism below is written from scratch. The only external libraries are for testing, and later Hibernate (as a JPA provider) and an embedded servlet container — neither of which is Spring.

## Roadmap

| Step | Mechanism built by hand | Spring counterpart | Status |
|---|---|---|---|
| 1 | Classpath scanning, bean definitions, constructor injection | `ApplicationContext` | 🚧 In progress |
| 2 | Custom stereotype annotations (`@PaymentProvider`) | `@Component` meta-annotations | ⏳ Planned |
| 3 | Dynamic proxies and interceptor chains | Spring AOP | ⏳ Planned |
| 4 | Proxy-managed JPA transactions, N+1, optimistic locking | `@Transactional`, `JpaTransactionManager` | ⏳ Planned |
| 5 | Conditional, discoverable auto-configuration | Spring Boot auto-configuration | ⏳ Planned |
| 6 | Container-managed servlet filter chain and front controller | `DelegatingFilterProxy`, `DispatcherServlet` | ⏳ Planned |

## Project layout

```
io.github.yildizanil.minipayment
├── framework   # the mini framework — knows nothing about payments
└── payment     # the payment SDK — built on the framework
```

The framework never depends on the payment package. The dependency points one way only, just like an application depends on Spring and never the reverse.

## Building

Requirements: JDK 25 and Maven 3.9+.

```bash
mvn verify
```

## Architecture decisions

Significant design decisions are recorded as ADRs in [`docs/adr`](docs/adr):

- [ADR-0001](docs/adr/0001-plain-java-single-module.md) — Plain Java, no Spring, single module
- [ADR-0002](docs/adr/0002-class-loading-without-initialization.md) — Load scanned classes without initializing them; scan directories only
