package view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import util.DatabaseUtil;
import util.LogUtil;

public class StudentFrame extends JFrame {
    private String studentId;
    private JTabbedPane tabbedPane;
    
    public StudentFrame(String studentId) {
        this.studentId = studentId;
        initUI();
    }
    
    private void initUI() {
        setTitle("学生系统 - " + studentId);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        
        // 添加选课面板
        tabbedPane.addTab("课程选择", createCourseSelectionPanel());
        // 添加我的课程面板
        tabbedPane.addTab("我的课程", createMyCoursePanel());
        // 添加成绩查询面板
        tabbedPane.addTab("成绩查询", createGradePanel());
        
        add(tabbedPane);
    }
    
    private JPanel createCourseSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建表格模型
        String[] columns = {"课程ID", "课程名称", "教师ID", "学分", "最大人数", "当前人数", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只允许编辑最后一列（按钮列）
                return column == 6;
            }
        };
        JTable table = new JTable(model);
        
        // 添加选课按钮列
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(
            new ButtonEditor(new JCheckBox(), "选课", (row, courseId) -> {
                LogUtil.info("选课按钮被点击，课程ID：" + courseId);
                selectCourse(courseId);  // 直接使用课程ID而不是从表格获取
                // 不在这里刷新，由selectCourse内部的refreshAllTables负责
            })
        );
        
        // 设置表格选择模式
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 添加刷新按钮
        JButton refreshBtn = new JButton("刷新课程列表");
        refreshBtn.addActionListener(e -> {
            LogUtil.info("刷新课程列表");
            loadAvailableCourses(model);
        });
        
        panel.add(refreshBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // 加载可选课程数据
        loadAvailableCourses(model);
        
        return panel;
    }
    
    private void loadAvailableCourses(DefaultTableModel model) {
        LogUtil.info("开始加载可选课程列表");
        model.setRowCount(0);
        try (Connection conn = DatabaseUtil.getConnection()) {
            // 修改SQL查询，确保正确获取当前学生的选课状态
            String sql = "SELECT c.*, " +
                        "(SELECT COUNT(*) FROM course_selection cs " +
                        "WHERE cs.course_id = c.course_id AND cs.student_id = ?) as is_selected " +
                        "FROM courses c";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            LogUtil.info("执行查询：" + sql + ", 参数：studentId=" + studentId);
            ResultSet rs = pstmt.executeQuery();
            
            int courseCount = 0;
            while (rs.next()) {
                courseCount++;
                boolean isSelected = rs.getInt("is_selected") > 0;
                Object[] row = {
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getString("teacher_id"),
                    rs.getInt("credit"),
                    rs.getInt("max_students"),
                    rs.getInt("current_students"),
                    isSelected ? "已选" : "选课"
                };
                model.addRow(row);
            }
            LogUtil.info("成功加载 " + courseCount + " 门课程");
        } catch (SQLException ex) {
            LogUtil.error("加载课程数据失败", ex);
            JOptionPane.showMessageDialog(this, "加载课程数据失败：" + ex.getMessage());
        }
    }
    
    private void selectCourse(String courseId) {
        LogUtil.info("学生 " + studentId + " 尝试选择课程 " + courseId);
        try (Connection conn = DatabaseUtil.getConnection()) {
            // 调用选课存储过程
            CallableStatement cstmt = conn.prepareCall("{call sp_select_course(?, ?, ?)}");
            cstmt.setString(1, studentId);
            cstmt.setString(2, courseId);
            cstmt.registerOutParameter(3, Types.INTEGER);
            LogUtil.info("执行选课存储过程，参数：studentId=" + studentId + ", courseId=" + courseId);
            
            cstmt.execute();
            
            int result = cstmt.getInt(3);
            LogUtil.info("选课存储过程返回结果：" + result);
            
            switch (result) {
                case 1:
                    LogUtil.info("选课成功");
                    JOptionPane.showMessageDialog(this, "选课成功！");
                    // 使用SwingUtilities确保在EDT线程中刷新UI
                    SwingUtilities.invokeLater(() -> {
                        LogUtil.info("开始刷新所有表格");
                        refreshAllTables();
                        LogUtil.info("完成刷新所有表格");
                    });
                    break;
                case -1:
                    LogUtil.info("选课失败：课程已满");
                    JOptionPane.showMessageDialog(this, "选课失败：课程已满！", "错误", JOptionPane.ERROR_MESSAGE);
                    break;
                case -2:
                    LogUtil.info("选课失败：已经选过这门课");
                    JOptionPane.showMessageDialog(this, "选课失败：已经选过这门课！", "错误", JOptionPane.ERROR_MESSAGE);
                    break;
                case -3:
                    LogUtil.info("选课失败：数据库错误");
                    JOptionPane.showMessageDialog(this, "选课失败：数据库错误！", "错误", JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    LogUtil.error("选课失败：未知错误，返回值：" + result, null);
                    JOptionPane.showMessageDialog(this, "选课失败：未知错误！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            LogUtil.error("选课过程发生异常", ex);
            JOptionPane.showMessageDialog(this, "选课失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createMyCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建表格模型，确保有足够的列
        String[] columns = {"课程ID", "课程名称", "教师ID", "学分", "操作"};  // 5列
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;  // 只允许编辑最后一列（按钮列）
            }
        };
        JTable table = new JTable(model);
        
        // 添加退课按钮列
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(
            new ButtonEditor(new JCheckBox(), "退课", (row, courseId) -> {
                LogUtil.info("退课按钮被点击，课程ID：" + courseId);
                dropCourse(courseId);  // 直接使用课程ID而不是从表格获取
                // 不在这里刷新，由dropCourse内部的refreshAllTables负责
            })
        );
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // 加载已选课程数据
        loadMyCourses(model);
        
        return panel;
    }
    
    private void loadMyCourses(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT c.* FROM courses c " +
                        "JOIN course_selection cs ON c.course_id = cs.course_id " +
                        "WHERE cs.student_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            LogUtil.info("执行查询：" + sql + ", 参数：studentId=" + studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = new Object[5];  // 确保数组大小与列数相同
                row[0] = rs.getString("course_id");
                row[1] = rs.getString("course_name");
                row[2] = rs.getString("teacher_id");
                row[3] = rs.getInt("credit");
                row[4] = "退课";  // 最后一列是退课按钮
                model.addRow(row);
            }
            LogUtil.info("成功加载已选课程数据");
        } catch (SQLException ex) {
            LogUtil.error("加载已选课程数据失败", ex);
            JOptionPane.showMessageDialog(this, "加载已选课程数据失败：" + ex.getMessage());
        }
    }
    
    private void dropCourse(String courseId) {
        LogUtil.info("学生 " + studentId + " 尝试退课 " + courseId);
        try (Connection conn = DatabaseUtil.getConnection()) {
            CallableStatement cstmt = conn.prepareCall("{call sp_drop_course(?, ?, ?)}");
            cstmt.setString(1, studentId);
            cstmt.setString(2, courseId);
            cstmt.registerOutParameter(3, Types.INTEGER);
            
            LogUtil.info("执行退课存储过程，参数：studentId=" + studentId + ", courseId=" + courseId);
            cstmt.execute();
            
            int result = cstmt.getInt(3);
            LogUtil.info("退课存储过程返回结果：" + result);
            
            switch (result) {
                case 1:
                    LogUtil.info("退课成功");
                    JOptionPane.showMessageDialog(this, "退课成功！");
                    // 使用SwingUtilities确保在EDT线程中刷新UI
                    SwingUtilities.invokeLater(() -> {
                        LogUtil.info("开始刷新所有表格");
                        refreshAllTables();
                        LogUtil.info("完成刷新所有表格");
                    });
                    break;
                case -1:
                    LogUtil.error("退课失败：未找到选课记录", new Exception("未找到选课记录"));
                    JOptionPane.showMessageDialog(this, "退课失败：未找到选课记录！", "错误", JOptionPane.ERROR_MESSAGE);
                    break;
                case -2:
                    LogUtil.error("退课失败：数据库错误", new Exception("数据库错误"));
                    JOptionPane.showMessageDialog(this, "退课失败：数据库错误！", "错误", JOptionPane.ERROR_MESSAGE);
                    break;
                case -3:
                    LogUtil.error("退课失败：课程人数错误", new Exception("课程人数错误"));
                    JOptionPane.showMessageDialog(this, "退课失败：课程人数错误！", "错误", JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    LogUtil.error("退课失败：未知错误，返回值：" + result, new Exception("未知错误"));
                    JOptionPane.showMessageDialog(this, "退课失败：未知错误！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            LogUtil.error("退课过程发生异常", ex);
            JOptionPane.showMessageDialog(this, "退课失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createGradePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建表格模型
        String[] columns = {"课程ID", "课程名称", "教师ID", "学分", "成绩"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        
        // 添加统计信息面板
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel totalCreditsLabel = new JLabel();
        JLabel avgScoreLabel = new JLabel();
        statsPanel.add(totalCreditsLabel);
        statsPanel.add(new JLabel("    "));  // 间隔
        statsPanel.add(avgScoreLabel);
        
        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // 加载成绩数据
        loadGrades(model, totalCreditsLabel, avgScoreLabel);
        
        return panel;
    }
    
    private void loadGrades(DefaultTableModel model, JLabel totalCreditsLabel, JLabel avgScoreLabel) {
        model.setRowCount(0);
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT c.*, cs.score FROM courses c " +
                        "JOIN course_selection cs ON c.course_id = cs.course_id " +
                        "WHERE cs.student_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            int totalCredits = 0;
            double totalScore = 0;
            int courseCount = 0;
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getString("teacher_id"),
                    rs.getInt("credit"),
                    rs.getDouble("score")
                };
                model.addRow(row);
                
                if (rs.getObject("score") != null) {
                    totalCredits += rs.getInt("credit");
                    totalScore += rs.getDouble("score");
                    courseCount++;
                }
            }
            
            // 更新统计信息
            totalCreditsLabel.setText("总学分：" + totalCredits);
            if (courseCount > 0) {
                avgScoreLabel.setText("平均分：" + String.format("%.2f", totalScore / courseCount));
            } else {
                avgScoreLabel.setText("平均分：暂无");
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载成绩数据失败：" + ex.getMessage());
        }
    }
    
    private void refreshAllTables() {
        LogUtil.info("刷新所有表格数据");
        
        try {
            // 获取并刷新选课面板的表格模型
            JTable courseTable = (JTable) ((JScrollPane) ((JPanel) tabbedPane.getComponentAt(0)).getComponent(1)).getViewport().getView();
            DefaultTableModel courseModel = (DefaultTableModel) courseTable.getModel();
            loadAvailableCourses(courseModel);
            LogUtil.info("已刷新选课表格，行数：" + courseModel.getRowCount());
            
            // 获取并刷新我的课程面板的表格模型
            JTable myCourseTable = (JTable) ((JScrollPane) ((JPanel) tabbedPane.getComponentAt(1)).getComponent(0)).getViewport().getView();
            DefaultTableModel myCourseModel = (DefaultTableModel) myCourseTable.getModel();
            loadMyCourses(myCourseModel);
            LogUtil.info("已刷新我的课程表格，行数：" + myCourseModel.getRowCount());
            
            // 获取并刷新成绩查询面板的表格模型和标签
            JPanel gradePanel = (JPanel) tabbedPane.getComponentAt(2);
            JTable gradeTable = (JTable) ((JScrollPane) gradePanel.getComponent(1)).getViewport().getView();
            DefaultTableModel gradeModel = (DefaultTableModel) gradeTable.getModel();
            JPanel statsPanel = (JPanel) gradePanel.getComponent(0);
            JLabel totalCreditsLabel = (JLabel) statsPanel.getComponent(0);
            JLabel avgScoreLabel = (JLabel) statsPanel.getComponent(2);
            loadGrades(gradeModel, totalCreditsLabel, avgScoreLabel);
            LogUtil.info("已刷新成绩表格，行数：" + gradeModel.getRowCount());
            
            // 强制更新UI
            tabbedPane.revalidate();
            tabbedPane.repaint();
        } catch (Exception e) {
            LogUtil.error("刷新表格时发生错误", e);
        }
    }
}

// 按钮渲染器
class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}

// 按钮编辑器
class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private boolean isPushed;
    private ButtonClickListener clickListener;
    private int currentRow;
    private Object currentValue;
    
    public ButtonEditor(JCheckBox checkBox, String label, ButtonClickListener listener) {
        super(checkBox);
        this.clickListener = listener;
        button = new JButton(label);
        button.setOpaque(true);
        
        // 只在按钮点击时传递当前值，不传递行索引
        button.addActionListener(e -> {
            LogUtil.info("按钮被点击：" + button.getText());
            fireEditingStopped();
        });
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        // 保存当前行和值
        currentRow = row;
        currentValue = table.getValueAt(row, 0);  // 保存课程ID
        
        if (value == null) {
            button.setText("");
        } else {
            button.setText(value.toString());
        }
        isPushed = true;
        LogUtil.info("获取单元格编辑器：row=" + row + ", courseId=" + currentValue);
        return button;
    }
    
    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            try {
                // 使用保存的courseId而不是行索引
                LogUtil.info("触发按钮事件，使用courseId: " + currentValue);
                if (currentValue != null) {
                    clickListener.onClick(currentRow, currentValue.toString());
                } else {
                    LogUtil.error("按钮点击错误：课程ID为空", new Exception("课程ID为空"));
                }
            } catch (Exception e) {
                LogUtil.error("按钮点击事件处理异常", e);
            }
        }
        isPushed = false;
        return button.getText();
    }
    
    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
}

@FunctionalInterface
interface ButtonClickListener {
    void onClick(int row, String id);
} 