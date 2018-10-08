# AOP基本概念 #

## 一、编写切点 ##

1.切点表达式

    "execution(* concert.Performance.perform(..))"

2.将一个POJO定义为切面

`@Aspect`注解

3.切面实例

    @Aspect
	public class Audience {
	    @Pointcut("execution(* concert.Performance.perform(..))") //定义可重用的切点，方法体为空，返回值为void
	    public void performance(){}
	
	
	    @Before("performance()")  //表演之前
	    public void silenceCellPhones(){
	        System.out.println("Silence cell phones");
	    }
	
	    @Before("performance()")  //表演之前
	    public void takeSeats(){
	        System.out.println("Taking Seats");
	    }
	
	    @AfterReturning("performance()")  //表演之后
	    public void applause(){
	        System.out.println("CLAP CLAP CLAP");
	    }
	
	    @AfterThrowing("performance()") //表演失败之后
	    public void demandRefund(){
	        System.out.println("Demand a refund");
	    }
	}