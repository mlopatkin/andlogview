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

import static name.mlopatkin.andlogview.widgets.MigConstraints.AC;
import static name.mlopatkin.andlogview.widgets.MigConstraints.CC;
import static name.mlopatkin.andlogview.widgets.MigConstraints.LC;

import name.mlopatkin.andlogview.BuildInfo;
import name.mlopatkin.andlogview.ErrorDialogsHelper;
import name.mlopatkin.andlogview.Main;
import name.mlopatkin.andlogview.widgets.LinkOpener;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.common.collect.ImmutableMap;

import net.miginfocom.swing.MigLayout;

import java.awt.FlowLayout;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

public class AboutUi extends JDialog {
    private static final int ICON_WIDTH = 100;
    private static final int ICON_HEIGHT = 100;
    private static final String ICON_PATH = "name/mlopatkin/andlogview/andlogview.svg";

    public AboutUi(Window owner) {
        super(owner, "About " + Main.APP_NAME, ModalityType.APPLICATION_MODAL);

        var content = getContentPane();
        content.setLayout(new MigLayout(
                LC().insets("dialog").fillX(),
                AC().align("center").gap().grow().align("left"))
        );

        var iconLabel = new JLabel(new FlatSVGIcon(ICON_PATH, ICON_WIDTH, ICON_HEIGHT));

        content.add(iconLabel);

        buildAboutData();

        var okButton = new JButton("Close");
        okButton.addActionListener(e -> dispose());
        content.add(okButton, CC().alignX("right").spanX());
        getRootPane().setDefaultButton(okButton);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    private void buildAboutData() {
        var isBundledRuntime = System.getProperty("jpackage.app-version") != null;
        var javaHome = isBundledRuntime ? "bundled" : System.getProperty("java.home");
        var replacements = ImmutableMap.<String, String>builder()
                .put("APP_NAME", Main.APP_NAME)
                .put("APP_VERSION", BuildInfo.VERSION)
                .put("REVISION", BuildInfo.REVISION)
                .put("BUILT_ON", BuildInfo.BUILT_ON.format(createDateFormatter()))
                .put("JAVA_VENDOR", System.getProperty("java.vendor"))
                .put("JVM_NAME", System.getProperty("java.vm.name"))
                .put("JAVA_VERSION", System.getProperty("java.version"))
                .put("JAVA_HOME", javaHome)
                .put("LAST_CHANGE_YEAR", String.valueOf(BuildInfo.BUILT_ON.getYear()))
                .build();

        var aboutContent = new JEditorPane("text/html", template("""
                <h1>{{ APP_NAME }}&nbsp;{{ APP_VERSION }}</h1>
                <p>
                Revision: {{ REVISION }}<br/>
                Built on: {{ BUILT_ON }}
                </p>
                <p>
                Java Runtime: {{ JAVA_VENDOR }} {{ JVM_NAME }} {{ JAVA_VERSION }}<br/>
                Java Home: {{ JAVA_HOME }}
                </p>
                <p>
                Copyright ⓒ 2011–{{ LAST_CHANGE_YEAR }}
                <a href="https://github.com/mlopatkin/andlogview/blob/HEAD/AUTHORS.md">AndLogView authors</a>.
                </p>
                <p>
                Powered by <a href="andlogview:licenses">open-source software</a>.
                </p>
                """, replacements));
        aboutContent.setEditable(false);

        aboutContent.addHyperlinkListener(new LinkOpener(this::onLinkOpeningFailed));
        aboutContent.addHyperlinkListener(this::onLinkClicked);

        // Without JPanel, the layout breaks, the window becomes much taller than necessary.
        var containment = new JPanel(new FlowLayout(FlowLayout.LEFT));
        containment.add(aboutContent);
        getContentPane().add(containment, CC().growX().wrap("push"));
    }

    private static String template(String template, Map<String, String> replacements) {
        for (var entry : replacements.entrySet()) {
            String key = entry.getKey();
            String replacement = entry.getValue();
            template = template.replace("{{ " + key + " }}", replacement);
        }
        return template;
    }

    private void onLinkClicked(HyperlinkEvent hyperlinkEvent) {
        if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            open(URI.create(hyperlinkEvent.getDescription()));
        }
    }

    private void onLinkOpeningFailed(URL target, Exception failure) {
        if (failure instanceof IOException) {
            ErrorDialogsHelper.showError(this, "Cannot open the url %s in the default browser", target.toString());
        }
    }

    private void open(URI target) {
        if ("andlogview".equalsIgnoreCase(target.getScheme()) && "licenses".equalsIgnoreCase(
                target.getSchemeSpecificPart())) {
            new LicensesUi(this).setVisible(true);
        }
    }

    private static DateTimeFormatter createDateFormatter() {
        // As the application is not localized, we should not use the system locale to format the date.
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.US);
    }
}
