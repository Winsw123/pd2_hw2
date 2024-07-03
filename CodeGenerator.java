import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DataType {
    //check int/boolean/string/void
    public static String check(String input) {
        if(input.contains("int")) {
            return "return 0;}";
        } else if(input.contains("boolean")) {
            return "return false;}";
        } else if(input.contains("String")) {
            return "return \"\";}";
        } else {
            return ";}";
        }
    }
}

class filereader {
    public String read(String fileName) {
        try{
            return Files.readString(Paths.get(fileName));
        }
        catch (IOException e) {
            //System.err.println("cant read" + fileName);
            e.printStackTrace();
            return "";
        }
}
}

class Store {
    public static void WriteIn(Map<Integer, String> myMap) {
        //write into the javafile
        String output = myMap.get(0) + ".java";
        try {
            //String output = "Example.java";
            //String content = "this is going to be written into file";
            File file = new File(output);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (int i = 1; i < myMap.size(); i++) {
                    String value = myMap.get(i);
                    bw.write(value + "\n");
                    //System.out.println("Key: " + i + ", Value: " + value);
                }
                bw.write("}");
            }
            //System.out.println("Java class has been generated: " + output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Parser {
    public static Map<String, StringBuilder> splitByClass(String file) {
        //split by class
        //detect the same class's attribute and store it together
        String input = file;
        Pattern pattern = Pattern.compile("class (\\w+)\\s*\\{([^}]*)\\}|(\\w+)\\s*:\\s*([^\\n]+)|class (\\w+)\\s*");//check pattern 1,2,3
        Matcher matcher = pattern.matcher(input);//matched with input

        HashMap<String, StringBuilder> classes = new HashMap<>();

        while (matcher.find()) {
            if (matcher.group(1) != null) { 
                String className = matcher.group(1);//position of first(\\w+) is after the class before the {
                String classBody = matcher.group(2).trim();//second() is the data in the {}
                classes.putIfAbsent(className, new StringBuilder());
                classes.get(className).append(classBody).append("\n");//store hashmap
            } else if (matcher.group(3) != null) { 
                String className = matcher.group(3);//third() is the classname (e.g : classname : "attribute")
                String classAttribute = matcher.group(4).trim();//forth() is the attribute
                classes.putIfAbsent(className, new StringBuilder());
                classes.get(className).append(className).append(" : ").append(classAttribute).append("\n");
            } else if(matcher.group(5) != null) {
                String className = matcher.group(5);//fifth() is the case which class doesnt have attribute
                String classBody = null;
                classes.putIfAbsent(className, new StringBuilder());
                classes.get(className).append(classBody).append("\n");
            }
        }
        //System.out.println("hi");
        return classes;
        /*for (String className : classes.keySet()) {
            System.out.println("Class: " + className);
            System.out.println(classes.get(className).toString());
        }*/
    }
}

public class CodeGenerator {
    public static String SplitString(String line) {
        //use for parameter
        //case 1 : contains multiple of parameter
        //case 2 : contains one parameter
        if(line.contains(",")){
            //use "," to split the multiple parameter and trim them, then reassemble
            String input = line;
            String result = "";
            String[] parameter = input.split(","); 
            for(String parameters : parameter) {
                parameters = parameters.trim();
                //System.out.println("p : " + parameters);
                parameters = parameters.replaceAll("\\s+"," ");//got case(int \\s+ a), thus need to split and reassemble
                result = result + parameters + ", ";
            }
            return result.substring(0,result.length() - 2);//-2 means delete ", " extra string
        }else {
            //similar with above function
            String input = line;
            input = input.trim();
            String[] parts = input.split("\\s+"); 
            return parts[0] + " " + parts[1];
        }
    }
    public static String SameStr(String line,Map<Integer, String> tempMap,int count){
        //used for getmethod to find the return variable
        //call the get function name and the stored objects compare it, if there is same then thats the return variable 
        int i = 0;
        String temp;
        String returnStr = "";
        while(i<=count-2) {//count is the key
            returnStr = tempMap.get(i++);
            //System.out.println(returnStr);
            //System.out.println(count);
            returnStr = returnStr.substring(returnStr.lastIndexOf(" ") + 1);//get functionname(format of the stored object : public/private type functionname)
            temp = returnStr.toLowerCase();//turn function name to lowercase
            //System.out.println(temp);
            if(temp.equals(line)) {return returnStr;}//compare
        }
        return "";
    }

