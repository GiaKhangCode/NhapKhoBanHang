package com.thebangcoffee.ui;

import com.thebangcoffee.dao.InventoryDAO;
import com.thebangcoffee.model.NguyenLieu;
import javax.swing.*;
import java.awt.*;

public class AddNguyenLieuDialog extends JDialog {
    private JTextField txtMa = new JTextField();
    private JTextField txtTen = new JTextField();
    private JTextField txtDVT = new JTextField();
    private InventoryDAO invDAO = new InventoryDAO();

    public AddNguyenLieuDialog(JFrame parent) {
        super(parent, "Thêm Nguyên Liệu Mới", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4, 2, 5, 5));

        add(new JLabel(" Mã NL:")); add(txtMa);
        add(new JLabel(" Tên NL:")); add(txtTen);
        add(new JLabel(" Đơn vị:")); add(txtDVT);

        JButton btnSave = new JButton("Lưu");
        btnSave.addActionListener(e -> {
            NguyenLieu nl = new NguyenLieu(txtMa.getText(), txtTen.getText(), txtDVT.getText(), 0);
            if (invDAO.addNguyenLieu(nl)) {
                JOptionPane.showMessageDialog(this, "Đã thêm nguyên liệu!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu!");
            }
        });
        add(new JLabel("")); add(btnSave);
    }
}
