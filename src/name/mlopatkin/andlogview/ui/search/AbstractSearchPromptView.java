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

package name.mlopatkin.andlogview.ui.search;

import name.mlopatkin.andlogview.widgets.DialogResult;
import name.mlopatkin.andlogview.widgets.DialogResult.DialogSubject;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;

import org.jspecify.annotations.Nullable;

/**
 * Base implementation of the {@link SearchPresenter.SearchPromptView}.
 */
public abstract class AbstractSearchPromptView implements SearchPresenter.SearchPromptView {
    private @Nullable DialogSubject<String> resultSubject;

    @Override
    @OverridingMethodsMustInvokeSuper
    public DialogResult<String> show() {
        Preconditions.checkState(resultSubject == null, "The dialog is already showing");
        resultSubject = new DialogSubject<>();
        return resultSubject.asResult();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void hide() {
        resultSubject = null;
    }

    @Override
    public boolean isShowing() {
        return resultSubject != null;
    }

    protected void commit(String result) {
        assert resultSubject != null;
        resultSubject.commit(result);
    }

    protected void discard() {
        assert resultSubject != null;
        resultSubject.discard();
    }
}
