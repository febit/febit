/**
 * Copyright 2013-present febit.org (support@febit.org)
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
package org.febit.web.filter;

import java.lang.reflect.InvocationTargetException;
import org.febit.service.ServiceResult;
import org.febit.service.ServiceResultCarrier;
import org.febit.web.ActionRequest;
import org.febit.web.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zqq90
 */
public class ErrorHandlerFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorHandlerFilter.class);

    @Override
    public Object invoke(ActionRequest actionRequest) {
        try {
            return actionRequest.invoke();
        } catch (Exception e) {
            Throwable carry;
            if (e instanceof InvocationTargetException) {
                carry = ((InvocationTargetException) e).getCause();
                if (carry == null) {
                    carry = e;
                }
            } else {
                carry = e;
            }
            if (carry instanceof ServiceResultCarrier) {
                ServiceResult result = ((ServiceResultCarrier) carry).getServiceResult();
                if (result != null) {
                    LOG.warn(actionRequest.actionConfig.path + result);
                    return result;
                }
            }
            LOG.error(actionRequest.actionConfig.path, e);
            return ServiceResult.error(ServiceResult.ERROR_SYS);
        }
    }
}
