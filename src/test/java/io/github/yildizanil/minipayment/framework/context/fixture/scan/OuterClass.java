package io.github.yildizanil.minipayment.framework.context.fixture.scan;

/**
 * Test fixture: holds a static nested class, which the scanner must return, and an anonymous
 * class, which the scanner must skip.
 */
public class OuterClass {

    public static class NestedClass {}

    Runnable anonymous = new Runnable() {
        @Override
        public void run() {}
    };
}
