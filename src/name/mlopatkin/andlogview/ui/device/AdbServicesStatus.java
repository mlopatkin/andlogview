/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.device;

import name.mlopatkin.andlogview.utils.events.Observable;

/**
 * Provides the status of the {@link AdbServices}. Note that ADB services may be initialized without any devices
 * connected.
 */
public interface AdbServicesStatus {
    interface Observer {
        /**
         * Called when the status of the services is updated.
         *
         * @param newStatus the new service status
         */
        void onAdbServicesStatusChanged(StatusValue newStatus);
    }

    /**
     * Returns the current status of the AdbServices
     *
     * @return the current status of the services
     */
    StatusValue getStatus();

    /**
     * Provides the way of subscribing to service status change notifications.
     *
     * @return the observable to observe for status updates
     */
    Observable<Observer> asObservable();

    /**
     * Base class for service status. Poor man's sealed class.
     */
    abstract class StatusValue {
        private StatusValue() {}

        public static NotInitialized notInitialized() {
            return NotInitialized.INSTANCE;
        }

        public static Initializing initializing() {
            return Initializing.INSTANCE;
        }

        public static Initialized initialized() {
            return Initialized.INSTANCE;
        }

        public static InitFailed failed(String failureMessage) {
            return new InitFailed(failureMessage);
        }
    }

    /**
     * ADB services were never initialized.
     */
    final class NotInitialized extends StatusValue {
        private static final NotInitialized INSTANCE = new NotInitialized();

        private NotInitialized() {}

        @Override
        public String toString() {
            return "AdbStatus{not initialized}";
        }
    }

    /**
     * ADB services are initializing.
     */
    final class Initializing extends StatusValue {
        private static final Initializing INSTANCE = new Initializing();

        private Initializing() {}

        @Override
        public String toString() {
            return "AdbStatus{initializing}";
        }
    }

    /**
     * ADB services are successfully initialized.
     */
    final class Initialized extends StatusValue {
        private static final Initialized INSTANCE = new Initialized();

        private Initialized() {}

        @Override
        public String toString() {
            return "AdbStatus{initialized}";
        }
    }

    /**
     * ADB services failed to initialize. Failure message can be obtained.
     */
    final class InitFailed extends StatusValue {
        private final String failureMessage;

        private InitFailed(String failureMessage) {
            this.failureMessage = failureMessage;
        }

        /**
         * Returns the failure message.
         *
         * @return the failure message
         */
        public String getFailureMessage() {
            return failureMessage;
        }

        @Override
        public String toString() {
            return String.format("AdbStatus{failed=%s}", failureMessage);
        }
    }
}
