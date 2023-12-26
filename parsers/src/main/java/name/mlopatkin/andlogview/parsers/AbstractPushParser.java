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

package name.mlopatkin.andlogview.parsers;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;

/**
 * A skeleton implementation of the {@link PushParser} that handles the most basic contract.
 * <p>
 * If the parser stopped itself before closing, it doesn't call {@link ParseEventsHandler#documentEnded()}.
 *
 * @param <H> the type of the event handler
 */
public abstract class AbstractPushParser<H extends PushParser.ParseEventsHandler> extends AbstractBasePushParser
        implements PushParser<H> {
    private final H eventsHandler;

    protected AbstractPushParser(H eventsHandler) {
        this.eventsHandler = eventsHandler;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() {
        if (!shouldStop()) {
            eventsHandler.documentEnded();
        }
    }

    @Override
    public final H getHandler() {
        return eventsHandler;
    }
}
