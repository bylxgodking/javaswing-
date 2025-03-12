package view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import util.DatabaseUtil;

public class TeacherFrame extends JFrame {
    private String teacherId;
    private JTabbedPane tabbedPane;
    
    public TeacherFrame(String teacherId) {
        this.teacherId = teacherId;
        initUI();
    }
    
    private void initUI() {
        setTitle("教师系统 - " + teacherId);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        
        // 添加我的课程面板
        tabbedPane.addTab("我的课程", createMyCoursePanel());
        // 添加成绩管理面板
        tabbedPane.addTab("成绩管理", createGradePanel());
        
        add(tabbedPane);
    }
    
    private JPanel createMyCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建表格模型
        String[] columns = {"课程ID", "课程名称", "学分", "最大人数", "当前人数"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        
        // 加载课程数据
        loadMyCourseData(model);
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
    
    private void loadMyCourseData(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT * FROM courses WHERE teacher_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getInt("credit"),
                    rs.getInt("max_students"),
                    rs.getInt("current_students")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载课程数据失败：" + ex.getMessage());
        }
    }
    
    private JPanel createGradePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 上部面板：课程选择
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> courseComboBox = new JComboBox<>();
        topPanel.add(new JLabel("选择课程："));
        topPanel.add(courseComboBox);
        
        // 中部面板：学生成绩表格
        String[] columns = {"学生ID", "学生姓名", "成绩"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // 只允许编辑成绩列
            }
        };
        JTable table = new JTable(model);
        
        // 添加成绩更新监听器
        table.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 2) { // 成绩列
                int row = e.getFirstRow();
                String studentId = (String) table.getValueAt(row, 0);
                String courseId = (String) courseComboBox.getSelectedItem();
                String scoreStr = (String) table.getValueAt(row, 2);
                try {
                    double score = Double.parseDouble(scoreStr);
                    updateStudentScore(studentId, courseId, score);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "成绩必须是数字！");
                    loadStudentGrades(model, courseId);
                }
            }
        });
        
        // 课程选择变化时更新学生列表
        courseComboBox.addActionListener(e -> {
            String selectedCourse = (String) courseComboBox.getSelectedItem();
            if (selectedCourse != null) {
                loadStudentGrades(model, selectedCourse);
            }
        });
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // 加载课程列表并自动选择第一个课程
        loadCourseList(courseComboBox, model);
        
        return panel;
    }
    
    private void loadCourseList(JComboBox<String> courseComboBox, DefaultTableModel gradeModel) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT course_id FROM courses WHERE teacher_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            courseComboBox.removeAllItems();
            boolean hasItems = false;
            
            while (rs.next()) {
                String courseId = rs.getString("course_id");
                courseComboBox.addItem(courseId);
                hasItems = true;
            }
            
            // 如果有课程，自动加载第一个课程的学生成绩
            if (hasItems) {
                String firstCourse = (String) courseComboBox.getItemAt(0);
                courseComboBox.setSelectedItem(firstCourse);
                loadStudentGrades(gradeModel, firstCourse);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载课程列表失败：" + ex.getMessage());
        }
    }
    
    private void loadStudentGrades(DefaultTableModel model, String courseId) {
        model.setRowCount(0);
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT s.student_id, s.name, cs.score " +
                        "FROM students s " +
                        "JOIN course_selection cs ON s.student_id = cs.student_id " +
                        "WHERE cs.course_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("score")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载学生成绩数据失败：" + ex.getMessage());
        }
    }
    
    private void updateStudentScore(String studentId, String courseId, double score) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE course_selection SET score = ? " +
                        "WHERE student_id = ? AND course_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, score);
            pstmt.setString(2, studentId);
            pstmt.setString(3, courseId);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "更新成绩失败：" + ex.getMessage());
        }
    }
} 