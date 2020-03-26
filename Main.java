import java.io.*;
import java.util.ArrayList;

/*****************************************************************************
 * Author: Spencer Palmeter
 * CSC-285 Data Structures
 * Problem 5 Part 2
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
 ** CHANGES FOR PART 2 **
 * 
 * Implemented Matrix operations.
 * Created a new Matrix class and converted all ints to a Matrix objects
 * 
 *****************************************************************************/
class Main {
    public static void main(String[] args) {
        try {
            // make a PrintStream that will be used to redirect system logs to an output file
            PrintStream syslog = new PrintStream(new File("output .txt"));
            System.setOut(syslog);

            // define variable table
            char[] vartable = {'A','B','C','0','1','2','3','4','5','6','7','8','9'};
            // define integer value table. A=|1,2|, B=|6,6|, C=|1,2|
            //                               |3,4|    |8,8|    |2,1|
            Matrix[] intvalues = {
                new Matrix('m',1,2,3,4),
                new Matrix('m',6,6,8,8),
                new Matrix('m',1,2,2,1),
                new Matrix('c',0,0,0,0),
                new Matrix('c',1,1,1,1),
                new Matrix('c',2,2,2,2),
                new Matrix('c',3,3,3,3),
                new Matrix('c',4,4,4,4),
                new Matrix('c',5,5,5,5),
                new Matrix('c',6,6,6,6),
                new Matrix('c',7,7,7,7),
                new Matrix('c',8,8,8,8),
                new Matrix('c',9,9,9,9)
                };

            // define the operator characters
            char[] operatorChars = {'@','%','*','/','+','-',')','(','#'};
            // define the evaluation priorities for the operator characters, the higher the priority, the higher the value
            int[] operatorPriorities = {3,2,2,2,1,1,99,-99,100};

            // expression to evaluate 2*A-3*((B-2*C)/(A+3)-B*3)
            char[] expression = {'2','*','A','-','3','*','(','(','B','-','2','*','C',')','/','(','A','+','3',')','-','B','*','3',')','#'};
            
            // create the stacks for the operands and operators
            GenericStackManager<Matrix> operands = new GenericStackManager<>();
            GenericStackManager<OperatorObj> operators = new GenericStackManager<>();

            // we must initialize the operator stack with an end of expression character
            System.out.println("Initializing operator stack with operator # with priority of -100");
            operators.pushnode(new OperatorObj('#',-100));

            System.out.println("Solving the epxression: " + expToString(expression));
            
            // start evaluating each character in expression
            Matrix ivalue;
            int i=0;
            while(expression[i] != '#'){
                System.out.println("parsing " + expression[i]);
                // check if the character is an operator or an operand
                if(((expression[i] >= '0') && (expression[i] <= '9')) || ((expression[i] >= 'A') && (expression[i] <= 'C'))) {
                    System.out.println("This is an operand " + expression[i]);

                    // get the integer value of the operand at i
                    ivalue = findValue(expression[i], vartable, intvalues, vartable.length-1);

                    // check if the operand at i has an integer value in the value table
                    if(ivalue.values[0] == -99) System.out.println("There is no value in the table for " + expression[i]);

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
            } // end while
            // finish out the remaining operations
            while((operators.peeknode()).operator != '#'){
                popEvalPush(operators, operands);
            }
            // pop the final answer off the stack 
            Matrix expressionValue = operands.popnode();
            
            System.out.println("\nThe result for this expression is " + expressionValue.toString());
            System.out.println("END OF EVALUATION");
            System.out.println("------------------------------------------");
            // end for loop

        } catch (Exception e) {
            System.out.println(e);
        }
    } // end main method

    // converts char array to a string
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

    // alternate method for finding Matrix values
    public static Matrix findValue(char x, char[] varTable, Matrix[] intValues, int last){
        System.out.println("Entering findValue");
        int i;
        Matrix valueToReturn = new Matrix();
        for(i=0; i<=last; i++){
            if(varTable[i] == x) valueToReturn = intValues[i];
        }
        System.out.println("Found the value " + valueToReturn + " for " + x);
        return valueToReturn;
    } // end findValue

    
    // this method receives to operands and an operator and completes the operation
    // it returns a matrix object containing the result of the operation
    public static Matrix intEval(Matrix operand1, char operator, Matrix operand2){
        System.out.println("Entering intEval for: " + operand1 + operator + operand2);
        Matrix result = new Matrix();
        
        // logic for a constant operating on a constant
        if(operand1.type == 'c' && operand2.type == 'c'){
            switch(operator){
                case '+':
                    result = new Matrix(operand1.values[0] + operand2.values[0]);
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '-':
                result = new Matrix(operand1.values[0] - operand2.values[0]);
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '*':
                result = new Matrix(operand1.values[0] * operand2.values[0]);
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '/':
                    if(operand2.values[0] !=0){
                        result = new Matrix(operand1.values[0] / operand2.values[0]);
                        System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                        return result;
                    } else {
                        System.out.println("Error: attempted divide by zero");
                        result = new Matrix();
                        return result;
                    }
                case '%':
                    result = new Matrix(operand1.values[0] % operand2.values[0]);
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '@':
                    if(operand2.values[0] < 0){
                        System.out.println("Error: attempted exponentiation to a power less than 0");
                        return new Matrix();
                    } else if (operand2.values[0] == 0) {
                        result = new Matrix(1);
                        System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                        return result;
                    } else {
                        result = new Matrix(operand1.values[0]);
                        for(int i=1; i < operand2.values[0]; i++){
                            result = new Matrix(result.values[0] * operand1.values[0]);
                        }
                        System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                        return result;
                    }
                default:
                    System.out.println("Bad operator " + operator);
                    return new Matrix();
            } // end switch
        } // end if

        // logic for a constant operating on a matrix
        if(operand1.type == 'c' && operand2.type == 'm'){
            switch(operator){
                case '+':
                    result = new Matrix(operand2);
                    for(int i=0; i < 4; i++){
                        result.values[i] += operand1.values[0];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '-':
                    result = new Matrix(operand2);
                    for(int i=0; i < 4; i++){
                        result.values[i] = operand1.values[0] - result.values[i];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '*':
                    result = new Matrix(operand2);
                    for(int i=0; i < 4; i++){
                        result.values[i] *= operand1.values[0];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '/':
                    result = new Matrix(operand2);
                    for(int i=0; i < 4; i++){
                        result.values[i] = operand1.values[0] / result.values[i];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                default:
                    System.out.println(operator + " does not work with matrices!");
                    return new Matrix();
            } // end switch
        } // end if

        // logic for a matrix operating on a constant
        if(operand1.type == 'm' && operand2.type == 'c'){
            switch(operator){
                case '+':
                    result = new Matrix(operand1);
                    for(int i=0; i < 4; i++){
                        result.values[i] += operand2.values[0];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '-':
                    result = new Matrix(operand1);
                    for(int i=0; i < 4; i++){
                        result.values[i] -= operand2.values[0];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '*':
                    result = new Matrix(operand1);
                    for(int i=0; i < 4; i++){
                        result.values[i] *= operand2.values[0];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '/':
                    result = new Matrix(operand1);
                    for(int i=0; i < 4; i++){
                        result.values[i] /= operand2.values[0];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                default:
                    System.out.println(operator + " does not work with matrices!");
                    return new Matrix();
            } // end switch
        } // end if

        // logic for a matrix operating on a matrix
        if(operand1.type == 'm' && operand2.type == 'm'){
            switch(operator){
                case '+':
                    result = new Matrix(operand1);
                    for(int i=0; i < 4; i++){
                        result.values[i] += operand2.values[i];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '-':
                    result = new Matrix(operand1);;
                    for(int i=0; i < 4; i++){
                        result.values[i] -= operand2.values[i];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '*':
                    result = new Matrix(operand1);;
                    for(int i=0; i < 4; i++){
                        result.values[i] *= operand2.values[i];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                case '/':
                    result = new Matrix(operand1);;
                    for(int i=0; i < 4; i++){
                        result.values[i] = operand1.values[i] / operand2.values[i];
                    }
                    System.out.println("Evaluated " + operand1.values[0] + operator + operand2.values[0] + " Result: " + result.values[0]);
                    return result;
                default:
                    System.out.println(operator + " does not work with matrices!");
                    return new Matrix();
            } // end switch
        } // end if
        return result;
    } // end intEval

    // this method pops the top to operands and the top operator off the stack and evaluates the operation
    public static void popEvalPush(GenericStackManager<OperatorObj> x, GenericStackManager<Matrix> y){
        Matrix a,b,c = new Matrix();
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

// This class is used to create object stacks of Type T, it contains the helper functions
// pushnode, popnode and peeknode
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

// class for creating Matrices. Matrices contain an int[] which holds 4 values and also
// has a character type for each matrix. Matrices with type 'm' are true matrices, while
// matrices with type 'c' are used to define integer constants.
class Matrix {
    protected int[] values;
    protected char type;

    // constructor for a new matrix
    public Matrix(char mtype, int val1, int val2, int val3, int val4){
        type = mtype;
        values = new int[]{val1, val2, val3, val4};
    }

    // make a constant Matrix out of a given number. Constants conatin 4 of the same num in values
    public Matrix(int num){
        type = 'c';
        values = new int[]{num, num ,num ,num};
    }

    // makes a deep COPY of a matrix. Important when completing matrix operations
    // to avoid reference errors and overwriting of default matrix values
    public Matrix(Matrix x){
        values = new int[]{0,0,0,0};
        for(int i = 0; i < 4; i++){
            values[i] = x.values[i];
        }
        this.type = x.type;
    }

    // generic matrix constructor, used for bad operations
    public Matrix(){
        type = 'c';
        values = new int[]{-99,-99,-99,-99};
    }

    // get methods for type and values
    public char getType(){return type;}
    public int[] getValues(){return values;}

    // toString method for printing matrix values
    @Override
    public String toString() {
        String s;
        if(type == 'm'){
            s = String.format("\n|%d, %d|\n|%d, %d|\n", values[0],values[1],values[2],values[3]);
        } else {
            s = String.format("%d",values[0]);
        }
        return s;
    }

} // end Matrix
// end program