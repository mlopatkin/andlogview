/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.device;

/**
 * This exception is thrown if the device or ADB host become unavailable when an operation is in progress.
 */
public class DeviceGoneException extends Exception {
    public DeviceGoneException() {
    }

    public DeviceGoneException(String message) {
        super(message);
    }

    public DeviceGoneException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceGoneException(Throwable cause) {
        super(cause);
    }
}
