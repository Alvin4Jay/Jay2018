package JVM.Chapter8.p83;

interface Encode {
    void encode(Derive person);
}

class Base {
    public void encrypt() {
        System.out.println("Base::speak");
    }
}

class Derive extends Base {
    @Override
    public void encrypt() {
        System.out.println("Derive::speak");
    }
}

public class MethodReference {
    public static void main(String[] args) {
        Encode encode = Derive::encrypt;
        System.out.println(encode);
    }
}