/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://bluebell.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
 * %%
 * Copyright (C) 2013 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * *********************************************************************************************************************
 *
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.sony;

import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@AllArgsConstructor
public enum StatusCode
  {
    OK(0),
    ANY(1),
    TIMEOUT(2),
    ILLEGAL_ARGUMENT(3),
    ILLEGAL_DATA_FORMAT(4),
    ILLEGAL_REQUEST(5),
    ILLEGAL_RESPONSE(6),
    ILLEGAL_STATE(7),
    ILLEGAL_TYPE(8),
    INDEX_OUT_OF_BOUNDS(9),
    NO_SUCH_ELEMENT(10),
    NO_SUCH_FIELD(11),
    NO_SUCH_METHOD(12),
    NULL_POINTER(13),
    UNSUPPORTED_VERSION(14),
    UNSUPPORTED_OPERATION(15),
    ALREADY_POLLING(40402);

    @Getter
    private final int code;

    @Nonnull
    public static StatusCode findStatusCode (final int code)
      {
        for (final StatusCode statusCode : values())
          {
            if (statusCode.getCode() == code)
              {
                return statusCode;
              }
          }

        throw new RuntimeException("Unexpected status code: " + code);
      }
  }
