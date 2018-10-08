package Wc_Skill.IdeaHotKeys.practiceFile;

/**
 * 类概述。
 *
 * @author xuanjian.xuwj
 */
public class User implements UserInterface{
    private final int userAge;
    private String name;


    public User(String name, int userAge) {
        this.name = name;
        this.userAge = userAge;
    }

    public int age(){
        if (name != null) {
            return 0;
        }
        return userAge;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void foo(){
        validate();
        loadDataFromDB();
    }

    private void loadDataFromDB() {
        setName("aaa");
        setName("aaa");
        setName("aaa");
    }

    private void validate() {
        setName("aaa");
        setName("aaa");
    }


    @Override
    public void say() {

    }
}
