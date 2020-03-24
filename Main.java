import java.io.*;
import java.util.ArrayList;

/*****************************************************************************
 * Author: Spencer Palmeter
 * CSC-285 Data Structures
 * Problem 6 Part 2
 * 
 * This program reads an expression one character at at a time, and solves
 * the equation in one pass. The evaluator accepts six different operators
 * (*, /, +, -, %, @) and solves them in appropriate order according to their
 * level of priority. The expression to be solved is stored in an array of
 * characters itself, and each operator and operand are stored in their own
 * respective character arrays. Each character array has a corresponding value
 * table
 * 
 * This program uses ArrayLists to simulate a stack data structure, the parser
 * functions by pushing operands and operators onto their respective stacks.
 * Each operator is given a priority which determines when values from each
 * stack need to be popped and evaluated
 * 
 *****************************************************************************/
class Main {
    public static void main(String[] args) {
        try {
            // make a PrintStream that will be used to redirect system logs to an output file
            PrintStream syslog = new PrintStream(new File("output .txt"));
            System.setOut(syslog);

            // define variable table
            char[] vartable = {'A','B','C','D','E','F','0','1','2','3','4','5','6','7','8','9'};
            // define integer value table. A=8, B=12, C=2, D=3, E=15, F=4
            int[] intvalues = {8,12,2,3,15,4,0,1,2,3,4,5,6,7,8,9};

            // define the operator characters
            char[] operatorChars = {'@','%','*','/','+','-',')','(','#'};
            // define the evaluation priorities for the operator characters, the higher the priority, the higher the value
            int[] operatorPriorities = {3,2,2,2,1,1,99,-99,100};

            // expression1 A@(2*(A-C*D))+(9*B/(2*C+1)-B*3)+E%(F-A)
            char[] expression1 = {'A','@','(','2','*','(','A','-','C','*','D',')',')','+','(','9','*','B','/','(','2','*','C','+','1',')','-','B','*','3',')','+','E','%','(','F','-','A',')','#'};
            // expression2 B*(3@(A-D)%(B-C@D))+4@D*2
            char[] expression2 = {'B','*','(','3','@','(','A','-','D',')','%','(','B','-','C','@','D',')',')','+','4','@','D','*','2','#'};

            // put both expressions to solve in an array
            char[][] expressionsToSolve = {expression1, expression2};

            // loop through the array of expressions, solve each expression and print
            for(char[] expression: expressionsToSolve){

                // create the stacks for the operands and operators
                GenericStackManager<Integer> operands = new GenericStackManager<>();
                GenericStackManager<OperatorObj> operators = new GenericStackManager<>();

                // we must initialize the operator stack with an end of expression character
                System.out.println("Initializing operator stack with operator # with priority of -100");
                operators.pushnode(new OperatorObj('#',-100));

                System.out.println("Solving the epxression: " + expToString(expression));
                
                // start evaluating each character in expression1
                int ivalue;
                int i=0;
                while(expression[i] != '#'){
                    System.out.println("parsing " + expression[i]);
                    //output.println("parsing " + expression[i]);
                    // check if the character is an operator or an operand
                    if(((expression[i] >= '0') && (expression[i] <= '9')) || ((expression[i] >= 'A') && (expression[i] <= 'F'))) {
                        System.out.println("This is an operand " + expression[i]);

                        // get the integer value of the operand at i
                        ivalue = findValue(expression[i], vartable, intvalues, vartable.length-1);

                        // check if the operand at i has an integer value in the value table
                        if(ivalue == -99) System.out.println("There is no value in the table for " + expression[i]);

                        // we have an operand, now we need to push it on the stack
                        System.out.println("Pushing " + ivalue + " onto the operand stack");
                        operands.pushnode(ivalue);

                    } else {
                        System.out.println("This is an operator " + expression[i]); 

                        if(expression[i] == '('){
                            System.out.println("pushing on operator stack " + expression[i]);
                            // create the node to push on the stack
                            OperatorObj nodeToPush = new OperatorObj(expression[i], -99);
                            operators.pushnode(nodeToPush);
                        } else if(expression[i] == ')') {
                            // operator is close parentheis, must start popping and evaluating until we reach open parenthesis
                            while(operators.peeknode().operator != '(') {
                                popEvalPush(operators, operands);
                            }
                            // now pop the ( node
                            operators.popnode();
                        } else {
                            // character is neither ( or ) and is another operator
                            int opPriority = findValue(expression[i], operatorChars, operatorPriorities, 7);
                            System.out.println("Peeking at top of stack " + operators.peeknode().priority);
                            while(opPriority <=(operators.peeknode().priority)){
                                popEvalPush(operators, operands);
                            }
                            OperatorObj nodeToPush = new OperatorObj(expression[i], opPriority);
                            operators.pushnode(nodeToPush);
                        }
                    }
                    i++;
                }
                // finish out the remaining operations
                while((operators.peeknode()).operator != '#'){
                    popEvalPush(operators, operands);
                } 
                int expressionValue = operands.popnode();

                System.out.println("\nThe result for this expression is " + expressionValue);
                System.out.println("END OF EVALUATION");
                System.out.println("------------------------------------------");
            } // end for loop

        } catch (Exception e) {
            System.out.println(e);
        }
    } // end main method

