package com.thebangcoffee.ui;

import com.thebangcoffee.dao.OrderDAO;
import com.thebangcoffee.dao.SanPhamDAO;
import com.thebangcoffee.dao.ShiftDAO;
import com.thebangcoffee.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.thebangcoffee.event.DataUpdateListener;
import com.thebangcoffee.event.EventManager;

public class POSFrame extends JFrame implements DataUpdateListener {
    private SanPhamDAO spDAO = new SanPhamDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private ShiftDAO shiftDAO = new ShiftDAO();
    
    private String currentShiftId = null;
    
    private JPanel panelProducts;
    private JTable tableBill;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private JButton btnPay;
    private long totalAmount = 0;
    
    private List<CTHoaDon> currentBillItems = new ArrayList<>();

    public POSFrame() {
        setTitle("The Bang Coffee - POS Bán Hàng");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initLeftPanel();
        initRightPanel();
        
        loadProducts();
        
        EventManager.getInstance().registerListener(this);
        checkAndOpenShift();
    }
    
    private void checkAndOpenShift() {
        currentShiftId = shiftDAO.getCurrentShift();
        if (currentShiftId == null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Hiện tại chưa có Ca làm việc nào mở. Bạn có muốn Mở Ca Mới (Nhận ca) không?", "Mở Ca Làm Việc", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String nvName = JOptionPane.showInputDialog(this, "Nhập tên nhân viên trực ca:");
                if (nvName != null && !nvName.trim().isEmpty()) {
                    if (shiftDAO.openShift(nvName)) {
                        currentShiftId = shiftDAO.getCurrentShift();
                        JOptionPane.showMessageDialog(this, "Đã mở ca làm việc thành công!");
                    }
                }
            }
        }
        
        if (currentShiftId == null) {
            btnPay.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Tính năng bán hàng đã bị khóa vì chưa mở ca.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    @Override
    public void onDataUpdated() {
        loadProducts();
    }

    @Override
    public void dispose() {
        EventManager.getInstance().removeListener(this);
        super.dispose();
    }

    private void initLeftPanel() {
        panelProducts = new JPanel(new GridLayout(0, 3, 10, 10));
        panelProducts.setBorder(BorderFactory.createTitledBorder("Danh sách món"));
        
        JScrollPane scroll = new JScrollPane(panelProducts);
        scroll.setPreferredSize(new Dimension(600, 0));
        add(scroll, BorderLayout.CENTER);
    }

    private void initRightPanel() {
        JPanel panelRight = new JPanel(new BorderLayout(5, 5));
        panelRight.setPreferredSize(new Dimension(380, 0));
        panelRight.setBorder(BorderFactory.createTitledBorder("Chi tiết hóa đơn"));

        String[] cols = {"Món", "Size", "Topping", "SL", "Giá"};
        tableModel = new DefaultTableModel(cols, 0);
        tableBill = new JTable(tableModel);
        panelRight.add(new JScrollPane(tableBill), BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new GridLayout(3, 1, 5, 5));
        lblTotal = new JLabel("Tổng tiền: 0 VNĐ", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotal.setForeground(Color.RED);
        
        btnPay = new JButton("THANH TOÁN");
        btnPay.setBackground(new Color(34, 139, 34));
        btnPay.setForeground(Color.WHITE);
        btnPay.setFont(new Font("Arial", Font.BOLD, 16));
        btnPay.addActionListener(e -> checkout());
        
        JButton btnCloseShift = new JButton("CHỐT CA LÀM VIỆC");
        btnCloseShift.setBackground(new Color(220, 53, 69));
        btnCloseShift.setForeground(Color.WHITE);
        btnCloseShift.addActionListener(e -> closeShift());

        panelBottom.add(lblTotal);
        panelBottom.add(btnPay);
        panelBottom.add(btnCloseShift);
        panelRight.add(panelBottom, BorderLayout.SOUTH);

        add(panelRight, BorderLayout.EAST);
    }

    private void loadProducts() {
        List<SanPham> list = spDAO.getAllSanPham();
        panelProducts.removeAll();
        for (SanPham sp : list) {
            JButton btn = new JButton("<html><center>" + sp.getTenSP() + "</center></html>");
            btn.setPreferredSize(new Dimension(150, 100));
            btn.addActionListener(e -> showOptions(sp));
            panelProducts.add(btn);
        }
        panelProducts.revalidate();
        panelProducts.repaint();
    }

    private void showOptions(SanPham sp) {
        ProductOptionDialog dialog = new ProductOptionDialog(this, sp);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            addItemToBill(dialog.getResult());
        }
    }

    private void addItemToBill(CTHoaDon item) {
        currentBillItems.add(item);
        
        StringBuilder toppingsStr = new StringBuilder();
        for (Topping t : item.getToppings()) {
            if (toppingsStr.length() > 0) toppingsStr.append(", ");
            toppingsStr.append(t.getTenTopping());
        }

        // Tìm tên món và size để hiển thị
        String itemName = "";
        String sizeName = "";
        for (BienTheSP bt : spDAO.getBienTheBySP(item.getMaBienThe().substring(0, 0))) { // Dummy logic, should store in model
             // Optimized: I'll just pass names into the model for UI display
        }
        
        // Cập nhật Table UI (Để đơn giản, tôi dùng Dialog đã set sẵn info)
        tableModel.addRow(new Object[]{
            "Sản phẩm", // Sẽ cải tiến sau
            item.getMaBienThe(), 
            toppingsStr.toString(),
            item.getSoLuong(),
            item.getDonGia()
        });
        
        calculateTotal();
    }

    private void calculateTotal() {
        totalAmount = 0;
        for (CTHoaDon item : currentBillItems) {
            totalAmount += item.getDonGia() * item.getSoLuong();
        }
        lblTotal.setText("Tổng tiền: " + String.format("%, d", totalAmount) + " VNĐ");
    }

    private void checkout() {
        if (currentBillItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có món nào trong bill!");
            return;
        }

        HoaDon hd = new HoaDon();
        hd.setMaHD("HD" + System.currentTimeMillis() / 1000);
        hd.setTongTien(totalAmount);
        hd.setChiTietHoaDon(currentBillItems);

        String stockWarning = orderDAO.checkStockAvailability(hd);
        if (stockWarning != null) {
            int confirm = JOptionPane.showConfirmDialog(this, stockWarning + "\n\nBạn có chắc chắn muốn tiếp tục bán và ghi nhận tồn kho âm không?", "Cảnh báo Tồn Kho", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return; // User cancelled
            }
        }

        if (orderDAO.processCheckout(hd)) {
            JOptionPane.showMessageDialog(this, "Thanh toán thành công! Kho đã được cập nhật.");
            tableModel.setRowCount(0);
            currentBillItems.clear();
            calculateTotal();
            EventManager.getInstance().fireDataUpdated();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi thanh toán!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void closeShift() {
        if (currentShiftId == null) {
            JOptionPane.showMessageDialog(this, "Không có ca làm việc nào đang mở!");
            return;
        }
        
        double revenue = shiftDAO.calculateShiftRevenue(currentShiftId);
        int confirm = JOptionPane.showConfirmDialog(this, "TỔNG KẾT CA LÀM VIỆC\n\nTổng doanh thu thu được: " + String.format("%,.0f", revenue) + " VNĐ\n\nBạn có chắc chắn muốn chốt ca và nộp tiền không?", "Chốt ca", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (shiftDAO.closeShift(currentShiftId, revenue)) {
                JOptionPane.showMessageDialog(this, "Đã chốt ca thành công!");
                currentShiftId = null;
                btnPay.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi chốt ca!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
