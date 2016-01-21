package utils;

/**
 * Blackboard is one of the helper class which is utilized in order to print out the messages of this system.
 * 
 * @author Cem Akpolat & Mursel Yildiz
 *
 */
public class BlackBoard {

	private static BlackBoard instance = new BlackBoard();
	/**
	 * Provide only one instance of this Class
	 */
	BlackBoard(){
		
	}
	/**
	 * 
	 * @return
	 */
	
	public static BlackBoard getInstance() {
		return instance;
	}
	/**
	 *This function prints out what it receives as String. 
	 * @param line
	 */
	public static void writeConsole(String line){
		System.out.println(line);
	}
	/**
	 * This function prints out what it receives along with the caller class name as string.
	 * @param className
	 * @param line
	 */
	
	public static void writeConsole(String className,String line){
		System.out.println(className+": "+line);
	}
	/*/**
	 * 
	 * @param className
	 * @param todo
	 */
	/*public static void toDo(String className,String todo){
		System.out.println("TODO:"+className+": "+todo);
	}
	*/
}
