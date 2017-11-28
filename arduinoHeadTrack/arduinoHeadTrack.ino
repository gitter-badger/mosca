#include "NAxisMotion.h"        //Contains the bridge code between the API and the Arduino Environment
#include <Wire.h>


struct message_t
{
  uint16_t heading;
  uint16_t roll;
  uint16_t pitch;
}; 


message_t message;
static const uint32_t GPSBaud = 9600;

NAxisMotion mySensor;
unsigned long lastStreamTime = 0;     // the last streamed time stamp
const int streamPeriod = 20;          // stream at 50Hz (time period(ms) =1000/frequency(Hz))


void setup()
{
  Serial.begin(115200);

  I2C.begin();

  mySensor.initSensor();   //The I2C Address can be inside this function in the library
  mySensor.setOperationMode(OPERATION_MODE_NDOF);
  mySensor.setUpdateMode(MANUAL);
    // The default is AUTO. Changing to MANUAL requires calling the 
    // relevant update functions prior to calling the read functions
    // Setting to MANUAL requires fewer reads to the sensor  
}

void loop()
{

  if ((millis() - lastStreamTime) >= streamPeriod)
  {
    lastStreamTime = millis();    
    mySensor.updateEuler();

    // correct heading data for 90deg rotation of hardware 
    float headingf = mySensor.readEulerHeading() - 90;  
    if (headingf < 0) {
      headingf = (360 + headingf);
    }
    
    //  adjust data to suit the receiving software
    if (headingf > 180) {
     headingf = -180 + (headingf - 180);
    }
    headingf = headingf * PI / 180; // convert heading to radians

    float rollf = mySensor.readEulerRoll();
    if (rollf > 180) {
     rollf = -180 + (rollf - 180);
    }
    rollf = rollf * PI / 180; // convert to radians

    float pitchf = mySensor.readEulerPitch() * -1;
    if (pitchf > 180) {
      pitchf = -180 + (pitchf - 180);
    }
    pitchf = pitchf * PI / 180; // convert to radians

    // note pitch and roll are swapped below because z axis of device is rotated by 90 degrees
   // on phones 
    
    message.heading = (headingf + PI) * 100;
    message.pitch = (rollf    + PI) * 100; 
    message.roll = (pitchf   + PI) * 100;
    
    Serial.write(251); 
    Serial.write(252); 
    Serial.write(253); 
    Serial.write(254);
    Serial.write( (uint8_t *) &message, sizeof(message) );
    Serial.write(255);    

  }
}





