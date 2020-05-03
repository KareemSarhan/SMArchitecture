public class ProgramCounter {
    int PC;
    public ProgramCounter()
    {
        PC=0;
    }
    public int Get()
    {
        return this.PC;
    }
    public void Set(int PC)
    {
        this.PC=PC;
    }
    public void Increment()
    {
        this.PC++;
    }
    //     public static void main(String[] args)
    // {
    //     ProgramCounter hoba = new ProgramCounter();
    //     System.out.println(hoba.Get());
    //     hoba.Increment();
    //     hoba.Increment();
    //     System.out.println(hoba.Get());
    //     hoba.Set(123);
    //     System.out.println(hoba.Get());
    // }
    
}
