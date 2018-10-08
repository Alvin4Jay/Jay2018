package IDEAHotKeys; /**
 * Created by Jay on 2018/3/23
 */
public enum Status {

    //1xx Informational
    CONTINUE(100),
    PROCESSING(102),
    CHECKPOINT(103),

    //2xx Success
    OK(200),
    CREATED(201),
    ACCEPTED(202);

    public int code;

    Status(int code) {
        this.code = code;
    }
}
