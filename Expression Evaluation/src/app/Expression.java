package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";

	/**
	 * Populates the vars list with simple variables, and arrays lists with arrays
	 * in the expression. For every variable (simple or array), a SINGLE instance is created 
	 * and stored, even if it appears more than once in the expression.
	 * At this time, values for all variables and all array items are set to
	 * zero - they will be loaded from a file in the loadVariableValues method.
	 * 
	 * @param expr The expression
	 * @param vars The variables array list - already created by the caller
	 * @param arrays The arrays array list - already created by the caller
	 */
	public static void 
	makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		/** COMPLETE THIS METHOD **/
		/** DO NOT create new vars and arrays - they are already created before being sent in
		 ** to this method - you just need to fill them in.
		 **/
		for(int i = 0; i<expr.length(); i+=0){
			char c = expr.charAt(i);
			if(Character.isLetter(c) == true){
				boolean isVar = true;
				String varName = c + "";
				for(int x = i+1; x<expr.length(); x++){
					char r = expr.charAt(x);
					if(Character.isLetter(r) == true){
						varName += r; 
						continue;
					}
					else if(r == '['){
						isVar = false;
						break;
					}
					else
						break;
				}
				if(isVar == true){
					Variable temp = new Variable(varName);
					vars.add(temp);
				}
				else{
					Array temp = new Array(varName);
					arrays.add(temp);
				}
				i = i + varName.length() + 1;
			}
			else{
				i++;
				continue;
			}

		}

	}

	/**
	 * Loads values for variables and arrays in the expression
	 * 
	 * @param sc Scanner for values input
	 * @throws IOException If there is a problem with the input 
	 * @param vars The variables array list, previously populated by makeVariableLists
	 * @param arrays The arrays array list - previously populated by makeVariableLists
	 */
	public static void 
	loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String tok = st.nextToken();
			Variable var = new Variable(tok);
			Array arr = new Array(tok);
			int vari = vars.indexOf(var);
			int arri = arrays.indexOf(arr);
			if (vari == -1 && arri == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				vars.get(vari).value = num;
			} else { // array symbol
				arr = arrays.get(arri);
				arr.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok," (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;              
				}
			}
		}
	}

	/**
	 * Evaluates the expression.
	 * 
	 * @param vars The variables array list, with values for all variables in the expression
	 * @param arrays The arrays array list, with values for all array items
	 * @return Result of evaluation
	 */
	public static float 
	evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		/** COMPLETE THIS METHOD **/
		expr = expr.replaceAll(" ", "");
		float result = 0;

		if(expr.length() == 1 && Character.isDigit(expr.charAt(0))) //base case: single digit constant
			return Character.getNumericValue(expr.charAt(0));

		boolean digit = true;										//base case: non-single digit constant
		for(int i = 0; i<expr.length(); i++){
			if(!Character.isDigit(expr.charAt(i)))
				digit = false;
		}
		if(digit == true)
			return stringToint(expr);


		boolean isVar = false, isArray = false;
		int varIndex = 0, arrIndex = 0;

		if(Character.isLetter(expr.charAt(0))==true){					
			String nameV = "";
			int size = 0;
			for(int i = 0; i<expr.length(); i++){		//retrieves the full name of the variable/array
				if(Character.isLetter(expr.charAt(i))){
					nameV = nameV + expr.charAt(i);
					size++;
				}else
					break;
			}
			for(int i = 0; i<vars.size(); i++){		//base case: checks if name is inside the vars Arraylist
				if(vars.get(i).name.equals(nameV)){	//checks where it is inside the var 
					varIndex = i;
					isVar = true;
					break;
				}
			}
			if(isVar == true && expr.length()==size)
				return vars.get(varIndex).value;

			for(int i = 0; i<arrays.size(); i++){		//base case: if not in the vars Arraylist,
				if(arrays.get(i).name.equals(nameV)){	//checks if name is inside the arrays ArrayList
					isArray = true;						//checks where it is inside the arrays
					arrIndex = i;
					break;
				}
			}
			if(isArray == true){						//if it is an array, then it finds that array's closing 
				int b = expr.indexOf(nameV)+size;		//and opening []'s
				int c = 0;
				int c2 = 0;

				int numisThere = 1;
				for(int i = b+1; i<expr.length(); i++){		
					if(expr.charAt(i) == '['){
						numisThere++;
					}
					if(expr.charAt(i) == ']'){
						numisThere--;
						if(numisThere==0){
							c = i;
							c2 = i;
							break;
						}
					}
				}

				if(expr.charAt(c-2)=='.' && expr.charAt(c-1)=='0')						
					c = c-2;
				float index = (int)evaluate(expr.substring(b+1, c), vars, arrays);	//index is what's evaluated inside []
				if(c2 == expr.length()-1)
					return arrays.get(arrIndex).values[(int) index];			//retrieves the array's value at index
			}
		}

		int par = 0;															//check for parentheses 
		boolean isPar = false;
		for(par = 0; par<expr.length();par++){ 								
			if(expr.charAt(par) == '('){
				isPar = true;
				break;
			}
		}
		int endPar = 0;									//if there is an open parenthesis, then find the closing one
		if(isPar == true){
			for(endPar = expr.length()-1; endPar > par;endPar--){ 								
				if(expr.charAt(endPar) == ')')
					break;
			}
		}
		if(endPar!=0){ 			//recursion to solve what is inside the parenthesis and the rest of the expression
			String inAns;
			if(par==0 && endPar == expr.length()-1)			
				return evaluate(expr.substring(par+1, endPar), vars, arrays);
			else if(par == 0){
				inAns = evaluate(expr.substring(par+1, endPar), vars, arrays) + "";
				inAns = inAns.substring(0,inAns.length()-2);
				return evaluate(inAns + expr.substring(endPar+1, expr.length()), vars, arrays);
			}else if(endPar == expr.length()-1){
				inAns = evaluate(expr.substring(par+1, endPar), vars, arrays) + "";
				inAns = inAns.substring(0,inAns.length()-2);
				return evaluate(expr.substring(0, par) + inAns, vars, arrays);
			}else{
				inAns = evaluate(expr.substring(par+1, endPar), vars, arrays) + "";
				inAns = inAns.substring(0,inAns.length()-2);
				return evaluate(expr.substring(0, par) + inAns + expr.substring(endPar+1, expr.length()), vars, arrays);
			}
		}

		String r1 = "";
		String r2 = "";
		int m;
		int traverse = -1;
		int isThere2 = 0;
		int isThere2Size = 0;
		
		for(m = expr.length()-1; m>=0 ;m--){ 			//check for + and - first
			if(expr.charAt(m) == ']'){					//There is an array, use for-loop to jump over array
				isThere2++;
				for(traverse = m-1; traverse>=0; traverse--){	
					isThere2Size++;
					if(expr.charAt(traverse) == '['){
						isThere2--;
						if(isThere2==0)
							break;
					}
					if(expr.charAt(traverse) == ']')
						isThere2++;
				}
			}
			if(traverse!=-1){							//now m is out of the array's []
				m = m-isThere2Size-2;
			}
			if(m>0){									//if there is still code left, then keep checking for '+' and '-'
				if(expr.charAt(m) == '+' || expr.charAt(m) == '-')
					break;
			}
		}
		if(m<0){										//check for * and / if + and - not found
			for(m = expr.length()-1; m>=0 ;m--){
				if(expr.charAt(m) == '*' || expr.charAt(m) == '/')
					break;
			}
		}
		if(m!=0){										//separates the expression into two where the operator is
			r1 = expr.substring(0, m);
			r2 = expr.substring(m+1, expr.length());
		}

		if(!r1.equals("") && !r2.equals("")){			//use recursion to solve basic expressions
			switch(expr.charAt(m)){
			case '+':
				result = evaluate(r1, vars, arrays) + evaluate(r2, vars, arrays);
				break;
			case '-':
				result = evaluate(r1, vars, arrays) - evaluate(r2, vars, arrays);
				break;
			case '*':
				result = evaluate(r1, vars, arrays) * evaluate(r2, vars, arrays);
				break;
			case '/':
				result = evaluate(r1, vars, arrays) / evaluate(r2, vars, arrays);
				break;
			}
		}
		return result;
	}
	private static int stringToint( String str ){	//because evaluate only returns floats, you need this method
		int i = 0, number = 0;						//to return any constant as an integer
		boolean isNegative = false;
		int len = str.length();
		if( str.charAt(0) == '-' ){
			isNegative = true;
			i = 1;
		}
		while( i < len ){
			number *= 10;
			number += ( str.charAt(i++) - '0' );
		}
		if( isNegative )
			number = -number;
		return number;
	}   
}
