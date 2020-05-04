import java.io.*;
import java.util.Hashtable;

/**
 * This class is our project's main class and contains all the stages.
 */
public class Processor {
	static String ALUresult;
	static boolean zero;
	public static ProgramCounter PC = new ProgramCounter();
	public static InstructionMemory IM = new InstructionMemory();
	/**
	 * Contains the instruction currently in use in 32 bit Binary Format 
	 * <p> Example : "00000001101011100111100000100101"
	 */
	public static String CI = "";
	// modules
	public RegisterFile registerFile;
	public Control control;
	public DataMemory dataMemory;
	// pipeline registers
	public Hashtable<String, String> ifIdRegisters;
	public Hashtable<String, Object> idExRegisters;
	public Hashtable<String, Object> exMemRegisters;
	public Hashtable<String, Object> memWbRegisters;
	// outputs
	public String[] executeControlSignals;
	public String[] memoryControlSignals;
	public String[] writeBackControlSignals;
	public String readData1;
	public String readData2;
	public String ALUOp;
	public String ALUSrc;
	public String Immediate;
	public String memoryReadData;
	// constructor
	public Processor() {
		registerFile = new RegisterFile(this);
		control = new Control(this);
		dataMemory = new DataMemory(this);
		
		// ifId ={PC, CI}
		ifIdRegisters = new Hashtable<String, String>();
		//idEx={writeBackControlSignals, memoryControlSignals, executeControlSignals, PC5.readData1,
		// 			readData2, immediate, writeRegister}
		idExRegisters = new Hashtable<String, Object>();
		//exMem={writeBackControlSignals, memoryControlSignals, ALUresult, readData2, writeRegister}
		exMemRegisters = new Hashtable<String, Object>();
		//memWb={writeBackControlSignals, memoryReadData, ALUresult, writeRegister}
		memWbRegisters = new Hashtable<String, Object>();
		
	}

