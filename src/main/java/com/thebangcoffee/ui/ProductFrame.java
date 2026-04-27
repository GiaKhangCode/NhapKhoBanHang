package com.thebangcoffee.ui;

import com.thebangcoffee.dao.InventoryDAO;
import com.thebangcoffee.dao.SanPhamDAO;
import com.thebangcoffee.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.UUID;
import com.thebangcoffee.event.EventManager;

public class ProductFrame extends JFrame {
    private SanPhamDAO spDAO = new SanPhamDAO();
    private InventoryDAO invDAO = new InventoryDAO();
    
    private JTable tableSP, tableSize, tableTopping;
    private DefaultTableModel modelSP, modelSize, modelTopping;
    private JTextField txtTenSP, txtMaSP;
    
    public ProductFrame() {
        setTitle("Quản lý Sản phẩm & Tùy chọn - The Bang Coffee");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Quản lý Món & Size", initProductTab());
        tabbedPane.addTab("Quản lý Topping", initToppingTab());
        
        add(tabbedPane);
        loadAllData();
    }
    
    private JPanel initProductTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Bên trái: Danh sách sản phẩm
        JPanel panelLeft = new JPanel(new BorderLayout());
        panelLeft.setBorder(BorderFactory.createTitledBorder("Danh sách món"));
        panelLeft.setPreferredSize(new Dimension(400, 0));
        
