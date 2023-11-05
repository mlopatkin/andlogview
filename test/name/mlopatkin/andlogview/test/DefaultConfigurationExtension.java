/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.test;

import name.mlopatkin.andlogview.config.Configuration;

import com.google.common.base.Throwables;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

/**
 * Runs a test with the {@link Configuration} instance being reset to the defaults. Restores old configuration object
 * afterward.
 */
public class DefaultConfigurationExtension implements InvocationInterceptor {
    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        Configuration.withDefaultConfiguration(() -> {
            try {
                InvocationInterceptor.super.interceptTestMethod(invocation, invocationContext, extensionContext);
            } catch (Throwable th) {   // OK to catch Throwable here
                Throwables.propagateIfPossible(th, Exception.class);
            }
            return null;
        });
    }
}
