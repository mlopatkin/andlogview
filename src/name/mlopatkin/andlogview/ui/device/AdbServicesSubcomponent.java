/*
 * Copyright 2022 Mikhail Lopatkin
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

import name.mlopatkin.andlogview.device.AdbDeviceList;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Module;
import dagger.Subcomponent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Scope;

/**
 * A special scope that includes services that require running ADB connection to work. Use {@link AdbServicesBridge} to
 * access services in this scope from outside.
 */
@Subcomponent
@AdbServicesSubcomponent.AdbServicesScoped
public interface AdbServicesSubcomponent extends AdbServices {
    @Scope
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface AdbServicesScoped {}

    @Subcomponent.Factory
    interface Factory {
        AdbServicesSubcomponent build(@BindsInstance AdbDeviceList server);
    }

    @Module(subcomponents = AdbServicesSubcomponent.class)
    abstract class AdbMainModule {
        @Binds
        abstract AdbServicesStatus getServicesStatus(AdbServicesBridge bridge);
    }
}
