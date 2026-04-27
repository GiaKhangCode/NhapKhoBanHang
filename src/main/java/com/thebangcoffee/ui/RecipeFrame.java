package com.thebangcoffee.ui;

import com.thebangcoffee.dao.CongThucDAO;
import com.thebangcoffee.dao.InventoryDAO;
import com.thebangcoffee.dao.SanPhamDAO;
import com.thebangcoffee.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.thebangcoffee.event.DataUpdateListener;
import com.thebangcoffee.event.EventManager;

public class RecipeFrame extends JFrame implements DataUpdateListener {
    private SanPhamDAO spDAO = new SanPhamDAO();
    private InventoryDAO invDAO = new InventoryDAO();
    private CongThucDAO ctDAO = new CongThucDAO();

    private JList<BienTheSP> listVariants;
    private DefaultListModel<BienTheSP> listModelVariants;
    private JTable tableRecipe;
    private DefaultTableModel tableModelRecipe;
    
    private List<NguyenLieu> allMaterials;

    public RecipeFrame() {
        setTitle("Thiết lập Công thức - The Bang Coffee");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15)); // Tăng khoảng cách giữa các panel
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Thêm padding xung quanh

        initLeftPanel();
        initRightPanel();

        loadData();
        
        // Đăng ký nhận sự kiện
        EventManager.getInstance().registerListener(this);
    }
    
    @Override
    public void onDataUpdated() {
        loadData();
        loadRecipeOfSelectedVariant(); // reload details if currently selected
    }
    
    @Override
    public void dispose() {
        EventManager.getInstance().removeListener(this);
        super.dispose();
    }

    private void initLeftPanel() {
        JPanel panelLeft = new JPanel(new BorderLayout());
        panelLeft.setBorder(BorderFactory.createTitledBorder("Chọn món & Size"));
        panelLeft.setPreferredSize(new Dimension(250, 0));

        listModelVariants = new DefaultListModel<>();
        listVariants = new JList<>(listModelVariants);
        listVariants.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof BienTheSP) {
                    BienTheSP bt = (BienTheSP) value;
                    String displayName = (bt.getTenSP() != null ? bt.getTenSP() : "Món") + " - Size " + bt.getTenSize();
                    label.setText(displayName);
                }
                return label;
            }
        });

        listVariants.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadRecipeOfSelectedVariant();
            }
        });

        panelLeft.add(new JScrollPane(listVariants), BorderLayout.CENTER);
        add(panelLeft, BorderLayout.WEST);
    }

    private void initRightPanel() {
        JPanel panelRight = new JPanel(new BorderLayout(10, 10));
        panelRight.setBorder(BorderFactory.createTitledBorder("Công thức chế biến"));

        String[] cols = {"Mã NL", "Tên Nguyên Liệu", "Định lượng (Gram/ml)", "ĐVT"};
        tableModelRecipe = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Chỉ cho phép sửa cột định lượng
            }
        };
        tableRecipe = new JTable(tableModelRecipe);
        tableRecipe.setRowHeight(25);
        
        panelRight.add(new JScrollPane(tableRecipe), BorderLayout.CENTER);

        // Nút điều khiển
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton(" Thêm nguyên liệu");
        btnAdd.addActionListener(e -> addIngredientRow());
        
        JButton btnRemove = new JButton(" Xóa dòng");
        btnRemove.addActionListener(e -> removeSelectedRow());
        
        JButton btnSave = new JButton(" LƯU CÔNG THỨC");
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 12));
        btnSave.addActionListener(e -> saveRecipe());

        panelButtons.add(btnAdd);
        panelButtons.add(btnRemove);
        panelButtons.add(btnSave);
        
        panelRight.add(panelButtons, BorderLayout.SOUTH);

        add(panelRight, BorderLayout.CENTER);
    }

    private void loadData() {
        // Load Variants
        List<SanPham> products = spDAO.getAllSanPham();
        listModelVariants.clear();
        for (SanPham sp : products) {
            for (BienTheSP bt : sp.getDanhSachBienThe()) {
                bt.setTenSP(sp.getTenSP()); // Gán tên sản phẩm vào biến thể
                listModelVariants.addElement(bt);
            }
        }

        // Load Materials for selection
        allMaterials = invDAO.getAllNguyenLieu();
    }

    private void loadRecipeOfSelectedVariant() {
        BienTheSP selected = listVariants.getSelectedValue();
        if (selected == null) return;

        List<CongThuc> list = ctDAO.getCongThucByBienThe(selected.getMaBienThe());
        tableModelRecipe.setRowCount(0);
        for (CongThuc ct : list) {
            NguyenLieu nl = findNL(ct.getMaNL());
            tableModelRecipe.addRow(new Object[]{
                ct.getMaNL(),
                nl != null ? nl.getTenNL() : "N/A",
                ct.getDinhLuong(),
                nl != null ? nl.getDvtCoBan() : ""
            });
        }
    }

    private NguyenLieu findNL(String maNL) {
        for (NguyenLieu nl : allMaterials) {
            if (nl.getMaNL().equals(maNL)) return nl;
        }
        return null;
    }

    private void addIngredientRow() {
        if (listVariants.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món cần thiết lập công thức!");
            return;
        }

        // Show a simple dialog to select Material
        NguyenLieu selectedNL = (NguyenLieu) JOptionPane.showInputDialog(
                this, "Chọn nguyên liệu:", "Thêm nguyên liệu",
                JOptionPane.QUESTION_MESSAGE, null,
                allMaterials.toArray(), null
        );

        if (selectedNL != null) {
            // Check if already exists
            for (int i = 0; i < tableModelRecipe.getRowCount(); i++) {
                if (tableModelRecipe.getValueAt(i, 0).equals(selectedNL.getMaNL())) {
                    JOptionPane.showMessageDialog(this, "Nguyên liệu này đã có trong công thức!");
                    return;
                }
            }
            tableModelRecipe.addRow(new Object[]{
                selectedNL.getMaNL(),
                selectedNL.getTenNL(),
                0.0,
                selectedNL.getDvtCoBan()
            });
        }
    }

    private void removeSelectedRow() {
        int row = tableRecipe.getSelectedRow();
        if (row != -1) {
            tableModelRecipe.removeRow(row);
        }
    }

    private void saveRecipe() {
        BienTheSP selected = listVariants.getSelectedValue();
        if (selected == null) return;

        List<CongThuc> list = new ArrayList<>();
        for (int i = 0; i < tableModelRecipe.getRowCount(); i++) {
            try {
                CongThuc ct = new CongThuc();
                ct.setMaBienThe(selected.getMaBienThe());
                ct.setMaNL(tableModelRecipe.getValueAt(i, 0).toString());
                ct.setDinhLuong(Double.parseDouble(tableModelRecipe.getValueAt(i, 2).toString()));
                list.add(ct);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Định lượng tại dòng " + (i + 1) + " không hợp lệ!");
                return;
            }
        }

        if (ctDAO.saveCongThuc(selected.getMaBienThe(), list)) {
            JOptionPane.showMessageDialog(this, "Lưu công thức thành công!");
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu công thức!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
