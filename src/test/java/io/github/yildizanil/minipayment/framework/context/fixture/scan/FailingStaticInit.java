package io.github.yildizanil.minipayment.framework.context.fixture.scan;

/**
 * Test fixture: its static initializer always fails. Scanning must only load the class, never
 * initialize it, so finding this class must not throw.
 */
public class FailingStaticInit {

    static {
        // "if (true)" keeps the compiler happy: a static block must be able to complete normally.
        if (true) {
            throw new IllegalStateException("Static initializer must not run during scanning");
        }
    }
}
