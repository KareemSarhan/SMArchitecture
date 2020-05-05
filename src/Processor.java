import java.io.*;
import java.util.Hashtable;

/**
 * This class is our project's main class and contains all the stages.
 */
public class Processor {
	public ProgramCounter PC;
	public InstructionMemory IM;
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
	public Hashtable<String, Object> ifIdRegisters;
	public Hashtable<String, Object> idExRegisters;
	public Hashtable<String, Object> exMemRegisters;
	public Hashtable<String, Object> memWbRegisters;
	// outputs
	public String []executeControlSignals;
	public String []memoryControlSignals;
	public String []writeBackControlSignals;
	public String readData1;
	public String readData2;
	public String Immediate;
	public String rd; // Write Register
	public String ALUOp;
	public String ALUresult;
	public String ALUSrc;
	public String Immediate;
	public String rd; // Write Register
	public String memoryReadData;
	// constructor
	public Processor() {
		registerFile = new RegisterFile(this);
		control = new Control(this);
		dataMemory = new DataMemory(this);
		PC = new ProgramCounter();
		IM = new InstructionMemory();
		
		// ifId ={PC, CI}
		ifIdRegisters = new Hashtable<String, Object>();
		//idEx={writeBackControlSignals, memoryControlSignals, executeControlSignals, PC5.readData1,
		// 			readData2, immediate, writeRegister}
		idExRegisters = new Hashtable<String, Object>();
		//exMem={writeBackControlSignals, memoryControlSignals, ALUresult, readData2, writeRegister}
		exMemRegisters = new Hashtable<String, Object>();
		//memWb={writeBackControlSignals, memoryReadData, ALUresult, writeRegister}
		memWbRegisters = new Hashtable<String, Object>();
		
	}

	// stages

