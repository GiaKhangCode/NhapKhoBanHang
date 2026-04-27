package com.thebangcoffee;

import com.thebangcoffee.ui.InventoryFrame;
import com.thebangcoffee.ui.POSFrame;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Sử dụng giao diện hiện đại FlatLaf
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            // Cấu hình UI toàn cục để giao diện to, rõ, dễ dùng hơn
            UIManager.put("defaultFont", new Font("Arial", Font.PLAIN, 14)); // Font mặc định to hơn
            UIManager.put("Button.arc", 10); // Bo góc nút
            UIManager.put("Component.arc", 10); // Bo góc field, combo box
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Table.rowHeight", 30); // Dòng bảng cao hơn
            UIManager.put("TableHeader.font", new Font("Arial", Font.BOLD, 14)); // Tiêu đề bảng in đậm
            UIManager.put("TableHeader.height", 35);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            POSFrame pos = new POSFrame();
            
            // Thêm một thanh Menu để chuyển đổi giữa các màn hình
            JMenuBar menuBar = new JMenuBar();
            JMenu menuSystem = new JMenu("Hệ thống");
            
            JMenuItem itemInventory = new JMenuItem("Quản lý Nhập Kho");
            itemInventory.addActionListener(e -> {
                new InventoryFrame().setVisible(true);
            });
            
            JMenuItem itemRecipe = new JMenuItem("Thiết lập Công thức");
            itemRecipe.addActionListener(e -> {
                new com.thebangcoffee.ui.RecipeFrame().setVisible(true);
            });

            JMenuItem itemProduct = new JMenuItem("Quản lý Sản phẩm");
            itemProduct.addActionListener(e -> {
                new com.thebangcoffee.ui.ProductFrame().setVisible(true);
            });
            
            menuSystem.add(itemInventory);
            menuSystem.add(itemRecipe);
            menuSystem.add(itemProduct);
            menuBar.add(menuSystem);
            pos.setJMenuBar(menuBar);
            
            pos.setVisible(true);
        });
    }
}
