package javabasiscode.Demo;

import java.io.UnsupportedEncodingException;

/**
 * 枚举类
 */
public class EnumTest {
    public static void main(String[] args) throws UnsupportedEncodingException {

        String name = CatEnum.BMW.getName();
        System.out.println(new String(name.getBytes("GBK"), "utf-8"));
    }
}

/**
 * 颜色枚举
 */
enum ColorEnum{
    RED, GREEN, BLUE;
}

/**
 * 性别枚举
 * 可用中文字符，不能单独使用数字
 */
enum SexEnum{
    /*男, 女, */FEMALE ,MALE;
}


enum CatEnum{
    /**
     * 1、带有构造方法的枚举，构造方法为只能为private(默认可不写private)；
     * 2、含带参构造方法的枚举，枚举值必须赋值；
     * 3、枚举中有了其他属性或方法之后，枚举值必须定义在最前面，且需要在最后一个枚举值后面加分号";"
     */
    BMW("宝马",1000000),
    JEEP("吉普",800000),
    MINI("mini",200000);

    private String name;
    private int price;

    CatEnum(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}