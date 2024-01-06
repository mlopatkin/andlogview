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

package name.mlopatkin.andlogview.widgets;

import java.awt.Color;
import java.util.function.Predicate;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * This class appends a simple verification to the text field. When the field loses focus then a predicate is called
 * with the field's new value. If the predicate fails then focus change is abandoned and the background of the text
 * field is changed to reddish.
 */
public class TextFieldVerifier {
    private final JTextComponent text;
    private final Predicate<String> textVerifier;

    private final InputVerifier inputVerifier = new Verifier();
    private final DocumentListener documentListener = new ChangeListener();

    private final Color defaultBackgroundColor;
    private final Color errorBackgroundColor;

    private TextFieldVerifier(JTextComponent text, Predicate<String> textVerifier) {
        this.text = text;
        this.textVerifier = textVerifier;
        this.defaultBackgroundColor = text.getBackground();
        this.errorBackgroundColor = new Color(0xFECBC0);
    }

    private void reset() {
        text.setBackground(defaultBackgroundColor);
    }

    private void showFailure() {
        text.setBackground(errorBackgroundColor);
    }

    public static <T extends JTextComponent> T verifyWith(T component, Predicate<String> textVerifier) {
        TextFieldVerifier verifier = new TextFieldVerifier(component, textVerifier);
        component.setInputVerifier(verifier.inputVerifier);
        component.getDocument().addDocumentListener(verifier.documentListener);
        return component;
    }

    private class Verifier extends InputVerifier {
        @Override
        public boolean verify(JComponent input) {
            assert input == text;
            return textVerifier.test(text.getText());
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldYieldFocus(JComponent input) {
            if (!super.shouldYieldFocus(input)) {
                showFailure();
                return false;
            }
            return true;
        }
    }

    private class ChangeListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            reset();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            reset();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            reset();
        }
    }
}
