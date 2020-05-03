public class ALU{
    String ALUOp;
    String Op1;
    String Op2;
    public ALU(String ALUOp, String Op1, String Op2){
        this.ALUOp=ALUOp;
        this.Op1=Op1;
        this.Op2=Op2;
    }
    public String ALUCont() {
        switch(ALUOp){
            case "000":{
                // Sub
                return this.subOp(Op1, Op2);	//-> Sub
            }
            case "001":{
                //Add
                return this.addOp(Op1, Op2); //-> SUB
            }
            case "010":{
                
                return "";
            }
            default:return "Wrong ALUOp Signal";
        }
    }
    public String subOp(String Operand1,String Operand2) {
        return Processor.toBinary((int)Long.parseLong(Operand1,2) - (int)Long.parseLong(Operand2,2));
    }
    public String addOp(String Operand1, String Operand2) {
        return Processor.toBinary((int)Long.parseLong(Operand1,2) + (int)Long.parseLong(Operand2,2));
    }
    public String MultiOp(String Op1,String Op2){
        return Processor.toBinary((Integer.parseInt(Op1,2))*(Integer.parseInt(Op2,2)));
    } 
    public static String ANDOp(String Operand1,String Operand2) {
        return Processor.toBinary((int)Long.parseLong(Operand1,2) & (int)Long.parseLong(Operand2,2));
    }
    public static String OROp(String Operand1,String Operand2) {
        return Processor.toBinary((int)Long.parseLong(Operand1,2) | (int)Long.parseLong(Operand2,2));
    }
    
    
    public static String sltOp(String Operand1, String Operand2) {
        return Processor.toBinary((((int)Long.parseLong(Operand1,2)<(int)Long.parseLong(Operand2,2))? 1 : 0));
    }
}