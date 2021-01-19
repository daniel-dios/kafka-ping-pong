package com.kafkapingpong.service;

import com.kafkapingpong.event.ErrorPongMessage;
import com.kafkapingpong.event.Message;
import com.kafkapingpong.event.PongMessage;
import com.kafkapingpong.repository.ErrorRepository;
import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.repository.SuccessRepository;
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
  private static final ProcessRequest SUCCESS_INPUT = new ProcessRequest(TRANSACTION_ID, false);
  private static final List<Message> EMPTY_LIST = List.of();
  private static final List<Message> LIST_OF_THREE_ERRORS = List.of(
      new Message(TRANSACTION_ID, true),
      new Message(TRANSACTION_ID, true),
      new Message(TRANSACTION_ID, true)
  );
  private static final Duration DURATION_FOR_COMPUTE_IMAGE = Duration.ofSeconds(30);
  private static final String PONG = "pong";

  private ProcessedRepository processedRepository;
  private ImageProcessor imageProcessor;
  private SuccessRepository pongRepository;
  private ErrorRepository errorRepository;
  private Processor processor;

  @BeforeEach
  void setUp() {
    processedRepository = mock(ProcessedRepository.class);
    imageProcessor = mock(ImageProcessor.class);
    pongRepository = mock(SuccessRepository.class);
    errorRepository = mock(ErrorRepository.class);
    processor = new Processor(processedRepository, imageProcessor, pongRepository, errorRepository, 4);
  }

  @ParameterizedTest
  @MethodSource("getErrors")
  void shouldComputeSuccessMessage(List<Message> errorList) {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(errorList);
    when(imageProcessor.compute(TRANSACTION_ID)).thenReturn(DURATION_FOR_COMPUTE_IMAGE);

    processor.process(SUCCESS_INPUT);

    verify(processedRepository).store(getMessage(false));
    verify(imageProcessor).compute(TRANSACTION_ID);
    verify(pongRepository).pong(argThat(
        s -> s.getPong().equals(PONG)
            && s.getTransactionId().equals(TRANSACTION_ID)
            && s.getOfMillis().compareTo(DURATION_FOR_COMPUTE_IMAGE) >= 0));
  }

  private static Stream<Arguments> getErrors() {
    return Stream.of(
        Arguments.of(List.of()),
        Arguments.of(LIST_OF_THREE_ERRORS)
    );
  }

  @Test
  void shouldComputeSuccessMessageWhenPreviousErrorWithSuccessBefore() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(List.of(
        new Message(TRANSACTION_ID, true),
        new Message(TRANSACTION_ID, true),
        new Message(TRANSACTION_ID, false),
        new Message(TRANSACTION_ID, true)
    ));
    when(imageProcessor.compute(TRANSACTION_ID)).thenReturn(DURATION_FOR_COMPUTE_IMAGE);

    processor.process(SUCCESS_INPUT);

    verify(processedRepository).store(getMessage(false));
    verify(imageProcessor).compute(TRANSACTION_ID);
    verify(pongRepository).pong(argThat(s -> s.getPong().equals(PONG)
        && s.getTransactionId().equals(TRANSACTION_ID)
        && s.getOfMillis().compareTo(DURATION_FOR_COMPUTE_IMAGE) >= 0));
  }

  @Test
  void shouldComputeSuccessMessageAndNotComputeImageWhenPreviousMessageWasConsumedWithNoError() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(List.of(new Message(TRANSACTION_ID, false)));

    processor.process(SUCCESS_INPUT);

    verify(pongRepository).pong(getPongMessage());
    verify(processedRepository, never()).store(any());
    verify(imageProcessor, never()).compute(any());
  }

  @ParameterizedTest
  @MethodSource("getSuccessStatus")
  void shouldComputeErrorMessageWithPreviousSuccess(List<Message> value) {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(value);

    processor.process(new ProcessRequest(TRANSACTION_ID, true));

    verify(processedRepository).store(getMessage(true));
    verify(errorRepository).pongForError(new ErrorPongMessage(TRANSACTION_ID, PONG, true));
    verify(pongRepository, never()).pong(any());
    verify(imageProcessor, never()).compute(any());
  }

  private Message getMessage(boolean b) {
    return argThat(getMessageMatcher(b));
  }

  private static Stream<Arguments> getSuccessStatus() {
    return Stream.of(
        Arguments.of(List.of()),
        Arguments.of(List.of(new Message(TRANSACTION_ID, false))),
        Arguments.of(List.of(
            new Message(TRANSACTION_ID, true),
            new Message(TRANSACTION_ID, true),
            new Message(TRANSACTION_ID, true),
            new Message(TRANSACTION_ID, false)))
    );
  }

  @Test
  void shouldComputeErrorMessageWhenReattemptsLowerThanMaximum() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(LIST_OF_THREE_ERRORS);

    processor.process(new ProcessRequest(TRANSACTION_ID, true));

    verify(processedRepository).find(TRANSACTION_ID);
    verify(processedRepository).store(getMessage(true));
    verify(errorRepository).pongForError(new ErrorPongMessage(TRANSACTION_ID, PONG, true));
    verify(pongRepository, never()).pong(any());
    verify(imageProcessor, never()).compute(any());
  }

  @Test
  void shouldNotComputeErrorMessageWhenReattemptsGreaterThanMaximum() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(LIST_OF_THREE_ERRORS);
    final var processor = new Processor(processedRepository, imageProcessor, pongRepository, errorRepository, 3);

    processor.process(new ProcessRequest(TRANSACTION_ID, true));

    verify(processedRepository, never()).store(any());
    verify(errorRepository, never()).pongForError(any());
    verify(pongRepository, never()).pong(any());
    verify(imageProcessor, never()).compute(any());
  }

  @Test
  void shouldProcessMessageAndNotComputeImageOnSuccessInputWhenPreviousErrorsAndLastSuccess() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(List.of(
        new Message(TRANSACTION_ID, true),
        new Message(TRANSACTION_ID, true),
        new Message(TRANSACTION_ID, true),
        new Message(TRANSACTION_ID, false)
    ));

    processor.process(SUCCESS_INPUT);

    verify(processedRepository).find(TRANSACTION_ID);
    verify(processedRepository, never()).store(any());
    verify(imageProcessor, never()).compute(any());
    verify(pongRepository).pong(getPongMessage());
  }

  private PongMessage getPongMessage() {
    return argThat(s -> s.getPong().equals(PONG) && s.getTransactionId().equals(TRANSACTION_ID));
  }

  private ArgumentMatcher<Message> getMessageMatcher(boolean expectedError) {
    return s -> (s.isError() == expectedError && s.getTransactionId().equals(ProcessorTest.TRANSACTION_ID));
  }
}
