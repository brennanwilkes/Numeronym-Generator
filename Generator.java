/**
	File:       	Generator.java
	Author:			Brennan Wilkes
	Date:			19/11/19
	Compiler:		Java JDK 1.8
*/

//Imports
import java.util.*;
import java.io.*;

/**
	This program generates numeronyms for phone numbers.
*/
public class Generator{
	
	/** Maximum word size / length of phone number (without area code) NOTE: This should never be changed*/
	public static final int MAX_WORD_SIZE = 7;
	
	/** Maximum number of same index and same length permutations outputed. ONLY FOR DISPLAY, ALL VALUES WILL STILL BE CALCULATED - Should always be >= 1 */
	public static final int MAX_NUMBER_PERMUTATIONS = 3;
	
	/** Maximum number of paths outputed. ONLY FOR DISPLAY, ALL VALUES WILL STILL BE CALCULATED - Should always be >= 1 */
	public static final int MAX_PATHS = 10;
	
	/** Input file for phone numbers */
	public static final String PHONENUMBER_FILE = "telephone.txt";
	
	/** Input file for valid words */
	public static final String WORDS_LIST_FILE = "word_list.txt";	//"sample_words.txt";//
	
	/** Output file for results */
	public static final String OUTPUT_FILE = "results.txt";
	
	/**
		Index of number-letter pairs. Forms shape:
		2 (A B C)
		3 (D E F)
		4 (G H I)
		5 (J K L)
		6 (M N O)
		7 (P Q R S)
		8 (T U V)
		9 (W X Y Z)
	*/
	public static final char[] WORD_NUMBER_INDEX = {'2','2','2','3','3','3','4','4','4','5','5','5','6','6','6','7','7','7','7','8','8','8','9','9','9','9'};

    public static void main (String[] args) throws FileNotFoundException{
		
		//Read phone numbers
		String[] numbers = read_file(PHONENUMBER_FILE);

		//Read and parse valid words	-	This takes 25% of runtime
		String[][][] word_index = generate_word_index(WORDS_LIST_FILE);
		
		//sort words					-	This takes 70% of runtime
		for(int digit=0;digit<word_index.length;digit++){
			sort(word_index[digit]);
		}
		
		//Initialize storage system
		int[][][][] phonenumber_paths = new int[numbers.length][][][];
		
		for(int num=0;num<numbers.length;num++){
			
			//Initialize array to size of 7!
			//Worst case sceanario, there are 7! possible paths to generate, i.e EVERY combination of numbers is a word.
			phonenumber_paths[num] = new int[factorial(MAX_WORD_SIZE)][][];
			
			//Recursively find all paths 
			generate_paths(0,MAX_WORD_SIZE,numbers[num].substring(3),word_index,new int[MAX_WORD_SIZE][],0,phonenumber_paths[num]);
		}
		
		//Output
		print_paths(numbers,phonenumber_paths,word_index,new PrintWriter(new File(OUTPUT_FILE)));	
	}
	
	/*
		Prints search results
		@param word_index dictionary of valid words
		@param search Search results
		@param num Phone number fragment that was searched
		@param size Size of word
		@param output output stream
	*/
	public static void print_results(String[][][] word_index, int[] search,String num,int size,PrintWriter output){
		for(int i=0;i<search.length;i++){
			output.println(num+": "+word_index[size-1][search[i]][0]);
		}
	}
	
	/*
		Loads and parses valid words
		@param srcFileName file name to pull words from
		@returns Three dimensional array of words, and corresponding number codes, sorted by length
	*/
	public static String[][][] generate_word_index(String srcFileName) throws FileNotFoundException{
		
		//Initialization and setup
		File inFile = new File(srcFileName);
		int[] word_count = countWords(inFile);
		Scanner scan = new Scanner(inFile);
		String [][][] index = new String[MAX_WORD_SIZE][][];
		for(int i=0;i<index.length;i++){
			index[i] = new String[word_count[i]][2];
		}
		String word;
		int[] count = new int[MAX_WORD_SIZE];

		while(scan.hasNext()){
			word = scan.next();
			if(isValidWord(word)){
				
				//Store the word and code in the array of words of corresponding size, at the next available slot
				index[word.length()-1][count[word.length()-1]] = new String[2];
				index[word.length()-1][count[word.length()-1]][0] = word; 
				index[word.length()-1][count[word.length()-1]][1] = gen_code(word);
				
				//Increment counter of specific word length
				count[word.length()-1]++;
			}
		}
		scan.close();
		return index;
	}
	
