public class ProgramCounter {
    static int PC;
    public ProgramCounter()
    {
        PC=0;
    }
    public int Get()
    {
        return ProgramCounter.PC;
    }
    public void Set(int PC)
    {
        ProgramCounter.PC=PC;
    }
    public void Increment()
    {
        ProgramCounter.PC++;
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
