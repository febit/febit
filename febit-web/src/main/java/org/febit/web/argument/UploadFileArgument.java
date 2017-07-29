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
package org.febit.web.argument;

import org.febit.web.ActionRequest;
import org.febit.web.upload.MultipartRequestWrapper;
import org.febit.web.upload.UploadFile;

/**
 *
 * @author zqq90
 */
public class UploadFileArgument implements Argument {

    @Override
    public Object resolve(ActionRequest actionRequest, Class type, String name, int index) {
        if (!(actionRequest.request instanceof MultipartRequestWrapper)) {
            return null;
        }
        final MultipartRequestWrapper multipartRequest = (MultipartRequestWrapper) actionRequest.request;
        if (type.isArray()) {
            return multipartRequest.getFiles(name);
        } else {
            return multipartRequest.getFile(name);
        }
    }

    @Override
    public Class[] matchTypes() {
        return new Class[]{
            UploadFile.class,
            UploadFile[].class
        };
    }
}
