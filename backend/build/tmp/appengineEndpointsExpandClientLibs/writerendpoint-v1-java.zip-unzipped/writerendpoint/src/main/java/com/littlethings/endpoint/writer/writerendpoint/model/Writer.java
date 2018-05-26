/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://github.com/google/apis-client-generator/
 * (build: 2015-11-16 19:10:01 UTC)
 * on 2015-12-06 at 14:47:31 UTC 
 * Modify at your own risk.
 */

package com.littlethings.endpoint.writer.writerendpoint.model;

/**
 * Model definition for Writer.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the writerendpoint. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class Writer extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String registrationId;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String secret;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long timestamp;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean writer;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public Writer setId(java.lang.Long id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getRegistrationId() {
    return registrationId;
  }

  /**
   * @param registrationId registrationId or {@code null} for none
   */
  public Writer setRegistrationId(java.lang.String registrationId) {
    this.registrationId = registrationId;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getSecret() {
    return secret;
  }

  /**
   * @param secret secret or {@code null} for none
   */
  public Writer setSecret(java.lang.String secret) {
    this.secret = secret;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getTimestamp() {
    return timestamp;
  }

  /**
   * @param timestamp timestamp or {@code null} for none
   */
  public Writer setTimestamp(java.lang.Long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getWriter() {
    return writer;
  }

  /**
   * @param writer writer or {@code null} for none
   */
  public Writer setWriter(java.lang.Boolean writer) {
    this.writer = writer;
    return this;
  }

  @Override
  public Writer set(String fieldName, Object value) {
    return (Writer) super.set(fieldName, value);
  }

  @Override
  public Writer clone() {
    return (Writer) super.clone();
  }

}
