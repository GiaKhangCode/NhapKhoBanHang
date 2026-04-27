package com.thebangcoffee.ui;

import com.thebangcoffee.dao.SanPhamDAO;
import com.thebangcoffee.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProductOptionDialog extends JDialog {
    private SanPham sp;
    private BienTheSP selectedBienThe;
    private List<Topping> selectedToppings = new ArrayList<>();
    private boolean confirmed = false;
    
    private ButtonGroup sizeGroup = new ButtonGroup();
    private List<JCheckBox> toppingChecks = new ArrayList<>();
    private List<Topping> allToppings;
    private JSpinner spinQty;

    public ProductOptionDialog(Frame parent, SanPham sp) {
        super(parent, "Tùy chọn món: " + sp.getTenSP(), true);
        this.sp = sp;
        this.allToppings = new SanPhamDAO().getAllTopping();
        
        setSize(400, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initUI();
    }

    private void initUI() {
        JPanel panelContent = new JPanel();
        panelContent.setLayout(new BoxLayout(panelContent, BoxLayout.Y_AXIS));
        panelContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Chọn Size
        panelContent.add(new JLabel("Chọn Size:"));
        for (BienTheSP bt : sp.getDanhSachBienThe()) {
            JRadioButton rb = new JRadioButton(bt.getTenSize() + " (" + String.format("%, d", bt.getGiaBan()) + ")");
            rb.addActionListener(e -> selectedBienThe = bt);
            sizeGroup.add(rb);
            panelContent.add(rb);
            if (selectedBienThe == null) {
                rb.setSelected(true);
                selectedBienThe = bt;
            }
        }
        panelContent.add(Box.createVerticalStrut(10));

        // 2. Chọn Topping
        panelContent.add(new JLabel("Chọn Topping:"));
        List<String> allowed = sp.getAllowedToppings();
        boolean hasTopping = false;
        for (Topping t : allToppings) {
            if (allowed != null && allowed.contains(t.getMaTopping())) {
                JCheckBox cb = new JCheckBox(t.getTenTopping() + " (+" + String.format("%,d", t.getGiaBan()) + ")");
                cb.putClientProperty("topping", t);
                toppingChecks.add(cb);
                panelContent.add(cb);
                hasTopping = true;
            }
        }
        if (!hasTopping) {
            panelContent.add(new JLabel("<i>Món này không có topping đi kèm</i>"));
        }
        panelContent.add(Box.createVerticalStrut(10));

        // 3. Chọn Đá
        panelContent.add(new JLabel("Chọn mức Đá:"));
        JPanel panelIce = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] iceLevels = {"100% Đá", "50% Đá", "Không Đá"};
        ButtonGroup iceGroup = new ButtonGroup();
        for (String level : iceLevels) {
            JRadioButton rb = new JRadioButton(level);
            if (level.equals("100% Đá")) rb.setSelected(true);
            iceGroup.add(rb);
            panelIce.add(rb);
        }
        panelContent.add(panelIce);

        // 4. Chọn Đường
        panelContent.add(new JLabel("Chọn mức Đường:"));
        JPanel panelSugar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] sugarLevels = {"100% Đường", "50% Đường", "Không Đường"};
        ButtonGroup sugarGroup = new ButtonGroup();
        for (String level : sugarLevels) {
            JRadioButton rb = new JRadioButton(level);
            if (level.equals("100% Đường")) rb.setSelected(true);
            sugarGroup.add(rb);
            panelSugar.add(rb);
        }
        panelContent.add(panelSugar);
        panelContent.add(Box.createVerticalStrut(10));

        // 5. Số lượng
        JPanel panelQty = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelQty.add(new JLabel("Số lượng:"));
        spinQty = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        panelQty.add(spinQty);
        panelContent.add(panelQty);

        add(new JScrollPane(panelContent), BorderLayout.CENTER);

        // Nút bấm
        JButton btnAdd = new JButton("Thêm vào Bill");
        btnAdd.addActionListener(e -> {
            confirmed = true;
            for (JCheckBox cb : toppingChecks) {
                if (cb.isSelected()) {
                    selectedToppings.add((Topping) cb.getClientProperty("topping"));
                }
            }
            dispose();
        });
        add(btnAdd, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() { return confirmed; }

    public CTHoaDon getResult() {
        CTHoaDon ct = new CTHoaDon();
        ct.setMaBienThe(selectedBienThe.getMaBienThe());
        ct.setSoLuong((int) spinQty.getValue());
        
        long totalDonGia = selectedBienThe.getGiaBan();
        for (Topping t : selectedToppings) {
            totalDonGia += t.getGiaBan();
        }
        ct.setDonGia(totalDonGia);
        ct.setToppings(selectedToppings);
        return ct;
    }
}
