package org.myrobotlab.service.abstracts;

import org.myrobotlab.framework.Service;
import org.myrobotlab.framework.ServiceType;
import org.myrobotlab.sensor.EncoderData;
import org.myrobotlab.service.interfaces.EncoderControl;
import org.myrobotlab.service.interfaces.EncoderController;

public class AbstractPinEncoder extends Service implements EncoderControl {

  private static final long serialVersionUID = 1L;
  public String pin;
  // default 12 bit encoder is 4096 steps of resolution
  public Integer resolution = 4096;
  public Double lastPosition = 0.0;
  public EncoderController controller = null;
  boolean enabled = true;
  // we can track the last update that we've recieved and specify the direction
  // even!
  protected long lastUpdate = 0;
  protected Double velocity = 0.0;

  public AbstractPinEncoder(String reservedKey) {
    super(reservedKey);
  }

  public void attach(EncoderController controller) throws Exception {
    if (this.controller == controller) {
      log.info("{} already attached to controller {}", getName(), controller.getName());
    }
    this.controller = controller;
    controller.attach(this);
    lastUpdate = System.currentTimeMillis();
  }

  static public ServiceType getMetaData() {
    ServiceType meta = new ServiceType(AbstractPinEncoder.class);
    meta.addDescription("encoder which operates with a single pin");
    meta.addCategory("encoder", "sensor");
    return meta;
  }

  public String getPin() {
    return pin;
  }

  public Double publishEncoderAngle(Double angle) {
    return angle;
  }

  // FIXME - remove this ...
  public void onEncoderData(EncoderData data) {
    // this is getting published from the arduino and updated here when it comes
    // in..
    // TODO: shoudl the messaging be setup differently?
    // TODO: compare with ultrasonic sensor and see that we're following the
    // same pattern
    // TODO: maybe use nanoTime? how accurate is this.
    long now = System.currentTimeMillis();
    long delta = now - lastUpdate;
    Double angle = 360.0 * data.value / resolution;
    if (delta > 0) {
      // we can compute velocity since the last update
      // This computes the change in degrees per second that the encoder is
      // currently moving at.
      velocity = (angle - this.lastPosition) / delta * 1000.0;
    } else {
      // no position update since the last tick.
      velocity = 0.0;
    }
    // update the previous values
    this.lastPosition = angle;
    this.lastUpdate = now;
    log.info("Encoder Data : {} Angle : {}", data, lastPosition);
  }

  public void setZeroPoint() {
    // pass the set zero point command to the controller
    controller.setZeroPoint(this);
  }
  
  public void setPin(String pin) {
    this.pin = pin;
  }

  public void setPin(Integer address) {
    this.pin = String.format("%d", address);
  }

  @Override
  public void disable() {
    enabled = false;
  }

  @Override
  public void enable() {
    enabled = true;
  }

  @Override
  public Boolean isEnabled() {
    return enabled;
  }

  @Override
  public EncoderData publishEncoderData(EncoderData data) {
    return data;
  }
}