package com.thebangcoffee.ui;

import com.thebangcoffee.dao.InventoryDAO;
import com.thebangcoffee.model.NguyenLieu;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.thebangcoffee.event.DataUpdateListener;
import com.thebangcoffee.event.EventManager;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class InventoryFrame extends JFrame implements DataUpdateListener {
    private InventoryDAO invDAO = new InventoryDAO();
    private JTable tableNL;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private List<NguyenLieu> allMaterials;

    public InventoryFrame() {
        setTitle("Quản lý Kho Nguyên Liệu - The Bang Coffee");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15)); // Tăng khoảng cách giữa các panel
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Thêm padding xung quanh

        initTopPanel();
        initCenterPanel();
        initBottomPanel();

        loadData();
        
        // Đăng ký nhận sự kiện cập nhật dữ liệu
        EventManager.getInstance().registerListener(this);
    }
    
    @Override
    public void onDataUpdated() {
        loadData();
    }
    
    @Override
    public void dispose() {
        EventManager.getInstance().removeListener(this);
        super.dispose();
    }

    private void initTopPanel() {
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelTop.setBorder(BorderFactory.createTitledBorder("Tìm kiếm & Thao tác"));

        panelTop.add(new JLabel("Tìm tên:"));
        txtSearch = new JTextField(20);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterTable();
            }
        });
        panelTop.add(txtSearch);

        JButton btnAddNL = new JButton("[+] Thêm Nguyên Liệu Mới");
        btnAddNL.setBackground(new Color(40, 167, 69));
        btnAddNL.setForeground(Color.WHITE);
        btnAddNL.addActionListener(e -> showAddNLDialog());
        panelTop.add(btnAddNL);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> loadData());
        panelTop.add(btnRefresh);

        add(panelTop, BorderLayout.NORTH);
    }

    private void initCenterPanel() {
        String[] cols = {"Mã NL", "Tên Nguyên Liệu", "Đơn vị tính", "Số lượng tồn"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableNL = new JTable(tableModel);
        tableNL.setRowHeight(25);
        
        JScrollPane scroll = new JScrollPane(tableNL);
        scroll.setBorder(BorderFactory.createTitledBorder("Danh sách tồn kho"));
        add(scroll, BorderLayout.CENTER);
    }

    private void initBottomPanel() {
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));

        JButton btnHuyPN = new JButton("HỦY PHIẾU NHẬP");
        btnHuyPN.setPreferredSize(new Dimension(180, 40));
        btnHuyPN.setBackground(new Color(220, 53, 69));
        btnHuyPN.setForeground(Color.WHITE);
        btnHuyPN.addActionListener(e -> showHuyPhieuNhapDialog());
        panelBottom.add(btnHuyPN);

        JButton btnDetail = new JButton("CHI TIẾT LÔ HÀNG");
        btnDetail.setPreferredSize(new Dimension(180, 40));
        btnDetail.addActionListener(e -> showLotDetail());
        panelBottom.add(btnDetail);

        JButton btnImport = new JButton("LẬP PHIẾU NHẬP KHO");
        btnImport.setPreferredSize(new Dimension(200, 40));
        btnImport.setBackground(new Color(0, 123, 255));
        btnImport.setForeground(Color.WHITE);
        btnImport.setFont(new Font("Arial", Font.BOLD, 14));
        btnImport.addActionListener(e -> showPhieuNhapDialog());
        panelBottom.add(btnImport);

        add(panelBottom, BorderLayout.SOUTH);
    }
    
    private void showHuyPhieuNhapDialog() {
        String maPN = JOptionPane.showInputDialog(this, "Nhập Mã Phiếu Nhập cần hủy (Ví dụ: PN171420...):");
        if (maPN != null && !maPN.trim().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn hủy phiếu nhập " + maPN + "?\nToàn bộ lô hàng và số lượng tồn kho liên quan sẽ bị xóa.", "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                String error = invDAO.huyPhieuNhapKho(maPN.trim());
                if (error == null) {
                    JOptionPane.showMessageDialog(this, "Hủy phiếu nhập thành công!");
                    EventManager.getInstance().fireDataUpdated();
                } else {
                    JOptionPane.showMessageDialog(this, error, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void showPhieuNhapDialog() {
        PhieuNhapDialog dialog = new PhieuNhapDialog(this);
        dialog.setVisible(true);
    }

    private void showLotDetail() {
        int row = tableNL.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nguyên liệu để xem chi tiết Lô!");
            return;
        }
        String maNL = tableNL.getValueAt(row, 0).toString();
        String tenNL = tableNL.getValueAt(row, 1).toString();
        
        PhieuHuyDialog dialog = new PhieuHuyDialog(this, maNL, tenNL);
        dialog.setVisible(true);
    }

    private void loadData() {
        allMaterials = invDAO.getAllNguyenLieu();
        displayToTable(allMaterials);
    }

    private void displayToTable(List<NguyenLieu> list) {
        tableModel.setRowCount(0);
        for (NguyenLieu nl : list) {
            tableModel.addRow(new Object[]{
                nl.getMaNL(),
                nl.getTenNL(),
                nl.getDvtCoBan(),
                nl.getSoLuongTon()
            });
        }
    }

    private void filterTable() {
        String text = txtSearch.getText().toLowerCase();
        List<NguyenLieu> filtered = allMaterials.stream()
                .filter(nl -> nl.getTenNL().toLowerCase().contains(text) || nl.getMaNL().toLowerCase().contains(text))
                .collect(Collectors.toList());
        displayToTable(filtered);
    }

    private void showAddNLDialog() {
        AddNguyenLieuDialog dialog = new AddNguyenLieuDialog(this);
        dialog.setVisible(true);
        loadData();
    }
}
