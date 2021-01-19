package com.kafkapingpong.service;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.repository.PongRepository;
import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.service.dto.ProcessRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessorTest {
  private static final UUID TRANSACTION_ID = java.util.UUID.randomUUID();
  private static final Message MESSAGE_SUCCESS = new Message(TRANSACTION_ID, false);
  private static final ProcessRequest SUCCESS_INPUT = new ProcessRequest(TRANSACTION_ID, false);
  private static final Message MESSAGE_ERROR = new Message(TRANSACTION_ID, true);
  private static final List<Message> LIST_OF_THREE_ERRORS = List.of(MESSAGE_ERROR, MESSAGE_ERROR, MESSAGE_ERROR);
  private static final Duration DURATION_FOR_COMPUTE_IMAGE = Duration.ofSeconds(30);

  private ProcessedRepository processedRepository;
  private ImageProcessor imageProcessor;
  private PongRepository pongRepository;
  private Processor processor;

  @BeforeEach
  void setUp() {
    processedRepository = mock(ProcessedRepository.class);
    imageProcessor = mock(ImageProcessor.class);
    pongRepository = mock(PongRepository.class);
    processor = new Processor(processedRepository, imageProcessor, pongRepository, 4);
  }

  @ParameterizedTest
  @MethodSource("getErrors")
  void shouldComputeSuccessMessage(List<Message> errorList) {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(errorList);
    when(imageProcessor.compute(TRANSACTION_ID)).thenReturn(DURATION_FOR_COMPUTE_IMAGE);

    processor.process(SUCCESS_INPUT);

    verify(processedRepository).store(getMessage(false));
    verify(imageProcessor).compute(TRANSACTION_ID);
    verify(pongRepository).pong(getMessage(false), argThat(m -> m.compareTo(DURATION_FOR_COMPUTE_IMAGE) >= 0));
  }

  private static Stream<Arguments> getErrors() {
    return Stream.of(
        Arguments.of(List.of()),
        Arguments.of(LIST_OF_THREE_ERRORS)
    );
  }

  @Test
  void shouldComputeSuccessMessageWhenPreviousErrorWithSuccessBefore() {
    when(processedRepository.find(TRANSACTION_ID))
        .thenReturn(List.of(MESSAGE_ERROR, MESSAGE_ERROR, MESSAGE_SUCCESS, MESSAGE_ERROR));
    when(imageProcessor.compute(TRANSACTION_ID))
        .thenReturn(DURATION_FOR_COMPUTE_IMAGE);

    processor.process(SUCCESS_INPUT);

    verify(processedRepository).store(getMessage(false));
    verify(imageProcessor).compute(TRANSACTION_ID);
    verify(pongRepository).pong(getMessage(false), argThat(m -> m.compareTo(DURATION_FOR_COMPUTE_IMAGE) >= 0));
  }

  @Test
  void shouldComputeSuccessMessageAndNotComputeImageWhenPreviousMessageWasConsumedWithNoError() {
    when(processedRepository.find(TRANSACTION_ID))
        .thenReturn(List.of(MESSAGE_SUCCESS));

    processor.process(SUCCESS_INPUT);

    verify(pongRepository).pong(getMessage(false), any());
    verify(processedRepository, never()).store(any());
    verify(imageProcessor, never()).compute(any());
  }

  @ParameterizedTest
  @MethodSource("getSuccessStatus")
  void shouldComputeErrorMessageWithPreviousSuccess(List<Message> value) {
    when(processedRepository.find(TRANSACTION_ID))
        .thenReturn(value);

    processor.process(new ProcessRequest(TRANSACTION_ID, true));

    verify(processedRepository).store(getMessage(true));
    verify(pongRepository).pongForError(getMessage(true));
    verify(pongRepository, never()).pong(any(), any());
    verify(imageProcessor, never()).compute(any());
  }

  private static Stream<Arguments> getSuccessStatus() {
    return Stream.of(
        Arguments.of(List.of()),
        Arguments.of(List.of(MESSAGE_SUCCESS)),
        Arguments.of(List.of(MESSAGE_ERROR, MESSAGE_ERROR, MESSAGE_ERROR, MESSAGE_SUCCESS))
    );
  }

  @Test
  void shouldComputeErrorMessageWhenReattemptsLowerThanMaximum() {
    when(processedRepository.find(TRANSACTION_ID))
        .thenReturn(LIST_OF_THREE_ERRORS);

    processor.process(new ProcessRequest(TRANSACTION_ID, true));

    verify(processedRepository).find(TRANSACTION_ID);
    verify(processedRepository).store(getMessage(true));
    verify(pongRepository).pongForError(getMessage(true));
    verify(pongRepository, never()).pong(any(), any());
    verify(imageProcessor, never()).compute(any());
  }

  @Test
  void shouldNotComputeErrorMessageWhenReattemptsGreaterThanMaximum() {
    when(processedRepository.find(TRANSACTION_ID))
        .thenReturn(LIST_OF_THREE_ERRORS);
    final var processor = new Processor(processedRepository, imageProcessor, pongRepository, 3);

    processor.process(new ProcessRequest(TRANSACTION_ID, true));

    verify(processedRepository, never()).store(any());
    verify(pongRepository, never()).pongForError(any());
    verify(pongRepository, never()).pong(any(), any());
    verify(imageProcessor, never()).compute(any());
  }

  @Test
  void shouldProcessMessageAndNotComputeImageOnSuccessInputWhenPreviousErrorsAndLastSuccess() {
    when(processedRepository.find(TRANSACTION_ID))
        .thenReturn(List.of(MESSAGE_ERROR, MESSAGE_ERROR, MESSAGE_ERROR, MESSAGE_SUCCESS));

    processor.process(SUCCESS_INPUT);

    verify(processedRepository).find(TRANSACTION_ID);
    verify(processedRepository, never()).store(any());
    verify(imageProcessor, never()).compute(any());
    verify(pongRepository).pong(getMessage(false), any());
  }

  private Message getMessage(boolean error) {
    return argThat(getMessageMatcher(error));
  }

  private ArgumentMatcher<Message> getMessageMatcher(boolean expectedError) {
    return s -> (s.isError() == expectedError && s.getTransactionId().equals(ProcessorTest.TRANSACTION_ID));
  }
}
