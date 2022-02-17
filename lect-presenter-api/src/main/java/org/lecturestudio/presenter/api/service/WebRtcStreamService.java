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

package org.lecturestudio.presenter.api.service;

import dev.onvoid.webrtc.RTCIceServer;
import dev.onvoid.webrtc.media.Device;
import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.audio.AudioDevice;
import dev.onvoid.webrtc.media.video.VideoCaptureCapability;
import dev.onvoid.webrtc.media.video.VideoDevice;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.command.ClosePresenterCommand;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.CameraStateEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.presenter.api.model.SharedScreenSource;
import org.lecturestudio.presenter.api.presenter.ReconnectStreamPresenter;
import org.lecturestudio.web.api.client.ClientFailover;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.exception.StreamMediaException;
import org.lecturestudio.web.api.janus.JanusHandlerException;
import org.lecturestudio.web.api.janus.JanusHandlerException.Type;
import org.lecturestudio.web.api.janus.JanusPeerConnectionMediaException;
import org.lecturestudio.web.api.janus.JanusStateHandlerListener;
import org.lecturestudio.web.api.janus.client.JanusWebSocketClient;
import org.lecturestudio.web.api.message.SpeechRequestMessage;
import org.lecturestudio.web.api.model.ScreenSource;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.client.StreamWebSocketClient;
import org.lecturestudio.web.api.stream.StreamContext;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.stream.service.StreamProviderService;
import org.lecturestudio.web.api.websocket.WebSocketBearerTokenProvider;
import org.lecturestudio.web.api.websocket.WebSocketHeaderProvider;

/**
 * The {@code WebRtcStreamService} is the interface between user interface and
 * the WebRTC servers.
 *
 * @author Alex Andres
 */
@Singleton
public class WebRtcStreamService extends ExecutableBase {

	private final ApplicationContext context;

	private final WebRtcStreamEventRecorder eventRecorder;

	private final ClientFailover clientFailover;

	@Inject
	@Named("stream.janus.websocket.url")
	private String janusWebSocketUrl;

	@Inject
	@Named("stream.state.websocket.url")
	private String streamStateWebSocketUrl;

	@Inject
	@Named("stream.publisher.api.url")
	private String streamPublisherApiUrl;

	@Inject
	@Named("stream.stun.servers")
	private String streamStunServers;

	private StreamContext streamContext;

	private StreamProviderService streamProviderService;

	private StreamWebSocketClient streamStateClient;

	private JanusWebSocketClient janusClient;

	private ChangeListener<Rectangle2D> cameraFormatListener;

	private ChangeListener<String> cameraDeviceListener;

	private ChangeListener<String> captureDeviceListener;

	private ChangeListener<String> playbackDeviceListener;

	private ExecutableState streamState;

	private ExecutableState cameraState;

	private ExecutableState screenShareState;


	@Inject
	public WebRtcStreamService(ApplicationContext context,
			WebRtcStreamEventRecorder eventRecorder)
			throws ExecutableException {
		this.context = context;
		this.eventRecorder = eventRecorder;
		this.clientFailover = new ClientFailover();
		this.clientFailover.addStateListener((oldState, newState) -> {
			if (newState == ExecutableState.Started) {
				context.getEventBus().post(new ShowPresenterCommand<>(
						ReconnectStreamPresenter.class));
			}
			else if (newState == ExecutableState.Stopped) {
				context.getEventBus().post(new ClosePresenterCommand(
						ReconnectStreamPresenter.class));
			}
		});

		eventRecorder.init();
	}

	public void acceptSpeechRequest(SpeechRequestMessage message) {
		if (!started()) {
			return;
		}

		long requestId = message.getRequestId();
		String userName = String.format("%s %s", message.getFirstName(),
				message.getFamilyName());

		janusClient.startRemoteSpeech(requestId, userName);
		streamProviderService.acceptSpeechRequest(requestId);
	}

	public void rejectSpeechRequest(SpeechRequestMessage message) {
		if (!started()) {
			return;
		}

		speechRequestRejected(message.getRequestId());
	}

