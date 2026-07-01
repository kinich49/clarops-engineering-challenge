package com.clara.challenge.utils;

import com.clara.challenge.entities.misc.SafeguardProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TtlEvaluatorTest {

    @Test
    void shouldNotBeBreached_WhenExpectedBeforeIsNull() {
        assertThat(TtlEvaluator.isBreached(null, new SafeguardProperties(false, 0L))).isFalse();
    }

    @Test
    void shouldNotBeBreached_WhenDeadlineNotYetReached() {
        var expectedBefore = Instant.now().plusSeconds(100);

        assertThat(TtlEvaluator.isBreached(expectedBefore, new SafeguardProperties(false, 0L))).isFalse();
    }

    @Test
    void shouldBeBreached_WhenDeadlineHasPassedAndSafeguardDisabled() {
        var expectedBefore = Instant.now().minusSeconds(100);

        assertThat(TtlEvaluator.isBreached(expectedBefore, new SafeguardProperties(false, 999L))).isTrue();
    }

    @Test
    void shouldBeBreached_WhenSafeguardEnabledButOffsetDoesNotCoverTheGap() {
        var expectedBefore = Instant.now().minusSeconds(1000);

        assertThat(TtlEvaluator.isBreached(expectedBefore, new SafeguardProperties(true, 10L))).isTrue();
    }

    @Test
    void shouldNotBeBreached_WhenSafeguardOffsetKeepsDeadlineInFuture() {
        var expectedBefore = Instant.now().minusSeconds(5);

        assertThat(TtlEvaluator.isBreached(expectedBefore, new SafeguardProperties(true, 60L))).isFalse();
    }
}
