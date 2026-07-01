package com.clara.challenge.filters.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.filters.EventIngestionContext;
import com.clara.challenge.filters.EventIngestionFilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class ValidEventShapeFilterTest {

  private final ValidEventShapeFilter subject = new ValidEventShapeFilter();

  @ParameterizedTest
  @NullAndEmptySource
  void shouldContinueChain_WhenEventIsFinalAndDoesNotDeclareNextExpectedEvent(
      String nextExpectedEvent) {
    var chain = mock(EventIngestionFilterChain.class);
    var context = buildContext(true, nextExpectedEvent, null);

    subject.doFilter(context, chain);

    assertThat(context.shouldIngest()).isTrue();
    verify(chain).doFilter(context);
  }

  @Test
  void shouldRejectEvent_WhenEventIsFinalAndDeclaresNextExpectedEvent() {
    var chain = mock(EventIngestionFilterChain.class);
    var context = buildContext(true, "myExpectedEvent", null);

    subject.doFilter(context, chain);

    assertThat(context.shouldIngest()).isFalse();
    assertThat(context.isInvalid()).isTrue();
    assertThat(context.getRejectionReason()).contains("myExpectedEvent");
    verifyNoInteractions(chain);
  }

  @Test
  void shouldContinueChain_WhenEventDeclaresTtlAndNextExpectedEvent() {
    var chain = mock(EventIngestionFilterChain.class);
    var context = buildContext(false, "myExpectedEvent", 60);

    subject.doFilter(context, chain);

    assertThat(context.shouldIngest()).isTrue();
    verify(chain).doFilter(context);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldRejectEvent_WhenEventDeclaresTtlButNoNextExpectedEvent(String nextExpectedEvent) {
    var chain = mock(EventIngestionFilterChain.class);
    var context = buildContext(false, nextExpectedEvent, 60);

    subject.doFilter(context, chain);

    assertThat(context.shouldIngest()).isFalse();
    assertThat(context.isInvalid()).isTrue();
    assertThat(context.getRejectionReason()).contains("nextEventTtlSeconds");
    verifyNoInteractions(chain);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldContinueChain_WhenEventDeclaresNeitherTtlNorNextExpectedEvent(
      String nextExpectedEvent) {
    var chain = mock(EventIngestionFilterChain.class);
    var context = buildContext(false, nextExpectedEvent, null);

    subject.doFilter(context, chain);

    assertThat(context.shouldIngest()).isTrue();
    verify(chain).doFilter(context);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldContinueChain_WhenTtlIsZero_RegardlessOfNextExpectedEvent(String nextExpectedEvent) {
    var chain = mock(EventIngestionFilterChain.class);
    var context = buildContext(false, nextExpectedEvent, 0);

    subject.doFilter(context, chain);

    assertThat(context.shouldIngest()).isTrue();
    verify(chain).doFilter(context);
  }

  private EventIngestionContext buildContext(
      boolean finalEvent, String nextExpectedEvent, Integer nextEventTtlSeconds) {
    var event = new Event();
    event.setEventId("event-1");
    event.setEventName("APPLICATION_RECEIVED");
    event.setFinalEvent(finalEvent);
    event.setNextExpectedEvent(nextExpectedEvent);
    event.setNextEventTtlSeconds(nextEventTtlSeconds);
    return new EventIngestionContext(event, null);
  }
}
