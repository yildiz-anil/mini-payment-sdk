package io.github.yildizanil.minipayment.framework.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.yildizanil.minipayment.framework.context.fixture.scan.FailingStaticInit;
import io.github.yildizanil.minipayment.framework.context.fixture.scan.OuterClass;
import io.github.yildizanil.minipayment.framework.context.fixture.scan.PlainClass;
import io.github.yildizanil.minipayment.framework.context.fixture.scan.sub.SubPackageClass;
import io.github.yildizanil.minipayment.framework.context.fixture.scanother.SiblingPackageClass;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Verifies that ClassPathScanner finds exactly the classes of a package and its subpackages. */
class ClassPathScannerTest {

    private static final String FIXTURE_PACKAGE =
            "io.github.yildizanil.minipayment.framework.context.fixture.scan";

    private final ClassPathScanner scanner =
            new ClassPathScanner(ClassPathScannerTest.class.getClassLoader());

    @Test
    void findsTopLevelClassesOfPackage() {
        List<Class<?>> classes = scanner.scan(FIXTURE_PACKAGE);

        assertTrue(classes.contains(PlainClass.class));
        assertTrue(classes.contains(OuterClass.class));
    }

    @Test
    void includesSubpackages() {
        assertTrue(scanner.scan(FIXTURE_PACKAGE).contains(SubPackageClass.class));
    }

    @Test
    void excludesSiblingPackageWithSamePrefix() {
        assertFalse(scanner.scan(FIXTURE_PACKAGE).contains(SiblingPackageClass.class));
    }

    @Test
    void includesNestedClassesButSkipsAnonymousOnes() {
        List<Class<?>> classes = scanner.scan(FIXTURE_PACKAGE);

        assertTrue(classes.contains(OuterClass.NestedClass.class));
        assertTrue(classes.stream().noneMatch(Class::isAnonymousClass));
    }

    @Test
    void doesNotRunStaticInitializers() {
        List<Class<?>> classes = assertDoesNotThrow(() -> scanner.scan(FIXTURE_PACKAGE));

        assertTrue(classes.contains(FailingStaticInit.class));
    }

    @Test
    void returnsClassesSortedByName() {
        List<Class<?>> expected = List.of(
                FailingStaticInit.class,
                OuterClass.class,
                OuterClass.NestedClass.class,
                PlainClass.class,
                SubPackageClass.class);

        assertEquals(expected, scanner.scan(FIXTURE_PACKAGE));
    }

    @Test
    void returnsEmptyListForUnknownPackage() {
        assertTrue(scanner.scan("io.github.yildizanil.does.not.exist").isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsNullOrBlankPackageName(String packageName) {
        assertThrows(IllegalArgumentException.class, () -> scanner.scan(packageName));
    }
}