    public static Map<Integer, String> setHashMap(String key, String inputString) {
        //multiple of cases
        //"+","-" process method is similar
        Map<Integer, String> map = new HashMap<>();
        Map<Integer, String> tempMap = new HashMap<>();
        String pattern = "[^\r\n]+";//until meet the "\n" is one data

            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(inputString);
            //System.out.println(classes.get(key).toString());
            int i = 0;//output paragraph
            int count = 0;
            String spaces = new String(new char[4]).replace('\0', ' ');//indentation unit
            if(inputString == null) {
                //if attribute is null
                //get the filename and create the first paragraph
                //System.out.println("HI");
                map.put(i++,key);//0 is the key of the filename
                String className = key;
                className = "public class " + className + " {";
                map.put(i++,className);
            }

            while(m.find()) {
                //is attribute != null
                String line = m.group();
                tempMap.put(count++,line);
                line = line.trim();
                //System.out.println("Read line: " + line);
                if(i == 0) {
                    //System.out.println("HI");
                        map.put(i++,key);
                        String className = key;
                        className = "public class " + className + " {";
                        map.put(i++,className);

                }
                if(i != 0) {
                    if(line.contains("(")==false) {
                        //if line is not a function
                        if(line.contains("+")) {
                            if(line.contains(map.get(0)) && line.contains(":")){
                                //case format(calssname : attribute)
                                line = line.replaceAll(map.get(0) + "\\s*:\\s*\\+","");
                                String var = SplitString(line);
                                line = spaces + "public " + var;//    public int var
                            }else {
                                //other case
                                line = line.replaceAll("\\s*\\+","");
                                String var = SplitString(line);
                                line = spaces + "public " + var;
                            }
                        map.put(i++,line + ";");
                        }else if(line.contains("-")) {
                             //case format(calssname : attribute)
                            if(line.contains(map.get(0)) && line.contains(":")) {
                                line = line.replaceAll(map.get(0) + "\\s*:\\s*\\-","");
                                String var = SplitString(line);
                                line = spaces + "private " + var;
                            }else {
                                //other case
                                line = line.replaceAll("\\s*\\-","");
                                String var = SplitString(line);
                                line = spaces + "private " + var;
                            }
                            map.put(i++,line + ";");
                        }
                    }else if(line.contains("(")) {
                        //if line is a function
                        int Upper = line.indexOf("(");
                        int Lower = line.indexOf(")");
                        String parameter = "";
                        parameter = line.substring(Upper,Lower + 1);
                        //System.out.println(parameter);
                        if(parameter.matches(".*[a-zA-Z].*")) {
                            //deal with parameter
                            parameter = SplitString(line.substring(Upper + 1,Lower));
                            //System.out.println("READ LINE : " + parameter);
                        }else {
                            parameter = "";
                        }
                        if(line.contains("+")) {
                            if(line.contains(map.get(0)) && line.contains(":")){
                                //case format(calssname : attribute)
                                line = line.replaceAll(map.get(0) + "\\s*:\\s*\\+",spaces + "public ");
                            }else {
                                //other case
                                line = line.replaceAll("\\s*\\+",spaces + "public ");
                                //System.err.println("-----------------");
                            }
                            //System.out.println(line);
                            //reassemble the data
                            String temp = line;
                            int index = temp.indexOf(")");
                            temp = temp.substring(index + 1, temp.length());//get data type
                            temp = temp.trim();
                            line = line.substring(0,index + 1);
                            line = line.substring(0,11) + temp + " " + line.substring(11,line.indexOf("(")).trim() + "(" + parameter + ") {";//public "type" "funcname"() {...
                            //System.out.println("read line : " + line);
                            if(line.contains("get") ) {
                                //get the function name as return value and check whether there is a same stored variable name
                                String returnStr = line.substring(line.indexOf("get") + 3,line.indexOf("("));
                                //System.out.println(returnStr);
                                returnStr = returnStr.toLowerCase();
                                //System.out.println(returnStr);
                                returnStr = SameStr(returnStr,tempMap,i);
                                if(returnStr.equals("")) {
                                    String returnResult = DataType.check(temp);
                                    line = line + "\n" + spaces + spaces + returnResult;
                                }else {
                                    line = line + "\n" + spaces + spaces + "return " + returnStr + ";\n" + spaces + "}";//doesnt same case
                                }
                            }else if(line.contains("set")) {
                                //get the function name as return value and check whether there is a same stored variable name
                                String returnStr = line.substring(line.indexOf("set") + 3,line.indexOf("("));
                                returnStr = returnStr.toLowerCase();
                                //System.out.println(returnStr);
                                returnStr = SameStr(returnStr,tempMap,i);
                                line = line + "\n" + spaces + spaces + "this." + returnStr + " = " + returnStr + ";\n" + spaces + "}";
                            }else {
                                String returnResult = DataType.check(temp);
                                line = line + returnResult;
                            }
                            map.put(i++,line);
                        }else if(line.contains("-")) {
                            if(line.contains(map.get(0)) && line.contains(":")){
                                line = line.replaceAll(map.get(0) + "\\s*:\\s*\\-",spaces + "private ");
                            }else {
                                line = line.replaceAll("\\s*\\-",spaces + "private ");
                            }
                                String temp = line;
                                int index = temp.indexOf(")");
                                temp = temp.substring(index + 1, temp.length());
                                temp = temp.trim();
                                line = line.substring(0,index + 1);
                                line = line.substring(0,12) + temp + " " + line.substring(12,line.indexOf("(")).trim() + "(" + parameter + ") {";//private "type" "funcname"(parameter) {...
                                if(line.contains("get") ) {
                                    String returnStr = line.substring(line.indexOf("get")+3,line.indexOf("("));
                                    //System.out.println(returnStr);
                                    returnStr = returnStr.toLowerCase();
                                    //System.out.println(returnStr);
                                    returnStr = SameStr(returnStr,tempMap,i);
                                    if(returnStr.equals("")) {
                                        String returnResult = DataType.check(temp);
                                        line = line + "\n" + spaces + spaces + returnResult;
                                    }else {
                                        line = line + "\n" + spaces + spaces + "return " + returnStr + ";\n" + spaces + "}";
                                    }
                                }else if(line.contains("set")) {
                                    String returnStr = line.substring(line.indexOf("set") + 3,line.indexOf("("));
                                    returnStr = returnStr.toLowerCase();
                                    //System.out.println(returnStr);
                                    returnStr = SameStr(returnStr,tempMap,i);
                                    line = line + "\n" + spaces + spaces + "this." + returnStr + " = " + returnStr + ";\n" + spaces + "}";
                                } else {
                                    String returnResult = DataType.check(temp);
                                    line = line + returnResult;
                                }
                                map.put(i++,line);
                            }
                    }
                }
            }
            return map;
        /*System.out.println("\nHashMap of Integer to String:");
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }*/
    }
    public static void main(String[] args) {
		    //read the whole file
            //catagorize it and store it in a hashmap using format (className, attribute)
            //handle the data then store it in another hashmap
            //write in the result in the created file
        if (args.length == 0) {
            //System.err.println("input mermaid filename");
            return;
        }
        String fileName = args[0];
        //System.out.println("Filename : " + fileName);
        
        filereader mermaidCodeReader = new filereader();
        //setHashMap(mermaidCodeReader.read(fileName));
        String Inputdata = mermaidCodeReader.read(fileName);
        //System.out.println(Inputdata);
        
        Map<String,StringBuilder> splittedMap = Parser.splitByClass(Inputdata);

        Set<String> keys = splittedMap.keySet();//each key means got how many of classes
        for(String key : keys){
            //System.out.println("value : " + splittedMap.get(key).toString());
            Store.WriteIn(setHashMap(key, splittedMap.get(key).toString()));
        }
    }
}

