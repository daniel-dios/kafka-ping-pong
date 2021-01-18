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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessorTest {

  private static final UUID TRANSACTION_TYPE = java.util.UUID.randomUUID();
  private static final Duration DURATION_FOR_COMPUTE_IMAGE = Duration.ofSeconds(30);

  private final ProcessedRepository processedRepository = mock(ProcessedRepository.class);
  private final ImageProcessor imageProcessor = mock(ImageProcessor.class);
  private final SuccessRepository pongRepository = mock(SuccessRepository.class);
  private final Processor processor = new Processor(processedRepository, imageProcessor, pongRepository);

  @Test
  void shouldProcessMessageForTheFirstTimeAndStoreWhenNoError() {
    when(processedRepository.find(TRANSACTION_TYPE)).thenReturn(Optional.empty());
    when(imageProcessor.compute(TRANSACTION_TYPE)).thenReturn(DURATION_FOR_COMPUTE_IMAGE);

    processor.process(new ProcessRequest(TRANSACTION_TYPE, false));

    verify(processedRepository).find(TRANSACTION_TYPE);
    verify(processedRepository).store(argThat(getMessageMatcher(TRANSACTION_TYPE, false)));
    verify(pongRepository).pong(argThat(getPongMatcher(DURATION_FOR_COMPUTE_IMAGE)));
  }

  private ArgumentMatcher<PongMessage> getPongMatcher(Duration durationForComputeImage) {
    return s -> s.getPong().equals("pong")
        && s.getTransactionType().equals(TRANSACTION_TYPE)
        && s.getOfMillis().compareTo(durationForComputeImage) > 0;
  }

  private ArgumentMatcher<Message> getMessageMatcher(UUID transactionType, boolean expectedError) {
    return s -> (s.isError() == expectedError && s.getTransactionType().equals(transactionType));
  }
}
