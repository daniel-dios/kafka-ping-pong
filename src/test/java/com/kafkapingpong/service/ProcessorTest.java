package com.kafkapingpong.service;

import com.kafkapingpong.event.Message;
import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.repository.SuccessRepository;
import com.kafkapingpong.service.dto.ProcessRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.time.Duration;
import java.util.Optional;
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

  private final ProcessedRepository processedRepository = mock(ProcessedRepository.class);
  private final ImageProcessor imageProcessor = mock(ImageProcessor.class);
  private final SuccessRepository pongRepository = mock(SuccessRepository.class);
  private final Processor processor = new Processor(processedRepository, imageProcessor, pongRepository);

  @Test
  void shouldProcessMessageAndComputeImageOnSuccessInput() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(Optional.empty());
    when(imageProcessor.compute(TRANSACTION_ID)).thenReturn(DURATION_FOR_COMPUTE_IMAGE);

    processor.process(new ProcessRequest(TRANSACTION_ID, false));

    verify(processedRepository).find(TRANSACTION_ID);
    verify(processedRepository).store(argThat(getMessageMatcher(TRANSACTION_ID, false)));
    verify(pongRepository).pong(argThat(
        s -> s.getPong().equals("pong")
            && s.getTransactionId().equals(TRANSACTION_ID)
            && s.getOfMillis().compareTo(DURATION_FOR_COMPUTE_IMAGE) > 0));
  }

  @Test
  void shouldProcessMessageAndNotComputeImageOnSuccessInput() {
    when(processedRepository.find(TRANSACTION_ID)).thenReturn(Optional.of(new Message(TRANSACTION_ID, false)));

    processor.process(new ProcessRequest(TRANSACTION_ID, false));

    verify(processedRepository).find(TRANSACTION_ID);
    verify(pongRepository).pong(argThat(
        s -> s.getPong().equals("pong")
            && s.getTransactionId().equals(TRANSACTION_ID)
            && s.getOfMillis().compareTo(DURATION_FOR_COMPUTE_IMAGE) < 1));
    verify(processedRepository, never()).store(any());
    verify(imageProcessor, never()).compute(any());
  }

  private ArgumentMatcher<Message> getMessageMatcher(UUID transactionId, boolean expectedError) {
    return s -> (s.isError() == expectedError && s.getTransactionId().equals(transactionId));
  }
}
