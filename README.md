# Swing Validation Library

A modern, fluent, and comprehensive validation library for Java Swing, designed to work seamlessly with **FlatLaf**.

![Demo Screenshot](docs/demo_screenshot.png)

## Features 🚀

- **Fluent API:** Chain rules easily (`.required().email().minLength(5)`)
- **16 Internal Rules:** Matches standard web framework validation capabilities.
  - Basic: `required`, `minLength`, `maxLength`, `pattern`, `email`
  - Numeric: `number`, `min`, `max`, `integer`, `between`, `digits`
  - Advanced: `url`, `oneOf`, `matches` (cross-field), `custom`
  - Conditional: `requiredWhen`, `when`
- **6 Error Display Styles:**
  - 🎈 **Balloon Tooltip:** Modern, customizable balloons (Dark, Danger, Warning).
  - 📝 **Inline Label:** Text below the field (Bootstrap style).
  - 🟥 **Outline Only:** Simple FlatLaf outline.
  - 🧱 **Bottom Block:** Solid color block attached to the field.
  - ❌ **Trailing Icon:** Error icon inside the text field (FlatLaf native).
  - 🎨 **Composite:** Combine multiple styles (e.g., Icon + Balloon).
- **Enterprise Grade:**
  - **Dynamic Forms:** Add/remove fields at runtime without memory leaks.
  - **Cross-Field Triggers:** Auto-revalidate dependent fields (e.g., Confirm Password).
  - **Extensible:** SPI architecture for custom component support.

## Installation 📦

### Using JitPack

1. Add the JitPack repository to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. Add the dependency:
```xml
<dependency>
    <groupId>com.github.YourUsername</groupId>
    <artifactId>swing-validation</artifactId>
    <version>Tag</version>
</dependency>
```

*(Replace `YourUsername` with your GitHub username and `Tag` with the release tag e.g., `1.0.0`)*

## Usage 💡

### Basic Validation

```java
FormValidator validator = new FormValidator();

// Simple rule
validator.field(txtUsername).required("Username is required");

// Chained rules
validator.field(txtEmail)
    .required("Email is required")
    .email("Invalid email format");

// Numeric validation
validator.field(txtAge)
    .number("Must be a number")
    .between(18, 60, "Age must be between 18 and 60");
```

### Enterprise Features

**Cross-Field Matching (Confirm Password):**
```java
validator.field(txtPassword)
    .required("Password is required");

// Automatically re-validates when txtPassword changes
validator.field(txtConfirm)
    .required("Confirm password is required")
    .matches(txtPassword, "Passwords do not match");
```

**Dynamic Forms:**
```java
// Remove a field and its rules/listeners
validator.removeField(txtPhone);
```

### Switching Display Styles

```java
// Use a specific style globally
validator.setErrorDisplay(new TrailingIconDisplay());

// Or perform complex composition
validator.setErrorDisplay(new CompositeErrorDisplay(
    new TrailingIconDisplay(),
    BalloonTooltipDisplay.danger()
));
```

## Requirements

- Java 17+ (Recommend 21)
- FlatLaf (Optional, but recommended for best visuals)

## License

MIT License
