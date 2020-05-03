public class InstructionMemory {
    int [] Memory ;

    public InstructionMemory()
    {
        Memory = new int [1024];
    }
    public void MemWrite(int Address,int Instruction)
    {
        this.Memory[Address]=Instruction;
    }
    public int MemRead(int Address)
    {
        return this.Memory[Address];
    }    
    // public static void main(String[] args) {
    //     DataMemory x = new DataMemory();
    //     x.MemWrite(10, 10);
    //     System.out.println(x.MemRead(11));
    // }
}
