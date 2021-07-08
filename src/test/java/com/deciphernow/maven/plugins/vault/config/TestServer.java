/*
 * Copyright 2017 Decipher Technology Studios LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deciphernow.maven.plugins.vault.config;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Provides unit tests for the {@link Server} class.
 */
public class TestServer {

  private static final Random RANDOM = new Random();
  private static final List<Path> PATHS = randomPaths(10, 10);
  private static final File SSL_CERTIFICATE = new File("/dev/null");
  private static final boolean SSL_VERIFY = RANDOM.nextBoolean();
  private static final boolean SKIP_EXECUTION = RANDOM.nextBoolean();
  private static final String TOKEN = UUID.randomUUID().toString();
  private static final String URL = UUID.randomUUID().toString();
  private static final Server INSTANCE = new Server(URL, TOKEN, 2, SSL_VERIFY, SSL_CERTIFICATE, PATHS, SKIP_EXECUTION);

  private static Path randomPath(int mappingCount) {
    return new Path(UUID.randomUUID().toString(), randomMappings(mappingCount));
  }

  private static List<Path> randomPaths(int pathCount, int mappingCount) {
    return IntStream.range(0, pathCount).mapToObj(i -> randomPath(mappingCount)).collect(Collectors.toList());
  }

  private static Mapping randomMapping() {
    return new Mapping(UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  private static List<Mapping> randomMappings(int count) {
    return IntStream.range(0, count).mapToObj(i -> randomMapping()).collect(Collectors.toList());
  }

  /**
   * Tests the {@link Server#getPaths()} property.
   */
  @Test
  public void testGetPaths() {
    assertEquals(PATHS, INSTANCE.getPaths());
  }

  /**
   * Tests the {@link Server#getSslCertificate()} property.
   */
  @Test
  public void testGetSslCertificate() {
    assertEquals(SSL_CERTIFICATE, INSTANCE.getSslCertificate());
  }

  /**
   * Tests the {@link Server#getSslVerify()} property.
   */
  @Test
  public void testGetSslVerify() {
    assertEquals(SSL_VERIFY, INSTANCE.getSslVerify());
  }

  /**
   * Tests the {@link Server#isSkipExecution()} property.
   */
  @Test
  public void testIsSkipExecution() {
    assertEquals(SKIP_EXECUTION, INSTANCE.isSkipExecution());
  }

  /**
   * Tests the {@link Server#getToken()} property.
   */
  @Test
  public void testGetToken() {
    assertEquals(TOKEN, INSTANCE.getToken());
  }

  /**
   * Tests the {@link Server#getUrl()} property.
   */
  @Test
  public void testGetUrl() {
    assertEquals(URL, INSTANCE.getUrl());
  }

  /**
   * Tests the {@link Server#equals(Object)} and {@link Server#hashCode()} methods.
   */
  @Test
  public void testEquality() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(INSTANCE, INSTANCE, new Server(URL, TOKEN, SSL_VERIFY, SSL_CERTIFICATE, PATHS, SKIP_EXECUTION));
    tester.addEqualityGroup(new Server(UUID.randomUUID().toString(), TOKEN, SSL_VERIFY, SSL_CERTIFICATE, PATHS, SKIP_EXECUTION));
    tester.addEqualityGroup(new Server(URL, UUID.randomUUID().toString(), SSL_VERIFY, SSL_CERTIFICATE, PATHS, SKIP_EXECUTION));
    tester.addEqualityGroup(new Server(URL, TOKEN, !SSL_VERIFY, SSL_CERTIFICATE, PATHS, SKIP_EXECUTION));
    tester.addEqualityGroup(new Server(URL, TOKEN, SSL_VERIFY, new File("/dev/random"), PATHS, SKIP_EXECUTION));
    tester.addEqualityGroup(new Server(URL, TOKEN, SSL_VERIFY, SSL_CERTIFICATE, randomPaths(10, 10), SKIP_EXECUTION));
    tester.testEquals();
  }

  /**
   * Tests serialization of the {@link Server} class.
   */
  @Test
  public void testSerialization() throws ClassNotFoundException, IOException {

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

      objectOutputStream.writeObject(INSTANCE);

      try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
           ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

        Server deserialized = (Server) objectInputStream.readObject();
        assertEquals(PATHS, deserialized.getPaths());
        assertEquals(SSL_CERTIFICATE, deserialized.getSslCertificate());
        assertEquals(SSL_VERIFY, deserialized.getSslVerify());
        assertEquals(TOKEN, deserialized.getToken());
        assertEquals(URL, deserialized.getUrl());
        assertEquals(SKIP_EXECUTION, deserialized.isSkipExecution());
      }
    }
  }

}
