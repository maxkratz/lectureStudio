/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.core.screencapture;

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopFrame;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.view.PresentationParameter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ScreenCaptureRenderer implements DocumentRenderer {

    private final Object lock = new Object();

    private final ScreenCaptureDocument document;

    private WindowCapturer capturer;

    public ScreenCaptureRenderer(ScreenCaptureDocument document) {
        this.document = document;
        capturer = new WindowCapturer();
        capturer.start((result, desktopFrame) -> {
            if (result == DesktopCapturer.Result.SUCCESS) {
                lastDesktopFrame = desktopFrame;
            }
        });
    }

    private int lastPageNumber;
    private DesktopFrame lastDesktopFrame;

    @Override
    public void render(Page page, PresentationParameter parameter, BufferedImage image) throws IOException {
        if (page.getDocument().isScreenCapture()) {
            synchronized (lock) {
                int pageNumber = page.getPageNumber();

                BufferedImage previewImage = document.getPageFrame(pageNumber);
                if (previewImage != null) {
                    Graphics2D g = image.createGraphics();
                    // System.out.println("Render Page " + page.getPageNumber() + ": " + previewImage.getWidth() + "x" + previewImage.getHeight());
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(previewImage, 0, 0, image.getWidth(), image.getHeight(), 0, 0, previewImage.getWidth(), previewImage.getHeight(), null);
                    g.dispose();
                }

//                DesktopSource captureSource = document.getScreenCaptureSource(pageNumber);
//                System.out.println("Render screen capture for window: " + captureSource.title);
//
//                if (lastDesktopFrame != null && lastPageNumber == pageNumber) {
//                    Graphics2D g = image.createGraphics();
//                    g.setColor(Color.WHITE);
//
//                    // Create screenshot from window and draw it to graphic
//                    image = ImageIO.read(new ByteArrayInputStream(lastDesktopFrame.buffer.array()));
//                    g.drawImage(image, 0, 0, lastDesktopFrame.frameSize.width, lastDesktopFrame.frameSize.height, null);
//                    g.dispose();
//                } else {
//                    capturer.selectSource(captureSource);
//                    capturer.captureFrame();
//                    lastPageNumber = pageNumber;
//                }
            }
        }
    }

    private BufferedImage cropAndScaleImage(BufferedImage sourceImage, int width, int height) {

        // TODO: Switch to WebRTC scale and cropping

        AffineTransform transform = new AffineTransform();
        transform.scale(2.0, 2.0);

        AffineTransformOp scaleOperation = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);

        BufferedImage newImage = new BufferedImage(width, height, sourceImage.getType());


        //ScreenCaptureUtils.createBufferedImage(width, height);
        return scaleOperation.filter(sourceImage, newImage);

//        List<VideoDevice> devices = MediaDevices.getVideoCaptureDevices();
//        for (VideoDevice device : devices) {
//            System.out.println(device.getName() + " " + device.getDescriptor());
//        }
//
//        return sourceImage;
    }
}
