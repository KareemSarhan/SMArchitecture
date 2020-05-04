
public class DataMemory {
	
	Processor processor;
	Block[] cache;
	String[] memory;
	
	public DataMemory(Processor processor) {
		this.processor = processor;
		cache=new Block[128];
		memory= new String[1024];
    }
	
	public void readData(int address) {
		if(cache[address%128]!=null && cache[address%128].validBit==1) {
			if(cache[address%128].tag==address/128) {
				processor.setMemoryReadData(cache[address%128].data);
				return;
			}
			cache[address%128].data=memory[address];
			cache[address%128].tag=address/128;
			cache[address%128].validBit=1;
			processor.setMemoryReadData(memory[address]);
			return;
		}
		cache[address%128]=new Block();
		cache[address%128].data=memory[address];
		cache[address%128].tag=address/128;
		cache[address%128].validBit=1;
		processor.setMemoryReadData(memory[address]);
	}
	
	
	public void writeData(int address, String data) {
		if(cache[address%128]!=null && cache[address%128].validBit==1) {
			if(cache[address%128].tag!=address/128) {
				cache[address%128].data=memory[address];
				cache[address%128].tag=address/128;
			}
			cache[address%128].data=data;
		}
		else {
			cache[address%128]=new Block();
			cache[address%128].data=data;
			cache[address%128].tag=address/128;
			cache[address%128].validBit=1;
		}
		memory[address]=data;
	}
	
}
