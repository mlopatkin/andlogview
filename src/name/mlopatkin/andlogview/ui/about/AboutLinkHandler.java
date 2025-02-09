/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.about;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

class AboutLinkHandler implements HyperlinkListener {
    private static final String SCHEME = "andlogview";

    @FunctionalInterface
    public interface LinkOpenHandler {
        void openLink(String authority, String path);
    }

    private static final Logger log = LoggerFactory.getLogger(AboutLinkHandler.class);

    private final LinkOpenHandler linkHandler;

    public AboutLinkHandler(LinkOpenHandler linkHandler) {
        this.linkHandler = linkHandler;
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
            return;
        }
        try {
            var uri = new URI(e.getDescription());
            var scheme = uri.getScheme();
            if (!SCHEME.equals(scheme)) {
                return;
            }

            var authority = Objects.requireNonNull(uri.getAuthority());
            var path = Objects.requireNonNull(uri.getPath());

            linkHandler.openLink(authority, path);
        } catch (URISyntaxException ex) {
            log.info("Invalid URI: {}", ex.getInput(), ex);
        }
    }
}
