/*
 * This file is a guide for how you need to document your code.
 * First priority is javadoc documentation for methods, 
 * second priority is commenting your code so people know what's happening. 
 */

/**
 * Every javadoc comment starts with a simple one or two sentence summary of what the 
 * class/method does. This class is designed to show you how to document with javadoc.
 * 
 * <p>If you need to type more than one or two sentences, make sure you put an HTML tag
 * for a new paragraph before the rest of the description. Also, you can make things
 * <b>bold</b> and <i>italic</i> to make your documentation more readable and understandable. 
 * 
 * <p>One more important thing is that every javadoc comment must use a "/**" at the beginning,
 * and use asterisks all the way down to the last line. Most IDEs will automatically help you 
 * if you just start with /** and then make a newline. 
 * 
 * <p>Someone who has never seen your code before should be able to read it (along with your
 * documentation) and be able to understand what is happening.
 * 
 * @author [name of 1st person that worked on this class]
 * @author [name of 2nd person that worked on this class]
 * @author etc.
 * @see [another relevant class or method that you think people should look at]
 * @see etc.
 *
 */
public class HowToDocument{
	
	
	/*
	 * You can either document paramters, or you can give them clear names that convey 
	 * their function. For example, IMPORTANT_PARAMETER is a bad variable name but 
	 * NUMBER_OF_PEOPLE_NAMED_TOM is better. 
	 */
	private static final int IMPORTANT_PARAMETER = 3;
	private static final int NUMBER_OF_PEOPLE_NAMED_TOM = 1;
	

	/*
	 * The same goes for instance variables. Document them, or give them good names. 
	 * In this case, n is a bad name but sampleDoc is better. 
	 */
	private int n;
	private String sampleDoc; 
	
	/*
	 * Note: you should not use javadoc-style comments (with /**) for documenting variables. 
	 * Instead, use single-line comments (//) or multi-line comments(/*).
	 */
	
	/**
	 * This is a basic constructor that initializes a HowToDocument object with a number
	 * and a sample documentation string.
	 * 
	 * <p>Both constructors and methods need documentation. See the method below for a sample
	 * of how to document a method.
	 * 
	 * @param num the number of the object
	 * @param doc the sample documentation string
	 */
	public HowToDocument(int num, String doc){
		this.n = num;
		this.sampleDoc = doc;
	}
	
	/**
	 * This method returns the sum of the object's n value and a random number.
	 * 
	 * @param range the range in which the random added number can be generated
	 * @return the sum of the object's n value and a random number
	 * @see [a related method or class]
	 */
	public int getRandomizedNum(int range){
		return this.n + (int)(Math.random()*range);
	}
	

	// For trivial one-line methods like getters and setters, you don't need documentation.
	public int getNum(){
		return this.n;
	}
	public void setNum(int num){
		this.n = num;
	}
	
}
 