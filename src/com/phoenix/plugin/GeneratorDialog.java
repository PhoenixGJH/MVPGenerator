package com.phoenix.plugin;

import com.phoenix.plugin.utils.TextUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GeneratorDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldAuthor;
    private JTextField textFieldClassName;
    private JCheckBox isFragmentCheckBox;
    private JCheckBox isGenerateBaseCheckBox;
    private JTextField textFieldBaseName;

    private DialogCallback callback;

    public GeneratorDialog(DialogCallback callback) {
        this.callback = callback;
        setTitle("MVP Helper");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setLocationRelativeTo(null);

        initView();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initView() {
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
    }

    private void onOK() {
        // add your code here
        if (callback != null) {
            callback.onOk(textFieldAuthor.getText().trim(), textFieldClassName.getText().trim(),
                    isFragmentCheckBox.isSelected(), isGenerateBaseCheckBox.isSelected(),
                    textFieldBaseName.getText().trim());
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    /**
     * 更新Base包名
     */
    public void updateBaseName(String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        textFieldBaseName.setText(name);
    }

    public interface DialogCallback {
        void onOk(String author, String className, boolean isFragment, boolean isGenerateBase, String baseName);
    }
}
