module org.lecturestudio.core {

	uses org.lecturestudio.core.audio.codec.AudioCodecProvider;

	requires com.artifex.mupdf;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires com.fasterxml.jackson.datatype.jdk8;
	requires com.fasterxml.jackson.datatype.jsr310;
	requires com.google.common;
	requires com.google.guice;
	requires commons.math3;
	requires java.desktop;
	requires java.logging;
	requires javax.inject;
	requires jlatexmath;
	requires JTransforms;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires org.apache.pdfbox;
	requires org.apache.fontbox;
	requires org.bouncycastle.provider;
	requires org.bouncycastle.pkix;
	requires org.knowm.xchart;
	requires org.lecturestudio.javaffmpeg;

	exports org.lecturestudio.core;
	exports org.lecturestudio.core.app;
	exports org.lecturestudio.core.app.configuration;
	exports org.lecturestudio.core.app.configuration.bind;
	exports org.lecturestudio.core.app.dictionary;
	exports org.lecturestudio.core.audio;
	exports org.lecturestudio.core.audio.bus;
	exports org.lecturestudio.core.audio.bus.event;
	exports org.lecturestudio.core.audio.codec;
	exports org.lecturestudio.core.audio.device;
	exports org.lecturestudio.core.audio.filter;
	exports org.lecturestudio.core.audio.sink;
	exports org.lecturestudio.core.audio.source;
	exports org.lecturestudio.core.beans;
	exports org.lecturestudio.core.bus;
	exports org.lecturestudio.core.bus.event;
	exports org.lecturestudio.core.camera;
	exports org.lecturestudio.core.camera.bus.event;
	exports org.lecturestudio.core.codec;
	exports org.lecturestudio.core.codec.h264;
	exports org.lecturestudio.core.converter;
	exports org.lecturestudio.core.geometry;
	exports org.lecturestudio.core.graphics;
	exports org.lecturestudio.core.inject;
	exports org.lecturestudio.core.input;
	exports org.lecturestudio.core.io;
	exports org.lecturestudio.core.model;
	exports org.lecturestudio.core.model.action;
	exports org.lecturestudio.core.model.listener;
	exports org.lecturestudio.core.model.shape;
	exports org.lecturestudio.core.net;
	exports org.lecturestudio.core.net.protocol;
	exports org.lecturestudio.core.net.rtp;
	exports org.lecturestudio.core.pdf;
	exports org.lecturestudio.core.presenter;
	exports org.lecturestudio.core.recording;
	exports org.lecturestudio.core.recording.action;
	exports org.lecturestudio.core.recording.edit;
	exports org.lecturestudio.core.recording.file;
	exports org.lecturestudio.core.render;
	exports org.lecturestudio.core.service;
	exports org.lecturestudio.core.text;
	exports org.lecturestudio.core.tool;
	exports org.lecturestudio.core.util;
	exports org.lecturestudio.core.view;

}