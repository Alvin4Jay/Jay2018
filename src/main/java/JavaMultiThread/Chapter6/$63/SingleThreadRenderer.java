package JavaMultiThread.Chapter6.$63;

import java.util.ArrayList;
import java.util.List;

/**
 * 串行渲染页面元素
 */
public abstract class SingleThreadRenderer {
    void renderPage(CharSequence source){
        renderText(source);  //渲染文本
        List<ImageData> imageData = new ArrayList<ImageData>();
        for (ImageInfo imageInfo : scanForImageInfo(source)) {
            imageData.add(imageInfo.downloadImage()); //获取图像数据
        }
        for (ImageData data : imageData) {
            renderImage(data); //渲染图像
        }
    }


    interface ImageData {
    }

    interface ImageInfo {
        ImageData downloadImage();
    }

    abstract void renderText(CharSequence s);
    abstract List<ImageInfo> scanForImageInfo(CharSequence s);
    abstract void renderImage(ImageData i);
}
