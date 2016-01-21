package utils;
/**
 * @author Cem Akpolat & Mursel Yildiz
 *
 */
public class ConsoleMessageInterpreter {
	/*default Server Port number will be assigned by user*/
	public int serverPortNumber=0;
	/*default Client Port number will be assigned by user*/
	public int clientPortNumber=0;
	public boolean exitSignal=false;
	public String className;
	
	public ConsoleMessageInterpreter(String[] args){
		//if args=0 there is any reaction
		className=this.getClass().getName();
		for(int place=0;place<args.length;place++){
			
			if(args[place].equalsIgnoreCase("-sp")){
				
				try {
					serverPortNumber = Integer.parseInt(args[place+1]);
				}
				catch(NumberFormatException nFE) {
					BlackBoard.writeConsole(className,"Not an Integer, please give an unused Port Number");
				}
				
				place++;//because of port number
			}
			else if(args[place].equalsIgnoreCase("-cp")){
				try {
					clientPortNumber = Integer.parseInt(args[place+1]);
				}
				catch(NumberFormatException nFE) {
				    BlackBoard.writeConsole(className,"Not an Integer, please give an unused Port Number");
				}
				place++;//because of port number
			}
			else if(args[place].equalsIgnoreCase("exit")){
				System.exit(0);//check that
				exitSignal=true;
			}
			
		}
	}
	
}
