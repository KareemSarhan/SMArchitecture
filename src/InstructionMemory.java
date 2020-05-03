import java.util.Vector;

public class InstructionMemory {
    int Size = 0;
	Vector<String> Memory ;
    public InstructionMemory()
    {
        Memory =new Vector<String>();
        Size=this.Memory.size();
    }
    /**
	 * Writes Instruction to the next empty cell in InstructionMemory.
	 */
    public void MemWrite(String Instruction)
    {
        this.Memory.add(Instruction);
        this.Size=this.Memory.size();
    }
     /**
	 * Reads Instruction in the given Memory Address.
	 */
    public String MemRead(int Address)
    {
        return this.Memory.get(Address);
    }
    // public static void main(String[] args) {
    //     DataMemory x = new DataMemory();
    //     x.MemWrite(10, 10);
    //     System.out.println(x.MemRead(11));
    // }
}
