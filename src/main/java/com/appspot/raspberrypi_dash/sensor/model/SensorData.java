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
 * on 2016-01-05 at 17:12:33 UTC 
 * Modify at your own risk.
 */

package com.appspot.raspberrypi_dash.sensor.model;

/**
 * Model definition for SensorData.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the sensor. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class SensorData extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String channel;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private com.google.api.client.util.DateTime dateTime;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double value;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getChannel() {
    return channel;
  }

  /**
   * @param channel channel or {@code null} for none
   */
  public SensorData setChannel(java.lang.String channel) {
    this.channel = channel;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public com.google.api.client.util.DateTime getDateTime() {
    return dateTime;
  }

  /**
   * @param dateTime dateTime or {@code null} for none
   */
  public SensorData setDateTime(com.google.api.client.util.DateTime dateTime) {
    this.dateTime = dateTime;
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
  public SensorData setId(java.lang.Long id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getValue() {
    return value;
  }

  /**
   * @param value value or {@code null} for none
   */
  public SensorData setValue(java.lang.Double value) {
    this.value = value;
    return this;
  }

  @Override
  public SensorData set(String fieldName, Object value) {
    return (SensorData) super.set(fieldName, value);
  }

  @Override
  public SensorData clone() {
    return (SensorData) super.clone();
  }

}