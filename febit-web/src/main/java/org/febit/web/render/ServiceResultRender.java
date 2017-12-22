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
package org.febit.web.render;

import org.febit.service.ServiceResult;
import org.febit.web.ActionRequest;
import org.febit.web.HttpStatus;
import org.febit.web.util.MatchTypes;
import org.febit.web.util.RenderUtil;

/**
 *
 * @author zqq90
 */
public class ServiceResultRender implements Render<ServiceResult>, MatchTypes {

    @Override
    public Object render(ActionRequest actionRequest, ServiceResult result) throws Exception {
        actionRequest.response.setStatus(resolveStatusCode(result));
        return RenderUtil.renderAsJson(actionRequest, result);
    }

    protected int resolveStatusCode(ServiceResult result) {
        switch (result.code) {
            case ServiceResult.OK:
                return HttpStatus.OK;
            case ServiceResult.REDIRECT:
                return HttpStatus.TEMPORARY_REDIRECT;
            case ServiceResult.ERROR_PARAM:
            case ServiceResult.ERROR_PARAM_FORMAT:
            case ServiceResult.ERROR_PARAM_REQUIRED:
                return HttpStatus.UNPROCESSABLE_ENTITY;
            case ServiceResult.ERROR_UPLOAD_TYPE:
                return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            case ServiceResult.ERROR_UPLOAD_TOOBIG:
                return HttpStatus.REQUEST_ENTITY_TOO_LARGE;
            case ServiceResult.ERROR_MODIFY_NOTFOUND:
            case ServiceResult.ERROR_DEL_NOTFOUND:
            case ServiceResult.ERROR_QUERY_NOTFOUND:
                return HttpStatus.NOT_FOUND;
            case ServiceResult.ERROR_ADD:
            case ServiceResult.ERROR_DEL:
            case ServiceResult.ERROR_QUERY:
            case ServiceResult.ERROR_RIGHT:
            case ServiceResult.ERROR_XSRF:
            case ServiceResult.ERROR_VERCODE:
            case ServiceResult.ERROR_NOT_LOGIN:
            case ServiceResult.ERROR_MODIFY:
            case ServiceResult.ERROR_MODIFY_UNABLE:
            case ServiceResult.ERROR_DEL_UNABLE:
            case ServiceResult.ERROR_UPLOAD:
                return HttpStatus.BAD_REQUEST;
            case ServiceResult.ERROR_UPLOAD_CANTWRITE:
            case ServiceResult.ERROR_SYS:
                return HttpStatus.INTERNAL_SERVER_ERROR;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Override
    public Class[] matchTypes() {
        return new Class[]{
            ServiceResult.class
        };
    }
}
