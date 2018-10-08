package Spring;

import java.util.logging.Logger;

/**
 * Java.Util.Logger  JUL
 *
 * @author xuanjian.xuwj
 */
public class Demo {

    private static Logger logger = Logger.getLogger(Demo.class.toString());

    public static void main(String[] args){

        System.out.println("-------------");
        logger.info("xxxxxxxx");
        logger.warning("yyyy");
        logger.severe("aaaaaaa");
    }

}