	public void startCameraStream() throws ExecutableException {
		if (streamState != ExecutableState.Started
			|| cameraState == ExecutableState.Started) {
			return;
		}

		setCameraState(ExecutableState.Starting);

		streamContext.getVideoContext().setSendVideo(true);

		setCameraState(ExecutableState.Started);
	}

	public void stopCameraStream() throws ExecutableException {
		if (cameraState != ExecutableState.Started) {
			return;
		}

		setCameraState(ExecutableState.Stopping);

		streamContext.getVideoContext().setSendVideo(false);

		setCameraState(ExecutableState.Stopped);
	}

	public void startScreenShare(SharedScreenSource screenSource)
			throws ExecutableException {
		if (streamState != ExecutableState.Started
				|| screenShareState == ExecutableState.Started) {
			return;
		}

		setScreenShareState(ExecutableState.Starting);

		streamContext.getScreenContext().setScreenSource(
				new ScreenSource(screenSource.getTitle(), screenSource.getId(),
						screenSource.isWindow()));

		setScreenShareState(ExecutableState.Started);
	}

	public void stopScreenShare() throws ExecutableException {
		if (screenShareState != ExecutableState.Started) {
			return;
		}

		setScreenShareState(ExecutableState.Stopping);

		streamContext.getScreenContext().setScreenSource(null);

		setScreenShareState(ExecutableState.Stopped);
	}

	public void mutePeerAudio(boolean mute) {
		if (!started()) {
			return;
		}

		streamContext.getAudioContext().setReceiveAudio(mute);
	}

	public void mutePeerVideo(boolean mute) {
		if (!started()) {
			return;
		}

		streamContext.getVideoContext().setReceiveVideo(mute);
	}

	public void stopPeerConnection(Long requestId) {
		if (!started()) {
			return;
		}

		speechRequestRejected(requestId);

		janusClient.stopRemoteSpeech(requestId);
	}

	public void shareDocument(Document document) throws IOException {
		if (streamState == ExecutableState.Started) {
			eventRecorder.shareDocument(document);
		}
	}

