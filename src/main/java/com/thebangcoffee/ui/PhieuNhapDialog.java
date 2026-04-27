package com.thebangcoffee.ui;

import com.thebangcoffee.dao.InventoryDAO;
import com.thebangcoffee.event.EventManager;
import com.thebangcoffee.model.ChiTietPhieuNhapKho;
import com.thebangcoffee.model.NguyenLieu;
import com.thebangcoffee.model.PhieuNhapKho;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhieuNhapDialog extends JDialog {
    private InventoryDAO invDAO = new InventoryDAO();
    private List<NguyenLieu> allMaterials;
    
    private JComboBox<NguyenLieu> comboNL;
    private JTextField txtQty;
    private JTextField txtPrice;
    private JTextField txtTyLeQuyDoi;
    private JSpinner spinHanSuDung;
    private JTextField txtGhiChu;
    
    private JTable tableCart;
    private DefaultTableModel tableModelCart;
    private JLabel lblTotal;
    
    private List<ChiTietPhieuNhapKho> cartItems = new ArrayList<>();
    
    public PhieuNhapDialog(JFrame parent) {
        super(parent, "Lập Phiếu Nhập Kho", true);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        allMaterials = invDAO.getAllNguyenLieu();
        
        initTopPanel();
        initCenterPanel();
        initBottomPanel();
    }
    
    private void initTopPanel() {
        JPanel panelTop = new JPanel(new GridLayout(0, 4, 10, 10));
        panelTop.setBorder(BorderFactory.createTitledBorder("Nhập thông tin"));
        
        comboNL = new JComboBox<>();
        for (NguyenLieu nl : allMaterials) {
            comboNL.addItem(nl);
        }
        
        txtQty = new JTextField();
        txtPrice = new JTextField();
        txtTyLeQuyDoi = new JTextField("1"); // Mặc định là 1
        
        spinHanSuDung = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinHanSuDung, "dd/MM/yyyy");
        spinHanSuDung.setEditor(dateEditor);
        
        panelTop.add(new JLabel("Chọn Nguyên Liệu:"));
        panelTop.add(comboNL);
        panelTop.add(new JLabel("Số lượng nhập:"));
        panelTop.add(txtQty);
        
        panelTop.add(new JLabel("Đơn giá (VNĐ):"));
        panelTop.add(txtPrice);
        panelTop.add(new JLabel("Tỉ lệ QĐ ra kho:"));
        panelTop.add(txtTyLeQuyDoi);
        
        panelTop.add(new JLabel("Hạn sử dụng:"));
        panelTop.add(spinHanSuDung);
        
        JButton btnAdd = new JButton("Thêm vào phiếu");
        btnAdd.setBackground(new Color(0, 123, 255));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> addToCart());
        panelTop.add(new JLabel("")); // Spacer
        panelTop.add(btnAdd);
        
        add(panelTop, BorderLayout.NORTH);
    }
    
    private void initCenterPanel() {
        String[] cols = {"Mã NL", "Tên Nguyên Liệu", "Hạn Sử Dụng", "SL Nhập", "Tỉ lệ QĐ", "Thực nhập (Kho)", "Đơn giá", "Thành tiền"};
        tableModelCart = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableCart = new JTable(tableModelCart);
        
        JScrollPane scroll = new JScrollPane(tableCart);
        scroll.setBorder(BorderFactory.createTitledBorder("Chi tiết phiếu nhập"));
        add(scroll, BorderLayout.CENTER);
    }
    
    private void initBottomPanel() {
        JPanel panelBottom = new JPanel(new BorderLayout(10, 10));
        
        JPanel pnlInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlInfo.add(new JLabel("Ghi chú:"));
        txtGhiChu = new JTextField(30);
        pnlInfo.add(txtGhiChu);
        
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal = new JLabel("Tổng tiền: 0 VNĐ");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(Color.RED);
        
        JButton btnSave = new JButton("LƯU PHIẾU NHẬP");
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        btnSave.addActionListener(e -> saveReceipt());
        
        pnlAction.add(lblTotal);
        pnlAction.add(Box.createHorizontalStrut(20));
        pnlAction.add(btnSave);
        
        panelBottom.add(pnlInfo, BorderLayout.CENTER);
        panelBottom.add(pnlAction, BorderLayout.EAST);
        
        add(panelBottom, BorderLayout.SOUTH);
    }
    
    private void addToCart() {
        NguyenLieu selected = (NguyenLieu) comboNL.getSelectedItem();
        if (selected == null) return;
        
        try {
            double qty = Double.parseDouble(txtQty.getText());
            double price = Double.parseDouble(txtPrice.getText());
            double tyle = Double.parseDouble(txtTyLeQuyDoi.getText());
            
            if (qty <= 0 || price < 0 || tyle <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng, đơn giá và tỉ lệ phải hợp lệ (>0)!");
                return;
            }
            
            double total = qty * price;
            java.util.Date expDate = (java.util.Date) spinHanSuDung.getValue();
            
            // Add to list
            ChiTietPhieuNhapKho ct = new ChiTietPhieuNhapKho();
            ct.setMaNL(selected.getMaNL());
            ct.setTenNL(selected.getTenNL());
            ct.setSoLuong(qty);
            ct.setDonGia(price);
            ct.setTyLeQuyDoi(tyle);
            ct.setThanhTien(total);
            ct.setHanSuDung(expDate);
            
            cartItems.add(ct);
            
            // Update UI
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            tableModelCart.addRow(new Object[]{
                ct.getMaNL(), ct.getTenNL(), sdf.format(ct.getHanSuDung()), ct.getSoLuong(), ct.getTyLeQuyDoi(), (ct.getSoLuong() * ct.getTyLeQuyDoi()), ct.getDonGia(), ct.getThanhTien()
            });
            
            updateTotal();
            
            txtQty.setText("");
            txtPrice.setText("");
            txtTyLeQuyDoi.setText("1");
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!");
        }
    }
    
    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(ChiTietPhieuNhapKho::getThanhTien).sum();
        lblTotal.setText("Tổng tiền: " + String.format("%,.0f", total) + " VNĐ");
    }
    
    private void saveReceipt() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phiếu nhập rỗng!");
            return;
        }
        
        String maPN = "PN" + System.currentTimeMillis(); // Generate ID
        double total = cartItems.stream().mapToDouble(ChiTietPhieuNhapKho::getThanhTien).sum();
        
        PhieuNhapKho pn = new PhieuNhapKho(maPN, new Date(), total, txtGhiChu.getText());
        
        if (invDAO.luuPhieuNhap(pn, cartItems)) {
            JOptionPane.showMessageDialog(this, "Lưu phiếu nhập thành công!");
            
            // FIRE EVENT TO UPDATE ALL FRAMES
            EventManager.getInstance().fireDataUpdated();
            
            dispose(); // Close dialog
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu phiếu nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
