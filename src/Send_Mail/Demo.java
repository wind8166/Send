package Send_Mail;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;

//SQL
import java.sql.Connection;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class Demo {

	private JFrame frmgui;
	private JTextField textName;
	private JTextField textEmail;
	private JLabel lbl_Cust_id;
	private JLabel lbl_Cust_Email;
	private JButton btn_input;
	private Connection con = null; // Database objects
	private Statement stat = null;
	private ResultSet rs = null;

	DataHandle test = new DataHandle();

	TimerTask Test = new MyTask();
	Timer timer = new Timer();
	/**
	 * Launch the application.
	 */
	private String selectSQL_cus = "select * from cust_data ";
	private JButton btn_delete;
	private JTable table;
	private JScrollPane scrollPane;

	public static void main(String[] args) {
		DataHandle test = new DataHandle();
		test.CreateTable();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Demo window = new Demo();
					window.frmgui.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	public Demo() {
		initialize();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost/gnamp_v4", "root", "");
			stat = con.createStatement();
		} catch (ClassNotFoundException e) {
			System.out.println("DriverClassNotFound :" + e.toString());
		} catch (SQLException x) {
			System.out.println("Exception :" + x.toString());

		} finally {
			Close();
		}
	}

	private void initialize() {
		frmgui = new JFrame();
		frmgui.setTitle("\u65B7\u7DDA\u81EA\u52D5\u5BC4\u4FE1\u7CFB\u7D71");
		frmgui.setBounds(100, 100, 600, 400);
		frmgui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmgui.getContentPane().setLayout(null);
		frmgui.setLocationRelativeTo(null);

		JButton btnStart = new JButton("\u7CFB\u7D71\u555F\u7528");
		JButton btnStop = new JButton("\u7CFB\u7D71\u505C\u7528");
		JButton btn_reload = new JButton("\u91CD\u65B0\u6574\u7406");
		btnStart.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "自動偵測系統開啟");
				test.CreateTable();
				timer.schedule(Test, 60, 60000);
				btnStart.setEnabled(false);
				btn_delete.setEnabled(false);
				btn_input.setEnabled(false);
				btnStop.setEnabled(true);
				btn_reload.setEnabled(false);
			}
		});
		btnStart.setToolTipText("start");
		btnStart.setBounds(450, 30, 100, 30);
		frmgui.getContentPane().add(btnStart);
		btnStop.setEnabled(false);
		btnStop.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "自動偵測系統關閉");
				// System.out.println("該功能目前採用重啟的方式，待更新!!");
				timer.cancel();
				Demo new_window = new Demo();
				new_window.frmgui.setVisible(true);
				frmgui.dispose();
			}
		});
		btnStop.setToolTipText("stop");
		btnStop.setBounds(450, 70, 100, 30);
		frmgui.getContentPane().add(btnStop);

		textName = new JTextField();
		textName.setBounds(100, 35, 120, 20);
		frmgui.getContentPane().add(textName);
		textName.setColumns(10);

		textEmail = new JTextField();
		textEmail.setBounds(100, 75, 200, 20);
		frmgui.getContentPane().add(textEmail);
		textEmail.setColumns(10);

		lbl_Cust_id = new JLabel("\u5BA2\u6236\u7DE8\u865F :");
		lbl_Cust_id.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
		lbl_Cust_id.setBounds(10, 35, 90, 20);
		frmgui.getContentPane().add(lbl_Cust_id);

		lbl_Cust_Email = new JLabel("\u96FB\u5B50\u4FE1\u7BB1 : ");
		lbl_Cust_Email.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
		lbl_Cust_Email.setBounds(10, 75, 90, 20);
		frmgui.getContentPane().add(lbl_Cust_Email);

		btn_input = new JButton("\u65B0\u589E\u8CC7\u6599");
		btn_input.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String Email = textEmail.getText().toString();
				String ID = textName.getText().toString();
				if (!Email.equals("") && !ID.equals("")) {
					test.InsertTable_Customer(ID, Email);
					Demo new_window = new Demo();
					new_window.frmgui.setVisible(true);
					frmgui.dispose();
				} else {
					JOptionPane.showMessageDialog(null, "客戶名稱或電子信箱未完整輸入，請重試...");
				}
			}
		});
		btn_input.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
		btn_input.setToolTipText("input");
		btn_input.setBounds(325, 30, 100, 30);
		frmgui.getContentPane().add(btn_input);
		// 重新整理按鈕

		btn_reload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Demo new_window = new Demo();
				new_window.frmgui.setVisible(true);
				frmgui.dispose();
			}
		});
		btn_reload.setToolTipText("Reload");
		btn_reload.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
		btn_reload.setBounds(450, 110, 100, 30);
		frmgui.getContentPane().add(btn_reload);
		// 刪除按鈕
		btn_delete = new JButton("\u522A\u9664\u8CC7\u6599");
		btn_delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String ID = textName.getText().toString();
				if (ID.equals("")) {
					JOptionPane.showMessageDialog(null, "未輸入客戶名稱，無法刪除!!");
				} else {
					test.Delete_Customer(ID);
					Demo new_window = new Demo();
					new_window.frmgui.setVisible(true);
					frmgui.dispose();
				}
			}
		});
		btn_delete.setToolTipText("delete");
		btn_delete.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
		btn_delete.setBounds(325, 70, 100, 30);
		frmgui.getContentPane().add(btn_delete);

		try {
			con = DriverManager.getConnection("jdbc:mysql://localhost/gnamp_v4", "root", "");
			stat = con.createStatement();
			rs = stat.executeQuery(selectSQL_cus);
			ResultSetMetaData md = rs.getMetaData();
			int columnCount = md.getColumnCount();
			Vector<String> columns = new Vector<String>(columnCount);

			// store column names
			for (int i = 1; i <= columnCount; i++)
				columns.add(md.getColumnName(i));
			Vector<Vector<String>> data = new Vector<Vector<String>>();
			Vector<String> row;

			// store row data
			while (rs.next()) {
				row = new Vector<String>(columnCount);
				for (int i = 1; i <= columnCount; i++) {
					row.add(rs.getString(i));
				}
				data.add(row);
			}

			scrollPane = new JScrollPane();
			scrollPane.setBounds(10, 140, 420, 200);
			frmgui.getContentPane().add(scrollPane);
			table = new JTable(data, columns);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
			scrollPane.setViewportView(table);

			JLabel lbl_Time = new JLabel("");
			lbl_Time.setBounds(414, 341, 170, 20);
			frmgui.getContentPane().add(lbl_Time);
			
			JButton btn_set = new JButton("\u8A2D\u5B9A");
			btn_set.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
				}
			});
			btn_set.setToolTipText("Reload");
			btn_set.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
			btn_set.setBounds(470, 310,80, 30);
			frmgui.getContentPane().add(btn_set);
		} catch (SQLException sqle) {
			System.out.println(sqle);
		} finally {
			Close();
		}
	}

	public void RefreshTable() {
		// 最笨的方法整個表單重新刷新
		Demo new_window = new Demo();
		new_window.frmgui.setVisible(true);
		frmgui.dispose();
	}

	public void Close() {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stat != null) {
				stat.close();
				stat = null;
			}
		} catch (SQLException e) {
			System.out.println("Close Exception :" + e.toString());
		}
	}

	// 讀出資料
	public void SelectTable() {
		try {
			stat = con.createStatement();
			rs = stat.executeQuery(selectSQL_cus);
			while (rs.next()) {
				String Cust_ID = rs.getString("ID");
				String Cust_Email = rs.getString("email");
				// selectSQL_old=selectSQL_old+" Where CSTM_ID=" + Cust_ID;
				System.out.println("Cust_ID=" + Cust_ID + " Cust_Email=" + Cust_Email);
			}
			System.out.println("該次查詢已結束");
		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}
}