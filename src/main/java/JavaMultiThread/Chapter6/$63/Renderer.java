package JavaMultiThread.Chapter6.$63;

import java.util.List;
import java.util.concurrent.*;

/**
 * CompletionService
 */
public abstract class Renderer {
    private final ExecutorService executor;

    public Renderer(ExecutorService executor){
        this.executor = executor;
    }

    void renderPage(CharSequence source){
        List<ImageInfo> imageInfos = scanForImageInfo(source);
        CompletionService<ImageData> completionService = new ExecutorCompletionService<ImageData>(executor);

        for (final ImageInfo info : imageInfos) {
            completionService.submit(new Callable<ImageData>() {
                @Override
                public ImageData call() throws Exception {
                    return info.downloadImage();
                }
            });
        }

        renderText(source);

        try {
            for (int t = 0, n = imageInfos.size(); t < n; t++) {
                Future<ImageData> f=  completionService.take();
                ImageData imageData = f.get();
                renderImage(imageData);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
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
