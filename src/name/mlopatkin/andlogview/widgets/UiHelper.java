/*
 * Copyright 2011 Mikhail Lopatkin
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

import com.google.common.html.HtmlEscapers;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class UiHelper {
    @SuppressWarnings("deprecation")
    private static final int SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    private UiHelper() {}

    public static void addPopupMenu(final JComponent component, final JPopupMenu menu) {
        addPopupMenu(component, menu::show);
    }

    @FunctionalInterface
    public interface PopupMenuDelegate<T extends JComponent> {
        void createAndShowMenu(T component, int x, int y);
    }

    public static <T extends JComponent> void addPopupMenu(T component, PopupMenuDelegate<T> delegate) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                delegate.createAndShowMenu(component, e.getX(), e.getY());
            }
        });
    }

    public static String covertToHtml(String value) {
        String escaped = HtmlEscapers.htmlEscaper().escape(value);
        String result = escaped.replace("\n", "<br>");
        return result;
    }

    public static String convertToSafe(String value) {
        String result = value.replace("\n", " ");
        return result;
    }

    /**
     * Binds a key combination to the action listener. The combination is handled when {@code component} is focused.
     * @param component the component which must be focused wto enable the combination
     * @param key the key combination as expected by {@link KeyStroke#getKeyStroke(String)}
     * @param actionKey the arbitrary action name
     * @param action the action to perform when key combination is pressed
     */
    public static void bindKeyFocused(JComponent component, String key, String actionKey, ActionListener action) {
        bindKeyFocused(component, KeyStroke.getKeyStroke(key), actionKey, action);
    }

    /**
     * Binds a key combination to the action listener. The combination is handled when {@code component} is focused.
     * @param component the component which must be focused wto enable the combination
     * @param key the fully qualified {@link KeyStroke}
     * @param actionKey the arbitrary action name
     * @param action the action to perform when key combination is pressed
     */
    public static void bindKeyFocused(JComponent component, KeyStroke key, String actionKey, ActionListener action) {
        component.getInputMap().put(key, actionKey);
        component.getActionMap().put(actionKey, wrapActionListener(action));
    }

    /**
     * Binds a key combination to the action listener. The combination is handled only when the window is focused.
     * @param window the window which focus enables the action
     * @param key the key combination as expected by {@link KeyStroke#getKeyStroke(String)}
     * @param actionKey the arbitrary action name
     * @param action the action to perform when key combination is pressed
     */
    public static void bindKeyGlobal(RootPaneContainer window, String key, String actionKey, ActionListener action) {
        bindKeyGlobal(window, KeyStroke.getKeyStroke(key), actionKey, action);
    }

    /**
     * Binds a key combination to the action listener. The combination is handled only when the window is focused. This
     * method doesn't handle key modifiers (shift, alt, etc).
     *
     * @param window the window which focus enables the action
     * @param keyCode the key code from {@link KeyEvent}
     * @param actionKey the arbitrary action name
     * @param action the action to perform when key combination is pressed
     */
    public static void bindKeyGlobal(RootPaneContainer window, int keyCode, String actionKey, ActionListener action) {
        bindKeyGlobal(window, KeyStroke.getKeyStroke(keyCode, 0), actionKey, action);
    }

    /**
     * Binds a key combination to the action listener. The combination is handled only when the window is focused. This
     * method allows you to use your own {@link KeyStroke} with needed key code and modifiers.
     *
     * @param window the window which focus enables the action
     * @param key the fully qualified {@link KeyStroke}
     * @param actionKey the arbitrary action name
     * @param action the action to perform when key combination is pressed
     */
    public static void bindKeyGlobal(RootPaneContainer window, KeyStroke key, String actionKey,
            ActionListener action) {
        JComponent component = window.getRootPane();
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, actionKey);
        component.getActionMap().put(actionKey, wrapActionListener(action));
    }

    /**
     * Creates a {@link KeyStroke} from key code with platform-dependent modifier.
     * Platform modifier can be Command or Control.
     *
     * @param keyCode the keycode to create {@link KeyStroke} with
     * @return newly created keystroke
     */
    public static KeyStroke createPlatformKeystroke(int keyCode) {
        return createPlatformKeystroke(keyCode, 0);
    }

    /**
     * Creates a {@link KeyStroke} from key code and modifiers combined with platform-dependent modifier.
     * Platform modifier can be Command or Control.
     *
     * @param keyCode the keycode to create {@link KeyStroke} with
     * @param extraModifiers the modifiers to add to platform modifier
     * @return newly created keystroke
     */
    public static KeyStroke createPlatformKeystroke(int keyCode, int extraModifiers) {
        return KeyStroke.getKeyStroke(keyCode, SHORTCUT_KEY_MASK | extraModifiers);
    }

    private static Action wrapActionListener(ActionListener actionListener) {
        if (actionListener instanceof Action) {
            return (Action) actionListener;
        }
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionListener.actionPerformed(e);
            }
        };
    }

    public interface DoubleClickListener {
        void mouseClicked(MouseEvent e);
    }

    private static final int DOUBLE_CLICK_COUNT = 2;

    public static void addDoubleClickListener(JComponent component, final DoubleClickListener listener) {
        assert listener != null;
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == DOUBLE_CLICK_COUNT && e.getButton() == MouseEvent.BUTTON1) {
                    listener.mouseClicked(e);
                }
            }
        });
    }

    public static void addDoubleClickAction(JComponent component, final Action action) {
        component.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("deprecation")
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == DOUBLE_CLICK_COUNT && e.getButton() == MouseEvent.BUTTON1) {
                    action.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED,
                            (String) action.getValue(Action.ACTION_COMMAND_KEY), e.getWhen(), e.getModifiers()));
                }
            }
        });
    }

    /**
     * Creates a wrapper around an existing action of the component to be used
     * in menus.
     *
     * @param c base component
     * @param actionKey key in the component's ActionMap
     * @param caption caption of the action wrapper
     * @param acceleratorKey accelerator key of the action wrapper
     * @return action that translates its
     *         {@link Action#actionPerformed(ActionEvent)} to the underlaying
     *         existing action.
     */
    public static Action createActionWrapper(
            final JComponent c, final String actionKey, String caption, final String acceleratorKey) {
        final Action baseAction = c.getActionMap().get(actionKey);
        Action result = new AbstractAction(caption) {
            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(acceleratorKey));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                ActionEvent newEvent = new ActionEvent(c, e.getID(), actionKey, e.getWhen(), e.getModifiers());
                baseAction.actionPerformed(newEvent);
            }

            @Override
            public void setEnabled(boolean newValue) {
                super.setEnabled(newValue);
                baseAction.setEnabled(newValue);
            }
        };
        return result;
    }

    public static boolean isTextFit(JComponent component, int width, String text) {
        FontMetrics m = component.getFontMetrics(component.getFont());
        int textWidth = m.stringWidth(text);
        return textWidth <= width;
    }

    private static final int DEFAULT_PADDING_PX = 3;

    public static boolean isTextFit(JComponent renderer, JTable table, int row, int column, String text) {
        return isTextFit(renderer, table, row, column, text, DEFAULT_PADDING_PX);
    }

    public static boolean isTextFit(JComponent renderer, JTable table, int row, int column, String text, int padding) {
        return isTextFit(renderer, table.getCellRect(row, column, false).width - padding, text);
    }

    public static final Border NO_BORDER = new EmptyBorder(0, 0, 0, 0);

    public static void setWidths(JComponent component, int width) {
        Dimension dim = component.getPreferredSize();
        dim.width = width;
        component.setPreferredSize(dim);
        component.setMaximumSize(dim);
        component.setMinimumSize(dim);
    }
}
