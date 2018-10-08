package Wc_Skill.IdeaHotKeys.practiceFile;

/**
 * 类概述。
 *
 * @author xuanjian.xuwj
 */
public enum Status {

    //1xx Informational

    CONTINUE(100),
    PROCESSING(102),
    CHECKING(103),

    //2xx Success
    OK(200),
    CREATED(201),
    ACCEPTED(202),

    //3xx Redirection
    FOUND(302);


    private int code;

    Status(int code) {
        this.code = code;
    }
}