	/**
		Saves a path in storage. Deep copies all non null layers
		@param path Path to store
		@param valid_paths array to store in
	*/
	public static void save_path(int[][] path,int[][][] valid_paths){
		for(int path_i=0;path_i<valid_paths.length;path_i++){
			
			//Find an empty slot
			if(valid_paths[path_i]==null){
				
				//Deep copy
				valid_paths[path_i] = new int[path.length][];
				for(int j=0;j<path.length && path[j]!=null;j++){
					valid_paths[path_i][j] = new int[path[j].length];
					for(int k=0;k<path[j].length;k++){
						valid_paths[path_i][j][k] = path[j][k];
					}
				}
				break;
			}
		}
	}
	
	/**
		Recursive algorithm to find all valid word combinations
		@param min minimum index to search with
		@param max maximum index to search with
		@param num Phone number being used
		@param word_index dictionary of valid words
		@param path Path to store values in
		@param valid_paths array to store valid paths in
	*/
	public static void generate_paths(int min,int max,String num,String[][][] word_index,int[][] path,int depth,int[][][] valid_paths){
		
		//END of recursion
		if(min==max){
			
			//Succesfully reached end of phone number with all valid words
			if(min==MAX_WORD_SIZE){
				save_path(path,valid_paths);
			}
			return;
		}
		
		/*
			Find words of the current search size in the current substring of the phone Number
			Example:
				Phone Number = 2336471
				min = 2
				max = 4
				
				Searches for three letter words with a code of 364
		*/ 
		int[] search = find(word_index[max-min-1],num.substring(min,max));
		
		//Found atleast one result
		if (search!=null){
			
			//Store search results in the path
			path[depth] = new int[search.length+1];
			for(int i=0;i<search.length;i++){
				path[depth][i] = search[i];
			}
			
			//Store the length of the search results in the end of the arry
			path[depth][search.length] = max-min-1;
			
			/*
				Recursively check for words after the last search
				Example:
					Phone Number = 2336471
					min = 2
					max = 4
					
					Next search will be for two letter words with a code of 71
			*/
			generate_paths(max,MAX_WORD_SIZE,num,word_index,path,depth+1,valid_paths);
		}
		
		//Clear residual stored path values
		for(int i=depth;i<path.length;i++){
			path[i]=null;
		}
		
		//Recursively shrink max search size by one
		generate_paths(min,max-1,num,word_index,path,depth,valid_paths);
	}
	
	/**
		Display all valid paths using some neat formatting
		@param numbers Phone numbers
		@param valid_paths Storage of all valid paths of all phone numbers
		@param word_index dictionary of valid words
		@param output Output stream
	*/
	public static void print_paths(String[] numbers,int[][][][] valid_paths,String[][][] word_index,PrintWriter output){
		int spacing;
		String path_component;
		for(int number = 0; number<numbers.length;number++){
			
			//Check that a number contains atleast one path
			if(valid_paths[number]!=null && valid_paths[number][0]!=null){
				if(number>0){
					output.println("--------");
				}
				output.println("TEL: "+numbers[number]);
				
				//Iterate over all paths or up to MAX_PATHS
				for(int path=0;path<valid_paths[number].length && path<MAX_PATHS && valid_paths[number][path]!=null;path++){
					
					output.println(numbers[number].substring(0,3));
					spacing = 3;
					
					//Iterate through the path
					for(int j=0;j<valid_paths[number][path].length && valid_paths[number][path][j]!=null;j++){
						if(valid_paths[number][path][j].length-1 >= 0){
							spacing = spacing+(word_index[valid_paths[number][path][j][ valid_paths[number][path][j].length-1 ] ] [ valid_paths[number][path][j][0] ] [0]).length();
						}
						
						//Iterate over all possible same length permutations or up to MAX_NUMBER_PERMUTATIONS
						for(int k=0;k<valid_paths[number][path][j].length-1 && k < MAX_NUMBER_PERMUTATIONS;k++){							
							
							//Print the data
							output.printf("%"+spacing+"s\n",word_index[valid_paths[number][path][j][ valid_paths[number][path][j].length-1 ] ] [ valid_paths[number][path][j][k] ] [0]);
						}
					}
				}
			}
		}
		output.close();
	}
	
	/*
		Loads a file line by line and removes whitespace
		@param srcFileName file name to pull words from
		@returns Array of lines
	*/
	public static String[] read_file(String srcFileName) throws FileNotFoundException{
		File inFile = new File(srcFileName);
		String[] text = new String[countLines(inFile)];
		Scanner scan = new Scanner(inFile);
		String line;
		int count = 0;
		while(scan.hasNextLine()){
			line = scan.nextLine();
			
			//Cool lil regex to remove whitespace
			text[count] = line.replaceAll("\\s","");
			count++;
		}

		scan.close();
		return text;
	}
	
	/**
		Generates the numerical code of a word
		@param word Word to convert
		@return Code of word
	*/
	public static String gen_code(String word){
		char[] res = new char[word.length()];
		for(int i=0;i<res.length;i++){
			res[i] = WORD_NUMBER_INDEX[(word.toLowerCase()).charAt(i)-'a'];
		}
		return new String(res);
	}
	
