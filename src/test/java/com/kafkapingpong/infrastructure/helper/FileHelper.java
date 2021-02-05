package com.kafkapingpong.infrastructure.helper;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class FileHelper {

  private FileHelper() {
    // utilities class
  }

  public static byte[] resourceToBytes(String path) {
    final ResourceLoader resourceLoader = new DefaultResourceLoader();

    try (InputStream is = resourceLoader.getResource(path).getInputStream()) {
      return IOUtils.toByteArray(is);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
