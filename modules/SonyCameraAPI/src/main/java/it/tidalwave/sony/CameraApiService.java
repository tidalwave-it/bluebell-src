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
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * Camera Remote API service (category). For example, "camera", "guide" and
 * so on. "Action List URL" is API request target URL of each service.
 * 
 * @author  fritz
 * @version $Id$
 *
 **********************************************************************************************************************/
@AllArgsConstructor @ToString
public class CameraApiService implements Serializable
  {
    private static final long serialVersionUID = 2342353456363463L;

    @Getter @Setter @Nonnull
    private String name;

    @Getter @Setter @Nonnull
    private String actionListUrl;

    /*******************************************************************************************************************
     *
     * Returns the endpoint URL of the category.
     *
     * @return endpoint URL
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getEndpointUrl()
      {
        String url = null;

        if (actionListUrl == null || name == null)
          {
            url = null;
          }
        else if (actionListUrl.endsWith("/"))
          {
            url = actionListUrl + name;
          }
        else
          {
            url = actionListUrl + "/" + name;
          }

        return url;
      }
  }

