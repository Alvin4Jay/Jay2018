package javabasiscode.IO.Serializable;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Jay on 2017/8/26
 */
public class User implements Serializable{
    private String name;
    private int age;
    private Date birthday;
    private transient String gender;

    /**
     在进行反序列化时，JVM会把传来的字节流中的serialVersionUID与本地相应实体（类）的
     serialVersionUID进行比较，如果相同就认为是一致的，可以进行反序列化，否则就会出现
     序列化版本不一致的异常。
     */
    private static final long serialVersionUID = -6849794470754667710L;
//    private static final long serialVersionUID = -6849794470754667711L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", birthday=" + birthday +
                '}';
    }
}
