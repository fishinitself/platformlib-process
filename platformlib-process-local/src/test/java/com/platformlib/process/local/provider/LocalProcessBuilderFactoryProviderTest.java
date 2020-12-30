package com.platformlib.process.local.provider;

import com.platformlib.process.factory.ProcessBuilders;
import com.platformlib.process.local.builder.LocalProcessBuilder;
import com.platformlib.process.local.impl.LocalProcessBuilderImpl;
import com.platformlib.process.local.specification.LocalProcessSpec;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test {@link LocalProcessBuilderFactoryProvider}.
 */
class LocalProcessBuilderFactoryProviderTest {

    @ParameterizedTest
    @MethodSource("provideSpecification")
    void testIsSuitable(final Object processSpec) {
        final LocalProcessBuilderFactoryProvider localProcessBuilderFactoryProvider = new LocalProcessBuilderFactoryProvider();
        assertTrue(localProcessBuilderFactoryProvider.isSuitable(processSpec));
    }

    @ParameterizedTest
    @MethodSource("provideSpecification")
    void testServiceProvider(final Object processSpec) {
        assertThat((Object) ProcessBuilders.newProcessBuilder(processSpec)).isInstanceOf(LocalProcessBuilder.class);
    }

    private static Stream<Arguments> provideSpecification() {
        return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(LocalProcessSpec.LOCAL_COMMAND),
                    Arguments.of(LocalProcessSpec.CURRENT_JAVA_COMMAND),
                    Arguments.of(new LocalProcessSpec(true)),
                    Arguments.of(new LocalProcessSpec(false)),
                    Arguments.of(LocalProcessBuilder.class),
                    Arguments.of(LocalProcessBuilderImpl.class)
                );
        }
}
