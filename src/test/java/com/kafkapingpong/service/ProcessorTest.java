package com.kafkapingpong.service;

import com.kafkapingpong.event.ErrorPongMessage;
import com.kafkapingpong.event.Message;
import com.kafkapingpong.repository.ErrorRepository;
import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.repository.SuccessRepository;
import com.kafkapingpong.service.dto.ProcessRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessorTest {

  private static final UUID TRANSACTION_ID = java.util.UUID.randomUUID();
  private static final Duration DURATION_FOR_COMPUTE_IMAGE = Duration.ofSeconds(30);

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
    processor = new Processor(processedRepository, imageProcessor, pongRepository, errorRepository, 10);
  }

  @Test
  void shouldProcessMessageAndComputeImageOnSuccessInput() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(List.of());
    when(imageProcessor.compute(TRANSACTION_ID)).thenReturn(DURATION_FOR_COMPUTE_IMAGE);

    processor.process(new ProcessRequest(TRANSACTION_ID, false));

    verify(processedRepository).find(TRANSACTION_ID);
    verify(processedRepository).store(argThat(getMessageMatcher(TRANSACTION_ID, false)));
    verify(imageProcessor).compute(TRANSACTION_ID);
    verify(pongRepository).pong(argThat(
        s -> s.getPong().equals("pong")
            && s.getTransactionId().equals(TRANSACTION_ID)
            && s.getOfMillis().compareTo(DURATION_FOR_COMPUTE_IMAGE) >= 0));
  }

  @Test
  void shouldProcessMessageAndComputeImageOnSuccessInputWhenPreviousErrors() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(List.of(
        new Message(TRANSACTION_ID, true),
        new Message(TRANSACTION_ID, true)));
    when(imageProcessor.compute(TRANSACTION_ID)).thenReturn(DURATION_FOR_COMPUTE_IMAGE);

    processor.process(new ProcessRequest(TRANSACTION_ID, false));

    verify(processedRepository).find(TRANSACTION_ID);
    verify(processedRepository).store(argThat(getMessageMatcher(TRANSACTION_ID, false)));
    verify(imageProcessor).compute(TRANSACTION_ID);
    verify(pongRepository).pong(argThat(
        s -> s.getPong().equals("pong")
            && s.getTransactionId().equals(TRANSACTION_ID)
            && s.getOfMillis().compareTo(DURATION_FOR_COMPUTE_IMAGE) >= 0));
  }

  @Test
  void shouldProcessMessageAndNotComputeImageOnSuccessInput() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(List.of(new Message(TRANSACTION_ID, false)));

    processor.process(new ProcessRequest(TRANSACTION_ID, false));

    verify(processedRepository).find(TRANSACTION_ID);
    verify(pongRepository).pong(argThat(
        s -> s.getPong().equals("pong")
            && s.getTransactionId().equals(TRANSACTION_ID)));
    verify(processedRepository, never()).store(any());
    verify(imageProcessor, never()).compute(any());
  }

  @Test
  void shouldProcessMessageWhenErrorAndNotCompute() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(List.of(new Message(TRANSACTION_ID, false)));

    processor.process(new ProcessRequest(TRANSACTION_ID, true));

    verify(processedRepository).find(TRANSACTION_ID);
    verify(processedRepository).store(argThat(getMessageMatcher(TRANSACTION_ID, true)));
    verify(errorRepository).pongForError(new ErrorPongMessage(TRANSACTION_ID, "pong", true));
    verify(pongRepository, never()).pong(any());
    verify(imageProcessor, never()).compute(any());
  }

  @Test
  void shouldNotSendErrorMessageAfterReAttempts() {
    final var messages = List.of(
        new Message(TRANSACTION_ID, true),
        new Message(TRANSACTION_ID, true),
        new Message(TRANSACTION_ID, true)
    );

    when(processedRepository.find(TRANSACTION_ID)).thenReturn(messages);
    processor = new Processor(processedRepository, imageProcessor, pongRepository, errorRepository, 3);

    processor.process(new ProcessRequest(TRANSACTION_ID, true));

    verify(processedRepository).find(TRANSACTION_ID);
    verify(processedRepository, never()).store(any());
    verify(errorRepository, never()).pongForError(any());
    verify(pongRepository, never()).pong(any());
    verify(imageProcessor, never()).compute(any());
  }

  @Test
  void shouldSendErrorMessageBeforeReAttempts() {
    final var messages = List.of(
        new Message(TRANSACTION_ID, true),
        new Message(TRANSACTION_ID, true),
        new Message(TRANSACTION_ID, true)
    );

    when(processedRepository.find(TRANSACTION_ID)).thenReturn(messages);
    processor = new Processor(processedRepository, imageProcessor, pongRepository, errorRepository, 4);

    processor.process(new ProcessRequest(TRANSACTION_ID, true));

    verify(processedRepository).find(TRANSACTION_ID);
    verify(processedRepository).store(argThat(getMessageMatcher(TRANSACTION_ID, true)));
    verify(errorRepository).pongForError(new ErrorPongMessage(TRANSACTION_ID, "pong", true));
    verify(pongRepository, never()).pong(any());
    verify(imageProcessor, never()).compute(any());
  }


  private ArgumentMatcher<Message> getMessageMatcher(UUID transactionId, boolean expectedError) {
    return s -> (s.isError() == expectedError && s.getTransactionId().equals(transactionId));
  }
}
