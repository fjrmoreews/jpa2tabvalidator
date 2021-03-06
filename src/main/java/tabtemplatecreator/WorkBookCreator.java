package tabtemplatecreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.persistence.OrderBy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import uml2rdf.utils.CustomUnit;
import uml2rdf.utils.Info;
import uml2rdf.utils.Ordered;
import uml2rdf.utils.StringFormat;
import uml2rdf.utils.Xref;
import java.util.SortedSet;
import java.util.TreeSet; 

public class WorkBookCreator {

	private static final String TYPE_TAG = "TYPE_TAG";
	private static final String ENUM_TAG = "ENUM_TAG";
	private static final String DESCRIPTION_TAG = "DESCRIPTION_TAG";
	private static final String DESCRIPTION2_TAG = "DESCRIPTION2_TAG";
	private static final String DESCRIPTION3_TAG = "DESCRIPTION3_TAG";
	private static final String UNIT_TAG = "UNIT_TAG";

	private final Log logger = LogFactory.getLog(getClass());

	// TODO : need update
	// TODO : create template lost the order of column
	//protected   String[] PREDEFINED_SORTED_LABELS={"ID","NAME","DATEEXP","OPERATOR","DEVICE","PRODCRIT","MOL","SAMPLE"};

	private String templateDir;

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
	
	public String getTemplateDir() {
		return templateDir;
	}
	
	public void setTemplateDir(String file) {
		this.templateDir = file;
	}
	
