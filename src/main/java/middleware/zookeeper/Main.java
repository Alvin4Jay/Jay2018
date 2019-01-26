package middleware.zookeeper;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ZkClient Demo
 *
 * @author xuanjian.xuwj
 */
public class Main {
	public static void main(String[] args) throws InterruptedException {
		// 连接zk服务器
		ZkClient zkClient = new ZkClient("zktestserver1.wacai.info:22181", 30000);
		// 添加子节点变更的监听器
		List<String> list = zkClient.subscribeChildChanges("/dubbo_test/com.alibaba.dubbo.demo.DemoService/providers", new IZkChildListener() {
			// 子节点变更，currentChilds为当前所有的全量的子节点
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				System.err.println("parentPath： " + parentPath + ", children: " +
						Arrays.toString(currentChilds.toArray(new String[0])));
			}
		});

		// 打印现有子节点
		System.err.println(Arrays.toString(list.toArray(new String[0])));

		TimeUnit.SECONDS.sleep(300);
		// 创建临时节点
		// zkClient.createEphemeral("/test");

		// 关闭连接
		zkClient.close();
	}
}
