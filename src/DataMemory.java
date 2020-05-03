
public class DataMemory {
	String MemRead;
	String MemWrite;
	String ad;
	String data;
	Block[] cache;
	String[] memory;
	public DataMemory () {
	cache=new Block[128];
	memory= new String[1024];
    }
	
	public String Readdata(int address) {
		if(cache[address%128]!=null && cache[address%128].validBit==1) {
			if(cache[address%128].tag==address/128) {
				return cache[address%128].data;
			}
			cache[address%128].data=memory[address];
			cache[address%128].tag=address/128;
			cache[address%128].validBit=1;
			return memory[address];
			}
		cache[address%128]=new Block();
		cache[address%128].data=memory[address];
		cache[address%128].tag=address/128;
		cache[address%128].validBit=1;
		return memory[address];
	}
	
	public void writedata(int address, String data1) {
		if(cache[address%128]!=null && cache[address%128].validBit==1) {
			cache[address%128].data=data1;
			if(cache[address%128].tag!=address/128) {
				cache[address%128].tag=address/128;
			}
		}
		else {
			cache[address%128]=new Block();
			cache[address%128].data=data1;
			cache[address%128].tag=address/128;
			cache[address%128].validBit=1;
		}
		memory[address]=data1;
}
	
	public void memoryaccess() {
		int address = Integer.parseInt(ad);
		if(MemRead.equals("1")) {
			String dataread = Readdata(address);
		}
		if(MemWrite.equals("1")) {
			writedata(address, data);
		}
	}
}
