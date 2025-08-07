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

package name.mlopatkin.andlogview.widgets;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.BiConsumer;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Opens links in the default browser. Shows an error message if it cannot be done.
 */
public class LinkOpener implements HyperlinkListener {
    private static final Logger log = LoggerFactory.getLogger(LinkOpener.class);

    private final BiConsumer<? super URL, ? super Exception> failureHandler;

    public LinkOpener(BiConsumer<? super URL, ? super Exception> failureHandler) {
        this.failureHandler = failureHandler;
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
            return;
        }

        @Nullable URL target = e.getURL();
        if (target == null) {
            return;
        }
        var protocol = target.getProtocol();
        if (("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))) {
            try {
                openUri(target.toURI());
            } catch (IOException ex) {
                log.error("Can't open the default browser", ex);
                failureHandler.accept(target, ex);
            } catch (URISyntaxException ex) {
                log.error("Can't parse the URL {}", target, ex);
                failureHandler.accept(target, ex);
            }
        }
    }

    /**
     * Opens the provided URI in the default browser if it is available.
     * @param uri the uri to open
     * @throws IOException if opening fails for some reason
     */
    public static void openUri(URI uri) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(uri);
        }
    }
}
