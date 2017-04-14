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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Represents a path on a Vault server.
 */
public class Path implements Serializable {

  private String name;

  private List<Mapping> mappings;

  /**
   * Initializes a new instance of the {@link Path} class.
   */
  public Path() { }

  /**
   * Initializes a new instance of the {@link Path} class.
   *
   * @param name the name of the path
   * @param mappings the mappings of the path
   */
  public Path(String name, List<Mapping> mappings) {
    this.name = name;
    this.mappings = mappings;
  }

  /**
   * Gets the name of this path.
   *
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the mappings of this path.
   *
   * @return the mappings
   */
  public List<Mapping> getMappings() {
    return this.mappings;
  }

  /**
   * Returns a hash code value for this path.
   *
   * @return the hash code
   */
  public int hashCode() {
    return Objects.hash(this.name, this.mappings);
  }

  /**
   * Returns a value indicating whether this path is equal to another object.
   *
   * @return {@code true} if the this path is equal to the object; otherwise, {@code false}
   */
  public boolean equals(Object object) {
    if (object instanceof Path) {
      Path that = (Path) object;
      return Objects.equals(this.name, that.name)
          && Objects.equals(this.mappings, that.mappings);
    }
    return false;
  }

}
