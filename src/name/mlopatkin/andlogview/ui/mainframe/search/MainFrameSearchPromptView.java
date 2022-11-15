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
import name.mlopatkin.andlogview.MainFrame;
import name.mlopatkin.andlogview.ui.mainframe.DialogFactory;
import name.mlopatkin.andlogview.ui.search.AbstractSearchPromptView;
import name.mlopatkin.andlogview.ui.search.SearchScoped;
import name.mlopatkin.andlogview.widgets.DialogResult;

import javax.inject.Inject;
import javax.swing.JTextField;

@SearchScoped
public class MainFrameSearchPromptView extends AbstractSearchPromptView {
    private final DialogFactory dialogFactory;
    private final MainFrame mainFrame;

    @Inject
    public MainFrameSearchPromptView(DialogFactory dialogFactory, MainFrame mainFrame) {
        this.dialogFactory = dialogFactory;
        this.mainFrame = mainFrame;
    }

    protected JTextField getSearchPatternField() {
        return mainFrame.getInstantSearchTextField();
    }

    @Override
    public DialogResult<String> show() {
        getSearchPatternField().setVisible(true);
        var r = super.show();
        mainFrame.showSearchField();
        return r;
    }

    @Override
    public void hide() {
        super.hide();
        getSearchPatternField().setVisible(false);
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
