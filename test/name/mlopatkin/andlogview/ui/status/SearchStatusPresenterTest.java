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

package name.mlopatkin.andlogview.ui.status;

import name.mlopatkin.andlogview.utils.MockUiThreadScheduler;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class SearchStatusPresenterTest {
    final MockView mockView = new MockView();
    final MockUiThreadScheduler mockScheduler = new MockUiThreadScheduler();

    final SearchStatusPresenter presenter = new SearchStatusPresenter(mockView, mockScheduler);

    @Test
    void notFoundMessageIsShown() {
        presenter.showNotFoundMessage();

        Assertions.assertTrue(mockView.getDisplayMessage().isPresent(), "Message should be shown");
    }

    @Test
    void notFoundMessageIsShownForATime() {
        presenter.showNotFoundMessage();
        mockScheduler.advance(SearchStatusPresenter.MESSAGE_SHOW_TIMEOUT_MS - 1);

        Assertions.assertTrue(mockView.getDisplayMessage().isPresent(), "Message should be shown");
    }

    @Test
    void notFoundMessageIsHiddenAfterTimeout() {
        presenter.showNotFoundMessage();
        mockScheduler.advance(SearchStatusPresenter.MESSAGE_SHOW_TIMEOUT_MS);

        Assertions.assertFalse(mockView.getDisplayMessage().isPresent(), "Message should not be shown");
    }

    @Test
    void resetHidesTheMessageImmediately() {
        presenter.showNotFoundMessage();
        presenter.reset();

        Assertions.assertFalse(mockView.getDisplayMessage().isPresent(), "Message should not be shown");
    }

    @Test
    void showingNotFoundMessageResetsHideTimer() {
        presenter.showNotFoundMessage();
        mockScheduler.advance(SearchStatusPresenter.MESSAGE_SHOW_TIMEOUT_MS - 1);
        presenter.showNotFoundMessage();
        mockScheduler.advance(SearchStatusPresenter.MESSAGE_SHOW_TIMEOUT_MS - 1);

        Assertions.assertTrue(mockView.getDisplayMessage().isPresent(), "Message should be shown");
    }

    @Test
    void showingNotFoundMessageResetsHideTimerButIsHiddenInTheEnd() {
        presenter.showNotFoundMessage();
        mockScheduler.advance(SearchStatusPresenter.MESSAGE_SHOW_TIMEOUT_MS - 1);
        presenter.showNotFoundMessage();
        mockScheduler.advance(SearchStatusPresenter.MESSAGE_SHOW_TIMEOUT_MS);

        Assertions.assertFalse(mockView.getDisplayMessage().isPresent(), "Message should not be shown");
    }

    private static class MockView implements SearchStatusPresenter.View {
        @Nullable
        private String currentMessage;

        public Optional<String> getDisplayMessage() {
            return Optional.ofNullable(currentMessage);
        }

        @Override
        public void showSearchMessage(String message) {
            currentMessage = message;
        }

        @Override
        public void hideSearchMessage() {
            currentMessage = null;
        }
    }
}
