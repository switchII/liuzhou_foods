package com.capinfo.gzzfzy.api.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenEntityOracle {

	private String packageOutPath = "com.capinfo.gzzfzy.api.domain";// ָ��ʵ���������ڰ���·��
	private String authorName = "LuoAnDong";// ��������
	private String tablename ;// ����
	private String[] colnames; // ��������
	private String[] colTypes; // ������������
	private int[] colSizes; // ������С����
	private boolean f_util = false; // �Ƿ���Ҫ�����java.util.*
	private boolean f_sql = false; // �Ƿ���Ҫ�����java.sql.*

	// ���ݿ�����
	private static final String URL = "jdbc:oracle:thin:@127.0.0.1:1521:oracle";
	private static final String NAME = "gzzjjs";
	private static final String PASS = "gzzjjs";
	private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";

	/*
	 * ���캯��
	 */
	public GenEntityOracle(String tablename) {
		this.tablename = tablename ; 
		// ��������
		Connection con;
		// ��Ҫ����ʵ����ı�
		String sql = "select * from " + tablename;
		Statement pStemt = null;
		try {
			try {
				Class.forName(DRIVER);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			con = DriverManager.getConnection(URL, NAME, PASS);
			pStemt = (Statement) con.createStatement();
			ResultSet rs = pStemt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int size = rsmd.getColumnCount(); // ͳ����
			colnames = new String[size];
			colTypes = new String[size];
			colSizes = new int[size];
			for (int i = 0; i < size; i++) {
				colnames[i] = rsmd.getColumnName(i + 1);
				colTypes[i] = rsmd.getColumnTypeName(i + 1);

				if (colTypes[i].equalsIgnoreCase("date")
						|| colTypes[i].equalsIgnoreCase("timestamp")) {
					f_util = true;
				}
				if (colTypes[i].equalsIgnoreCase("blob")
						|| colTypes[i].equalsIgnoreCase("char")) {
					f_sql = true;
				}
				colSizes[i] = rsmd.getColumnDisplaySize(i + 1);
			}

			String content = parse(colnames, colTypes, colSizes);

			try {
				File directory = new File("");
				String path = this.getClass().getResource("").getPath();

				System.out.println(path);
				System.out.println("src/?/"
						+ path.substring(path.lastIndexOf("/com/", path
								.length())));
				String outputPath = directory.getAbsolutePath() + "/src/"
						+ this.packageOutPath.replace(".", "/") + "/"
						+buildTableName(tablename) + ".java";
				FileWriter fw = new FileWriter(outputPath);
				PrintWriter pw = new PrintWriter(fw);
				pw.println(content);
				pw.flush();
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		}
	}

	/**
	 * ���ܣ�����ʵ�����������
	 * 
	 * @param colnames
	 * @param colTypes
	 * @param colSizes
	 * @return
	 */
	private String parse(String[] colnames, String[] colTypes, int[] colSizes) {
		StringBuffer sb = new StringBuffer();

		sb.append("package " + this.packageOutPath + ";\r\n");
		sb.append("\r\n");
		
		// �ж��Ƿ��빤�߰�
		if (f_util) {
			sb.append("import java.util.Date;\r\n");
		}
		if (f_sql) {
			sb.append("import java.sql.*;\r\n");
		}
		
		// ע�Ͳ���
		sb.append("   /**\r\n");
		sb.append("    * " + tablename + " ʵ����\r\n");
		sb.append("    * " + new Date() + "\r\n");
		sb.append("    * @author " + this.authorName + "\r\n");
		sb.append("    */ \r\n");
		// ʵ�岿��
		sb.append("\r\n\r\npublic class " + initcap(buildTableName(tablename)) + "{\r\n");
		processAllAttrs(sb);// ����
		processAllMethod(sb);// get set����
		sb.append("}\r\n");
		return sb.toString();
	}

	/**
	 * �����ƹ���
	 * @param tablename2
	 * @return
	 */
	private String buildTableName(String tablename) {
		String arr[] = tablename.split("_") ; 
		String newTableName = "" ; 
		for(String a : arr){
			newTableName += this.initcap(a.toLowerCase()) ; 
		}
		return newTableName;
	}

	/**
	 * ���ܣ�������������
	 * 
	 * @param sb
	 */
	private void processAllAttrs(StringBuffer sb) {

		for (int i = 0; i < colnames.length; i++) {
			sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " "
					+ colnames[i].toLowerCase() + ";\r\n");
		}

	}

	/**
	 * ���ܣ��������з���
	 * 
	 * @param sb
	 */
	private void processAllMethod(StringBuffer sb) {

		for (int i = 0; i < colnames.length; i++) {
			sb.append("\tpublic void set" + initcap(colnames[i].toLowerCase()) + "("
					+ sqlType2JavaType(colTypes[i]) + " " + colnames[i].toLowerCase()
					+ "){\r\n");
			sb.append("\tthis." + colnames[i].toLowerCase() + "=" + colnames[i].toLowerCase() + ";\r\n");
			sb.append("\t}\r\n");
			sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get"
					+ initcap(colnames[i].toLowerCase()) + "(){\r\n");
			sb.append("\t\treturn " + colnames[i].toLowerCase() + ";\r\n");
			sb.append("\t}\r\n");
		}

	}

	/**
	 *  ���ܣ��������ַ���������ĸ�ĳɴ�д  
	 * @param str
	 * @return  
	 */
	private String initcap(String str) {

		char[] ch = str.toCharArray();
		if (ch[0] >= 'a' && ch[0] <= 'z') {
			ch[0] = (char) (ch[0] - 32);
		}

		return new String(ch);
	}

	/**
	 * ���ܣ�����е��������� 
	 */
	private String sqlType2JavaType(String sqlType) {

		if (sqlType.equalsIgnoreCase("binary_double")) {
			return "double";
		} else if (sqlType.equalsIgnoreCase("binary_float")) {
			return "float";
		} else if (sqlType.equalsIgnoreCase("blob")) {
			return "byte[]";
		} else if (sqlType.equalsIgnoreCase("blob")) {
			return "byte[]";
		} else if (sqlType.equalsIgnoreCase("char")
				|| sqlType.equalsIgnoreCase("nvarchar2")
				|| sqlType.equalsIgnoreCase("varchar2")) {
			return "String";
		} else if (sqlType.equalsIgnoreCase("date")
				|| sqlType.equalsIgnoreCase("timestamp")
				|| sqlType.equalsIgnoreCase("timestamp with local time zone")
				|| sqlType.equalsIgnoreCase("timestamp with time zone")) {
			return "Date";
		} else if (sqlType.equalsIgnoreCase("number")) {
			return "Long";
		}

		return "String";
	}

	/**
	 * ���� TODO
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> tables = new ArrayList<String>() ; 
		
		tables.add("escrow_dm");
		tables.add("escrow_zjjs_cssz");
		tables.add("escrow_zjjs_jhzthz");
		tables.add("escrow_zjjs_ssdf");
		tables.add("escrow_zjjs_ssdf_h");
		tables.add("escrow_zjjs_ssds");
		tables.add("escrow_zjjs_ssds_h");
		tables.add("escrow_zjjs_ssxy");
		tables.add("escrow_zjjs_yhdzb"); 
		
		for(String table : tables){
			new GenEntityOracle(table);
		}
	}

}