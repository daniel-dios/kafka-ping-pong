package com.kafkapingpong.service;

import com.kafkapingpong.repository.ProcessedRepository;
import com.kafkapingpong.service.dto.ProcessRequest;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessorTest {

  private static final UUID TRANSACTION_TYPE = java.util.UUID.randomUUID();

  private final ProcessedRepository processedRepository = mock(ProcessedRepository.class);
  private final ImageProcessor imageProcessor = mock(ImageProcessor.class);
  private final Processor processor = new Processor(processedRepository, imageProcessor);

  @Test
  void shouldProcessMessageForTheFirstTimeAndStoreWhenNoError() {
    when(processedRepository.find(TRANSACTION_TYPE)).thenReturn(Optional.empty());

    processor.process(new ProcessRequest(TRANSACTION_TYPE, false));

    verify(processedRepository).find(TRANSACTION_TYPE);

    verify(processedRepository).store(argThat(s -> (!s.isError() && s.getTransactionType().equals(TRANSACTION_TYPE))));
    verify(imageProcessor).compute(TRANSACTION_TYPE);
  }
}
