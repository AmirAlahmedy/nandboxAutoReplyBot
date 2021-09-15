
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
public class Database {
	
	public Connection connection = null;
	

	public Database(String dbName) throws SQLException
	{
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+dbName+".db");
		this.connection = connection;
	}
	
	public void createTables() throws SQLException
	{
		String sql = "CREATE TABLE IF NOT EXISTS messages (\n"
                + "	chatId varchar(255),\n"
                + "	message varchar(255) NOT NULL,\n"
                + "	PRIMARY KEY(chatId)\n"
                + ");";
		Statement s = this.connection.createStatement();
		s.execute(sql);
		
		sql = "CREATE TABLE IF NOT EXISTS Users (\n"
                + "	chatId varchar(255),\n"
                + "	userId varchar(255) NOT NULL,\n"
                + "	PRIMARY KEY(chatId,userId)\n"
                + ");";
		s = this.connection.createStatement();
		s.execute(sql);
	}
	
	public void createUsersTable() throws SQLException
	{
		String sql = "CREATE TABLE IF NOT EXISTS Users (\n"
                + "	chatId varchar(255),\n"
                + "	userId varchar(255) NOT NULL,\n"
                + "	PRIMARY KEY(chatId,userId)\n"
                + ");";
		Statement s = this.connection.createStatement();
		s.execute(sql);
	}
	
	
	
	
	public void insertMessage(String chatId,String message) throws SQLException
	{
		String sql = "insert into messages values (?,?)";
		
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
        pstmt.setString(1, chatId);
        pstmt.setString(2, message);
        pstmt.executeUpdate();
	}
	
	public void updateMessage(String chatId,String message) throws SQLException
	{
		String sql = "Update messages Set message = ? where chatId = ?";
		
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
        pstmt.setString(1, message);
        pstmt.setString(2, chatId);
        pstmt.executeUpdate();
	}
	
	public  boolean isSet(String chatId) throws SQLException
	{
		String sql = "Select message from messages where chatId = ?";
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
		pstmt.setString(1, chatId);
        
        ResultSet rs = pstmt.executeQuery();
       
       while (rs.next()) {
    	   return true;
       }
       return false;
	}
	
	public void insertUser(String chatId,String userId) throws SQLException
	{
		String sql = "insert into users values (?,?)";
		
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
        pstmt.setString(1, chatId);
        pstmt.setString(2, userId);
        pstmt.executeUpdate();
	}

	public boolean userExists(String chatId, String userId) throws SQLException
	{
		String sql = "Select chatId from Users where chatId = ? and userId = ?";
		
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
		pstmt.setString(1, chatId);
		pstmt.setString(2, userId);
		pstmt.executeQuery();
		
		ResultSet rs = pstmt.executeQuery();
		
		while(rs.next())
		{
			return true;
		}
		return false;
	}
	
	public void clearUsers(String chatId) throws SQLException
	{
		 String sql = "DELETE FROM Users WHERE chatId = ?";
			
			PreparedStatement pstmt = this.connection.prepareStatement(sql);
	        pstmt.setString(1, chatId);
	        pstmt.executeUpdate();
	}
	
	public ArrayList<String> getMessage(String chatId) throws SQLException
	{
		String sql = "SELECT message from messages where chatId = ?";
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
		pstmt.setString(1, chatId);
		
		ResultSet rs = pstmt.executeQuery();
		
		ArrayList<String> message = new ArrayList<String>();
		while(rs.next())
		{
			message.add(rs.getString("message"));
			return message;
		}
		
		return message;
	}
	
}

