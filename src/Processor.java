import java.io.*;

/**
 * This class is our project's main class and contains all the stages.
 */
public class Processor {
	public static InstructionMemory IM = new InstructionMemory();
	public static ProgramCounter PC = new ProgramCounter();
	/**
	 * Contains the instruction currently in use in 32 bit Binary Format 
	 * Example : 00000001101011100111100000100101
	 */
	public static String CI = "";

	public static void main(String[] args) throws IOException {
		System.out.println("SMArchitecture");
	}
	/**
	 * Loads User input to the memory
	 */
	public static void Load() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		for (String Instruction = br.readLine(); !Instruction.isEmpty(); Instruction = br.readLine()) {
			IM.MemWrite(Instruction);
		}
	}
	/**
	 * Converts from int to 32 bits Binary String .
	 * Example toBinary(3): "00000000000000000000000000000011"
	 */
	public static String toBinary(int x) {
        StringBuilder result = new StringBuilder();
        for (int i = 31; i >= 0 ; i--) {
            int mask = 1 << i;
            result.append((x & mask) != 0 ? 1 : 0);
        }
        return result.toString();
	}
	/**
	 * Gets next instruction from IM bassed on PC
	 * Increments PC by 1
	 * Print Next PC,Instruction
	 */
	public static void Fetch() {
		IM.MemRead(0);
		CI = IM.MemRead(PC.Get());
		PC.Increment();
		System.out.println
		(
		"Next PC: "+toBinary(PC.Get())+"\n"+
		"Instruction: "+CI
		);
	}
		/**
	 * TestCases 
	 * Fetch 
00100000000010000000000000000101
10001110000010010000000000000000 
00000001010010110110000000100000
00000001101011100111100000100101 
10101110001100100000000000000000
00010010000000000000000011111111 
	 * Decode Execute Memory WriteBack
	 */
	/**
	 * Describe any added variable's use and write an one example or more to what it
	 * will contain.
	 */
	/**
	 * Write comments you want to leave here !!
	 * 
	 */
}