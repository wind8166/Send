package Send_Mail;

//SQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
//Timer
import java.util.TimerTask;
//Mail
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

public class DataHandle {
	private Connection con = null; // Database objects
	private Statement stat, stat_new, stat_cus = null;
	private ResultSet rs_new, rs_old, rs_cus = null;
	// private PreparedStatement pst = null;
	private String selectSQL_cus = "select * from cust_data ";
	private String selectSQL_new = "select * from check_data ";
	private String selectSQL_old = "select * from tb_terminal ";

	public DataHandle() {
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

	public void Close() {
		try {
			if (rs_new != null) {
				rs_new.close();
				rs_new = null;
			}
			if (rs_old != null) {
				rs_old.close();
				rs_old = null;
			}
			if (rs_cus != null) {
				rs_cus.close();
				rs_cus = null;
			}
			if (stat != null) {
				stat.close();
				stat = null;
			}
			if (stat_new != null) {
				stat_new.close();
				stat_new = null;
			}
			if (stat_cus != null) {
				stat_cus.close();
				stat_cus = null;
			}
		} catch (SQLException e) {
			System.out.println("Close Exception :" + e.toString());
		}
	}

	public void CreateTable() {
		try {
			con = DriverManager.getConnection("jdbc:mysql://localhost/gnamp_v4", "root", "");
			stat = con.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS Cust_data (" + " ID     VARCHAR(20) "
					+ ", email     VARCHAR(50)" + ",Time     VARCHAR(50))");
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS Check_data (" + " CSTM_ID     VARCHAR(20) "
					+ " , DEV_ID     VARCHAR(20) " + " , Name     CHAR(20) " + " , OnState     INT(2) "
					+ " , Count     INT(5)) ");
		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}

	public void SelectTable() {
		try {
			stat = con.createStatement();
			stat_new = con.createStatement();
			stat_cus = con.createStatement();

			rs_cus = stat_cus.executeQuery(selectSQL_cus);
			while (rs_cus.next()) {
				String Cust_ID = rs_cus.getString("ID");
				String Cust_Email = rs_cus.getString("email");
				// selectSQL_old=selectSQL_old+" Where CSTM_ID=" + Cust_ID;
				// System.out.println(selectSQL_old +" Where CSTM_ID=" +
				// Cust_ID);
				rs_old = stat.executeQuery(selectSQL_old + " Where CSTM_ID=" + Cust_ID);
				while (rs_old.next()) {

					// 最外層加入判斷客戶(或者直接寫哪間客戶)
					String Dev_old = rs_old.getString("DEV_ID");
					String Name_old = rs_old.getString("Name");
					// System.out.println("Cust_ID="+Cust_ID+"
					// Name_old="+Name_old );
					rs_new = stat_new.executeQuery(selectSQL_new);
					if (rs_old.getInt("ONLINE_STATE") == 0) {
						int check = 0;
						while (rs_new.next()) {
							String Dev_new = rs_new.getString("DEV_ID");
							if (Dev_old.equals(Dev_new)) {
								check = 1;
								break;
							} else {
								check = 0;
							}
						}
						if (check == 0) {
							// System.out.println("需新增一筆資料");
							String qry = "INSERT INTO `check_data`(CSTM_ID,DEV_ID,Name,OnState,Count) VALUES ('"
									+ rs_old.getString("Cstm_ID") + "','" + rs_old.getString("Dev_ID") + "','"
									+ rs_old.getString("Name") + "','" + rs_old.getString("ONLINE_STATE") + "','" + "1"
									+ "')";
							stat_new.executeUpdate(qry);
						} else {
							// System.out.println("僅需在count+1");
							int count = rs_new.getInt("Count");
							// int time = rs_cus.getInt("Time");
							// 查詢使用者需要時間，目前都預設15次每次1分鐘
							int time = 15;
							if (count < time - 1) {
								count++;
								String qry = "UPDATE `check_data` SET Count= " + count + " Where Dev_ID=" + Dev_old;
								stat_new.executeUpdate(qry);
							} else {
								// 不要歸零直接刪除該資料
								count = 0;
								System.out.println("該" + Name_old + "需寄送通知信!!");
								String qry = "DELETE From `check_data` Where Dev_ID=" + Dev_old;
								stat_new.executeUpdate(qry);
								DataHandle test = new DataHandle();
								test.mail(Name_old, Dev_old, Cust_Email);
							}
						}
					} else {
						// Online_State重新上線的時候該如何處理(移除該項資料，當作重未斷線)
						// System.out.println("該"+Dev_old+"已重新上線!!");
						String qry = "DELETE From `check_data` Where Dev_ID=" + Dev_old;
						stat_new.executeUpdate(qry);
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Close();
		}
	}

	public void InsertTable_Customer(String ID, String Email) {
		try {
			stat_cus = con.createStatement();
			rs_cus = stat_cus.executeQuery(selectSQL_cus + "Where ID=" + ID);
			rs_cus.last();
			int numberOfColumns = rs_cus.getRow();
			if (numberOfColumns == 0) {
				String qry = "INSERT INTO `cust_data`(ID,Email,time) VALUES ('" + ID + "','" + Email + "','15')";
				stat_cus.executeUpdate(qry);
				JOptionPane.showMessageDialog(null, "已成功新增該客戶!!");
			} else {
				JOptionPane.showMessageDialog(null, "資料庫已有該客戶，請勿重複寫入!!");
			}

		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}

	public void Delete_Customer(String ID) {
		try {
			//移除Check_data
			stat_new=con.createStatement();
			stat_new.executeUpdate("DELETE From `check_data` Where CSTM_ID=" + ID);
			//移除Cus_data
			stat_cus = con.createStatement();
			String qry_delete = "DELETE From `cust_data` Where ID=" + ID;
			stat_cus.executeUpdate(qry_delete);
			JOptionPane.showMessageDialog(null, "已移除該客戶，請重新整理");
		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}
	// 寄信基礎
	public void mail(String Dev_Name, String Dev_ID, String Cus_Mail) throws AddressException, MessagingException {

		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.auth", "true");
		final String username = "service@radi.com.tw";
		final String password = "@rd80265896#";
		Session session = Session.getDefaultInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		try {
			// 延遲2秒鐘
			Thread.currentThread();
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(username));
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Cus_Mail, false));
		// 10進制轉換16進制
		Long intValue = Long.valueOf(Dev_ID);
		String New_Dev_ID = Long.toHexString(intValue);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		// System.out.println("10進位:" + intValue + " 轉16進位=" + New_Dev_ID);
		msg.setSubject(Dev_Name + "已斷線超過15分鐘!");
		String Txt = " 機器名稱 " + Dev_Name + "，機器ID為 " + New_Dev_ID + "\n目前已於 " + sdf.format(new Date()) + " 連續斷線超過15分鐘。";
		msg.setText(Txt);
		msg.setSentDate(new Date());
		Transport.send(msg);
		System.out.println("Message sent.  " + new Date());
	}
}

class MyTask extends TimerTask {
	@Override
	public void run() {
		DataHandle test = new DataHandle();
		test.SelectTable();
		System.out.println(new Date()); // 輸出時間

	}
}