	public WorkBookCreator(String file, String fType, String jar){
		this.templateDir = file;
		this.fileType=fType;
		this.generatedjar=jar;
	}
// use for create a spread sheet
	public void createSpreadSheetFromClasses() {
		logger.info("start:createSpreadSheetFromClasses");
		try {
			Sheet sheet =null;
			List<Class<?>> classList=selectJPAClassesFromJar();

			for(Class<?> cl :classList){
				FileOutputStream outputStream =null;
				File sfile =null;
				Workbook workbook =null;

				String clname = cl.getSimpleName().toLowerCase();
				String templateName=clname;
				String sheetName="data_info"+"("+clname+")";
				String extension=null;

				if(this.fileType.toLowerCase().equals("excel")){
					extension="xls";
					workbook = new HSSFWorkbook(); // define the workbook like HSSWorkbook ==> poi.jar

				}
				else if (this.fileType.toLowerCase().equals("oo")){
					extension="xlsx";
					workbook = new XSSFWorkbook();

				}
				else if (this.fileType.toLowerCase().equals("csv")){
					//TODO implement is  ??
				}
				if(extension==null){
					throw new CreateDataParsingException("ouput template not supported or not defined: " + this.fileType);
				}
				templateName+="."+extension;
				sfile = new File(this.templateDir+"/"+templateName);
				sheet = workbook.createSheet(sheetName);
				outputStream = new FileOutputStream(sfile);
				
				// create the col and row of the spread sheet with cl (the class list issue of .jar)
				Set<Field> cFields = selectAllDeclaredFields(cl,0);

				logger.debug(">>>"+cFields.size());
				Row row =null;
				int rowNum = 0;
				row = sheet.createRow(rowNum);
				//new row : attr name
				int colNum = 0;	
				for(Field field:cFields){					
					Cell cell = row.createCell(colNum);
					
					//FIXME
					//cell.setCellValue(field.getName().toUpperCase());
					//cell.setCellValue(field.getName());
			 
					String str = field.getName();
					String formatted = StringFormat.firstToUpperCase(str);

					cell.setCellValue(formatted);
					colNum++;
				}
				
				
				//new row : desc
				rowNum++;
				row = sheet.createRow(rowNum);
				colNum = 0;	
				for(Field field:cFields){									
					Cell cell = row.createCell(colNum);
				 
					cell.setCellValue(commentField("#",field,DESCRIPTION_TAG));

					colNum++;
				}
				
				//new row : type, unit, enumeration
				rowNum++;
				row = sheet.createRow(rowNum);
				colNum = 0;	
				
				// field the cell of the spread sheet (template)
				for(Field field:cFields){									
					Cell cell = row.createCell(colNum);
					//type
					String v=commentField("#",field,TYPE_TAG);
					v+=commentField(",\n ",field,ENUM_TAG);
					v+=commentField(",\n ",field,UNIT_TAG);
					cell.setCellValue(v);
					colNum++;
				}
				

				
			 
				//new row : desc2
				rowNum++;
				row = sheet.createRow(rowNum);
				colNum = 0;	
				for(Field field:cFields){									
					Cell cell = row.createCell(colNum);
			 
					cell.setCellValue(commentField("#",field,DESCRIPTION2_TAG));

					colNum++;
				}
				
				//new row : desc3
				rowNum++;
				row = sheet.createRow(rowNum);
				colNum = 0;	
				for(Field field:cFields){									
					Cell cell = row.createCell(colNum);
					 
					cell.setCellValue(commentField("#",field,DESCRIPTION3_TAG));

					colNum++;
				}
				
				
				
				try {
					workbook.write(outputStream);
					workbook.close();
					logger.info(String.format("workbook:write:%s",sfile.getAbsolutePath()));       
				} 
				catch (FileNotFoundException e) {
					e.printStackTrace();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				logger.debug("Done");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

private String commentField(String pre,Field field,String tag) {
	if (tag.equals(TYPE_TAG)) {
		String s= "";
		String d=field.getType().getSimpleName().toLowerCase();
		if(d!=null && !d.equals("")){
			s+=pre+""+d;
		}
		return s;	
	}
	else if (tag.equals(ENUM_TAG)) {
		String s= "";
		String d=controlledValues(field);
		if(d!=null && !d.equals("")){
			s+=pre+" only "+d;
		}
		return s;	
	}
	else if(tag.equals(DESCRIPTION_TAG)){
		String s="";
		String d=description(field,1);
		if(d!=null && !d.equals("")){
			s+=pre+""+d;
		}
		return s;
	}
	else if(tag.equals(DESCRIPTION2_TAG)){
		String s="";
		String d=description(field,2);
		if(d!=null && !d.equals("")){
			s+=pre+""+d;
		}
		return s;
	}
	else if(tag.equals(DESCRIPTION3_TAG)){
		String s="";
		String d=description(field,3);
		if(d!=null && !d.equals("")){
			s+=pre+""+d;
		}
		return s;
	}
	else if(tag.equals(UNIT_TAG)){
		String s="";
		String d=unit(field);
		if(d!=null && !d.equals("")){
			s+=pre+" unit:"+d;
		}
		return s;
	}
	return null;
	
}


	
	// select all class present in jar file. use in the previous function to create a spread sheet template
	private List<Class<?>> selectJPAClassesFromJar()
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {

		List<Class<?>> classList =new ArrayList<Class<?>>();
		String jarPath=this.generatedjar;
		@SuppressWarnings({ "resource", "deprecation" })
		URLClassLoader loader1 = new URLClassLoader(new URL[] {new File(jarPath).toURL()}, Thread.currentThread().getContextClassLoader());
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
		if(classList.size()==0){
			throw new ClassNotFoundException("no classes in jar target class "+ jarPath );
		}
		return classList;
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
		jar.close();
		result.toArray(new String[result.size()]);
		return result ;
	}
	
	// list all attribute in class and sort them
	private Set<Field> selectAllDeclaredFields(  Class<?> type, int level){
		Map<Integer,List<Field>> fieldsM = new  HashMap<Integer, List<Field>> ();
		Map<String,Integer>priorityAttr = new LinkedHashMap<String,Integer>();
		//
		
		/*
		Set<Field> fields = new TreeSet<>(new Comparator<Field>() {
	        @Override
	        public int compare(Field o1, Field o2) {
	            // Define comparing logic here
	        	
	            return o1.getName().compareTo(o2.getName());
	            
	        }
	    });
		*/
		logger.debug("selectAllDeclaredFields:"+type.getSimpleName());
		LinkedHashSet<Field> fieldsn =new LinkedHashSet<Field>();
		
		Map <Field,Integer> sortedOrderedAll =new LinkedHashMap<Field,Integer>() ;
		Map <Field,Integer> sortedRevOrderedAll =new LinkedHashMap<Field,Integer>() ;
		Map <Field,Integer> sortedOtherAll =new LinkedHashMap<Field,Integer>() ;

		
		level=recursiveDefineAllFields(fieldsM, type,level);
		System.out.println(level);
		while(level>=0){
			List<Field> fields = fieldsM.get(level);
			level--;
			
			Map <Field,Integer> fieldsmapOrdered = new HashMap<Field,Integer>();
			Map <Field,Integer> revFieldsmapOrdered = new HashMap<Field,Integer>();
			
			Map <Field,Integer> fieldsmap = new HashMap<Field,Integer>();
			int idx=-1;
			
			 
			
			Class<Ordered> annot= Ordered.class;
			for(Field f:fields){
			    Annotation[] anl = f.getAnnotations();
				if(anl!=null){
					for(Annotation a : anl){
						System.out.println("0"+a);
					}
				}
				
				if (f.isAnnotationPresent(annot)) {
					
					
						Integer index = orderedIndex(f);
						
						if(index!=null && index>-1){ 
						  fieldsmapOrdered.put(f,index);
						}else{
							Integer rindex = revOrderedIndex(f);
							if(rindex!=null && rindex>-1){ 
								  revFieldsmapOrdered.put(f,rindex);
							}	
						}
						
					
				}
			}
			
			for(Field f:fields){
				if(     !fieldsmapOrdered.containsKey(f) 
						&& !fieldsmap.containsKey(f) ){
					if(     !revFieldsmapOrdered.containsKey(f) ){
					  idx++;
					  fieldsmap.put(f,idx);
					}
					
				}
			}
			Map <Field,Integer> sortedOrdered =SortMapUtil.sortByValue(fieldsmapOrdered);
			Map <Field,Integer> sortedRevOrdered =SortMapUtil.sortByValue(revFieldsmapOrdered);
			
			Map <Field,Integer> sortedOther =SortMapUtil.sortByValue(fieldsmap);

			sortedOrderedAll.putAll(sortedOrdered);
			sortedRevOrderedAll.putAll(sortedRevOrdered);
			
			sortedOtherAll.putAll(sortedOther);
			
		}
	
		
		
		int ix=0;
		for(Field f:sortedOrderedAll.keySet()){
			fieldsn.add(f);
			logger.debug("\t\tfieldName:"+f.getName()+", "+ix);
			ix++;
		}
		for(Field f:sortedOtherAll.keySet()){
			fieldsn.add(f);
			logger.debug("\t\tfieldName:"+f.getName()+", "+ix);
			ix++;
		}
		for(Field f:sortedRevOrderedAll.keySet()){
			fieldsn.add(f);
			logger.debug("\t\tfieldName:"+f.getName()+", "+ix);
			ix++;
		}
		
		return fieldsn;
	}
	
	
	private static Integer orderedIndex(Field field) {
		Ordered anno = (Ordered) field.getAnnotation(Ordered.class);
		Integer value = anno.index();
		
		return value;
	
	}
	
	
	private static Integer revOrderedIndex(Field field) {
		Ordered anno = (Ordered) field.getAnnotation(Ordered.class);
		Integer value = anno.rindex();
		
		return value;
	
	}
	
	private static String description(Field field, int idx) {
		
		Info anno = (Info) field.getAnnotation(Info.class);
		
		if(anno!=null){
		 String value = null;
		 
		 
		 if(idx==1) {
			 value=anno.description();
		 }
		 else if(idx==2) {
			 value=anno.description2();
		 }
		 else if(idx==3) {
			 value=anno.description3();
		 }
		
		 
		
		 
		 if(value==null){
			return "";
		 }
		 return value;
		
		}else{
			return "";
		}
	
	}

	private static String unit(Field field) {
		CustomUnit anno = (CustomUnit) field.getAnnotation(CustomUnit.class);
		if(anno!=null){
		 String value = anno.value();
		 if(value==null){
			return "";
		 }
		  return value;
		}return "";
	}
	private static String controlledValues(Field field) {
	
		Info anno = (Info) field.getAnnotation(Info.class);
		if(anno!=null){
		String value = anno.enumerate();
		if(value==null){
			return "";
		}
		return value;
		}
		else{
			return "";
		}
	}
	
	// list all hierarchycal  class tree
	public Integer recursiveDefineAllFields(Map<Integer,List<Field>> fieldsM, Class<?> type,int level) {
		List<Field>fields= new ArrayList<Field>();
		fields.addAll(Arrays.asList(type.getDeclaredFields()));
		fieldsM.put(level, fields);
		logger.debug("\t\trecDefineAllFields:"+type.getSimpleName());
		if (type.getSuperclass() != null) {
			level++;
			level=recursiveDefineAllFields(fieldsM, type.getSuperclass(),level);
		}
		return level;
	}
}	