package edu.swust.cs.excellent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import edu.swust.cs.excellent.model.Group;
import edu.swust.cs.excellent.model.Student;

public class ExcelTool {

	/**
	 * 工作表文件
	 */
	private static  HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
	/**
	 * 工作表
	 */
	private static  HSSFSheet hssfSheet = hssfWorkbook.createSheet("write");


	/**
	 * 
	 * @param filename 待操作excel文件名
	 * @param sql 以excel一行数据为参数的sql语句
	 * @param ColCount 将要导入的数据的字段总个数
	 * @return int[] 1：改行成功导入  -1：改行丢失  
	 * @throws IOException
	 */

	public static  int[] readExcel(String filename, String sql,
			int ColCount) throws  IOException {
		Map<String, Object> map = null;
		int[] ret = null;

		HSSFWorkbook workBook = new HSSFWorkbook(new FileInputStream(
				filename));

		HSSFSheet sheet = workBook.getSheet("Sheet1");
		int rows = sheet.getPhysicalNumberOfRows();
		Object[][] paras = new Object[rows-1][ColCount];
		for (int i = 1; i < rows; i++) {
			HSSFRow row = sheet.getRow(i);
			if (row != null) {
				int cells = row.getPhysicalNumberOfCells();
				//	int cells = ColCount;
				for (int j = 0; j < cells; j++) {
					HSSFCell cell = row.getCell((short) j);
					if (cell != null) {
						switch (cell.getCellType()) {
						case HSSFCell.CELL_TYPE_FORMULA:
							break;
						case HSSFCell.CELL_TYPE_NUMERIC:
							paras[i-1][j] = (int) cell.getNumericCellValue();		
							break;
						case HSSFCell.CELL_TYPE_STRING:
							paras[i-1][j] = cell.getRichStringCellValue().getString();
							break;
						default:
							paras[i-1][j] = null;
							break;
						}
					}
				}
			}
		}
		ret = new int[rows-1];

		for (int i = 0;i< paras.length ; i++){
			try {
				ret[i] = Db.update(sql, paras[i]);
			} catch (Exception e) {
				ret[i] = -1;
				continue;
			}
		} 
		workBook = null;
		return ret;
	}

	/**
	 * 
	 * @param s 表头
	 * @param sql 得到数据的sql语句
	 * @param file 工作表文件引用
	 */
	public  static synchronized  void writeExcel(String[] s,String sql, File file){
		int  count = 0;
		// 创建工作薄

		// 创建行
		HSSFRow row = hssfSheet.createRow(0);
		// 创建单元格，设置表头 创建列
		HSSFCell cell = null;
		//遍历表头
		for (int i = 0; i < s.length; i++) {
			//创建传入进来的表头的个数
			cell = row.createCell((short) i);
			//表头的值就是传入进来的值
			//	cell.setCellValue(s[i]);
			cell.setCellValue(new HSSFRichTextString(s[i]));

		}
		//新增一个行就累加
		row = hssfSheet.createRow(++count);


		// 得到所有记录 行：列
		List<Record> list = Db.find(sql);
		Record record = null;

		if (list != null) {
			//获取所有的记录 有多少条记录就创建多少行
			for (int i = 0; i < list.size(); i++) {
				//row = hssfSheet.createRow(++count);
				// 得到所有的行 一个record就代表 一行
				record = list.get(i);
				//在有所有的记录基础之上，便利传入进来的表头,再创建N行
				for (int j = 0; j < s.length; j++) {
					cell = row.createCell((short) j);
					//把每一行的记录再次添加到表头下面 如果为空就为 "" 否则就为值
					cell.setCellValue(new HSSFRichTextString(record.get(s[j])==null?"":record.get(s[j]).toString()));
				}
			}
		}
		try {
			FileOutputStream fileOutputStreane = new FileOutputStream(file);
			hssfWorkbook.write(fileOutputStreane);
			fileOutputStreane.flush();
			fileOutputStreane.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param ss 表头意义
	 * @param s 表头  以在Model中 的字段排列制定excel中列顺序
	 * @param group 小组列表
	 * @param stus 小组成员嵌套列表
	 * @param file 工作表文件引用
	 */
	public static boolean writeCombineExcel(String[] ss,String[] s, List<Group> group,
			List<ArrayList<Student>> stus, int max, File file) {

		int  count = 0;
		// 创建工作薄

		// 创建行
		HSSFRow row = hssfSheet.createRow(0);
		// 创建单元格，设置表头 创建列
		HSSFCell cell = null;
		//遍历表头
		for (int i = 0; i < ss.length; i++) {
			//创建传入进来的表头的个数
			cell = row.createCell((short) i);
			//表头的值就是传入进来的值
			//	cell.setCellValue(s[i]);
			cell.setCellValue(new HSSFRichTextString(ss[i]));

		}
		for (int i=s.length;i<s.length+2*max;i+=2){
			cell = row.createCell((short) i);
			cell.setCellValue(new HSSFRichTextString("学号"));
			cell = row.createCell((short) (i+1));
			cell.setCellValue(new HSSFRichTextString("姓名"));
		}
		//新增一个行就累加
		row = hssfSheet.createRow(++count);


		// 得到所有记录 行：列
		List<Group> list = group;
		Group record = null;

		if (list != null) {
			//获取所有的记录 有多少条记录就创建多少行
			for (int i = 0; i < list.size(); i++) {
				//row = hssfSheet.createRow(++count);
				// 得到所有的行 一个record就代表 一行
				record = list.get(i);
				//在有所有的记录基础之上，便利传入进来的表头,再创建N行
				for (int j = 0; j < s.length; j++) {
					cell = row.createCell((short) j);
					//把每一行的记录再次添加到表头下面 如果为空就为 "" 否则就为值
					cell.setCellValue(new HSSFRichTextString(record.get(s[j])==null?"":record.get(s[j]).toString()));
				}
				for (int j=s.length;j<s.length+2*max;j+=2){
					cell = row.createCell((short) j);
					cell.setCellValue(new HSSFRichTextString(stus.get(i).get(j-s.length)==null?"":record.get(s[j]).toString()));
				}
			}
		}
		try {
			FileOutputStream fileOutputStreane = new FileOutputStream(file);
			hssfWorkbook.write(fileOutputStreane);
			fileOutputStreane.flush();
			fileOutputStreane.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}