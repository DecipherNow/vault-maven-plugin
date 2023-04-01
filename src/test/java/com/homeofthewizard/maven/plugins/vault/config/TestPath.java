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

package com.homeofthewizard.maven.plugins.vault.config;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Provides unit tests for the {@link Path} class.
 */
public class TestPath {

  private static final String NAME = UUID.randomUUID().toString();
  private static final List<Mapping> MAPPINGS = randomMappings(10);
  private static final Path INSTANCE = new Path(NAME, MAPPINGS);

  private static Mapping randomMapping() {
    return new Mapping(UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  private static List<Mapping> randomMappings(int count) {
    return IntStream.range(0, count).mapToObj(i -> randomMapping()).collect(Collectors.toList());
  }

  /**
   * Tests the {@link Path#getName()} property.
   */
  @Test
  public void testGetName() {
    assertEquals(NAME, INSTANCE.getName());
  }

  /**
   * Tests the {@link Path#getMappings()} property.
   */
  @Test
  public void testGetProperty() {
    assertEquals(MAPPINGS, INSTANCE.getMappings());
  }

  /**
   * Tests the {@link Path#equals(Object)} and {@link Path#hashCode()} methods.
   */
  @Test
  public void testEquality() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(INSTANCE, INSTANCE, new Path(NAME, MAPPINGS));
    tester.addEqualityGroup(new Path(NAME, ImmutableList.of()));
    tester.addEqualityGroup(new Path(UUID.randomUUID().toString(), MAPPINGS));
    tester.testEquals();
  }

  /**
   * Tests serialization of the {@link Path} class.
   */
  @Test
  public void testSerialization() throws ClassNotFoundException, IOException {

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

      objectOutputStream.writeObject(INSTANCE);

      try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
           ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

        Path deserialized = (Path) objectInputStream.readObject();
        assertEquals(NAME, deserialized.getName());
        assertEquals(MAPPINGS, deserialized.getMappings());
      }
    }
  }

}
