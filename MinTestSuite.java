import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.BufferedReader;

import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;
import java.net.URL;
import java.net.URLClassLoader; 


public class MinTestSuite{
	
	private String jarFile;
	private String testCasesFile;

    private static List<Request> requestList;
  
    
    public MinTestSuite(String jarFile, String testCasesFile){ 
    	  	    	
           this.jarFile = jarFile;
           this.testCasesFile = testCasesFile;
    }

    /**
     * Run test Suite related with this MinTestSuite instance
     * **/
    public boolean runTestSuite(){
    	
    	boolean result = false;
    	
    	//read file with test cases information
        try{
        	
        	//System.out.println("test cases file: "+ this.testCasesFile);
            FileReader fr = new FileReader(new File(testCasesFile));
       
            //set list of test cases to run:
            
             requestList = getTestCasesListOfRequests(jarFile, fr);

            if(requestList!=null){
            	result = runTestCases(requestList);
            }else{
                System.err.println("Empty list of test cases (requests)");
            }
        }catch(FileNotFoundException e){
        
        	System.err.println("Error with file with test cases list: " + testCasesFile);
            e.printStackTrace();
        }
        
        return result;
    	
    }
    
    public static void main(String[] args){

        if(args==null){
            System.err.println("Missing file list of test cases to run");
            return;
        }
        
        if(args[0].equals("")){
            System.err.println("Missing subject app jar's path");
            return;
        }

        if(args[1]==null || args[1].equals("")){
            System.err.println("Missing file with subject app test cases to execute");
            return;
        }

        
        //testLoadingClassesFromJARFile(args[0]);
        
        //read file with test cases information
        try{
        	//System.out.println("test cases file: "+ args[1]);
            FileReader fr = new FileReader(new File(args[1]));
       
            //set list of test cases to run:
            
            requestList = getTestCasesListOfRequests(args[0], fr);

            if(requestList!=null){
                //System.out.println("Number of tests to run is: "+requestList.size());
                runTestCases(requestList);
            }
        }catch(FileNotFoundException e){
        
            e.printStackTrace();
        }

    }


    /*
     * test loading classes from jar file
     * **/
    public static void testLoadingClassesFromJARFile(String pathToJAR){
    
        String className = "";
        //System.out.println("testing loading jar: "+pathToJAR);

        try{
            JarFile jarFile = new JarFile(pathToJAR);
            Enumeration<JarEntry> entries = jarFile.entries();

            URL[] urls = {new URL("jar:file:" + pathToJAR+ "!/")};
            URLClassLoader classLoader = URLClassLoader.newInstance(urls);

            while(entries.hasMoreElements()){
                JarEntry je = entries.nextElement();
                if(je.isDirectory() || !je.getName().endsWith(".class")){
                 continue;
                } 

                className = je.getName().substring(0, je.getName().length()-6);
                className = className.replace('/','.');

                Class<?> classObj = classLoader.loadClass(className);
                //if(classObj != null)
                //    System.out.println("\t Loaded: "+classObj.getName());
            }

        } catch(ClassNotFoundException excep){
            System.out.println("not able to load class: "+ className);
            excep.printStackTrace();
        } catch(IOException excep){
            excep.printStackTrace();
        }
    }



    /**
     * return a list of Request objects representing the test class file and test case method to
     * run from an application test suite
     *
     * */
    public static List<Request> getTestCasesListOfRequests(String pathToJAR, FileReader fileReader){

        //get class loader for this class:
        List<Request> list = new ArrayList<Request>();
        String cname = "";

        try{
            URL[] urls = {new URL("jar:file:" + pathToJAR+ "!/")};
            URLClassLoader classLoader = URLClassLoader.newInstance(urls);
    
            //get file data as a String
            BufferedReader br = new BufferedReader(fileReader);

            String line;
            Class<?> classname = null;
            StringBuilder sb = new StringBuilder();

            while((line = br.readLine())!=null){
       
                if(!line.startsWith("//")){
                	//System.out.println("line: "+line);
                    String fileNtest = line.substring(line.indexOf(":")+1, line.length());
                    // test file name separated from test case name by '#'
                    String[] parts = fileNtest.split("#");
                    cname = parts[0].trim();
                    //System.out.println("Class to be analyzed: "+ cname);
                    
                    //System.out.println("Analyzing file: "+fileNtest);
                    
                    classname = classLoader.loadClass(cname);
                    //System.out.println("Success loading: "+ cname);
                    
                    Request request = Request.method(classname, parts[1].trim());
                
                    if(request!=null){
                        list.add(request);
                        sb.append(classname.getName());
                        sb.append("#");
                        sb.append(parts[1]);
                        sb.append(", ");
                    }else{
                        System.err.println("request was null for: "+classname+ "#"+parts[1]);
                    }
                }
            }

            //System.out.println("Tests to run: " + sb.toString());

       }catch(IOException e){
            e.printStackTrace();
       }catch(ClassNotFoundException e){
           System.err.println("Not able to load class. Error with class: " + cname);
           e.printStackTrace();
       }

       //System.out.println("Total number of test cases analyzed: "+ list.size());

        return list;
    }


    /**
     * run test cases specified by @param requests using JUnitCore api
     * */
    public static boolean runTestCases(List<Request> requests){
        
        JUnitCore core = new JUnitCore();

        double accRunTime = 0d;
        int accCount = 0;
        int accFailedCount = 0;
        List<Failure> listFail = new LinkedList<Failure>();

        for(Request rqst: requests){
            
            // run single test case represented by this request 
            //System.out.println("Running test... ");
            Result result = core.run(rqst);

            accCount += result.getRunCount();
            accRunTime += result.getRunTime();

            if (result.getFailureCount()>0){
            	Runner runner = rqst.getRunner();
            	
                System.out.println("test Failed: "+runner.getDescription());
                accFailedCount += result.getFailureCount();
                listFail.addAll(listFail.size(), result.getFailures());
            }
           
        }

        System.out.println("==== Summary Minimized Test Suite ==== \n#tests: "+accCount);
        if(accFailedCount > 0){
            System.out.println("#failedTests: "+accFailedCount);
            System.out.println("The following tests failed: ");
            for(Failure f : listFail){
                System.out.println(f.getDescription() + "\n" + f.getMessage() + "\n"+ f.getTrace() +"\n ========");
            }
            System.exit(-1); // abnormal termination of the program
        }
        
        return (accFailedCount > 0) ? false : true;

    }
}
