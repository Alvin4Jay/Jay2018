package 数据结构.Nowcoder.day92;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Created by Jay on 2017/9/2
 */
public class Main2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int N = sc.nextInt(),  C = sc.nextInt();

        Stu[] stus = new Stu[N];
        for (int i = 0; i < stus.length; i++) {
            stus[i] = new Stu();
            stus[i].id = sc.nextInt();
            stus[i].name = sc.next();
            stus[i].grade = sc.nextInt();
        }

        if(C == 1){
            Arrays.sort(stus, new Comparator<Stu>() {
                @Override
                public int compare(Stu o1, Stu o2) {
                    return o1.id-o2.id;
                }
            });
        }else if(C == 2){
            Arrays.sort(stus, new Comparator<Stu>() {
                @Override
                public int compare(Stu o1, Stu o2) {
                    if(o1.name.compareTo(o1.name) != 0){
                        return o1.name.compareTo(o1.name);
                    }else {
                        return o1.id-o2.id;
                    }
                }
            });
        }else {
            Arrays.sort(stus, new Comparator<Stu>() {
                @Override
                public int compare(Stu o1, Stu o2) {
                    if(o1.grade != o2.grade){
                        return o1.grade- o2.grade;
                    }else {
                        return o1.id- o2.id;
                    }
                }
            });
        }

        for (int i = 0; i < stus.length; i++) {
            System.out.printf("%06d %s %d\n", stus[i].id, stus[i].name, stus[i].grade);
        }
    }

    static class Stu{
        int id;
        String name;
        int grade;

    }
}
