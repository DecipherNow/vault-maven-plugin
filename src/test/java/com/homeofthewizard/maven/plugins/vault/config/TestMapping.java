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

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Provides unit tests for the {@link Mapping} class.
 */
public class TestMapping {

  private static final String KEY = UUID.randomUUID().toString();
  private static final String PROPERTY = UUID.randomUUID().toString();
  private static final Mapping INSTANCE = new Mapping(KEY, PROPERTY);

  /**
   * Tests the {@link Mapping#getKey()} property.
   */
  @Test
  public void testGetKey() {
    assertEquals(KEY, INSTANCE.getKey());
  }

  /**
   * Tests the {@link Mapping#getProperty()} property.
   */
  @Test
  public void testGetProperty() {
    assertEquals(PROPERTY, INSTANCE.getProperty());
  }

  /**
   * Tests the {@link Mapping#equals(Object)} and {@link Mapping#hashCode()} methods.
   */
  @Test
  public void testEquality() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(INSTANCE, INSTANCE, new Mapping(KEY, PROPERTY));
    tester.addEqualityGroup(new Mapping(KEY, UUID.randomUUID().toString()));
    tester.addEqualityGroup(new Mapping(UUID.randomUUID().toString(), PROPERTY));
    tester.testEquals();
  }

  /**
   * Tests serialization of the {@link Mapping} class.
   */
  @Test
  public void testSerialization() throws ClassNotFoundException, IOException {

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

      objectOutputStream.writeObject(INSTANCE);

      try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
           ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

        Mapping deserailized = (Mapping) objectInputStream.readObject();
        assertEquals(KEY, deserailized.getKey());
        assertEquals(PROPERTY, deserailized.getProperty());
      }
    }
  }
}