	@Override
	protected void initInternal() {
		streamState = ExecutableState.Stopped;
		cameraState = ExecutableState.Stopped;
		screenShareState = ExecutableState.Stopped;
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (streamState == ExecutableState.Started) {
			return;
		}

		setStreamState(ExecutableState.Starting);

		PresenterContext pContext = (PresenterContext) context;
		PresenterConfiguration config = (PresenterConfiguration) context
				.getConfiguration();
		AudioConfiguration audioConfig = config.getAudioConfig();
		StreamConfiguration streamConfig = config.getStreamConfig();
		Course course = pContext.getCourse();

		boolean streamCamera = streamConfig.getCameraEnabled();

		if (streamCamera) {
			setCameraState(ExecutableState.Starting);
		}

		streamContext = createStreamContext(course, config);
		streamStateClient = createStreamStateClient(course, config);
		janusClient = createJanusClient(streamContext);
		janusClient.setRejectedConsumer(this::speechRequestRejected);
		janusClient.setJanusStateHandlerListener(new JanusStateHandlerListener() {

			@Override
			public void connected() {
				eventRecorder.setConnected();
			}

			@Override
			public void disconnected() {

			}

			@Override
			public void error(Throwable throwable) {
				logException(throwable, "Janus state error");

				if (throwable instanceof JanusHandlerException) {
					Throwable cause = throwable.getCause();
					var handlerException = (JanusHandlerException) throwable;

					if (handlerException.getType() == Type.PUBLISHER
							&& cause instanceof JanusPeerConnectionMediaException) {
						var pcMediaException = (JanusPeerConnectionMediaException) cause;

						context.getEventBus().post(new StreamMediaException(
								pcMediaException.getMediaType(),
								pcMediaException));
					}
				}
			}
		});

		eventRecorder.setCourse(course);
		eventRecorder.setStreamProviderService(streamProviderService);

		clientFailover.addExecutable(janusClient);
		clientFailover.addExecutable(streamStateClient.getReconnectExecutable());

		try {
			streamStateClient.start();
			janusClient.start();

			// As of now, it's mandatory to start the event-recorder after the
			// clients started.
			eventRecorder.start();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		cameraFormatListener = (observable, oldValue, newValue) -> {
			streamContext.getVideoContext().setCaptureCapability(
					new VideoCaptureCapability((int) newValue.getWidth(),
							(int) newValue.getHeight(),
							(int) streamConfig.getCameraFormat()
									.getFrameRate()));
			streamContext.getVideoContext().setBitrate(
					streamConfig.getCameraCodecConfig().getBitRate());
		};
		cameraDeviceListener = (observable, oldValue, newValue) -> {
			VideoDevice cameraDevice = getDeviceByName(
					MediaDevices.getVideoCaptureDevices(),
					streamConfig.getCameraName());

			streamContext.getVideoContext().setCaptureDevice(cameraDevice);
		};
		captureDeviceListener = (observable, oldValue, newValue) -> {
			AudioDevice captureDevice = getDeviceByName(
					MediaDevices.getAudioCaptureDevices(),
					audioConfig.getCaptureDeviceName());

			streamContext.getAudioContext().setRecordingDevice(captureDevice);
		};
		playbackDeviceListener = (observable, oldValue, newValue) -> {
			AudioDevice playbackDevice = getDeviceByName(
					MediaDevices.getAudioRenderDevices(),
					audioConfig.getPlaybackDeviceName());

			streamContext.getAudioContext().setPlaybackDevice(playbackDevice);
		};

		streamConfig.getCameraCodecConfig().viewRectProperty().addListener(cameraFormatListener);
		streamConfig.cameraNameProperty().addListener(cameraDeviceListener);
		audioConfig.captureDeviceNameProperty().addListener(captureDeviceListener);
		audioConfig.playbackDeviceNameProperty().addListener(playbackDeviceListener);

		setStreamState(ExecutableState.Started);

		if (streamCamera) {
			setCameraState(ExecutableState.Started);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (streamState != ExecutableState.Started) {
			return;
		}

		setStreamState(ExecutableState.Stopping);

		try {
			eventRecorder.stop();

			disposeExecutable(clientFailover);
			disposeExecutable(streamStateClient);
			disposeExecutable(janusClient);
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		PresenterConfiguration config = (PresenterConfiguration) context
				.getConfiguration();
		AudioConfiguration audioConfig = config.getAudioConfig();
		StreamConfiguration streamConfig = config.getStreamConfig();

		streamConfig.getCameraCodecConfig().viewRectProperty().removeListener(cameraFormatListener);
		streamConfig.cameraNameProperty().removeListener(cameraDeviceListener);
		audioConfig.captureDeviceNameProperty().removeListener(captureDeviceListener);
		audioConfig.playbackDeviceNameProperty().removeListener(playbackDeviceListener);

		setStreamState(ExecutableState.Stopped);
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		eventRecorder.destroy();
	}

	private void speechRequestRejected(Long requestId) {
		streamProviderService.rejectSpeechRequest(requestId);
	}

	/**
	 * Searches the provided list for a device with the provided name.
	 *
	 * @param devices The device list in which to search for the device.
	 * @param name    The name of the device to search for.
	 * @param <T>     The device type.
	 *
	 * @return The device with the specified name or {@code null} if not found.
	 */
	private <T extends Device> T getDeviceByName(List<T> devices, String name) {
		return devices.stream()
				.filter(device -> device.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Sets the new stream state of this controller.
	 *
	 * @param state The new state.
	 */
	private void setStreamState(ExecutableState state) {
		this.streamState = state;

		context.getEventBus().post(new StreamingStateEvent(streamState));
	}

	/**
	 * Sets the new camera state of this controller.
	 *
	 * @param state The new state.
	 */
	private void setCameraState(ExecutableState state) {
		this.cameraState = state;

		context.getEventBus().post(new CameraStateEvent(cameraState));
	}

	/**
	 * Sets the new screen-sharing state of this controller.
	 *
	 * @param state The new state.
	 */
	private void setScreenShareState(ExecutableState state) {
		this.screenShareState = state;

//		context.getEventBus().post(new CameraStateEvent(cameraState));
	}

	private void disposeExecutable(Executable executable) throws ExecutableException {
		if (executable.started()) {
			executable.stop();
		}
		if (executable.stopped()) {
			executable.destroy();
		}
	}

	private StreamWebSocketClient createStreamStateClient(Course course,
			PresenterConfiguration config) {
		StreamConfiguration streamConfig = config.getStreamConfig();

		ServiceParameters stateWsParameters = new ServiceParameters();
		stateWsParameters.setUrl(streamStateWebSocketUrl);

		ServiceParameters streamApiParameters = new ServiceParameters();
		streamApiParameters.setUrl(streamPublisherApiUrl);

		TokenProvider tokenProvider = streamConfig::getAccessToken;

		streamProviderService = new StreamProviderService(streamApiParameters,
				tokenProvider);

		WebSocketHeaderProvider headerProvider = new WebSocketBearerTokenProvider(
				tokenProvider);

		return new StreamWebSocketClient(context.getEventBus(),
				stateWsParameters, headerProvider, eventRecorder, course);
	}

	private JanusWebSocketClient createJanusClient(StreamContext streamContext) {
		ServiceParameters janusWsParameters = new ServiceParameters();
		janusWsParameters.setUrl(janusWebSocketUrl);

		return new JanusWebSocketClient(context.getEventBus(), janusWsParameters,
				streamContext, eventRecorder, clientFailover);
	}

	private StreamContext createStreamContext(Course course, PresenterConfiguration config) {
		AudioConfiguration audioConfig = config.getAudioConfig();
		StreamConfiguration streamConfig = config.getStreamConfig();
		VideoCodecConfiguration cameraConfig = streamConfig.getCameraCodecConfig();

		Rectangle2D cameraViewRect = cameraConfig.getViewRect();

		AudioDevice audioPlaybackDevice = getDeviceByName(
				MediaDevices.getAudioRenderDevices(),
				audioConfig.getPlaybackDeviceName());
		AudioDevice audioCaptureDevice = getDeviceByName(
				MediaDevices.getAudioCaptureDevices(),
				audioConfig.getCaptureDeviceName());
		VideoDevice videoCaptureDevice = getDeviceByName(
				MediaDevices.getVideoCaptureDevices(),
				streamConfig.getCameraName());

		StreamContext streamContext = new StreamContext();
		streamContext.getAudioContext().setSendAudio(streamConfig.getMicrophoneEnabled());
		streamContext.getAudioContext().setReceiveAudio(true);
		streamContext.getAudioContext().setRecordingDevice(audioCaptureDevice);
		streamContext.getAudioContext().setPlaybackDevice(audioPlaybackDevice);

		streamContext.getVideoContext().setSendVideo(streamConfig.getCameraEnabled());
		streamContext.getVideoContext().setReceiveVideo(true);
		streamContext.getVideoContext().setCaptureDevice(videoCaptureDevice);
		streamContext.getVideoContext().setCaptureCapability(
				new VideoCaptureCapability((int) cameraViewRect.getWidth(),
						(int) cameraViewRect.getHeight(),
						(int) streamConfig.getCameraFormat().getFrameRate()));
		streamContext.getVideoContext().setBitrate(cameraConfig.getBitRate());

		streamContext.getScreenContext().setScreenSource(null);
		streamContext.getScreenContext().setFrameRate(30);

		RTCIceServer iceServer = new RTCIceServer();
		iceServer.urls.add(streamStunServers);

		streamContext.getRTCConfig().iceServers.add(iceServer);

		streamContext.setCourse(course);

		streamContext.setPeerStateConsumer(event -> {
			context.getEventBus().post(event);
		});
		streamContext.setOnRemoteVideoFrame(videoFrame -> {
			context.getEventBus().post(videoFrame);
		});

		streamConfig.enableMicrophoneProperty().addListener((o, oldValue, newValue) -> {
			streamContext.getAudioContext().setSendAudio(newValue);
		});

		return streamContext;
	}
}
