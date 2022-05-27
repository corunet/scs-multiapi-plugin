/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.corunet.api.generator.plugin.asyncapi.parameter;

public class FileSpec {

  private String filePath;

  private OperationParameterObject supplier;

  private OperationParameterObject consumer;

  private OperationParameterObject streamBridge;

  public String getFilePath() {
    return filePath;
  }

  public OperationParameterObject getSupplier() {
    return supplier;
  }

  public OperationParameterObject getConsumer() {
    return consumer;
  }

  public OperationParameterObject getStreamBridge() {
    return streamBridge;
  }
}