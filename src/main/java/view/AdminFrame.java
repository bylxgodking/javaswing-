package view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import util.DatabaseUtil;

public class AdminFrame extends JFrame {
    private String adminId;
    private JTabbedPane tabbedPane;
    
    public AdminFrame(String adminId) {
        this.adminId = adminId;
        initUI();
    }
    
    private void initUI() {
        setTitle("管理员系统");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        
        // 添加课程管理面板
        tabbedPane.addTab("课程管理", createCoursePanel());
        // 添加教师管理面板
        tabbedPane.addTab("教师管理", createTeacherPanel());
        // 添加选课统计面板
        tabbedPane.addTab("选课统计", createStatisticsPanel());
        
        add(tabbedPane);
    }
    
    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建表格模型
        String[] columns = {"课程ID", "课程名称", "教师ID", "学分", "最大人数", "当前人数"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        
        // 添加工具栏
        JToolBar toolBar = new JToolBar();
        JButton addBtn = new JButton("添加课程");
        JButton editBtn = new JButton("编辑课程");
        JButton deleteBtn = new JButton("删除课程");
        
        toolBar.add(addBtn);
        toolBar.add(editBtn);
        toolBar.add(deleteBtn);
        
        // 添加事件监听
        addBtn.addActionListener(e -> showAddCourseDialog());
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                showEditCourseDialog(table.getValueAt(row, 0).toString());
            }
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                deleteCourse(table.getValueAt(row, 0).toString());
            }
        });
        
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // 加载课程数据
        loadCourseData(model);
        
        return panel;
    }
    
    private void loadCourseData(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT * FROM courses";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getString("teacher_id"),
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
    
    private void showAddCourseDialog() {
        JDialog dialog = new JDialog(this, "添加课程", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        
        JTextField courseIdField = new JTextField();
        JTextField courseNameField = new JTextField();
        JTextField teacherIdField = new JTextField();
        JTextField creditField = new JTextField();
        JTextField maxStudentsField = new JTextField();
        
        panel.add(new JLabel("课程ID:"));
        panel.add(courseIdField);
        panel.add(new JLabel("课程名称:"));
        panel.add(courseNameField);
        panel.add(new JLabel("教师ID:"));
        panel.add(teacherIdField);
        panel.add(new JLabel("学分:"));
        panel.add(creditField);
        panel.add(new JLabel("最大人数:"));
        panel.add(maxStudentsField);
        
        JButton confirmBtn = new JButton("确认");
        JButton cancelBtn = new JButton("取消");
        
        confirmBtn.addActionListener(e -> {
            try {
                String courseId = courseIdField.getText().trim();
                String courseName = courseNameField.getText().trim();
                String teacherId = teacherIdField.getText().trim();
                
                if (courseId.isEmpty() || courseName.isEmpty() || teacherId.isEmpty() ||
                    creditField.getText().isEmpty() || maxStudentsField.getText().isEmpty()) {
                    throw new IllegalArgumentException("所有字段都必须填写！");
                }
                
                int credit = Integer.parseInt(creditField.getText().trim());
                int maxStudents = Integer.parseInt(maxStudentsField.getText().trim());
                
                if (credit <= 0 || maxStudents <= 0) {
                    throw new IllegalArgumentException("学分和最大人数必须大于0！");
                }
                
                addCourse(courseId, courseName, teacherId, credit, maxStudents);
                dialog.dispose();
                refreshCourseTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "学分和最大人数必须是数字！");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        panel.add(confirmBtn);
        panel.add(cancelBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void addCourse(String courseId, String courseName, String teacherId, 
                          int credit, int maxStudents) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 首先检查教师是否存在
                String checkSql = "SELECT COUNT(*) FROM teachers WHERE teacher_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, teacherId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    throw new IllegalArgumentException("教师ID不存在，请先添加教师！");
                }
                
                // 添加课程
                String sql = "INSERT INTO courses (course_id, course_name, teacher_id, credit, max_students) " +
                            "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, courseId);
                pstmt.setString(2, courseName);
                pstmt.setString(3, teacherId);
                pstmt.setInt(4, credit);
                pstmt.setInt(5, maxStudents);
                pstmt.executeUpdate();
                
                conn.commit();
                JOptionPane.showMessageDialog(this, "添加课程成功！");
            } catch (SQLException | IllegalArgumentException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }
    
    private void showEditCourseDialog(String courseId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT * FROM courses WHERE course_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog(this, "编辑课程", true);
                dialog.setSize(400, 300);
                dialog.setLocationRelativeTo(this);
                
                JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
                
                JTextField courseNameField = new JTextField(rs.getString("course_name"));
                JTextField teacherIdField = new JTextField(rs.getString("teacher_id"));
                JTextField creditField = new JTextField(String.valueOf(rs.getInt("credit")));
                JTextField maxStudentsField = new JTextField(String.valueOf(rs.getInt("max_students")));
                
                panel.add(new JLabel("课程ID:"));
                panel.add(new JLabel(courseId));
                panel.add(new JLabel("课程名称:"));
                panel.add(courseNameField);
                panel.add(new JLabel("教师ID:"));
                panel.add(teacherIdField);
                panel.add(new JLabel("学分:"));
                panel.add(creditField);
                panel.add(new JLabel("最大人数:"));
                panel.add(maxStudentsField);
                
                JButton confirmBtn = new JButton("确认");
                JButton cancelBtn = new JButton("取消");
                
                confirmBtn.addActionListener(e -> {
                    try {
                        updateCourse(courseId, 
                                   courseNameField.getText().trim(),
                                   teacherIdField.getText().trim(),
                                   Integer.parseInt(creditField.getText().trim()),
                                   Integer.parseInt(maxStudentsField.getText().trim()));
                        dialog.dispose();
                        refreshCourseTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "更新失败：" + ex.getMessage());
                    }
                });
                
                cancelBtn.addActionListener(e -> dialog.dispose());
                
                panel.add(confirmBtn);
                panel.add(cancelBtn);
                
                dialog.add(panel);
                dialog.setVisible(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载课程数据失败：" + ex.getMessage());
        }
    }
    
    private void updateCourse(String courseId, String courseName, String teacherId, 
                            int credit, int maxStudents) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE courses SET course_name = ?, teacher_id = ?, " +
                        "credit = ?, max_students = ? WHERE course_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, courseName);
            pstmt.setString(2, teacherId);
            pstmt.setInt(3, credit);
            pstmt.setInt(4, maxStudents);
            pstmt.setString(5, courseId);
            pstmt.executeUpdate();
        }
    }
    
    private void deleteCourse(String courseId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除这门课程吗？这将同时删除所有相关的选课记录！",
            "确认删除",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // 首先删除选课记录
                    String deleteSql = "DELETE FROM course_selection WHERE course_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(deleteSql);
                    pstmt.setString(1, courseId);
                    pstmt.executeUpdate();
                    
                    // 然后删除课程
                    deleteSql = "DELETE FROM courses WHERE course_id = ?";
                    pstmt = conn.prepareStatement(deleteSql);
                    pstmt.setString(1, courseId);
                    pstmt.executeUpdate();
                    
                    conn.commit();
                    refreshCourseTable();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "删除失败：" + ex.getMessage());
            }
        }
    }
    
    private void refreshCourseTable() {
        DefaultTableModel model = (DefaultTableModel) ((JTable) ((JScrollPane) 
            ((JPanel) tabbedPane.getComponentAt(0)).getComponent(1)).getViewport().getView()).getModel();
        loadCourseData(model);
    }
    
    private JPanel createTeacherPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建表格模型
        String[] columns = {"教师ID", "姓名", "所属院系", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        
        // 添加工具栏
        JToolBar toolBar = new JToolBar();
        JButton addBtn = new JButton("添加教师");
        JButton editBtn = new JButton("编辑教师");
        JButton deleteBtn = new JButton("删除教师");
        
        toolBar.add(addBtn);
        toolBar.add(editBtn);
        toolBar.add(deleteBtn);
        
        // 添加事件监听
        addBtn.addActionListener(e -> showAddTeacherDialog());
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                showEditTeacherDialog(table.getValueAt(row, 0).toString());
            }
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                deleteTeacher(table.getValueAt(row, 0).toString());
            }
        });
        
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // 加载教师数据
        loadTeacherData(model);
        
        return panel;
    }
    
    private void loadTeacherData(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT t.*, u.username FROM teachers t " +
                        "JOIN users u ON t.teacher_id = u.user_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("teacher_id"),
                    rs.getString("name"),
                    rs.getString("department"),
                    "编辑"
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载教师数据失败：" + ex.getMessage());
        }
    }
    
    private void showAddTeacherDialog() {
        JDialog dialog = new JDialog(this, "添加教师", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField teacherIdField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField departmentField = new JTextField();
        
        panel.add(new JLabel("教师ID:"));
        panel.add(teacherIdField);
        panel.add(new JLabel("姓名:"));
        panel.add(nameField);
        panel.add(new JLabel("所属院系:"));
        panel.add(departmentField);
        
        JButton confirmBtn = new JButton("确认");
        JButton cancelBtn = new JButton("取消");
        
        confirmBtn.addActionListener(e -> {
            try {
                String teacherId = teacherIdField.getText().trim();
                String name = nameField.getText().trim();
                String department = departmentField.getText().trim();
                
                if (teacherId.isEmpty() || name.isEmpty() || department.isEmpty()) {
                    throw new IllegalArgumentException("所有字段都必须填写！");
                }
                
                addTeacher(teacherId, name, department);
                dialog.dispose();
                refreshTeacherTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        panel.add(confirmBtn);
        panel.add(cancelBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void addTeacher(String teacherId, String name, String department) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 添加用户账号
                String sql = "INSERT INTO users (user_id, username, password, role) VALUES (?, ?, ?, 'teacher')";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, teacherId);
                pstmt.setString(2, teacherId); // 用教师ID作为用户名
                pstmt.setString(3, teacherId); // 用教师ID作为初始密码
                pstmt.executeUpdate();
                
                // 添加教师信息
                sql = "INSERT INTO teachers (teacher_id, name, department) VALUES (?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, teacherId);
                pstmt.setString(2, name);
                pstmt.setString(3, department);
                pstmt.executeUpdate();
                
                conn.commit();
                JOptionPane.showMessageDialog(this, "添加教师成功！\n初始密码为教师ID");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }
    
    private void deleteTeacher(String teacherId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除这位教师吗？这将同时删除相关的课程和选课记录！",
            "确认删除",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // 首先删除选课记录
                    String sql = "DELETE FROM course_selection WHERE course_id IN " +
                               "(SELECT course_id FROM courses WHERE teacher_id = ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, teacherId);
                    pstmt.executeUpdate();
                    
                    // 删除课程
                    sql = "DELETE FROM courses WHERE teacher_id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, teacherId);
                    pstmt.executeUpdate();
                    
                    // 删除教师信息
                    sql = "DELETE FROM teachers WHERE teacher_id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, teacherId);
                    pstmt.executeUpdate();
                    
                    // 删除用户账号
                    sql = "DELETE FROM users WHERE user_id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, teacherId);
                    pstmt.executeUpdate();
                    
                    conn.commit();
                    refreshTeacherTable();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "删除教师失败：" + ex.getMessage());
            }
        }
    }
    
    private void refreshTeacherTable() {
        DefaultTableModel model = (DefaultTableModel) ((JTable) ((JScrollPane) 
            ((JPanel) tabbedPane.getComponentAt(1)).getComponent(1)).getViewport().getView()).getModel();
        loadTeacherData(model);
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建表格模型
        String[] columns = {"课程ID", "课程名称", "选课人数", "平均分"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        
        // 加载统计数据
        loadStatisticsData(model);
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
    
    private void loadStatisticsData(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT c.course_id, c.course_name, " +
                        "COUNT(cs.student_id) as student_count, " +
                        "AVG(cs.score) as avg_score " +
                        "FROM courses c " +
                        "LEFT JOIN course_selection cs ON c.course_id = cs.course_id " +
                        "GROUP BY c.course_id, c.course_name";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getInt("student_count"),
                    rs.getDouble("avg_score")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载统计数据失败：" + ex.getMessage());
        }
    }
    
    private void showEditTeacherDialog(String teacherId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // 获取教师信息
            String sql = "SELECT t.*, u.username FROM teachers t " +
                        "JOIN users u ON t.teacher_id = u.user_id " +
                        "WHERE t.teacher_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog(this, "编辑教师", true);
                dialog.setSize(400, 300);
                dialog.setLocationRelativeTo(this);
                
                JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
                
                JTextField teacherIdField = new JTextField(teacherId);
                teacherIdField.setEditable(false); // 教师ID不允许修改
                JTextField nameField = new JTextField(rs.getString("name"));
                JTextField departmentField = new JTextField(rs.getString("department"));
                
                panel.add(new JLabel("教师ID:"));
                panel.add(teacherIdField);
                panel.add(new JLabel("姓名:"));
                panel.add(nameField);
                panel.add(new JLabel("所属院系:"));
                panel.add(departmentField);
                
                JButton confirmBtn = new JButton("确认");
                JButton cancelBtn = new JButton("取消");
                
                confirmBtn.addActionListener(e -> {
                    try {
                        String name = nameField.getText().trim();
                        String department = departmentField.getText().trim();
                        
                        if (name.isEmpty() || department.isEmpty()) {
                            throw new IllegalArgumentException("所有字段都必须填写！");
                        }
                        
                        updateTeacher(teacherId, name, department);
                        dialog.dispose();
                        refreshTeacherTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, ex.getMessage());
                    }
                });
                
                cancelBtn.addActionListener(e -> dialog.dispose());
                
                panel.add(confirmBtn);
                panel.add(cancelBtn);
                
                dialog.add(panel);
                dialog.setVisible(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "获取教师信息失败：" + ex.getMessage());
        }
    }
    
    private void updateTeacher(String teacherId, String name, String department) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE teachers SET name = ?, department = ? WHERE teacher_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, department);
            pstmt.setString(3, teacherId);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "更新教师信息成功！");
        }
    }
} 