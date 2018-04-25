package tabvalidator;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

 
 
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
 

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class ReadWorkBook {

	
    private final Log logger = LogFactory.getLog(getClass());
    
	private String file;

	private String fileType;

	private String generatedjar;

	public String getGeneratedjar() {
		return generatedjar;
	}

	public void setGeneratedjar(String jar) {
		this.generatedjar = jar;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public ReadWorkBook(String file, String fType, String jar){
		this.file = file;
		this.fileType=fType;
		this.generatedjar=jar;
	}

	public ReadWorkBook(){}

	//*********** Automatic Classloader ***********

/*
	//WARNING : This code is too old !
	private static ArrayList<Class<?>> defineClasses(String packageName) throws ClassNotFoundException, IOException {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList classes = new ArrayList();
		for (File dir : dirs) {
			classes.addAll(findClasses(dir, packageName));
		}
		return  classes;
	}

	private static List<Class<?>> findClasses(File repoName, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>> ();
		if (!repoName.exists()) {
			return classes;
		}
		File[] files = repoName.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
			 
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6))
						);
			}
		}
		return classes;
	}*/

	//*********** Validator implementation ***********
 
	public Set validate(Object ba) {

		logger.info(String.format("going to validate instance of %s ",ba.getClass() ));
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		Set<?> constraintViolations = validator.validate(ba);

		return constraintViolations;
	}

	//*********** How to read An Excel File ***********

	public void parseFile() {
			logger.info("start:parseFile");
		// To access an input file content
		try {
			FileInputStream file = new FileInputStream(new File(this.file));

			Sheet sheet =null;
			if(this.fileType.toLowerCase().equals("excel")){
				Workbook workbook = new HSSFWorkbook(file);
				sheet = workbook.getSheetAt(0);	

			}
			else if (this.fileType.toLowerCase().equals("oo")){
				Workbook workbook = new XSSFWorkbook(file);
				sheet = workbook.getSheetAt(0);	

			}
			else if (this.fileType.toLowerCase().equals("csv")){
				//TODO implement is  ??
			}
			parseSheet(sheet);


			file.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("end:parseFile");

	}

	private void parseSheet(Sheet sheet) throws Exception {
		boolean goon=true;
		String stopCause="";
		int maxHeaderColIndex=0;
		HashMap<Integer,String> colIdx2FieldN = new HashMap<Integer,String>();

		//Find the referenced template according to the sheet
		String sheetName = sheet.getSheetName();
		String classN=extractClassNameFromLabel(sheetName);
		Object ba = selectTargetClass(classN);

		//Iteration trough each rows
		int rowIndex=0;

		//sheet.getRow(2);
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			//row.getCell(2);
			Iterator<Cell> cellIterator = row.cellIterator();
			int columnIndex=0;





				// Iteration through each cells and content reading
			while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();

					Object v =null;

					if (cell.getCellTypeEnum().equals(CellType.NUMERIC)){

						v = new Double(cell.getNumericCellValue());



					}
					else if(cell.getCellTypeEnum().equals(CellType.STRING)){
						v = cell.getStringCellValue();

					}else{
						v = cell.toString();

					}
					logger.debug("Cell value type checked");

					/*	switch (cell.getCellType()) {
					case Cell.CELL_TYPE_NUMERIC:
						//CellType en = cell.getCellTypeEnum();
						if (cell.getCellTypeEnum().equals(CellType.NUMERIC)){
							System.out.print( "dfhzkjfh ");
						}

						System.out.print(cell.getNumericCellValue() + " - "); break;
					case Cell.CELL_TYPE_STRING:
						System.out.print(v+ " - "); break;
				} 	
					 */


					String fieldName=null;
					if(rowIndex==0){
						//header parsing
						String vc=null;
						if(v!=null ){
							vc=v.toString().trim();
							if(!vc.equals("")){
								fieldName=vc;
								colIdx2FieldN.put(columnIndex, fieldName);
								maxHeaderColIndex++;

							}else{
								goon=false;
								stopCause=String.format(" header field index %s is an empty string",columnIndex);
							}
						}else{
							goon=false;
							stopCause=String.format(" header field index %s has a null value",columnIndex);

						}


					}else{
						//data row parsing
						if(columnIndex > maxHeaderColIndex){
							goon=false;
							stopCause=String.format("data row has column number greater than headers (%s/%s) ",columnIndex, maxHeaderColIndex);

						}
						fieldName=colIdx2FieldN.get(columnIndex);
						logger.debug(String.format("index:%s, field:%s", columnIndex,fieldName));
			
						Object r = defineValueType(v, ba, fieldName);

//:
						try {
							PropertyUtils.setSimpleProperty(ba, fieldName, r);
						}
						catch(java.lang.NoSuchMethodException e) {
							
						}
						logger.debug("val: "+r);
					}

					logger.debug( String.format("Cell value indexation row %s , col %s"  ,(rowIndex+1), (columnIndex+1)  ));
				
					columnIndex++;
			}

				if(goon==false){
					throw new DataParsingException(stopCause);
				}
			

			if(rowIndex!=0){
				//validate each datarow using validation framework  after last col

				doValidate(colIdx2FieldN, rowIndex, columnIndex, ba); 
			}


			logger.debug("");
			rowIndex++;


		}
	}
 
	
	private Object selectTargetClass(String name)
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		Object ba = null;
	 
		List<Class<?>> classList =new ArrayList<Class<?>>();
		
		 
		String jarPath=this.generatedjar;
 
		@SuppressWarnings({ "resource", "deprecation" })
		URLClassLoader loader1 = new URLClassLoader(new URL[] {new File(jarPath).toURL()}, Thread.currentThread().getContextClassLoader());
		 
		
	 
		
		 if(name==null) {
				throw new ClassNotFoundException("sheet class name not provided");
			}
			
	 
		Set<String> clFiles= selectClassFromJar(jarPath);
   
		for(String clf: clFiles) {
			 try {
			Class<?> cl = loader1.loadClass(clf);
			classList.add(cl);
			   logger.debug("cl:"+cl);
			 }catch(Exception e) {
				// e.printStackTrace();
			}
		}
	 
	
		
		for (Class<?> cls : classList) {
			String className = cls.getSimpleName();
			
			logger.debug("repo:"+className);
			if (name.toLowerCase().equals(className.toLowerCase())) {
			 
				logger.debug("dyn data class:"+cls.getName());
				ba = cls.newInstance();
				break;
			}
			 
		}
 
		
		
		if(ba==null) {
			throw new ClassNotFoundException("target class "+ name+ " not found");
		}
		return ba;
	}



	private static Set<String> selectClassFromJar(String jarPath) throws IOException, UnsupportedEncodingException {
		
		
		JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
        while(entries.hasMoreElements()) {
          String name = entries.nextElement().getName();
           
            String entry = name ;
            
            if(entry.endsWith(".class")) {
            	entry=entry.replace(".class", "");
            	entry=entry.replaceAll("/", ".");
                 result.add(entry);
                
            }
        }
          result.toArray(new String[result.size()]);
          return result ;
	}

	private   String extractClassNameFromLabel(String s) {
		if(s!=null) {
		 
			int sidx=s.indexOf("(");
			
			if(sidx>=0) {
				int eidx=s.indexOf(")");
				if(eidx>=0 && eidx > sidx) {
					 s=s.substring(sidx+1, eidx);
				}
			}
		}
		logger.debug("class label:"+ s);
		return s;
	}
	
	
	
	
	
	private Set<String> getClassFields(final Class<?> type){
		
		Set<String> fields = new HashSet<String>();
		for (Field field : type.getDeclaredFields()) {
			fields.add(field.getName());
		}
		return fields;
	}
	
	private boolean listHasProperty(final Set<String> properties, final String propertyName) {
		
		if (properties.contains(propertyName)) {
			return true;
		} 
		return false;
	}
	
	 
	private Object defineValueType(Object v, Object ba, String fieldName) throws Exception {
		
		//FIXME :  better hierarchy management
		Field fi = null;
		Set<String> currentFields = getClassFields(ba.getClass());
		Set<String> p1Fields = getClassFields(ba.getClass().getSuperclass());
		Set<String> p2Fields = getClassFields(ba.getClass().getSuperclass().getSuperclass());
		Set<String> rootFields = getClassFields(ba.getClass().getSuperclass().getSuperclass().getSuperclass());
		boolean isCurrentField = listHasProperty(currentFields, fieldName);
		boolean isParent1Field = listHasProperty(p1Fields, fieldName);
		boolean isParent2Field = listHasProperty(p2Fields, fieldName);
		boolean isRootField = listHasProperty(rootFields, fieldName);
		
		if  (isCurrentField) {
			fi = ba.getClass().getDeclaredField(fieldName);
		}
		else if (isParent1Field) {
			fi = ba.getClass().getSuperclass().getDeclaredField(fieldName);
			}
		else if (isParent2Field) {
			fi = ba.getClass().getSuperclass().getSuperclass().getDeclaredField(fieldName);
		}
		else if (isRootField) {
			fi = ba.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField(fieldName);
		}
		
		Object r=null;
		if(fi!=null) {
			Class<?> tp = fi.getType();
			logger.debug(String.format("field: %s, type: %s, value: %s", fieldName,tp,v));
			if(tp.equals(Long.class) || tp.getName().equals("long") ) {

				if(v.getClass().equals(String.class)) {
					r = new Long(v.toString());
				}

				else if (v.getClass().equals(Double.class)) {

					Double d = (Double)v;
					r = new Long(d.longValue());
				}
			}
			else if(tp.equals(Double.class) || tp.getName().equals("double")) {

				if(v.getClass().equals(String.class)) {
					r = new Double(v.toString());
				}

				else if (v.getClass().equals(Double.class)) {

					r = (Double)v;

				}


			}
			else if(tp.equals(Float.class) || tp.getName().equals("float") ) {

				if(v.getClass().equals(String.class)) {
					r = new Float(v.toString());
				}

				else if (v.getClass().equals(Double.class)) {

					Double d = (Double)v;
					r = new Float(d.floatValue());
				}
			}
			else if(tp.equals(Integer.class) || tp.getName().equals("int")) {
				if(v.getClass().equals(String.class)) {

					r = new Integer(v.toString());
				}

				else if (v.getClass().equals(Double.class)) {

					Double d = (Double)v;
					r = new Integer(d.intValue());
				}
			}
			else if(tp.equals(Boolean.class) || tp.getName().equals("boolean")) {
				if(v.getClass().equals(Boolean.class)) {
					//FIXME : WARNING !!! this is a hard coded conversion rule
					String bo= v.toString().toLowerCase();
					if(bo.equals("true")) {
						r=true;
					}
					else if(bo.equals("false")) {
						r=false;
					}
					else if(bo.equals("t") || bo.equals("T")) {
						r=true;
					}
					else if(bo.equals("f") || bo.equals("F")) {
						r=false;
					}

				}

				else if (v.getClass().equals(Double.class)) {
					//FIXME : WARNING !!! this is a hard coded conversion rule
					Double d = (Double)v;
					Integer bo=d.intValue();
					if(bo>0) {
						r=true;
					}else {
						r=false;   
					}

				}
			}
			else if(tp.equals(Short.class) || tp.getName().equals("short") ) {

				if(v.getClass().equals(String.class)) {
					r = new Short(v.toString());
				}

				else if (v.getClass().equals(Double.class)) {

					Double d = (Double)v;
					r = new Short(d.shortValue());
				}
			}

			else if(tp.equals(String.class)) {
				r=v.toString();
			}else {
				throw new Exception("type "+tp.getName()+" not managed ");
			}
		}


		//TODO :  Character, Byte, Void

		return r;
	}

	private void doValidate(HashMap<Integer, String> colIdx2FieldN,
			int rowIndex, int columnIndex, Object ba) {


		Set<?> violations = validate(ba);

		if (!violations.isEmpty()){


			String serializedObject	= ReflectionToStringBuilder.toString(ba);

			String	msg=String.format("error:%s \n== rowIndex:%s , colIndex:%s \n== header : %s \n object:%s ",
					violations.toString(),
					(rowIndex+1),(columnIndex+1),colIdx2FieldN,serializedObject);
			throw new ValidationException(msg);
		}

	}	



}	