	/**
		Standard factorial prorgram
		@param n Number to calculate with
		@return n!
	*/
	public static int factorial(int n){
		int fact = 1;
		for(int i=2;i<=n;i++) {
		    fact=fact*i;
		}
		return fact;
	}
	
    /**
    	Counts the number of valid words in a file
    	@param src Source file
    	@return number of words
    */
    public static int[] countWords(File src) throws FileNotFoundException{
        Scanner scan = new Scanner(src);
        int[] count = new int[MAX_WORD_SIZE];
		String word;
        while (scan.hasNext()){
			word = scan.next();
			if(isValidWord(word)){
				count[word.length()-1]++;
			}
        }
        scan.close();
        return count;
    }

	/**
    	Counts the number of lines in a file
    	@param src Source file
    	@return number of words
    */
    public static int countLines(File src) throws FileNotFoundException{
        Scanner scan = new Scanner(src);
		int count = 0;
        while (scan.hasNextLine()){
			scan.nextLine();
			count++;
        }
        scan.close();
        return count;
    }
    
    
    /**
    	Finds the indexes of a given element in an array
    	@param arr Array to search
    	@param search Element to search for
    	@param max Last relevent element of array to search
    	@return Indice of element, or -1 if not found
    */
    public static int[] find(String[][] arr,String search){
        int lower = binary_search(arr,0,arr.length-1,search);
        if(lower==-1){
        	return null;
        }
		int upper = lower;        
        for(;lower-1>=0 && arr[lower][1].equals(arr[lower-1][1]);lower--){}
        for(;upper+1<arr.length && arr[upper][1].equals(arr[upper+1][1]);upper++){}
        
        
        int[] index = new int[upper-lower+1];
        for(int i=0;i<index.length;i++){
        	index[i] = lower+i;
        }
        
        return index;
    }
    
    /**
    	Recursive binary search algorithm
    	@param arr Array to be searched
    	@param min Minimum index of segmented array
    	@param max Maximim index of segmented array
    	@param search Element to search for
    	@return Indice of element, or -1 if not found
    */
    public static int binary_search(String[][] arr,int min,int max,String search){
		int index = min+(max-min)/2;
		if(index<min||index>max){
			return -1;
		}
		if(arr[index][1].equals(search)){
			return index;
		}
		if(Integer.parseInt(arr[index][1]) > Integer.parseInt(search)){
			return binary_search(arr,min,index-1,search);
		}
		return binary_search(arr,index+1,max,search);
    }

   /**
		Confirms that a symbol is a valid alphabetic letter
		@param src source text
		@param i index of symbol
		@return boolean validity
	*/
    public static boolean isRawLet(String src, int i){
        return Character.toLowerCase(src.charAt(i)) >= 'a' && Character.toLowerCase(src.charAt(i)) <= 'z';
    }
    
	/**
		Confirms that a word is valid
		@param src source word
		@return boolean validity
	*/
    public static boolean isValidWord(String word){
		if(word.length()>MAX_WORD_SIZE){
			return false;
		}
		for(int i=0;i<word.length();i++){
			if(!isRawLet(word,i)){
				return false;
			}
		}
		return true;
	}
	
	
	/**
    	Begins recursive quicksort process of an array with maximum "max" value
    	@param arr Array to sort
    */
    public static void sort(String[][] arr){
        sort(arr,0,arr.length-1);
    }
    
    /**
    	Recursive quicksort method to sort an array. Calls itself with each partitioned array segment when min is less than max
    	@param arr Array to be operated on
    	@param min Minimum index to partition with
    	@param max Maximum index to partition with
    */
    public static void sort(String[][] arr, int min, int max){
        if(min<max){
            
            //Split array into two partitions and recurse
            int partition_index = partition(arr,min,max);
            sort(arr,min,partition_index-1);
            sort(arr,partition_index+1,max);
        }
    }
    
    /**
    	Iterates through the given partition, and swaps all elements less than the last value
    	@param arr Array to sort
    	@param min Index to iterate from
    	@param max Index to iterate to
    	@return Next index to partition by
    */
    public static int partition(String[][] arr, int min, int max){
        int index = min-1;
        
        //value to reflect around
        String pivot = arr[max][1];
        
        for(int i=min;i<max;i++){
            if(arr[i][1].compareTo(pivot) < 0){
                swap(arr,++index,i);
            }
        }
        swap(arr,index+1,max);
        return index+1;
    }
    
    /**
    	Swaps two values in an array
    	@param arr Array to be used
    	@param min Indice of first value
    	@param max Indice of second value
    */
    public static void swap(String[][] arr, int min, int max){
        String[] temp = arr[min];
        arr[min] = arr[max];
        arr[max] = temp;
    }
}
