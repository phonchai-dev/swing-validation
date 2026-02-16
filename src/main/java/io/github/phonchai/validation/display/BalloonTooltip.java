package io.github.phonchai.validation.display;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

/**
 * A custom Swing component that renders a web-style balloon tooltip
 * with an arrow (caret) pointing toward the target component.
 *
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Rounded rectangle background with configurable arc radius</li>
 * <li>Triangular arrow on any side (TOP, BOTTOM, LEFT, RIGHT)</li>
 * <li>Drop shadow for depth perception</li>
 * <li>Fade-in / fade-out animation via alpha compositing</li>
 * <li>Anti-aliased text rendering</li>
 * </ul>
 *
 * <p>
 * This component is designed to be added to a {@link JLayeredPane}
 * overlay, making it <em>layout-agnostic</em> — it works with any
 * layout manager the parent form uses.
 * </p>
 *
 * @author phonchai
 * @since 1.0.0
 */
public class BalloonTooltip extends JComponent {

    /**
     * Arrow position relative to the balloon body.
     */
    public enum ArrowPosition {
        /** Arrow points downward (balloon is above the target). */
        TOP,
        /** Arrow points upward (balloon is below the target). */
        BOTTOM,
        /** Arrow points right (balloon is to the left). */
        LEFT,
        /** Arrow points left (balloon is to the right). */
        RIGHT
    }

    // ── Configuration ───────────────────────────────────────────

    private String message;
    private Color bgColor;
    private Color textColor;
    private Color borderColor;
    private int arc;
    private int arrowSize;
    private ArrowPosition arrowPosition;
    private boolean shadowEnabled;
    private Font textFont;
    private float alpha = 1.0f;

    // ── Layout constants ────────────────────────────────────────

    private static final int PADDING_X = 12;
    private static final int PADDING_Y = 6;
    private static final int SHADOW_SIZE = 4;
    private static final int SHADOW_OPACITY = 30;

    // ── Constructor ─────────────────────────────────────────────

    /**
     * Creates a balloon tooltip with the given configuration.
     */
    public BalloonTooltip(String message, Color bgColor, Color textColor,
            Color borderColor, int arc, int arrowSize,
            ArrowPosition arrowPosition, boolean shadowEnabled,
            Font textFont) {
        this.message = message;
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.borderColor = borderColor;
        this.arc = arc;
        this.arrowSize = arrowSize;
        this.arrowPosition = arrowPosition;
        this.shadowEnabled = shadowEnabled;
        this.textFont = textFont != null ? textFont : UIManager.getFont("Label.font");
        if (this.textFont == null) {
            this.textFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        }

        setOpaque(false);
    }

    // ── Public API ──────────────────────────────────────────────

    /**
     * Updates the displayed message and repaints.
     */
    public void setMessage(String message) {
        this.message = message;
        revalidate();
        repaint();
    }

    public String getMessage() {
        return message;
    }

    /**
     * Sets the alpha (opacity) for fade animation.
     *
     * @param alpha value between 0.0 (transparent) and 1.0 (opaque)
     */
    public void setAlpha(float alpha) {
        this.alpha = Math.max(0f, Math.min(1f, alpha));
        repaint();
    }

    public float getAlpha() {
        return alpha;
    }

    public ArrowPosition getArrowPosition() {
        return arrowPosition;
    }

    public void setArrowPosition(ArrowPosition arrowPosition) {
        this.arrowPosition = arrowPosition;
        revalidate();
        repaint();
    }

    // ── Size calculation ────────────────────────────────────────

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(textFont);
        int textWidth = fm.stringWidth(message);
        int textHeight = fm.getHeight();

        int width = textWidth + (PADDING_X * 2);
        int height = textHeight + (PADDING_Y * 2);

        // Add space for arrow
        switch (arrowPosition) {
            case TOP, BOTTOM -> height += arrowSize;
            case LEFT, RIGHT -> width += arrowSize;
        }

        // Add space for shadow
        if (shadowEnabled) {
            width += SHADOW_SIZE * 2;
            height += SHADOW_SIZE * 2;
        }

