package 数据结构.Sword2Offer;

/**
 * 数值的整数次方（多种边界情况）
 */
public class P11_Power {
    public double Power(double base, int exponent) {
        if(equals(base, 0.0) && exponent<0){ //base为0，ex为负数，无意义
            return 0.0;
        }

        if(exponent < 0){
            return 1.0/powerWithExponent(base, -exponent);
        }else{
            return powerWithExponent(base, exponent);
        }
    }

    public boolean equals(double num1,double num2){
        if(num1-num2 >-0.0000001 && num1-num2<0.0000001){
            return true;
        }else {
            return false;
        }
    }

    public double powerWithExponent(double base, int exponent){
        if(exponent == 0){ //指数为0
            return 1;
        }

        if(exponent == 1){ //指数为1
            return base;
        }

        double result = 1.0;
        for (int i = 0; i < exponent; i++) {
            result *= base;
        }
        return result;
    }
}
