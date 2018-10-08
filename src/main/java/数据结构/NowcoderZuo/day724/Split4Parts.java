package 数据结构.NowcoderZuo.day724;

import java.util.HashMap;
import java.util.HashSet;

// 能否把一个正数数组分成4部分问题，每部分的累加和相等，分割点的值不算
public class Split4Parts {
	//方法一 左右指针
	public static boolean canSplits1(int[] arr) {
		if (arr == null || arr.length < 7) { // < 7不够分
			return false;
		}
		HashSet<String> set = new HashSet<String>(); //黑盒
		int allSum = 0; //所有数的和
		for (int i = 0; i < arr.length; i++) {
			allSum += arr[i];
		}
		int leftSum = arr[0]; //左边的和
		for (int i = 1; i < arr.length - 1; i++) { //1---n-2 , 0 n-1位置不会被作为划分点
			//左和_右和（_防止歧义）
			set.add(String.valueOf(leftSum) + "_" + String.valueOf(allSum - leftSum - arr[i]));
			leftSum += arr[i];
		}
		int l = 1; //分1
		int lsum = arr[0]; //左边部分的累加和(不包括l)
		int r = arr.length - 2; //分2
		int rsum = arr[arr.length - 1];//右边部分的累加和(不包括r)
		while (l < r - 3) { //l == r-3退出循环
			if (lsum == rsum) { //确认可能情况
				String lkey = String.valueOf(lsum * 2 + arr[l]);
				String rkey = String.valueOf(rsum * 2 + arr[r]);
				if (set.contains(lkey + "_" + rkey)) { //如果存在，一定在l,r中间位置
					return true;
				}
				lsum += arr[l++];  //lsum += arr[r--]; //不存在
			} else if (lsum < rsum) {
				lsum += arr[l++];
			} else {
				rsum += arr[r--];
			}
		}
		return false;
	}

	//方法二 一个指针p
	public static boolean canSplits2(int[] arr) {
		if (arr == null || arr.length < 7) {
			return false;
		}
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(); //value位置前的和为key
		int sum = arr[0];
		for (int i = 1; i < arr.length; i++) {
			map.put(sum, i); //i之前的累加和
			sum += arr[i]; //计算累加和
		}
		//sum += arr[arr.length-1];
		int lsum = arr[0]; //P左边的和
		for (int s1 = 1; s1 < arr.length - 5; s1++) { //  1<=s1<=length-6 分1位置
			int checkSum = lsum * 2 + arr[s1]; //2a+分1
			if (map.containsKey(checkSum)) { //分2存在
				int s2 = map.get(checkSum); //分2位置
				checkSum += lsum + arr[s2]; //3a+分1+分2
				if (map.containsKey(checkSum)) { //分3存在
					int s3 = map.get(checkSum); //分3位置
					if (checkSum + arr[s3] + lsum == sum) {//考虑所有成分的和是否等于sum
						return true;
					}
				}
			}
			lsum += arr[s1];//不成立，p指针右移
		}
		return false;
	}

	public static int[] generateRondomArray() { //对数器
		int[] res = new int[(int) (Math.random() * 10) + 7]; //7到17
		for (int i = 0; i < res.length; i++) {
			res[i] = (int) (Math.random() * 10) + 1; //1-11
		}
		return res;
	}

	public static void main(String[] args) {
		int testTime = 3000000;
		boolean hasErr = false;
		for (int i = 0; i < testTime; i++) {
			int[] arr = generateRondomArray();
			if (canSplits1(arr) ^ canSplits2(arr)) { //结果不一样
				hasErr = true;
				break;
			}
		}
		if (hasErr) {
			System.out.println("233333");
		} else {
			System.out.println("666666");
		}

	}
}
