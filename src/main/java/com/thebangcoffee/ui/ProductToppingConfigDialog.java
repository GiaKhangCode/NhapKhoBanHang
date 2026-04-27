package com.thebangcoffee.ui;

import com.thebangcoffee.dao.SanPhamDAO;
import com.thebangcoffee.model.SanPham;
import com.thebangcoffee.model.Topping;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProductToppingConfigDialog extends JDialog {
    private SanPham sp;
    private SanPhamDAO spDAO = new SanPhamDAO();
    private List<JCheckBox> toppingChecks = new ArrayList<>();
    private boolean saved = false;

    public ProductToppingConfigDialog(JFrame parent, SanPham sp) {
        super(parent, "Thiết lập Topping cho món: " + sp.getTenSP(), true);
        this.sp = sp;
        setSize(400, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initUI();
    }

    private void initUI() {
        JPanel pnlList = new JPanel();
        pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));

        List<Topping> allToppings = spDAO.getAllTopping();
        List<String> allowed = sp.getAllowedToppings();

        for (Topping t : allToppings) {
            JCheckBox cb = new JCheckBox(t.getTenTopping() + " (+" + String.format("%,d", t.getGiaBan()) + ")");
            cb.putClientProperty("toppingId", t.getMaTopping());
            if (allowed != null && allowed.contains(t.getMaTopping())) {
                cb.setSelected(true);
            }
            toppingChecks.add(cb);
            pnlList.add(cb);
        }

        JScrollPane scroll = new JScrollPane(pnlList);
        scroll.setBorder(BorderFactory.createTitledBorder("Chọn các Topping được phép"));
        add(scroll, BorderLayout.CENTER);

        JButton btnSave = new JButton("LƯU THIẾT LẬP");
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        btnSave.addActionListener(e -> saveConfig());
        add(btnSave, BorderLayout.SOUTH);
    }

    private void saveConfig() {
        List<String> selectedIds = new ArrayList<>();
        for (JCheckBox cb : toppingChecks) {
            if (cb.isSelected()) {
                selectedIds.add(cb.getClientProperty("toppingId").toString());
            }
        }

        if (spDAO.saveSPToppings(sp.getMaSP(), selectedIds)) {
            JOptionPane.showMessageDialog(this, "Đã lưu cấu hình Topping!");
            sp.setAllowedToppings(selectedIds); // Update local object
            saved = true;
            com.thebangcoffee.event.EventManager.getInstance().fireDataUpdated();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
