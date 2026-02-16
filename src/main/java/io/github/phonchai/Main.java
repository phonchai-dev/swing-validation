package io.github.phonchai;

import com.formdev.flatlaf.FlatLightLaf;
import io.github.phonchai.validation.FormValidator;
import io.github.phonchai.validation.display.BalloonTooltip;
import io.github.phonchai.validation.display.BalloonTooltipDisplay;
import io.github.phonchai.validation.display.BottomBlockDisplay;
import io.github.phonchai.validation.display.CompositeErrorDisplay;
import io.github.phonchai.validation.display.InlineLabelDisplay;
import io.github.phonchai.validation.display.OutlineErrorDisplay;
import io.github.phonchai.validation.display.TrailingIconDisplay;

import javax.swing.*;
import java.awt.*;

/**
 * Demo application showcasing the Swing Validation library.
 *
 * <p>
 * Demonstrates all three error display styles:
 * </p>
 * <ol>
 * <li>Dark Balloon Tooltip</li>
 * <li>Danger Balloon Tooltip</li>
 * <li>Inline Label</li>
 * </ol>
 */
public class Main {

    private static FormValidator validator;

    public static void main(String[] args) {
        // Install FlatLaf
        FlatLightLaf.setup();
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ComboBox.arc", 8);
        UIManager.put("Button.arc", 8);

        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Swing Validation — Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 650);
        frame.setLocationRelativeTo(null);

        // Main panel with scrollable form
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Title
        JLabel title = new JLabel("Swing Validation Demo");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        // Form Content
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private static JPanel createFormPanel() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // ── Display Style Selector ──────────────────────────────
        JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        stylePanel.setBorder(BorderFactory.createTitledBorder("Error Display Style"));
        stylePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        String[] styles = { "Dark Balloon (Top)", "Danger Balloon (Bottom)", "Inline Label", "Outline Only",
                "Bottom Block (Style A)", "Right Balloon (Style B)", "Trailing Icon", "Icon + Tooltip (Composite)" };
        JComboBox<String> cbStyle = new JComboBox<>(styles);
        cbStyle.setPreferredSize(new Dimension(250, 32));
        stylePanel.add(new JLabel("Style: "));
        stylePanel.add(cbStyle);
        form.add(stylePanel);
        form.add(Box.createVerticalStrut(10));

        // ── Form Fields ─────────────────────────────────────────
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(BorderFactory.createTitledBorder("Registration Form"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Username
        JTextField txtUsername = createField(fieldsPanel, gbc, 0, "Username *");

        // Row 1: Email
        JTextField txtEmail = createField(fieldsPanel, gbc, 1, "Email *");

        // Row 2: Phone
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Phone"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel phonePanel = new JPanel(new BorderLayout(5, 0)); // Panel for phone field and button
        JTextField txtPhone = new JTextField();
        txtPhone.setPreferredSize(new Dimension(300, 32));
        phonePanel.add(txtPhone, BorderLayout.CENTER);
        JButton btnRemovePhone = new JButton("Remove");
        btnRemovePhone.putClientProperty("JButton.buttonType", "roundRect");
        phonePanel.add(btnRemovePhone, BorderLayout.EAST);
        fieldsPanel.add(phonePanel, gbc);

        // Row 3: Password
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Password *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPasswordField txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(300, 32));
        fieldsPanel.add(txtPassword, gbc);

        // Row 4: Confirm Password
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Confirm Password *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPasswordField txtConfirm = new JPasswordField();
        txtConfirm.setPreferredSize(new Dimension(300, 32));
        fieldsPanel.add(txtConfirm, gbc);

        // Row 5: Age
        JTextField txtAge = createField(fieldsPanel, gbc, 5, "Age");

        // Row 6: Country
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Country *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JComboBox<String> cbCountry = new JComboBox<>();
        cbCountry.addItem(null); // Placeholder
        cbCountry.addItem("Thailand");
        cbCountry.addItem("Japan");
        cbCountry.addItem("United States");
        cbCountry.addItem("Germany");
        cbCountry.setSelectedIndex(-1);
        cbCountry.setPreferredSize(new Dimension(300, 32));
        fieldsPanel.add(cbCountry, gbc);

        // Row 7: Agree checkbox + conditional field
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JCheckBox chkEmployed = new JCheckBox("Currently Employed");
        fieldsPanel.add(chkEmployed, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField txtCompany = new JTextField();
        txtCompany.setPreferredSize(new Dimension(300, 32));
        txtCompany.putClientProperty("JTextField.placeholderText", "Company name (required if employed)");
        fieldsPanel.add(txtCompany, gbc);

        fieldsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldsPanel.getPreferredSize().height + 20));
        form.add(fieldsPanel);
        form.add(Box.createVerticalStrut(15));

        // ── Buttons ─────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton btnValidate = new JButton("Validate All");
        btnValidate.setPreferredSize(new Dimension(150, 36));
        btnValidate.setBackground(new Color(0x43, 0x85, 0xF4));
        btnValidate.setForeground(Color.WHITE);

