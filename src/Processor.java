import java.io.*;

/**
 * This class is our project's main class and contains all the stages.
 */
public class Processor {
	public static InstructionMemory IM = new InstructionMemory();
	public static ProgramCounter PC = new ProgramCounter();
	/**
	 * Contains the instruction currently in use in 32 bit Binary Format Example :
	 * 00000001101011100111100000100101
	 */
	public static String CI = "";
	// modules
	private RegisterFile registerFile;
	private Control control;
	// pipeline registers
	private String[] ifIdRegisters;
	private Object[] idExRegisters;
	//private Object[] exMemRegisters;
	private Object[] memWbRegisters;
	// outputs
	private String[] executeControlSignals;
	private String[] memoryControlSignals;
	private String[] writeBackControlSignals;
	private String readData1;
	private String readData2;

	// constructor
	public Processor() {
		registerFile = new RegisterFile(this);
		control = new Control(this);

		ifIdRegisters = new String[2];
		idExRegisters = new Object[8];
		// exMemRegisters = new Object[6];
		memWbRegisters = new Object[4];
	}

	public static void main(String[] args) throws IOException {
		System.out.println("SMArchitecture");
		Load();
		while (PC.Get()<IM.Size) {
			Fetch();

		}
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
	 *Converts from int to 32 bits Binary String.
	 * <p>
	 *Example toBinary(3):"00000000000000000000000000000011"
	 */
	public static String toBinary(int x) {
		StringBuilder result = new StringBuilder();
		for (int i = 31; i >= 0; i--) {
			int mask = 1 << i;
			result.append((x & mask) != 0 ? 1 : 0);
		}
		return result.toString();
	}

	/**
	 * Gets next instruction from IM bassed on PC Increments PC by 1 
	 * <p> Prints Next PC,Instruction
	 */
	public static void Fetch() {
		IM.MemRead(0);
		CI = IM.MemRead(PC.Get());
		PC.Increment();
		System.out.println("Next PC: " + toBinary(PC.Get()) + "\n" + "Instruction: " + CI);
	}

	// setters
	public void setReadData1(String readData1) {
		this.readData1 = readData1;
	}

	public void setReadData2(String readData2) {
		this.readData2 = readData2;
	}

	public void setExecuteControlSignals(String[] executeControlSignals) {
		this.executeControlSignals = executeControlSignals;
	}

	public void setMemoryControlSignals(String[] memoryControlSignals) {
		this.memoryControlSignals = memoryControlSignals;
	}

	public void setWriteBackControlSignals(String[] writeBackControlSignals) {
		this.writeBackControlSignals = writeBackControlSignals;
	}

	public static String convertDecToBinUnsigned(int decimalNumber) {

		String result = "";
		for (int i = 0; i < 32; i++) {
			int remainder = decimalNumber % 2;
			decimalNumber /= 2;
			result = remainder + result;
		}
		return result;
	}

	public static int convertBinToDecUnsigned(String binaryNumber) {

		int result = 0;
		int power = 0;
		for (int i = binaryNumber.length() - 1; i >= 0; i--) {
			int x = 0;
			if (binaryNumber.charAt(i) == '1')
				x = 1;
			result += (int) (Math.pow(2, power)) * x;
			power++;
		}

		return result;
	}

	public String SignExtend(String immediate) {

		if (immediate.charAt(0) == '1')
			for (int i = 0; i < 20; i++)
				immediate = "1" + immediate;
		else
			for (int i = 0; i < 20; i++)
				immediate = "0" + immediate;

		return immediate;
	}

	// stages
	public void InstructionDecodeStage() {

		// get the values from the previous stage
		String pc = ifIdRegisters[0];
		String instruction = ifIdRegisters[1];

		// getting fields from the instruction
		String opcode = "";
		String rd = "";
		String r1 = "";
		String r2 = "";
		String immediate = "";

		for (int i = 0; i < 5; i++)
			opcode += instruction.charAt(i);
		for (int i = 5; i < 10; i++)
			rd += instruction.charAt(i);
		for (int i = 10; i < 15; i++)
			r1 += instruction.charAt(i);
		for (int i = 15; i < 20; i++)
			r2 += instruction.charAt(i);
		for (int i = 20; i < 32; i++)
			immediate += instruction.charAt(i);

		// control unit
		control.decode(opcode);

		// register file
		registerFile.read(r1, r2);

		// sign extend
		immediate = SignExtend(immediate);

		// save outputs to pipeline registers
		idExRegisters[0] = writeBackControlSignals;
		idExRegisters[1] = memoryControlSignals;
		idExRegisters[2] = executeControlSignals;
		idExRegisters[3] = pc;
		idExRegisters[4] = readData1;
		idExRegisters[5] = readData2;
		idExRegisters[6] = immediate;
		idExRegisters[7] = rd;
	}

	public void WriteBack() {

		// get the values from the previous stage
		String[] writeBackControlSignals = (String[]) memWbRegisters[0];
		String aluResult = (String) memWbRegisters[2];
		String memoryReadData = (String) memWbRegisters[1];
		String writeRegister = (String) memWbRegisters[3];

		// choose which data to write
		String writeData;
		if (writeBackControlSignals[1].equals("0"))
			writeData = aluResult;
		else
			writeData = memoryReadData;

		// register file
		registerFile.write(writeRegister, writeData, writeBackControlSignals[0]);
	}
	// helper methods

	// stages
	/**
	 * TestCases 
	 * Fetch 
	 * 00100000000010000000000000000101
	 * 10001110000010010000000000000000 
	 * 00000001010010110110000000100000
	 * 00000001101011100111100000100101 
	 * 10101110001100100000000000000000
	 * 00010010000000000000000011111111 
	 * Decode 
	 * Execute 
	 * Memory 
	 * WriteBack
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