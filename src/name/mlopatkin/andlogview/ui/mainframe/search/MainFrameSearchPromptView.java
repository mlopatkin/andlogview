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

package name.mlopatkin.andlogview.ui.mainframe.search;

import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.ui.search.AbstractSearchPromptView;
import name.mlopatkin.andlogview.ui.search.SearchScoped;
import name.mlopatkin.andlogview.widgets.DialogResult;
import name.mlopatkin.andlogview.widgets.UiHelper;

import java.awt.event.KeyEvent;

import javax.inject.Inject;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

@SearchScoped
public class MainFrameSearchPromptView extends AbstractSearchPromptView {
    private static final KeyStroke KEY_HIDE_AND_START_SEARCH = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private static final String ACTION_HIDE_AND_START_SEARCH = "hide_and_start_search";

    private final DialogFactory dialogFactory;
    private final MainFrameSearchUi mainFrame;

    private boolean keyBindingInitialized;

    @Inject
    public MainFrameSearchPromptView(DialogFactory dialogFactory, MainFrameSearchUi mainFrame) {
        this.dialogFactory = dialogFactory;
        this.mainFrame = mainFrame;
    }

    private void initKeyBindings() {
        keyBindingInitialized = true;
        UiHelper.bindKeyFocused(
                getSearchPatternField(), KEY_HIDE_AND_START_SEARCH, ACTION_HIDE_AND_START_SEARCH,
                e -> commit());
    }

    private JTextField getSearchPatternField() {
        return mainFrame.getSearchField();
    }

    @Override
    public DialogResult<String> show() {
        if (!keyBindingInitialized) {
            initKeyBindings();
        }
        var r = super.show();
        mainFrame.showSearchField();
        return r;
    }

    @Override
    public void hide() {
        super.hide();
        mainFrame.hideSearchField();
    }

    @Override
    public void focus() {
        getSearchPatternField().requestFocusInWindow();
    }

    public void commit() {
        commit(getSearchPatternField().getText());
    }

    @Override
    public void discard() {
        super.discard();
    }

    @Override
    public boolean isShowing() {
        assert getSearchPatternField().isShowing() == super.isShowing();
        return super.isShowing();
    }

    @Override
    public void clearSearchPattern() {
        getSearchPatternField().setText("");
    }

    @Override
    public void showPatternError(String errorMessage) {
        ErrorDialogsHelper.showError(dialogFactory.getOwner(), errorMessage);
    }
}