    public static String expToString(char[] expression){
        String s = "";
        for(char character: expression) s+= character;
        return s;
    }


    // this method finds the character x in the variable table and returns the corresponding value from intValues
    public static int findValue(char x, char[] varTable, int[] intValues, int last){
        System.out.println("Entering findValue");
        int i, valueToReturn = -99;
        for(i=0; i<=last; i++){
            if(varTable[i] == x) valueToReturn = intValues[i];
        }
        System.out.println("Found the value " + valueToReturn + " for " + x);
        return valueToReturn;
    } // end findValue

    public static int intEval(int operand1, char operator, int operand2){
        System.out.println("Entering intEval for: " + operand1 + operator + operand2);
        int result;
        switch(operator){
            case '+':
                result = operand1 + operand2;
                System.out.println("Evaluated " + operand1 + operator + operand2 + " Result: " + result);
                return result;
            case '-':
                result = operand1 - operand2;
                System.out.println("Evaluated " + operand1 + operator + operand2 + " Result: " + result);
                return result;
            case '*':
                result = operand1 * operand2;
                System.out.println("Evaluated " + operand1 + operator + operand2 + " Result: " + result);
                return result;
            case '/':
                if(operand2 !=0){
                    result = operand1 / operand2;
                    System.out.println("Evaluated " + operand1 + operator + operand2 + " Result: " + result);
                    return result;
                } else {
                    System.out.println("Error: attempted divide by zero");
                    return -99;
                }
            case '%':
                result = operand1 % operand2;
                System.out.println("Evaluated " + operand1 + operator + operand2 + " Result: " + result);
                return result;
            case '@':
                if(operand2 < 0){
                    System.out.println("Error: attempted exponentiation to a power less than 0");
                    return -99;
                } else if (operand2 == 0) {
                    result = 1;
                    System.out.println("Evaluated " + operand1 + operator + operand2 + " Result: " + result);
                    return result;
                } else {
                    result = operand1;
                    for(int i=1; i < operand2; i++){
                        result = result * operand1;
                    }
                    System.out.println("Evaluated " + operand1 + operator + operand2 + " Result: " + result);
                    return result;
                }
            default:
                System.out.println("Bad operator " + operator);
                return -99;
        } // end switch
    } // end intEval

    public static void popEvalPush(GenericStackManager<OperatorObj> x, GenericStackManager<Integer> y){
        int a,b,c;
        char operator;
        // pop off the top operator
        operator = x.popnode().getOperator();

        // pop off the top two operands
        a=y.popnode();
        b=y.popnode();

        System.out.println("In popEvalPush, evaluating "+ b + operator + a);
        // get the value for the operation
        c=intEval(b, operator, a);
        System.out.println("Pushing operand " + c + " onto the stack");
        // push the result back onto the operand stack
        y.pushnode(c);
        return;
    } // end popEvalPush

} // end main

class GenericStackManager<T> {

    protected ArrayList<T> myStack;
    protected int number;

    // Stack Manager constructor
    public GenericStackManager() {
        // initialize stack and allocate some memory
        number = 0;
        myStack = new ArrayList<>(100);
    }

    // returns number of items currently on stack
    public int getNumber(){return number;}

    // pushes node x onto the top of the stack
    public int pushnode(T x){
        System.out.println("entering pushnode");
        myStack.add(number,x);
        number++;
        return number;
    }

    // get top node value, remove node from the stack, decrement number and return the node value
    public T popnode(){
        System.out.println("entering popnode");
        T nodevalue;
        nodevalue = myStack.get(number-1);
        myStack.remove(number-1);
        number--;
        return nodevalue;
    }
    // peeks at the node at the top of the stack and returns its value. Does not remove the node from the stack
    public T peeknode(){
        T nodevalue;
        nodevalue = myStack.get(number-1);
        return nodevalue;
    }

} // end GenericStackManager

// defines the Operator Object which contains the operator character and a priority
class OperatorObj {
    protected char operator;
    protected int priority;

    // constructor for an Operator object
    public OperatorObj(char operator, int priority){
        this.operator = operator;
        this.priority = priority;
    }
    // get methods for operator and priority
    public char getOperator(){return operator;}
    public int getPriority(){return priority;}

} // end OperatorObj