	public static void main(String[] args) throws IOException {
		
		Processor p = new Processor();
		
		System.err.println("SMArchitecture");
		Load();
		int clockCycle = 0;
		String instructionInFetch = "";
		String instructionInDecode = "";
		String instructionInExecute = "";
		String instructionInMemory = "";
		String instructionInWriteBack = "";
		
		while (PC.Get()<IM.Size) {
			
			System.out.println("Clock cycle " + clockCycle);
			instructionInFetch = IM.MemRead(PC.Get());
			
			if (clockCycle==0) {
				System.out.println();
				System.out.println("Instruction: " + instructionInFetch + " is in Fetch stage");
				Fetch();
			}
			else if (clockCycle==1) {
				System.out.println();
				System.out.println("Instruction: " + instructionInFetch + " is in Fetch stage");
				Fetch();
				System.out.println();
				System.out.println("Instruction: " + instructionInDecode + " is in Decode stage");
				p.instructionDecode();
			}
			else if (clockCycle==2) {
				System.out.println();
				System.out.println("Instruction: " + instructionInFetch + " is in Fetch stage");
				Fetch();
				System.out.println();
				System.out.println("Instruction: " + instructionInDecode + " is in Decode stage");
				p.instructionDecode();
				System.out.println();
				System.out.println("Instruction: " + instructionInExecute + " is in Execute stage");
				//execute
			}
			else if (clockCycle==3) {
				System.out.println();
				System.out.println("Instruction: " + instructionInFetch + " is in Fetch stage");
				Fetch();
				System.out.println();
				System.out.println("Instruction: " + instructionInDecode + " is in Decode stage");
				p.instructionDecode();
				System.out.println();
				System.out.println("Instruction: " + instructionInExecute + " is in Execute stage");
				//execute
				System.out.println();
				System.out.println("Instruction: " + instructionInMemory + " is in Memory Access stage");
				p.memoryAccess();
			}
			else {
				System.out.println();
				System.out.println("Instruction: " + instructionInFetch + " is in Fetch stage");
				Fetch();
				System.out.println();
				System.out.println("Instruction: " + instructionInDecode + " is in Decode stage");
				p.instructionDecode();
				System.out.println();
				System.out.println("Instruction: " + instructionInExecute + " is in Execute stage");
				//execute
				System.out.println();
				System.out.println("Instruction: " + instructionInMemory + " is in Memory Access stage");
				p.memoryAccess();
				System.out.println();
				System.out.println("Instruction: " + instructionInWriteBack + " is in Write Back stage");
				p.writeBack();
			}
			System.out.println();
			System.out.println("--------------------------------------------------------------------------");
			System.out.println();
			
			clockCycle++;
			//shifting instructions
			instructionInWriteBack = instructionInMemory;
			instructionInMemory = instructionInExecute;
			instructionInExecute = instructionInDecode;
			instructionInDecode = instructionInFetch;
			//shifting registers
			
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
	public void Execute() {
		// getting the inputs required for the execution
		//executeControlSignals = {ALUControl, ALUSrc, Jump, Branch}
		String [] exeControlSignals= (String[]) idExRegisters.get("executeControlSignals");
		String ALUOp=executeControlSignals[0];
		String Branch=exeControlSignals[3];
		if(Branch.equals("01")){
			//read1 and read2 bytgabo mnen fel instruction fel decode?????
			ALU alu=new ALU(ALUOp, readData1, readData2);
			ALUresult=alu.ALUCont();
			if(Integer.parseInt(ALUresult,2)==0)
				//Set the PC
				PC.Set(Integer.parseInt(Immediate,2));
			return ;
		}
		if(Branch.equals("10")){
			ALU alu=new ALU(ALUOp, readData1, readData2);
			ALUresult=alu.ALUCont();
			if((int)Long.parseLong(ALUresult,2) <0)
				//Set the PC
				PC.Set(Integer.parseInt(Immediate,2));
			return;
		}
		String Jump=exeControlSignals[2];
		if(Jump.equals("1")){
			PC.Set(Integer.parseInt(Immediate,2));
			return;
		}
		String ALUSrc=executeControlSignals[1];
		if(ALUSrc.equals("1")){
			ALU alu=new ALU(ALUOp, ReadData1, Immediate);
			ALUresult=alu.ALUCont();
			return;
		}
	}
	//Additional helper methods
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
	public void instructionDecode() {

		// get the values from the previous stage
		String pc = ifIdRegisters.get("PC");
		String instruction = ifIdRegisters.get("CI");

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
		
		//printing
		System.out.println("Next PC: " + pc);
		System.out.println("Read Register 1: " + r1);
		System.out.println("Read Register 2: " + r2);
		System.out.println("Write Register: " + rd);
		System.out.println("Read data 1: " + readData1);
		System.out.println("Read data 2: " + readData2);
		System.out.println("Immediate: " + immediate);
		System.out.println("EX controls: ALUControl: " + executeControlSignals[0] + ", ALUSrc: " + executeControlSignals[1] + ", Jump: " + executeControlSignals[2] + ", Branch: " + executeControlSignals[3]);
		System.out.println("MEM controls: MemRead: " + memoryControlSignals[0] + ", MemWrite: " + memoryControlSignals[1]);
		System.out.println("WB controls: RegWrite: " + writeBackControlSignals[0] + ", MemToReg: " + writeBackControlSignals[1]);	
	}
	
	public void writeBack() {
		// get the values from the previous stage
		String[] writeBackControlSignals = (String[]) memWbRegisters.get("writeBackControlSignals");
		String aluResult = (String) memWbRegisters.get("ALUresult");
		String memoryReadData = (String) memWbRegisters.get("memoryReadData");
		String writeRegister = (String) memWbRegisters.get("writeRegister");

		// choose which data to write
		String writeData;
		if (writeBackControlSignals[1].equals("0"))
			writeData = aluResult;
		else
			writeData = memoryReadData;

		// register file
		registerFile.write(writeRegister, writeData, writeBackControlSignals[0]);
		
		//printing
		System.out.println("Write data: " + writeData);
		System.out.println("Write Register: " + writeRegister);
		System.out.println("WB controls: RegWrite: " + writeBackControlSignals[0] + ", MemToReg: " + writeBackControlSignals[1]);
	}

	public void memoryAccess() {
		
		String[] memoryControlSignals = (String[]) exMemRegisters.get("memoryControlSignals");
		String address = (String) exMemRegisters.get("ALUresult");
		String data = (String) exMemRegisters.get("readData2");
		
		int addressInt = Integer.parseInt(address);
		String MemRead = memoryControlSignals[0];
		String MemWrite = memoryControlSignals[1];
		if(MemRead.equals("1")) {
			dataMemory.readData(addressInt);
		}
		if(MemWrite.equals("1")) {
			dataMemory.writeData(addressInt, data);
		}
		
		//printing
		
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

	public void setMemoryReadData(String memoryReadData) {
		this.memoryReadData = memoryReadData;
	}
	
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
}