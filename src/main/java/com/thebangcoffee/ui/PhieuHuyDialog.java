package com.thebangcoffee.ui;

import com.thebangcoffee.dao.InventoryDAO;
import com.thebangcoffee.model.LoNguyenLieu;
import com.thebangcoffee.event.EventManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PhieuHuyDialog extends JDialog {
    private InventoryDAO invDAO = new InventoryDAO();
    private JTable table;
    private DefaultTableModel model;
    private String maNL;

    public PhieuHuyDialog(JFrame parent, String maNL, String tenNL) {
        super(parent, "Xuất Hủy Lô - " + tenNL, true);
        this.maNL = maNL;
        setSize(700, 400);
        setLocationRelativeTo(parent);
        
        initUI();
        loadLots();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] cols = {"Mã Lô", "Số lượng ban đầu", "Còn lại (Kho)", "Hạn sử dụng"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnHuy = new JButton("XUẤT HỦY LÔ ĐÃ CHỌN");
        btnHuy.setBackground(Color.RED);
        btnHuy.setForeground(Color.WHITE);
        btnHuy.setFont(new Font("Arial", Font.BOLD, 12));
        btnHuy.addActionListener(e -> processHuy());
        pnlBottom.add(btnHuy);
        
        add(pnlBottom, BorderLayout.SOUTH);
    }
    
    private void loadLots() {
        List<LoNguyenLieu> lots = invDAO.getDanhSachLoByNL(maNL);
        model.setRowCount(0);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        for (LoNguyenLieu lo : lots) {
            boolean isExpired = false;
            if (lo.getHanSuDung() != null && lo.getHanSuDung().before(new java.util.Date())) {
                isExpired = true;
            }
            
            String dateStr = lo.getHanSuDung() != null ? sdf.format(lo.getHanSuDung()) : "Không xác định";
            if (isExpired) dateStr += " (HẾT HẠN)";
            
            model.addRow(new Object[]{
                lo.getMaLo(), lo.getSoLuongBanDau(), lo.getSoLuongConLai(), dateStr
            });
        }
    }
    
    private void processHuy() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 Lô để hủy!");
            return;
        }
        
        String maLo = model.getValueAt(row, 0).toString();
        double tonLo = Double.parseDouble(model.getValueAt(row, 2).toString());
        
        String inputSL = JOptionPane.showInputDialog(this, "Lô này còn " + tonLo + " đơn vị. Nhập số lượng muốn XUẤT HỦY:", tonLo);
        if (inputSL == null || inputSL.trim().isEmpty()) return;
        
        String lyDo = JOptionPane.showInputDialog(this, "Nhập lý do hủy (ví dụ: Hư hỏng, Quá hạn...):", "Quá hạn sử dụng");
        if (lyDo == null) return;
        
        try {
            double slHuy = Double.parseDouble(inputSL);
            if (slHuy <= 0 || slHuy > tonLo) {
                JOptionPane.showMessageDialog(this, "Số lượng hủy phải > 0 và <= Tồn lô!");
                return;
            }
            
            if (invDAO.huyLoHang(maLo, slHuy, lyDo)) {
                JOptionPane.showMessageDialog(this, "Xuất hủy thành công!");
                EventManager.getInstance().fireDataUpdated();
                loadLots();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi hủy lô!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ!");
        }
    }
}
