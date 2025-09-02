
package Micow.ProjectC.Micow_Cashier;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class UIUtils {
    public static final String LOGO_PATH = "cold-brew-logo-iced-coffee-brewed-coffee-coffee.jpg";

    public static ImageIcon loadImageIcon(String path, int width, int height) {
        try {
            URL imgURL = UIUtils.class.getResource(path);
            if (imgURL != null) {
                Image img = new ImageIcon(imgURL).getImage();
                Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            } else {
                System.err.println("Image resource not found: " + path);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error loading image " + path + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}