package sandbox;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class TestAnnot {
public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
	
	  Annotated ba = new Annotated();
	  
	  
	  Class<Xref> annot = Xref.class;
	  
	  Map<String, String> m =  extractXrefInfo(ba,annot);    
	 
	  System.out.println(m);
}

private static  Map<String, String> extractXrefInfo(
		Annotated object, Class<Xref> annot) throws IllegalAccessException {
	Class<?> clazz = object.getClass();
	
	
	 Map<String, String> m = new HashMap<>();
	    for (Field field : clazz.getDeclaredFields()) {
	    	System.out.println("->"+field);
	        field.setAccessible(true);
	        Annotation[] anl = field.getAnnotations();
	        if(anl!=null){
		        for(Annotation a : anl){
		        	System.out.println("0"+a);
		        }
	        }
	        if (field.isAnnotationPresent(annot)) {
	        	System.out.println("1");
	            m.put(annot.getSimpleName(),getKey(field,annot) );
	        }
	    }
	    return m;
}

private static String getKey(Field field, Class c) {
    Xref anno = (Xref) field.getAnnotation(c);
	String value = anno.arg1()+"--"+anno.arg2();
    return value.isEmpty() ? field.getName() : value;
}


}