	/**
	 * Loads User input to the memory
	 */
	 public void Load() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		for (String Instruction = br.readLine(); !Instruction.isEmpty(); Instruction = br.readLine()) {
			IM.MemWrite(Instruction);
		}
	}

	/**
	 * Gets next instruction from IM bassed on PC Increments PC by 1 
	 * <p> Prints Next PC,Instruction
	 */
	public void Fetch() {
		IM.MemRead(0);
		CI = IM.MemRead(PC.Get());
		PC.Increment();
		System.out.println("Next PC: " + toBinary(PC.Get()) + "\n" + "Instruction: " + CI);
	}

	public void instructionDecode() {

		// get the values from the previous stage
		String pc = ifIdRegisters.get("PC");
		String instruction = (String)ifIdRegisters.get("CI");

		// getting fields from the instruction
		String opcode = "";
		// String rd = "";
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
		this.rd = rd;
		
		// control unit
		control.decode(opcode);

		// register file
		registerFile.read(r1, r2);

		// sign extend
		Immediate = SignExtend(immediate);
		
		//printing
		System.out.println("Next PC: " + pc);
		System.out.println("Read Register 1: " + r1);
		System.out.println("Read Register 2: " + r2);
		System.out.println("Write Register: " + rd);
		System.out.println("Read data 1: " + readData1);
		System.out.println("Read data 2: " + readData2);
		System.out.println("Immediate: " + Immediate);
		System.out.println("EX controls: ALUControl: " + executeControlSignals[0] + ", ALUSrc: " + executeControlSignals[1] + ", Jump: " + executeControlSignals[2] + ", Branch: " + executeControlSignals[3]);
		System.out.println("MEM controls: MemRead: " + memoryControlSignals[0] + ", MemWrite: " + memoryControlSignals[1]);
		System.out.println("WB controls: RegWrite: " + writeBackControlSignals[0] + ", MemToReg: " + writeBackControlSignals[1]);	
	}

	public void Execute() {
		//executeControlSignals = {ALUControl, ALUSrc, Jump, Branch}
		// getting the inputs required for the execution
		String [] exeControlSignals= (String[]) idExRegisters.get("executeControlSignals");
		String ALUOp=exeControlSignals[0];
		String Branch=exeControlSignals[3];
		String PC = (String) idExRegisters.get("PC");
		if(Branch.equals("01")){
			//Branch on Equal
			ALU alu=new ALU(ALUOp, readData1, readData2);
			ALUresult=alu.ALUCont();
			if(Integer.parseInt(ALUresult,2)==0)
				//Set the PC
				PC = Immediate;
			return;
		}
		if(Branch.equals("10")){
			//Branch on less than
			ALU alu=new ALU(ALUOp, readData1, readData2);
			ALUresult=alu.ALUCont();
			if((int)Long.parseLong(ALUresult,2) <0)
				//Set the PC
				PC = Immediate;
			return;
		}
		String Jump=exeControlSignals[2];
		if(Jump.equals("1")){
			PC = readData1;
			return;
		}
		String ALUSrc=exeControlSignals[1];
		if(ALUSrc.equals("1")){
			//for Addi and ANDi and Set on less than immediate
			ALU alu=new ALU(ALUOp, readData1, Immediate);
			ALUresult=alu.ALUCont();
			return;
		}
		ALU alu=new ALU(ALUOp, readData1, readData2);
		ALUresult=alu.ALUCont();
		System.out.println("ALU result/address: "+ALUresult+"\n"+
							"Read Data 2: "+readData2+"\n"+
							"WB controls: RegWrite: " + writeBackControlSignals[0] + ", MemToReg: " + writeBackControlSignals[1]+"\n"+
							"MEM controls: MemRead: " + memoryControlSignals[0] + ", MemWrite: " + memoryControlSignals[1]);
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
		else if(MemWrite.equals("1")) {
			dataMemory.writeData(addressInt, data);
			memoryReadData="00000000000000000000000000000000";
		}
		else
			memoryReadData="00000000000000000000000000000000";
		
		//printing
		System.out.println("ALU Result: " + address);
		if(data!=null) {
		System.out.println("Write data: " + memoryReadData);
		}
		System.out.println("WB controls: MemRead: " + memoryControlSignals[0] + ", MemWrite: " + memoryControlSignals[1]);
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







	//Additional helper methods

	public static String toBinary(int x) {
		StringBuilder result = new StringBuilder();
		for (int i = 31; i >= 0; i--) {
			int mask = 1 << i;
			result.append((x & mask) != 0 ? 1 : 0);
		}
		return result.toString();
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
	
	public static void main(String[] args) throws IOException {
		
		Processor p = new Processor();
		
		// System.err.println("SMArchitecture");
		p.Load();
		int clockCycle = 0;
		String instructionInFetch = "";
		String instructionInDecode = "";
		String instructionInExecute = "";
		String instructionInMemory = "";
		String instructionInWriteBack = "";
		int i=1;
		
		while ((clockCycle-p.PC.Get()) < 4) {
			
			System.out.println("Clock cycle " + i);
			//shifting instructions
			instructionInWriteBack = instructionInMemory;
			instructionInMemory = instructionInExecute;
			instructionInExecute = instructionInDecode;
			instructionInDecode = instructionInFetch;
			if (p.PC.Get()<p.IM.Size)
				instructionInFetch = p.IM.MemRead(p.PC.Get());
			else
				instructionInFetch = "";

			if (clockCycle==0) {
				//fetch only
				System.out.println();
				System.out.println("Instruction: " + instructionInFetch + " is in Fetch stage");
				p.Fetch();

				//shifting registers
				//after fetch
				p.ifIdRegisters.put("PC", convertDecToBinUnsigned(p.PC.Get()));
				p.ifIdRegisters.put("CI", CI);	
				
			}
			else if (clockCycle==1) {
				//fetch and decode
				System.out.println();
				System.out.println("Instruction: " + instructionInFetch + " is in Fetch stage");
				p.Fetch();
				System.out.println();
				System.out.println("Instruction: " + instructionInDecode + " is in Decode stage");
				p.instructionDecode();

				//shifting registers
				//after decode
				p.idExRegisters.put("writeBackControlSignals", p.writeBackControlSignals);
				p.idExRegisters.put("memoryControlSignals", p.memoryControlSignals);
				p.idExRegisters.put("executeControlSignals", p.executeControlSignals);
				p.idExRegisters.put("PC", p.ifIdRegisters.get("PC"));
				p.idExRegisters.put("readData1", p.readData1);
				p.idExRegisters.put("readData2", p.readData2);
				p.idExRegisters.put("immediate", p.Immediate);
				p.idExRegisters.put("writeRegister", p.rd);
				//after fetch
				p.ifIdRegisters.put("PC", convertDecToBinUnsigned(p.PC.Get()));
				p.ifIdRegisters.put("CI", CI);

				
			}
			else if (clockCycle==2) {
				//fetch, decode and execute
				System.out.println();
				System.out.println("Instruction: " + instructionInFetch + " is in Fetch stage");
				p.Fetch();
				System.out.println();
				System.out.println("Instruction: " + instructionInDecode + " is in Decode stage");
				p.instructionDecode();
				System.out.println();
				System.out.println("Instruction: " + instructionInExecute + " is in Execute stage");
				p.Execute();
				
				//shifting registers
				//after execute
				p.exMemRegisters.put("writeBackControlSignals",p.idExRegisters.get("writeBackControlSignals"));
				p.exMemRegisters.put("memoryControlSignals", p.idExRegisters.get("memoryControlSignals"));
				p.exMemRegisters.put("ALUresult", p.ALUresult);
				p.exMemRegisters.put("readData2", p.idExRegisters.get("readData2"));
				p.exMemRegisters.put("writeRegister", p.idExRegisters.get("writeRegister"));
				//after decode
				p.idExRegisters.put("writeBackControlSignals", p.writeBackControlSignals);
				p.idExRegisters.put("memoryControlSignals", p.memoryControlSignals);
				p.idExRegisters.put("executeControlSignals", p.executeControlSignals);
				p.idExRegisters.put("PC", p.ifIdRegisters.get("PC"));
				p.idExRegisters.put("readData1", p.readData1);
				p.idExRegisters.put("readData2", p.readData2);
				p.idExRegisters.put("immediate", p.Immediate);
				p.idExRegisters.put("writeRegister", p.rd);
				//after fetch
				p.ifIdRegisters.put("PC", convertDecToBinUnsigned(p.PC.Get()));
				p.ifIdRegisters.put("CI", CI);
				
			}
			else if (clockCycle==3) {

				System.out.println();
				System.out.println("Instruction: " + instructionInFetch + " is in Fetch stage");
				p.Fetch();
				System.out.println();
				System.out.println("Instruction: " + instructionInDecode + " is in Decode stage");
				p.instructionDecode();
				System.out.println();
				System.out.println("Instruction: " + instructionInExecute + " is in Execute stage");
				p.Execute();
				System.out.println();
				System.out.println("Instruction: " + instructionInMemory + " is in Memory Access stage");
				p.memoryAccess();

				//shifting registers
				//after memory access
				p.memWbRegisters.put("writeBackControlSignals", p.exMemRegisters.get("writeBackControlSignals"));
				p.memWbRegisters.put("memoryReadData", p.memoryReadData);
				p.memWbRegisters.put("ALUresult", p.exMemRegisters.get("ALUresult"));
				p.memWbRegisters.put("writeRegister", p.exMemRegisters.get("writeRegister"));
				//after execute
				p.exMemRegisters.put("writeBackControlSignals",p.idExRegisters.get("writeBackControlSignals"));
				p.exMemRegisters.put("memoryControlSignals", p.idExRegisters.get("memoryControlSignals"));
				p.exMemRegisters.put("ALUresult", p.ALUresult);
				p.exMemRegisters.put("readData2", p.idExRegisters.get("readData2"));
				p.exMemRegisters.put("writeRegister", p.idExRegisters.get("writeRegister"));
				//after decode
				p.idExRegisters.put("writeBackControlSignals", p.writeBackControlSignals);
				p.idExRegisters.put("memoryControlSignals", p.memoryControlSignals);
				p.idExRegisters.put("executeControlSignals", p.executeControlSignals);
				p.idExRegisters.put("PC", p.ifIdRegisters.get("PC"));
				p.idExRegisters.put("readData1", p.readData1);
				p.idExRegisters.put("readData2", p.readData2);
				p.idExRegisters.put("immediate", p.Immediate);
				p.idExRegisters.put("writeRegister", p.rd);
				//after fetch
				p.ifIdRegisters.put("PC", convertDecToBinUnsigned(p.PC.Get()));
				p.ifIdRegisters.put("CI", CI);

				
			}
			else if (clockCycle >=4) {
				
				if (p.PC.Get()<p.IM.Size) {
					System.out.println();
					System.out.println("Instruction: " + instructionInFetch + " is in Fetch stage");
					p.Fetch();
					System.out.println();
					System.out.println("Instruction: " + instructionInDecode + " is in Decode stage");
					p.instructionDecode();
					System.out.println();
					System.out.println("Instruction: " + instructionInExecute + " is in Execute stage");
					p.Execute();
					System.out.println();
					System.out.println("Instruction: " + instructionInMemory + " is in Memory Access stage");
					p.memoryAccess();
					System.out.println();
					System.out.println("Instruction: "+instructionInWriteBack+" is in Write Back stage");
					p.writeBack();

					//shifting registers
					//after memory access
					p.memWbRegisters.put("writeBackControlSignals", p.exMemRegisters.get("writeBackControlSignals"));
					p.memWbRegisters.put("memoryReadData", p.memoryReadData);
					p.memWbRegisters.put("ALUresult", p.exMemRegisters.get("ALUresult"));
					p.memWbRegisters.put("writeRegister", p.exMemRegisters.get("writeRegister"));
					//after execute
					p.exMemRegisters.put("writeBackControlSignals",p.idExRegisters.get("writeBackControlSignals"));
					p.exMemRegisters.put("memoryControlSignals", p.idExRegisters.get("memoryControlSignals"));
					p.exMemRegisters.put("ALUresult", p.ALUresult);
					p.exMemRegisters.put("readData2", p.idExRegisters.get("readData2"));
					p.exMemRegisters.put("writeRegister", p.idExRegisters.get("writeRegister"));
					//after decode
					p.idExRegisters.put("writeBackControlSignals", p.writeBackControlSignals);
					p.idExRegisters.put("memoryControlSignals", p.memoryControlSignals);
					p.idExRegisters.put("executeControlSignals", p.executeControlSignals);
					p.idExRegisters.put("PC", p.ifIdRegisters.get("PC"));
					p.idExRegisters.put("readData1", p.readData1);
					p.idExRegisters.put("readData2", p.readData2);
					p.idExRegisters.put("immediate", p.Immediate);
					p.idExRegisters.put("writeRegister", p.rd);
					//after fetch
					p.ifIdRegisters.put("PC", convertDecToBinUnsigned(p.PC.Get()));
					p.ifIdRegisters.put("CI", CI);
				}
				//no the last Cycles
				else if (clockCycle-p.PC.Get() == 0){
					//No Fetching

					//thr cycle

					System.out.println();
					System.out.println("Instruction: " + instructionInDecode + " is in Decode stage");
					p.instructionDecode();
					System.out.println();
					System.out.println("Instruction: " + instructionInExecute + " is in Execute stage");
					p.Execute();
					System.out.println();
					System.out.println("Instruction: " + instructionInMemory + " is in Memory Access stage");
					p.memoryAccess();
					System.out.println();
					System.out.println("Instruction: "+instructionInWriteBack+" is in Write Back stage");
					p.writeBack();

					//shifting registers
					//after memory access
					p.memWbRegisters.put("writeBackControlSignals", p.exMemRegisters.get("writeBackControlSignals"));
					p.memWbRegisters.put("memoryReadData", p.memoryReadData);
					p.memWbRegisters.put("ALUresult", p.exMemRegisters.get("ALUresult"));
					p.memWbRegisters.put("writeRegister", p.exMemRegisters.get("writeRegister"));
					//after execute
					p.exMemRegisters.put("writeBackControlSignals",p.idExRegisters.get("writeBackControlSignals"));
					p.exMemRegisters.put("memoryControlSignals", p.idExRegisters.get("memoryControlSignals"));
					p.exMemRegisters.put("ALUresult", p.ALUresult);
					p.exMemRegisters.put("readData2", p.idExRegisters.get("readData2"));
					p.exMemRegisters.put("writeRegister", p.idExRegisters.get("writeRegister"));
					//after decode
					p.idExRegisters.put("writeBackControlSignals", p.writeBackControlSignals);
					p.idExRegisters.put("memoryControlSignals", p.memoryControlSignals);
					p.idExRegisters.put("executeControlSignals", p.executeControlSignals);
					p.idExRegisters.put("PC", p.ifIdRegisters.get("PC"));
					p.idExRegisters.put("readData1", p.readData1);
					p.idExRegisters.put("readData2", p.readData2);
					p.idExRegisters.put("immediate", p.Immediate);
					p.idExRegisters.put("writeRegister", p.rd);
				}
				else if(clockCycle - p.PC.Get() == 1){
					//No Fetching and Decoding

					//the cycle

					System.out.println();
					System.out.println("Instruction: " + instructionInExecute + " is in Execute stage");
					p.Execute();
					System.out.println();
					System.out.println("Instruction: " + instructionInMemory + " is in Memory Access stage");
					p.memoryAccess();
					System.out.println();
					System.out.println("Instruction: "+instructionInWriteBack+" is in Write Back stage");
					p.writeBack();

					//shifting registers
					//after memory access
					p.memWbRegisters.put("writeBackControlSignals", p.exMemRegisters.get("writeBackControlSignals"));
					p.memWbRegisters.put("memoryReadData", p.memoryReadData);
					p.memWbRegisters.put("ALUresult", p.exMemRegisters.get("ALUresult"));
					p.memWbRegisters.put("writeRegister", p.exMemRegisters.get("writeRegister"));
					//after execute
					p.exMemRegisters.put("writeBackControlSignals",p.idExRegisters.get("writeBackControlSignals"));
					p.exMemRegisters.put("memoryControlSignals", p.idExRegisters.get("memoryControlSignals"));
					p.exMemRegisters.put("ALUresult", p.ALUresult);
					p.exMemRegisters.put("readData2", p.idExRegisters.get("readData2"));
					p.exMemRegisters.put("writeRegister", p.idExRegisters.get("writeRegister"));
				}
				else if (clockCycle - p.PC.Get() == 2){
					// no fetching, decoding and executing
					
					//the cycle

					System.out.println();
					System.out.println("Instruction: " + instructionInMemory + " is in Memory Access stage");
					p.memoryAccess();
					System.out.println();
					System.out.println("Instruction: "+instructionInWriteBack+" is in Write Back stage");
					p.writeBack();

					//shifting registers
					//after memory access
					p.memWbRegisters.put("writeBackControlSignals", p.exMemRegisters.get("writeBackControlSignals"));
					p.memWbRegisters.put("memoryReadData", p.memoryReadData);
					p.memWbRegisters.put("ALUresult", p.exMemRegisters.get("ALUresult"));
					p.memWbRegisters.put("writeRegister", p.exMemRegisters.get("writeRegister"));
					
				}
				else if (clockCycle-p.PC.Get() == 3){
					//last cycle
					// no fetching, decoding, executing and memory access
					
					//the cycle

					System.out.println();
					System.out.println("Instruction: "+instructionInWriteBack+" is in Write Back stage");
					p.writeBack();

					
				}
				
				
			}
			
			
			System.out.println();
			System.out.println("--------------------------------------------------------------------------");
			System.out.println();
			
			if((p.PC.Get()) != p.IM.Size)
				clockCycle=p.PC.Get();
			else
				clockCycle++;
				
			i++;
			
		}
	}

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