        modelSP = new DefaultTableModel(new String[]{"Mã SP", "Tên Sản Phẩm"}, 0);
        tableSP = new JTable(modelSP);
        tableSP.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSizesOfSelectedSP();
        });
        panelLeft.add(new JScrollPane(tableSP), BorderLayout.CENTER);
        
        JPanel panelLeftBottom = new JPanel(new GridLayout(4, 2, 5, 5));
        panelLeftBottom.add(new JLabel("Mã SP:"));
        txtMaSP = new JTextField();
        panelLeftBottom.add(txtMaSP);
        panelLeftBottom.add(new JLabel("Tên SP:"));
        txtTenSP = new JTextField();
        panelLeftBottom.add(txtTenSP);
        
        JButton btnAddSP = new JButton("Thêm món");
        btnAddSP.addActionListener(e -> addSanPham());
        panelLeftBottom.add(btnAddSP);
        
        JButton btnDelSP = new JButton("Xóa món");
        btnDelSP.addActionListener(e -> deleteSanPham());
        panelLeftBottom.add(btnDelSP);
        
        JButton btnConfigTopping = new JButton("Thiết lập Topping");
        btnConfigTopping.addActionListener(e -> configTopping());
        panelLeftBottom.add(btnConfigTopping);
        panelLeftBottom.add(new JLabel("")); // Spacer
        
        panelLeft.add(panelLeftBottom, BorderLayout.SOUTH);
        
        // Bên phải: Danh sách Size của món đang chọn
        JPanel panelRight = new JPanel(new BorderLayout());
        panelRight.setBorder(BorderFactory.createTitledBorder("Các Size & Giá bán"));
        
        modelSize = new DefaultTableModel(new String[]{"Mã Size", "Tên Size", "Giá bán"}, 0);
        tableSize = new JTable(modelSize);
        panelRight.add(new JScrollPane(tableSize), BorderLayout.CENTER);
        
        JPanel panelRightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAddSize = new JButton("+ Thêm");
        btnAddSize.addActionListener(e -> addSize());
        JButton btnEditSize = new JButton("Sửa");
        btnEditSize.addActionListener(e -> editSize());
        JButton btnDelSize = new JButton("Xóa");
        btnDelSize.addActionListener(e -> deleteSize());
        
        panelRightBottom.add(btnAddSize);
        panelRightBottom.add(btnEditSize);
        panelRightBottom.add(btnDelSize);
        panelRight.add(panelRightBottom, BorderLayout.SOUTH);
        
        panel.add(panelLeft, BorderLayout.WEST);
        panel.add(panelRight, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel initToppingTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        modelTopping = new DefaultTableModel(new String[]{"Mã Topping", "Tên Topping", "Giá bán", "NL Liên kết"}, 0);
        tableTopping = new JTable(modelTopping);
        panel.add(new JScrollPane(tableTopping), BorderLayout.CENTER);
        
        JPanel panelForm = new JPanel(new GridLayout(0, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createTitledBorder("Thêm/Sửa Topping"));
        
        JTextField tMa = new JTextField();
        JTextField tTen = new JTextField();
        JTextField tGia = new JTextField();
        JComboBox<NguyenLieu> comboNL = new JComboBox<>();
        List<NguyenLieu> nlList = invDAO.getAllNguyenLieu();
        for (NguyenLieu nl : nlList) comboNL.addItem(nl);
        
        tableTopping.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableTopping.getSelectedRow();
                if (row != -1) {
                    tMa.setText(modelTopping.getValueAt(row, 0).toString());
                    tTen.setText(modelTopping.getValueAt(row, 1).toString());
                    tGia.setText(modelTopping.getValueAt(row, 2).toString());
                    String maNL = modelTopping.getValueAt(row, 3) != null ? modelTopping.getValueAt(row, 3).toString() : "";
                    for (int i = 0; i < comboNL.getItemCount(); i++) {
                        if (comboNL.getItemAt(i).getMaNL().equals(maNL)) {
                            comboNL.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });
        
        panelForm.add(new JLabel("Mã Topping:")); panelForm.add(tMa);
        panelForm.add(new JLabel("Tên Topping:")); panelForm.add(tTen);
        panelForm.add(new JLabel("Giá bán:")); panelForm.add(tGia);
        panelForm.add(new JLabel("Nguyên liệu trừ kho:")); panelForm.add(comboNL);
        
        JButton btnSave = new JButton("Lưu Topping");
        btnSave.addActionListener(e -> {
            try {
                Topping tp = new Topping();
                tp.setMaTopping(tMa.getText());
                tp.setTenTopping(tTen.getText());
                tp.setGiaBan(Long.parseLong(tGia.getText()));
                NguyenLieu selNL = (NguyenLieu) comboNL.getSelectedItem();
                if (selNL != null) tp.setMaNL_LienKet(selNL.getMaNL());
                tp.setDinhLuongHaoHut(1.0); // Mặc định trừ 1 đơn vị
                
                if (spDAO.saveTopping(tp)) {
                    JOptionPane.showMessageDialog(this, "Đã lưu Topping!");
                    loadAllData();
                    EventManager.getInstance().fireDataUpdated();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi nhập liệu!");
            }
        });
        
        JButton btnDelete = new JButton("Xóa Topping");
        btnDelete.addActionListener(e -> {
            int row = tableTopping.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Topping cần xóa!");
                return;
            }
            String maTopping = modelTopping.getValueAt(row, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa Topping này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (spDAO.deleteTopping(maTopping)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa Topping!");
                    tMa.setText(""); tTen.setText(""); tGia.setText("");
                    loadAllData();
                    EventManager.getInstance().fireDataUpdated();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa Topping này (Có thể đã dùng trong hóa đơn).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        JPanel pnlBtns = new JPanel(new GridLayout(1, 2, 10, 10));
        pnlBtns.add(btnSave);
        pnlBtns.add(btnDelete);
        panelForm.add(new JLabel("")); // Spacer
        panelForm.add(pnlBtns);
        
        panel.add(panelForm, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadAllData() {
        // Load SP
        List<SanPham> list = spDAO.getAllSanPham();
        modelSP.setRowCount(0);
        for (SanPham sp : list) modelSP.addRow(new Object[]{sp.getMaSP(), sp.getTenSP()});
        
        // Load Topping
        List<Topping> toppings = spDAO.getAllTopping();
        modelTopping.setRowCount(0);
        for (Topping t : toppings) modelTopping.addRow(new Object[]{t.getMaTopping(), t.getTenTopping(), t.getGiaBan(), t.getMaNL_LienKet()});
    }
    
    private void loadSizesOfSelectedSP() {
        int row = tableSP.getSelectedRow();
        if (row == -1) {
            modelSize.setRowCount(0);
            return;
        }
        String maSP = tableSP.getValueAt(row, 0).toString();
        List<BienTheSP> sizes = spDAO.getBienTheBySP(maSP);
        modelSize.setRowCount(0);
        for (BienTheSP bt : sizes) modelSize.addRow(new Object[]{bt.getMaBienThe(), bt.getTenSize(), bt.getGiaBan()});
    }
    
    private void addSanPham() {
        if (txtMaSP.getText().isEmpty() || txtTenSP.getText().isEmpty()) return;
        SanPham sp = new SanPham(txtMaSP.getText(), txtTenSP.getText());
        if (spDAO.insertSanPham(sp)) {
            loadAllData();
            txtMaSP.setText(""); txtTenSP.setText("");
            EventManager.getInstance().fireDataUpdated();
        }
    }
    
    private void deleteSanPham() {
        int row = tableSP.getSelectedRow();
        if (row == -1) return;
        String maSP = tableSP.getValueAt(row, 0).toString();
        if (spDAO.deleteSanPham(maSP)) {
            loadAllData();
            EventManager.getInstance().fireDataUpdated();
        }
    }
    
    private void addSize() {
        int row = tableSP.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món trước!");
            return;
        }
        String maSP = tableSP.getValueAt(row, 0).toString();
        
        String sizeName = JOptionPane.showInputDialog(this, "Nhập tên Size (ví dụ: M, L):");
        String priceStr = JOptionPane.showInputDialog(this, "Nhập giá bán:");
        
        if (sizeName != null && priceStr != null) {
            try {
                BienTheSP bt = new BienTheSP("BT" + System.nanoTime(), maSP, sizeName, Long.parseLong(priceStr));
                if (spDAO.insertBienThe(bt)) {
                    loadSizesOfSelectedSP();
                    EventManager.getInstance().fireDataUpdated();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
            }
        }
    }
    
    private void editSize() {
        int row = tableSize.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Size cần sửa!");
            return;
        }
        String maBienThe = tableSize.getValueAt(row, 0).toString();
        String currentSize = tableSize.getValueAt(row, 1).toString();
        String currentPrice = tableSize.getValueAt(row, 2).toString();
        
        String sizeName = JOptionPane.showInputDialog(this, "Nhập tên Size mới:", currentSize);
        if (sizeName == null) return;
        String priceStr = JOptionPane.showInputDialog(this, "Nhập giá bán mới:", currentPrice);
        if (priceStr == null) return;
        
        try {
            BienTheSP bt = new BienTheSP(maBienThe, "", sizeName, Long.parseLong(priceStr));
            if (spDAO.updateBienThe(bt)) {
                loadSizesOfSelectedSP();
                EventManager.getInstance().fireDataUpdated();
                JOptionPane.showMessageDialog(this, "Đã sửa Size!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
        }
    }

    private void deleteSize() {
        int row = tableSize.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Size cần xóa!");
            return;
        }
        String maBienThe = tableSize.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa Size này sẽ xóa cả Công Thức của nó. Tiếp tục?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (spDAO.deleteBienThe(maBienThe)) {
                loadSizesOfSelectedSP();
                EventManager.getInstance().fireDataUpdated();
                JOptionPane.showMessageDialog(this, "Đã xóa Size!");
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa (có thể đã có giao dịch)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void configTopping() {
        int row = tableSP.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món để thiết lập Topping!");
            return;
        }
        String maSP = tableSP.getValueAt(row, 0).toString();
        List<SanPham> list = spDAO.getAllSanPham();
        SanPham selectedSP = null;
        for (SanPham sp : list) {
            if (sp.getMaSP().equals(maSP)) {
                selectedSP = sp; break;
            }
        }
        if (selectedSP != null) {
            ProductToppingConfigDialog dialog = new ProductToppingConfigDialog(this, selectedSP);
            dialog.setVisible(true);
        }
    }
}
