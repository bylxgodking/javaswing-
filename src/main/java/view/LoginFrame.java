package view;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import util.DatabaseUtil;

public class LoginFrame extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    public LoginFrame() {
        setTitle("选课管理系统登录");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建面板
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        
        // 添加组件
        panel.add(new JLabel("用户ID:"));
        userIdField = new JTextField();
        panel.add(userIdField);

        panel.add(new JLabel("密码:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("角色:"));
        String[] roles = {"学生", "教师", "管理员"};
        roleComboBox = new JComboBox<>(roles);
        panel.add(roleComboBox);

        JButton loginButton = new JButton("登录");
        panel.add(loginButton);

        // 添加登录事件
        loginButton.addActionListener(e -> login());

        add(panel);
    }

    private void login() {
        String userId = userIdField.getText();
        String password = new String(passwordField.getPassword());
        String role;
        
        // 替换 switch 表达式为传统 switch 语句
        switch(roleComboBox.getSelectedIndex()) {
            case 0:
                role = "student";
                break;
            case 1:
                role = "teacher";
                break;
            case 2:
                role = "admin";
                break;
            default:
                role = "";
                break;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT * FROM users WHERE user_id = ? AND password = ? AND role = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "登录成功！");
                openMainFrame(userId, role);
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "登录失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openMainFrame(String userId, String role) {
        this.dispose(); // 关闭登录窗口
        switch (role) {
            case "admin":
                new AdminFrame(userId).setVisible(true);
                break;
            case "teacher":
                new TeacherFrame(userId).setVisible(true);
                break;
            case "student":
                new StudentFrame(userId).setVisible(true);
                break;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
} 