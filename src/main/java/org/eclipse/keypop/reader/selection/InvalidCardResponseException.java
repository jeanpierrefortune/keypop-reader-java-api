/* **************************************************************************************
 * Copyright (c) 2023 Calypso Networks Association https://calypsonet.org/
 *
 * This file is part of Eclipse Keypop.
 *
 * Eclipse Keypop is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License. A copy of the License is located at
 *
 * http://opensource.org/licenses/MIT
 ************************************************************************************** */
package org.eclipse.keypop.reader.selection;

/**
 * Indicates that a response received from the card during the selection process was invalid.
 *
 * @since 1.0.0
 */
public class InvalidCardResponseException extends RuntimeException {

  /**
   * @param message The message to identify the exception context.
   * @since 1.0.0
   */
  public InvalidCardResponseException(String message) {
    super(message);
  }

  /**
   * @param message The message to identify the exception context.
   * @param cause The cause.
   * @since 1.0.0
   */
  public InvalidCardResponseException(String message, Throwable cause) {
    super(message, cause);
  }
}
