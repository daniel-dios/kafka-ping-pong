package com.kafkapingpong.usecase;

import com.kafkapingpong.domain.message.Message;
import com.kafkapingpong.domain.message.MessageRepository;
import com.kafkapingpong.domain.message.Payload;
import com.kafkapingpong.domain.message.PongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessorTest {
  private static final UUID TRANSACTION_ID = java.util.UUID.randomUUID();
  private static final Message SUCCESS = new Message(TRANSACTION_ID, new Payload("ping", false));
  private static final Message ERROR = new Message(TRANSACTION_ID, new Payload("ping", true));
  private static final int MAX_ATTEMPTS = 4;
  private static final List<Message> LIST_OF_THREE_ERRORS = List.of(ERROR, ERROR, ERROR);
  private static final Duration DURATION_FOR_COMPUTE_IMAGE = Duration.ofSeconds(30);

  private MessageRepository messageRepository;
  private ImageProcessor imageProcessor;
  private PongRepository pongRepository;
  private Processor processor;

  @BeforeEach
  void setUp() {
    messageRepository = mock(MessageRepository.class);
    imageProcessor = mock(ImageProcessor.class);
    pongRepository = mock(PongRepository.class);
    processor = new Processor(messageRepository, imageProcessor, pongRepository, MAX_ATTEMPTS);
  }

  @ParameterizedTest
  @MethodSource("getErrors")
  void shouldComputeSuccessMessage(List<Message> errorList) {
    when(messageRepository.getLast(TRANSACTION_ID, MAX_ATTEMPTS)).thenReturn(errorList);
    when(imageProcessor.compute(TRANSACTION_ID)).thenReturn(DURATION_FOR_COMPUTE_IMAGE);

    processor.process(SUCCESS);

    verify(messageRepository).store(same(SUCCESS));
    verify(imageProcessor).compute(TRANSACTION_ID);
    verify(pongRepository).pong(same(SUCCESS), argThat(m -> m.compareTo(DURATION_FOR_COMPUTE_IMAGE) >= 0));
  }

  private static Stream<Arguments> getErrors() {
    return Stream.of(
        Arguments.of(List.of()),
        Arguments.of(LIST_OF_THREE_ERRORS)
    );
  }

  @Test
  void shouldComputeSuccessMessageWhenPreviousErrorWithSuccessBefore() {
    when(messageRepository.getLast(TRANSACTION_ID, MAX_ATTEMPTS)).thenReturn(List.of(ERROR, ERROR, SUCCESS, ERROR));
    when(imageProcessor.compute(TRANSACTION_ID)).thenReturn(DURATION_FOR_COMPUTE_IMAGE);

    processor.process(SUCCESS);

    verify(messageRepository).store(same(SUCCESS));
    verify(imageProcessor).compute(TRANSACTION_ID);
    verify(pongRepository).pong(same(SUCCESS), argThat(m -> m.compareTo(DURATION_FOR_COMPUTE_IMAGE) >= 0));
  }

  @Test
  void shouldComputeSuccessMessageAndNotComputeImageWhenPreviousMessageWasConsumedWithNoError() {
    when(messageRepository.getLast(TRANSACTION_ID, MAX_ATTEMPTS)).thenReturn(List.of(SUCCESS));

    processor.process(SUCCESS);

    verify(pongRepository).pong(same(SUCCESS), any());
    verify(messageRepository, never()).store(any());
    verify(imageProcessor, never()).compute(any());
  }

  @ParameterizedTest
  @MethodSource("getSuccessStatus")
  void shouldComputeErrorMessageWithPreviousSuccess(List<Message> value) {
    when(messageRepository.getLast(TRANSACTION_ID, MAX_ATTEMPTS)).thenReturn(value);

    processor.process(ERROR);

    verify(messageRepository).store(same(ERROR));
    verify(pongRepository).pongForError(same(ERROR));
    verify(pongRepository, never()).pong(any(), any());
    verify(imageProcessor, never()).compute(any());
  }

  private static Stream<Arguments> getSuccessStatus() {
    return Stream.of(
        Arguments.of(List.of()),
        Arguments.of(List.of(SUCCESS)),
        Arguments.of(List.of(SUCCESS, ERROR, ERROR, ERROR))
    );
  }

  @Test
  void shouldComputeErrorMessageWhenReattemptsLowerThanMaximum() {
    when(messageRepository.getLast(TRANSACTION_ID, MAX_ATTEMPTS)).thenReturn(LIST_OF_THREE_ERRORS);

    processor.process(ERROR);

    verify(messageRepository).getLast(TRANSACTION_ID, MAX_ATTEMPTS);
    verify(messageRepository).store(same(ERROR));
    verify(pongRepository).pongForError(same(ERROR));
    verify(pongRepository, never()).pong(any(), any());
    verify(imageProcessor, never()).compute(any());
  }

  @Test
  void shouldNotComputeErrorMessageWhenReattemptsGreaterThanMaximum() {
    when(messageRepository.getLast(TRANSACTION_ID, 3)).thenReturn(LIST_OF_THREE_ERRORS);
    final var processor = new Processor(messageRepository, imageProcessor, pongRepository, 3);

    processor.process(ERROR);

    verify(messageRepository).store(ERROR);
    verify(pongRepository).dlq(same(ERROR));
    verify(pongRepository, never()).pongForError(any());
    verify(pongRepository, never()).pong(any(), any());
    verify(imageProcessor, never()).compute(any());
  }

  @Test
  void shouldProcessMessageAndNotComputeImageOnSuccessInputWhenPreviousErrorsAndLastSuccess() {
    when(messageRepository.getLast(TRANSACTION_ID, MAX_ATTEMPTS)).thenReturn(List.of(SUCCESS, ERROR, ERROR, ERROR));

    processor.process(SUCCESS);

    verify(messageRepository).getLast(TRANSACTION_ID, MAX_ATTEMPTS);
    verify(messageRepository, never()).store(any());
    verify(imageProcessor, never()).compute(any());
    verify(pongRepository).pong(same(SUCCESS), any());
  }
}
