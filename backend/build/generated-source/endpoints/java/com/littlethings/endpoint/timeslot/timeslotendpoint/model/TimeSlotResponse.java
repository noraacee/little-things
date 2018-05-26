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
 * on 2015-12-06 at 14:47:30 UTC 
 * Modify at your own risk.
 */

package com.littlethings.endpoint.timeslot.timeslotendpoint.model;

/**
 * Model definition for TimeSlotResponse.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the timeslotendpoint. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class TimeSlotResponse extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer errorCode;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean success;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private TimeSlot timeSlot;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<TimeSlot> timeSlots;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getErrorCode() {
    return errorCode;
  }

  /**
   * @param errorCode errorCode or {@code null} for none
   */
  public TimeSlotResponse setErrorCode(java.lang.Integer errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public TimeSlotResponse setId(java.lang.Long id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getSuccess() {
    return success;
  }

  /**
   * @param success success or {@code null} for none
   */
  public TimeSlotResponse setSuccess(java.lang.Boolean success) {
    this.success = success;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public TimeSlot getTimeSlot() {
    return timeSlot;
  }

  /**
   * @param timeSlot timeSlot or {@code null} for none
   */
  public TimeSlotResponse setTimeSlot(TimeSlot timeSlot) {
    this.timeSlot = timeSlot;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<TimeSlot> getTimeSlots() {
    return timeSlots;
  }

  /**
   * @param timeSlots timeSlots or {@code null} for none
   */
  public TimeSlotResponse setTimeSlots(java.util.List<TimeSlot> timeSlots) {
    this.timeSlots = timeSlots;
    return this;
  }

  @Override
  public TimeSlotResponse set(String fieldName, Object value) {
    return (TimeSlotResponse) super.set(fieldName, value);
  }

  @Override
  public TimeSlotResponse clone() {
    return (TimeSlotResponse) super.clone();
  }

}