        return new Dimension(width, height);
    }

    // ── Painting ────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            // Apply alpha for fade animation
            if (alpha < 1.0f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }

            int w = getWidth();
            int h = getHeight();

            // Calculate body rectangle (excluding arrow and shadow)
            int shadowOffset = shadowEnabled ? SHADOW_SIZE : 0;
            Rectangle bodyRect = calculateBodyRect(w, h, shadowOffset);

            // Draw shadow
            if (shadowEnabled) {
                paintShadow(g2, bodyRect);
            }

            // Draw body (rounded rect)
            Shape bodyShape = new RoundRectangle2D.Float(
                    bodyRect.x, bodyRect.y, bodyRect.width, bodyRect.height, arc, arc);
            g2.setColor(bgColor);
            g2.fill(bodyShape);

            // Draw border
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(bodyShape);
            }

            // Draw arrow
            paintArrow(g2, bodyRect);

            // Draw text
            g2.setFont(textFont);
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            int textX = bodyRect.x + PADDING_X;
            int textY = bodyRect.y + PADDING_Y + fm.getAscent();
            g2.drawString(message, textX, textY);
        } finally {
            g2.dispose();
        }
    }

    /**
     * Calculates the body rectangle excluding arrow area and shadow.
     */
    private Rectangle calculateBodyRect(int w, int h, int shadowOffset) {
        int x = shadowOffset;
        int y = shadowOffset;
        int bw = w - shadowOffset * 2;
        int bh = h - shadowOffset * 2;

        switch (arrowPosition) {
            case TOP -> {
                y += arrowSize;
                bh -= arrowSize;
            }
            case BOTTOM -> bh -= arrowSize;
            case LEFT -> {
                x += arrowSize;
                bw -= arrowSize;
            }
            case RIGHT -> bw -= arrowSize;
        }

        return new Rectangle(x, y, bw, bh);
    }

    /**
     * Paints the triangular arrow.
     */
    private void paintArrow(Graphics2D g2, Rectangle bodyRect) {
        GeneralPath arrow = new GeneralPath();
        int cx; // center x of arrow base
        int cy; // center y of arrow base

        switch (arrowPosition) {
            case TOP -> {
                // Arrow points upward, base at top edge of body
                cx = bodyRect.x + bodyRect.width / 2;
                cy = bodyRect.y;
                arrow.moveTo(cx - arrowSize, cy);
                arrow.lineTo(cx, cy - arrowSize);
                arrow.lineTo(cx + arrowSize, cy);
            }
            case BOTTOM -> {
                // Arrow points downward, base at bottom edge of body
                cx = bodyRect.x + bodyRect.width / 2;
                cy = bodyRect.y + bodyRect.height;
                arrow.moveTo(cx - arrowSize, cy);
                arrow.lineTo(cx, cy + arrowSize);
                arrow.lineTo(cx + arrowSize, cy);
            }
            case LEFT -> {
                // Arrow points left, base at left edge of body
                cx = bodyRect.x;
                cy = bodyRect.y + bodyRect.height / 2;
                arrow.moveTo(cx, cy - arrowSize);
                arrow.lineTo(cx - arrowSize, cy);
                arrow.lineTo(cx, cy + arrowSize);
            }
            case RIGHT -> {
                // Arrow points right, base at right edge of body
                cx = bodyRect.x + bodyRect.width;
                cy = bodyRect.y + bodyRect.height / 2;
                arrow.moveTo(cx, cy - arrowSize);
                arrow.lineTo(cx + arrowSize, cy);
                arrow.lineTo(cx, cy + arrowSize);
            }
        }

        arrow.closePath();

        g2.setColor(bgColor);
        g2.fill(arrow);

        // Draw arrow border
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(arrow);

            // Overdraw the base line to seamlessly connect to the body
            g2.setColor(bgColor);
            g2.setStroke(new BasicStroke(2.0f));
            switch (arrowPosition) {
                case TOP -> g2.drawLine(
                        bodyRect.x + bodyRect.width / 2 - arrowSize + 1,
                        bodyRect.y,
                        bodyRect.x + bodyRect.width / 2 + arrowSize - 1,
                        bodyRect.y);
                case BOTTOM -> g2.drawLine(
                        bodyRect.x + bodyRect.width / 2 - arrowSize + 1,
                        bodyRect.y + bodyRect.height,
                        bodyRect.x + bodyRect.width / 2 + arrowSize - 1,
                        bodyRect.y + bodyRect.height);
                case LEFT -> g2.drawLine(
                        bodyRect.x,
                        bodyRect.y + bodyRect.height / 2 - arrowSize + 1,
                        bodyRect.x,
                        bodyRect.y + bodyRect.height / 2 + arrowSize - 1);
                case RIGHT -> g2.drawLine(
                        bodyRect.x + bodyRect.width,
                        bodyRect.y + bodyRect.height / 2 - arrowSize + 1,
                        bodyRect.x + bodyRect.width,
                        bodyRect.y + bodyRect.height / 2 + arrowSize - 1);
            }
        }
    }

    /**
     * Paints a soft drop shadow behind the body.
     */
    private void paintShadow(Graphics2D g2, Rectangle bodyRect) {
        // Create a small buffered image for the shadow with gaussian-like effect
        int padding = SHADOW_SIZE;
        for (int i = 0; i < padding; i++) {
            float ratio = (float) (i + 1) / padding;
            int alphaValue = (int) (SHADOW_OPACITY * (1.0f - ratio));
            g2.setColor(new Color(0, 0, 0, Math.max(0, alphaValue)));
            g2.fill(new RoundRectangle2D.Float(
                    bodyRect.x - i, bodyRect.y - i,
                    bodyRect.width + i * 2, bodyRect.height + i * 2,
                    arc + i, arc + i));
        }
    }
}
