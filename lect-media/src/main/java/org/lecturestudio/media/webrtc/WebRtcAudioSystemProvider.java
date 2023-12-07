/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.media.webrtc;

import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.media.Device;
import dev.onvoid.webrtc.media.DeviceChangeListener;
import dev.onvoid.webrtc.media.MediaDevices;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.audio.AudioDeviceChangeListener;
import org.lecturestudio.core.audio.AudioPlayer;
import org.lecturestudio.core.audio.AudioRecorder;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.device.AudioDevice;

/**
 * {@code AudioSystemProvider} implementation based on the native WebRTC audio
 * module implementation.
 *
 * @author Alex Andres
 */
public class WebRtcAudioSystemProvider implements AudioSystemProvider {

	private final List<AudioDeviceChangeListener> changeListeners = new ArrayList<>();


	public WebRtcAudioSystemProvider() {
		MediaDevices.addDeviceChangeListener(new DeviceChangeListener() {

			@Override
			public void deviceConnected(Device device) {
				AudioDevice audioDevice = new AudioDevice(device.getName(), device.getDescriptor());

				for (var listener : changeListeners) {
					listener.deviceConnected(audioDevice);
				}
			}

			@Override
			public void deviceDisconnected(Device device) {
				AudioDevice audioDevice = new AudioDevice(device.getName(), device.getDescriptor());

				for (var listener : changeListeners) {
					listener.deviceDisconnected(audioDevice);
				}
			}
		});
	}

	@Override
	public void addDeviceChangeListener(AudioDeviceChangeListener listener) {
		changeListeners.add(listener);
	}

	@Override
	public void removeDeviceChangeListener(AudioDeviceChangeListener listener) {
		changeListeners.remove(listener);
	}

	@Override
	public AudioDevice getDefaultRecordingDevice() {
		var device = MediaDevices.getDefaultAudioCaptureDevice();

		if (nonNull(device)) {
			return new AudioDevice(device.getName(), device.getDescriptor());
		}

		var devices = MediaDevices.getAudioCaptureDevices();

		if (devices.isEmpty()) {
			return null;
		}

		device = devices.get(0);

		return new AudioDevice(device.getName(), device.getDescriptor());
	}

	@Override
	public AudioDevice getDefaultPlaybackDevice() {
		var device = MediaDevices.getDefaultAudioRenderDevice();

		if (nonNull(device)) {
			return new AudioDevice(device.getName(), device.getDescriptor());
		}

		var devices = MediaDevices.getAudioRenderDevices();

		if (devices.isEmpty()) {
			return null;
		}

		device = devices.get(0);

		return new AudioDevice(device.getName(), device.getDescriptor());
	}

	@Override
	public AudioDevice[] getRecordingDevices() {
		return getDevices(MediaDevices.getAudioCaptureDevices());
	}

	@Override
	public AudioDevice[] getPlaybackDevices() {
		return getDevices(MediaDevices.getAudioRenderDevices());
	}

	@Override
	public AudioPlayer createAudioPlayer() {
		return new WebRtcAudioPlayer();
	}

	@Override
	public AudioRecorder createAudioRecorder() {
		return new WebRtcAudioRecorder();
	}

	@Override
	public String getProviderName() {
		return "WebRTC Audio";
	}

	private AudioDevice[] getDevices(List<? extends Device> devices) {
		if (devices.isEmpty()) {
			return new AudioDevice[0];
		}

		AudioDevice[] devArray = new AudioDevice[devices.size()];

		for (int i = 0; i < devArray.length; i++) {
			var device = devices.get(i);
			devArray[i] = new AudioDevice(device.getName(), device.getDescriptor());
		}

		return devArray;
	}
}