        JButton btnClear = new JButton("Clear Validation");
        btnClear.setPreferredSize(new Dimension(150, 36));

        JButton btnSubmit = new JButton("Submit");
        btnSubmit.setPreferredSize(new Dimension(150, 36));
        btnSubmit.setBackground(new Color(0x28, 0xA7, 0x45));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setEnabled(false);

        btnPanel.add(btnValidate);
        btnPanel.add(btnClear);
        btnPanel.add(btnSubmit);
        form.add(btnPanel);
        form.add(Box.createVerticalStrut(15));

        // ── Status Label ────────────────────────────────────────
        JLabel lblStatus = new JLabel("Status: Waiting for validation...");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        form.add(lblStatus);
        form.add(Box.createVerticalGlue());

        // ════════════════════════════════════════════════════════
        // SET UP VALIDATION
        // ════════════════════════════════════════════════════════

        validator = new FormValidator(); // Default: dark balloon

        // Username: required, min 3 chars
        validator.field(txtUsername)
                .required("Username is required")
                .minLength(3, "Username must be at least 3 characters");

        // Email: required, valid format
        validator.field(txtEmail)
                .required("Email is required")
                .email("Please enter a valid email address");

        // Phone: optional, but must match pattern if provided
        validator.field(txtPhone).pattern("^\\d{10}$", "Must be 10 digits");

        // Enterprise Feature: Cross-field validation (auto-trigger)
        validator.field(txtPassword).required("Password is required").minLength(6, "At least 6 chars");
        validator.field(txtConfirm)
                .required("Confirm Password is required")
                .matches(txtPassword, "Passwords do not match");

        // Age: optional, number, range 1-150
        validator.field(txtAge)
                .number("Age must be a number")
                .min(1, "Age must be at least 1")
                .max(150, "Age must be at most 150");

        // Country: required
        validator.field(cbCountry)
                .required("Please select a country");

        // Company: required only when employed
        validator.field(txtCompany)
                .requiredWhen(chkEmployed::isSelected, "Company name is required when employed");

        // Trigger re-validation when checkbox changes
        chkEmployed.addActionListener(e -> {
            // This will re-run the conditional validation
            if (validator.isRealTimeEnabled()) {
                validator.validate();
            }
        });

        // Enterprise Feature: Dynamic Form (Remove Field)
        btnRemovePhone.addActionListener(e -> {
            fieldsPanel.remove(phonePanel); // Remove the panel containing phone field and button
            validator.removeField(txtPhone); // Dynamic Clean-up
            fieldsPanel.revalidate();
            fieldsPanel.repaint();
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(btnRemovePhone),
                    "Phone field removed and validation rules cleared!");
        });

        // Listen for validation state
        validator.onValidationChanged(allValid -> {
            btnSubmit.setEnabled(allValid);
            lblStatus.setText(allValid
                    ? "✅ All fields are valid!"
                    : "❌ Please fix the errors above");
            lblStatus.setForeground(allValid
                    ? new Color(0x28, 0xA7, 0x45)
                    : new Color(0xDC, 0x35, 0x45));
        });

        // ── Button Actions ──────────────────────────────────────
        btnValidate.addActionListener(e -> validator.validate());

        btnClear.addActionListener(e -> {
            validator.clearValidation();
            lblStatus.setText("Status: Validation cleared");
            lblStatus.setForeground(UIManager.getColor("Label.foreground"));
        });

        btnSubmit.addActionListener(e -> {
            if (validator.validate()) {
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(btnSubmit),
                        "Form submitted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // ── Style Switcher ──────────────────────────────────────
        cbStyle.addActionListener(e -> {
            validator.clearValidation();
            switch (cbStyle.getSelectedIndex()) {
                case 0 -> validator.setErrorDisplay(BalloonTooltipDisplay.dark());
                case 1 -> validator.setErrorDisplay(BalloonTooltipDisplay.danger());
                case 2 -> validator.setErrorDisplay(new InlineLabelDisplay());
                case 3 -> validator.setErrorDisplay(new OutlineErrorDisplay());
                case 4 -> validator.setErrorDisplay(new BottomBlockDisplay());
                case 5 -> validator.setErrorDisplay(BalloonTooltipDisplay.builder()
                        .bgColor(new Color(0xDC, 0x35, 0x45)) // Danger Red
                        .textColor(Color.WHITE)
                        .preferredPosition(BalloonTooltip.ArrowPosition.LEFT) // Arrow left = Balloon Right
                        .build());
                case 6 -> validator.setErrorDisplay(new TrailingIconDisplay());
                case 7 -> validator.setErrorDisplay(new CompositeErrorDisplay(
                        new TrailingIconDisplay(),
                        BalloonTooltipDisplay.danger()));
            }
            lblStatus.setText("Style changed to: " + cbStyle.getSelectedItem());
            lblStatus.setForeground(UIManager.getColor("Label.foreground"));
        });

        return form;
    }

    private static JTextField createField(JPanel panel, GridBagConstraints gbc,
            int row, String label) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(300, 32));
        panel.add(field, gbc);
        return field;
    }
}