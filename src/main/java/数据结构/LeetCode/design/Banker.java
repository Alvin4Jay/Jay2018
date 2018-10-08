package 数据结构.LeetCode.design;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/4
 */
public class Banker {
    //进程数n=5, 资源种类m=3
    private static int available[]=new int[3];         //资源数m
    private static int max[][]=new int[5][3];          //最大需求nxm
    private static int allocation[][]=new int[5][3];   //分配nxm
    private static int need[][]=new int[5][3];         //需求nxm

    private static int request[]=new int[3];           //存放请求request[i]

    private Scanner scanner = new Scanner(System.in);
    private int thread;  //线程号

    //初始化
    public void getData(){
        System.out.println("请输入A,B,C三类资源的数目：");
        //输入A,B,C三类资源数量available
        for(int i=0;i<3;i++){
            available[i] = scanner.nextInt(); //初始化
        }
        //输入进程对三类资源的最大需求max
        for(int i=0;i<5;i++){
            System.out.println("请输入进程"+i+"对A,B,C三类资源的最大需求");
            for(int j=0;j<3;j++){
                max[i][j] = scanner.nextInt();
            }
        }
        //输入进程分配的三类资源数allocation
        for(int i=0;i<5;i++){
            System.out.println("请输入进程"+i+"已分配的A,B,C三类资源数");
            for(int j=0;j<3;j++){
                allocation[i][j] = scanner.nextInt();
            }
        }
        //计算进程还需要的三类资源数need
        for(int i=0;i<5;i++){
            for(int j=0;j<3;j++){
                need[i][j] = max[i][j]-allocation[i][j];
            }
        }
        //重新计算available
        for(int i=0;i<3;i++){
            for(int j=0;j<5;j++){
                available[i] -= allocation[j][i]; //减去已经分配的
            }
        }
    }

    //用户输入要申请资源的线程和申请的资源，并进行判断
    public void getThread(){
        System.out.println("请输入申请资源的线程");
        int thread = scanner.nextInt();     //线程
        if(thread <0 || thread > 4){
            System.out.println("该线程不存在,请重新输入");
            getThread();
        }else{
            this.thread = thread;
            System.out.println("请输入申请的资源(三种，若某种资源不申请则填0)");
            for(int i=0;i<3;i++){
                request[i] = scanner.nextInt();
            }
            //判断request[i]是否小于need[i], 不满足返回
            if(request[0]>need[thread][0] || request[1]>need[thread][1] || request[2]>need[thread][2]){
                System.out.println(thread+"线程申请的资源超出其需要的资源，请重新输入");
                getThread();
                return;
            }else{
                if(request[0]> available[0]||request[1]> available[1]||request[2]> available[2]){
                    System.out.println(thread+"线程申请的资源大于系统资源，请重新输入");
                    getThread();
                    return;
                }
            }

            changeData(thread);
            if(check(thread)){ //检查分配后的安全性，
                getThread();
            }else{
                recoverData(thread);  //恢复状态
                getThread();
            }

        }
    }

    //thread线程请求响应后，试探性分配资源
    public void changeData(int thread){
        for(int i=0;i<3;i++){
            //重新调整系统资源数
            available[i]-=request[i];
            //计算各个线程拥有资源
            allocation[thread][i] += request[i];
            //重新计算需求
            need[thread][i] -= request[i];
        }
    }

    //安全性检查为通过，分配失败时调用，恢复系统原状
    public void recoverData(int thread){
        for(int i=0;i<3;i++){
            //重新调整系统资源数
            available[i] += request[i];
            //计算各个线程拥有资源
            allocation[thread][i] -= request[i];
            //重新计算需求
            need[thread][i] += request[i];
        }
    }

    //对线程thread安全性检查
    public boolean check(int thread){
        boolean finish[] = new boolean[5];  //线程执行完成状态
        int work[] = new int[3]; //可用资源数
        int queue[] = new int[5];   //由于存放安全队列

        int k = 0;//安全队列下标
        int j;  //要判断的线程

        int i;
        //是否分配的标志，该进程还没执行完
        for(i=0;i<5;i++)
            finish[i]=false;
        //j = thread;
        for(i=0;i<3;i++){
            work[i]=available[i];
        }

        j = 0;
        while(j < 5){
            for(i=0; i<3; i++){
                if(finish[j]){ //已经finish，直接下一个
                    j++;
                    break;
                }else if(need[j][i] > work[i]){ //不满足，下一个
                    //System.out.println(need[j][i]+"*"+i+work[i]);
                    j++;
                    break;
                }else if(i==2){
                    for(int m=0;m<3;m++){
                        work[m]+=allocation[j][m];
                    }
                    finish[j]=true;
                    queue[k]=j;
                    k++;
                    j=0;   //从最小线程再开始判断
                }
            }
        }

        //判断是否都属于安全状态
        for(int p=0;p<5;p++){
            if(finish[p] = false){
                System.out.println("系统不安全，资源申请失败");
                return false;
            }
        }
        System.out.println("资源申请成功，安全队列为：");
        for(int q=0;q<5;q++){
            System.out.println(queue[q]);
        }
        return true;
    }

    //打印need和available，需要时调用
    public void showData(){
        System.out.println("need");
        for(int i=0;i<5;i++){
            for(int j=0;j<3;j++){
                System.out.print(need[i][j]+"     ");
            }
        }
        System.out.println("available");
        for(int j=0;j<3;j++){
            System.out.print(available[j]+"     ");
        }
    }
    public static void main(String[] args) {
        Banker bk=new Banker();
        bk.getData();
        bk.getThread();

    }

}
